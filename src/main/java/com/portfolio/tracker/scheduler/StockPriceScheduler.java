package com.portfolio.tracker.scheduler;

import com.portfolio.tracker.client.StockApiClient;
import com.portfolio.tracker.dto.GlobalQuoteResponse;
import com.portfolio.tracker.entity.Price;
import com.portfolio.tracker.repository.AssetCatalogRepository;
import com.portfolio.tracker.repository.PriceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class StockPriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(StockPriceScheduler.class);
    
    private final StockApiClient stockApiClient;
    private final PriceRepository priceRepository;
    private final AssetCatalogRepository assetCatalogRepository;

    public StockPriceScheduler(
            StockApiClient stockApiClient, 
            PriceRepository priceRepository,
            AssetCatalogRepository assetCatalogRepository) {
        this.stockApiClient = stockApiClient;
        this.priceRepository = priceRepository;
        this.assetCatalogRepository = assetCatalogRepository;
    }

    @Scheduled(cron = "${portfolio.jobs.stock-sync.cron}", zone = "${portfolio.jobs.stock-sync.zone}")
    @Transactional
    public void syncStockPrices() {
        log.info("Starting daily stock price synchronization batch...");

//        List<String> portfolioTickers = assetCatalogRepository.findPortfolioTickers();
        List<AssetCatalogRepository.AssetProjection> activeAssets = assetCatalogRepository.findPortfolioTickersWithExchange();

        if (activeAssets.isEmpty()) {
            log.info("No active portfolio assets found in assets_catalog. Exiting job.");
            return;
        }

        for (AssetCatalogRepository.AssetProjection asset : activeAssets) {
            String baseTicker = asset.getTicker();
            String ticker = baseTicker;
            String exchange = asset.getExchange();
            if (exchange!=null && exchange.trim().length()>0) {
                ticker = baseTicker + "." + exchange;
            }
            try {
                log.info("searching for ticker symbol :"+ticker);
                GlobalQuoteResponse response = stockApiClient.fetchDailyPrice(ticker);
                
                if (response != null && response.getQuote() != null && response.getQuote().getPrice() != null) {
                    GlobalQuoteResponse.StockQuote quote = response.getQuote();
                    
                    boolean alreadyExists = priceRepository.existsByTickerAndTradeDate(
                            quote.getSymbol(), 
                            quote.getLatestTradingDay()
                    );

                    if (alreadyExists) {
                        log.info("Price data already exists for {} on {}. Skipping save.", 
                                quote.getSymbol(), quote.getLatestTradingDay());
                        continue;
                    }

                    Price priceEntity = new Price();
                    priceEntity.setTicker(quote.getSymbol());
                    priceEntity.setTradeDate(quote.getLatestTradingDay());
                    priceEntity.setOpenPrice(new BigDecimal(quote.getOpen()));
                    priceEntity.setHighPrice(new BigDecimal(quote.getHigh()));
                    priceEntity.setLowPrice(new BigDecimal(quote.getLow()));
                    priceEntity.setClosePrice(new BigDecimal(quote.getPrice()));
                    priceEntity.setVolume(Long.parseLong(quote.getVolume()));
                    priceEntity.setFetchedAt(LocalDateTime.now());
                    
                    priceRepository.save(priceEntity);
                    log.info("Successfully saved new price for ticker: {}", ticker);
                } else {
                    log.warn("Received empty or invalid response for ticker: {}", ticker);
                }
                
                 Thread.sleep(12500); // Uncomment if using free-tier API
                
            } catch (Exception e) {
                log.error("Failed to sync stock price for ticker: {}", ticker, e);
            }
        }
        log.info("Completed daily stock price sync batch.");
    }
}
