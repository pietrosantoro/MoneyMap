package com.pietrosantoro.controller;

import com.pietrosantoro.dto.EtfInfo;
import com.pietrosantoro.service.JustEtfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
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
            @RequestParam(name = "forceRefresh", defaultValue = "false") Boolean forceRefresh,
            @RequestParam(name = "locale", defaultValue = "it") String locale,
            @RequestParam(name = "currency", defaultValue = "EUR") String currency
    ) throws Exception {
        log.info("GET etf price for ISIN: " + isin);
        if(!forceRefresh || forceRefresh == false)
            return service.getEtfInfo(isin, locale, currency);
        else
            return service.refreshEtfInfo(isin, locale, currency);
    }
}
