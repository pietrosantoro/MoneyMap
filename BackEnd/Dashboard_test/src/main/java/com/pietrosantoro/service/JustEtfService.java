package com.pietrosantoro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pietrosantoro.dto.EtfInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class JustEtfService {

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

        Double price = quote.latestQuote != null ? quote.latestQuote.raw : null;
        Double dtdPrc = quote.dtdPrc != null ? quote.dtdPrc.raw : null;
        Double dtdAmt = quote.dtdAmt != null ? quote.dtdAmt.raw : null;

        EtfInfo out = new EtfInfo();
        out.setIsin(cleanIsin);
        out.setName((name == null || name.isBlank()) ? cleanIsin : name);
        out.setPrice(price);
        out.setDtdPrc(dtdPrc);
        out.setDtdAmt(dtdAmt);
        out.setAsOf(quote.latestQuoteDate);
        out.setVenue(quote.quoteTradingVenue);
        return out;
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
