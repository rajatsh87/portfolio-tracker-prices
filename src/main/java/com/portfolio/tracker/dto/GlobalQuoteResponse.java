package com.portfolio.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class GlobalQuoteResponse {

    @JsonProperty("Global Quote")
    private StockQuote quote;

    public StockQuote getQuote() { return quote; }
    public void setQuote(StockQuote quote) { this.quote = quote; }

    public static class StockQuote {
        @JsonProperty("01. symbol")
        private String symbol;
        
        @JsonProperty("02. open")
        private String open;
        
        @JsonProperty("03. high")
        private String high;
        
        @JsonProperty("04. low")
        private String low;

        @JsonProperty("05. price")
        private String price;
        
        @JsonProperty("06. volume")
        private String volume;

        @JsonProperty("07. latest trading day")
        private LocalDate latestTradingDay;

        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getOpen() { return open; }
        public void setOpen(String open) { this.open = open; }
        public String getHigh() { return high; }
        public void setHigh(String high) { this.high = high; }
        public String getLow() { return low; }
        public void setLow(String low) { this.low = low; }
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        public String getVolume() { return volume; }
        public void setVolume(String volume) { this.volume = volume; }
        public LocalDate getLatestTradingDay() { return latestTradingDay; }
        public void setLatestTradingDay(LocalDate latestTradingDay) { this.latestTradingDay = latestTradingDay; }
    }
}
