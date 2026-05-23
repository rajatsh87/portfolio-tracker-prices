package com.portfolio.tracker.client;

import com.portfolio.tracker.dto.GlobalQuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class StockApiClient {

    private final RestClient restClient;
    private final String apiKey;

    public StockApiClient(
            @Value("${portfolio.api.base-url}") String baseUrl,
            @Value("${portfolio.api.key}") String apiKey) {
        
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Retryable(
        retryFor = {RestClientException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 2000)
    )
    public GlobalQuoteResponse fetchDailyPrice(String ticker) {
        return this.restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("function", "GLOBAL_QUOTE")
                        .queryParam("symbol", ticker)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .body(GlobalQuoteResponse.class);
    }
}
