package com.portfolio.tracker.scheduler;

import com.portfolio.tracker.client.StockApiClient;
import com.portfolio.tracker.dto.GlobalQuoteResponse;
import com.portfolio.tracker.entity.Price;
import com.portfolio.tracker.repository.PriceRepository;
import com.portfolio.tracker.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockPriceScheduler.class);

    private final StockApiClient stockApiClient;
    private final PriceRepository priceRepository;
    private final TransactionRepository transactionRepository;

    public StockPriceScheduler(StockApiClient stockApiClient, PriceRepository priceRepository, TransactionRepository transactionRepository) {
        this.stockApiClient = stockApiClient;
        this.priceRepository = priceRepository;
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(cron = "${portfolio.jobs.stock-sync.cron}", zone = "${portfolio.jobs.stock-sync.zone}")
    @EventListener(ApplicationReadyEvent.class)
    public void syncStockPrices() {
        log.info("Starting daily stock price synchronization batch...");
        List<TransactionRepository.AssetProjection> activeAssets = transactionRepository.findPortfolioTickersWithExchange();

        if (activeAssets.isEmpty()) {
            log.info("No active portfolio assets found in assets_catalog. Exiting job.");
            return;
        }

        for (TransactionRepository.AssetProjection asset : activeAssets) {
            String baseTicker = asset.getTicker();
            String ticker = baseTicker;
            String exchange = asset.getExchange();
            boolean alreadyExists = priceRepository.existsByTickerAndExchangeAndTradeDate(
                    baseTicker,exchange, LocalDate.now());
            if (alreadyExists) {
                log.info("Price data already exists for {} on {}. Skipping save.",ticker,LocalDate.now());
                continue;
            }

            if (exchange!=null && exchange.trim().length()>0) {
                ticker = baseTicker + "." + exchange;
            }
            try {
                log.info("searching for ticker symbol :"+ticker);
                GlobalQuoteResponse response = stockApiClient.fetchDailyPrice(ticker);

                if (response != null && response.getQuote() != null && response.getQuote().getPrice() != null) {
                    GlobalQuoteResponse.StockQuote quote = response.getQuote();
                    if (!quote.getLatestTradingDay().equals(LocalDate.now())) {
                        if (priceRepository.existsByTickerAndExchangeAndTradeDate(baseTicker, exchange, quote.getLatestTradingDay())) {
                            log.info("API returned older trade date ({}) which already exists. Skipping save.", quote.getLatestTradingDay());
                            continue;
                        }
                    }
                    Price priceEntity = new Price();
                    String[] tickerExchange = quote.getSymbol().split("\\.");
                    priceEntity.setTicker(tickerExchange[0]);
                    priceEntity.setExchange(tickerExchange.length > 1 ? tickerExchange[1] : null);
                    priceEntity.setTradeDate(quote.getLatestTradingDay());
                    priceEntity.setOpenPrice(new BigDecimal(quote.getOpen()));
                    priceEntity.setHighPrice(new BigDecimal(quote.getHigh()));
                    priceEntity.setLowPrice(new BigDecimal(quote.getLow()));
                    priceEntity.setClosePrice(new BigDecimal(quote.getPrice()));
                    priceEntity.setVolume(Long.parseLong(quote.getVolume()));
                    priceEntity.setFetchedAt(LocalDateTime.now());
                    priceEntity.setPreviousClose(new BigDecimal(quote.getPreviousClose()));
                    priceEntity.setChange(new BigDecimal(quote.getChange()));
                    priceEntity.setChangePercentage(quote.getChangePercentage());
                    try {
                        priceRepository.save(priceEntity);
                    }
                    catch (Exception e){
                        log.error("Error occurred while saving data Exception :"+e.getMessage());
                    }
                    log.info("Successfully saved new price for ticker: {}", ticker);
                } else {
                    log.warn("Received empty or invalid response for ticker: {}", ticker);
                }

                 Thread.sleep(12500); // Uncomment if using free-tier API

            } catch (DataIntegrityViolationException e) {
                log.warn("Duplicate constraint triggered for {}. It was likely inserted by another process. Skipping.", baseTicker);
            } catch (Exception e) {
                // Catch network timeouts, JSON parsing errors, etc.
                log.error("Failed to sync stock price for ticker: {}", ticker, e);
            }
        }
        log.info("Completed daily stock price sync batch.");
    }
}
