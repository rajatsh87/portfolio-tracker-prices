package com.portfolio.tracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "assets_catalog")
public class AssetCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ticker;

    private String name;

    @Column(name = "portfolio_flag")
    private Boolean portfolioFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getPortfolioFlag() { return portfolioFlag; }
    public void setPortfolioFlag(Boolean portfolioFlag) { this.portfolioFlag = portfolioFlag; }
}
