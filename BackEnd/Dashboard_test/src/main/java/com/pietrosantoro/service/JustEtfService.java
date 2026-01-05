package com.pietrosantoro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pietrosantoro.dto.EtfInfo;
import com.pietrosantoro.factory.JustEtfFactory;
import com.pietrosantoro.model.JustEtfServiceModels;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Service
public class JustEtfService {

    @Autowired
    private JustEtfFactory factory;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private final ObjectMapper om = new ObjectMapper();

    @Cacheable(cacheNames = "etfInfo", key = "#isin + '|' + #locale + '|' + #currency")
    public EtfInfo getEtfInfo(String isin, String locale, String currency) throws Exception {
        String cleanIsin = (isin == null ? "" : isin.trim());
        if (cleanIsin.isEmpty()) throw new IllegalArgumentException("ISIN mancante");

        JustEtfServiceModels.QuoteResponse quote = fetchQuote(cleanIsin, locale, currency);
        String name = fetchName(cleanIsin);

        return factory.fromQuoteToEtfInfo(quote,name,cleanIsin);
    }

    @CachePut(cacheNames = "etfInfo", key = "#isin + '|' + #locale + '|' + #currency")
    public EtfInfo refreshEtfInfo(String isin, String locale, String currency) throws Exception{
        String cleanIsin = (isin == null ? "" : isin.trim());
        if (cleanIsin.isEmpty()) throw new IllegalArgumentException("ISIN mancante");

        JustEtfServiceModels.QuoteResponse quote = fetchQuote(cleanIsin, locale, currency);
        String name = fetchName(cleanIsin);

        return factory.fromQuoteToEtfInfo(quote,name,cleanIsin);
    }

    private JustEtfServiceModels.QuoteResponse fetchQuote(String isin, String locale, String currency) throws Exception {
        String url = "https://www.justetf.com/api/etfs/" + isin + "/quote"
                + "?locale=" + enc(locale)
                + "&currency=" + enc(currency);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .header("User-Agent", "Mozilla/5.0 (justetf-proxy local)")
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new RuntimeException("justETF quote HTTP " + resp.statusCode() + ": " + resp.body());
        }
        log.info("JustEtf quote response: " + resp.body());
        return om.readValue(resp.body(), JustEtfServiceModels.QuoteResponse.class);
    }

    private String fetchName(String isin) {
        try {
            String url = "https://www.justetf.com/en/etf-profile.html?isin=" + enc(isin);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (justetf-proxy local)")
                    .timeout(15000)
                    .get();

            var h1 = doc.selectFirst("h1");
            if (h1 != null) {
                String t = h1.text().trim();
                if (!t.isBlank()) return t;
            }

            String title = doc.title() != null ? doc.title().trim() : "";
            if (!title.isBlank()) return title.split("\\|")[0].trim();

            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private static String enc(String s) {
        try {
            return java.net.URLEncoder.encode(s == null ? "" : s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
