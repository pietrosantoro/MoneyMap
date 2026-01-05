package com.pietrosantoro.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class JustEtfServiceModels {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QuoteResponse {
        public RawLocalized latestQuote;
        public String latestQuoteDate;
        public RawLocalized dtdPrc;
        public RawLocalized dtdAmt;
        public String quoteTradingVenue;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RawLocalized {
        public Double raw;
        public String localized;
    }
}
