package com.portfolio.tracker.repository;

import com.portfolio.tracker.entity.AssetCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetCatalogRepository extends JpaRepository<AssetCatalog, Long> {

    // Projection interface to map the specific columns
    interface AssetProjection {
        String getTicker();
        String getExchange();
    }

    @Query(
            value = "SELECT a.ticker AS ticker, COALESCE(a.exchange, '') AS exchange FROM assets_catalog a WHERE a.portfolio_flag = true",
            nativeQuery = true
    )
    List<AssetProjection> findPortfolioTickersWithExchange();
}