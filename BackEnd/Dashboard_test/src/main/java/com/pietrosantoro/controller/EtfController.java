package com.pietrosantoro.controller;

import com.pietrosantoro.dto.EtfInfo;
import com.pietrosantoro.service.JustEtfService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class EtfController {

    private final JustEtfService service;

    public EtfController(JustEtfService service) {
        this.service = service;
    }

    @GetMapping("/etf/{isin}")
    public EtfInfo getEtf(
            @PathVariable String isin,
            @RequestParam(defaultValue = "it") String locale,
            @RequestParam(defaultValue = "EUR") String currency
    ) throws Exception {
        return service.getEtfInfo(isin, locale, currency);
    }
}
