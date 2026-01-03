package com.pietrosantoro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtfInfo {

    private String isin;
    private String name;

    private Double price;   // latestQuote.raw
    private Double dtdPrc;  // dtdPrc.raw
    private Double dtdAmt;  // dtdAmt.raw

    private String asOf;    // latestQuoteDate
    private String venue;   // quoteTradingVenue
}
