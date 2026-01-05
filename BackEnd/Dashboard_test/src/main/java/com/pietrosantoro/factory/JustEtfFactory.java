package com.pietrosantoro.factory;

import com.pietrosantoro.dto.EtfInfo;
import com.pietrosantoro.model.JustEtfServiceModels;
import org.springframework.stereotype.Component;

@Component
public class JustEtfFactory {

    public EtfInfo fromQuoteToEtfInfo(JustEtfServiceModels.QuoteResponse quote, String name, String cleanIsin){

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
}
