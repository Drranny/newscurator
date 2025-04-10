package kr.ac.dankook.cs.curation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NewsApiConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Value("${newsapi.date.from}")
    private String fromDate;

    @Value("${newsapi.date.to}")
    private String toDate;

    @Value("${newsapi.url}")
    private String apiUrl;

    @Value("${newsapi.key}")
    private String apiKey;

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}
