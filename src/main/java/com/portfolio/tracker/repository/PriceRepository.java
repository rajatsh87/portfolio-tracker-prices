package com.portfolio.tracker.repository;

import com.portfolio.tracker.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {
    
    boolean existsByTickerAndExchangeAndTradeDate(String ticker, String exchange, LocalDate tradeDate);
}
