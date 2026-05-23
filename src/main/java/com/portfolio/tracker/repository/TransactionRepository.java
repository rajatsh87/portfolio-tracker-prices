package com.portfolio.tracker.repository;


import com.portfolio.tracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    interface AssetProjection {
        String getTicker();
        String getExchange();
    }
    @Query(value = "SELECT a.ticker AS ticker, COALESCE(a.exchange, '') AS exchange \n" +
            "FROM assets_catalog a WHERE a.id in ( select distinct asset_id from transactions )",
            nativeQuery = true)
    List<AssetProjection> findPortfolioTickersWithExchange();
}