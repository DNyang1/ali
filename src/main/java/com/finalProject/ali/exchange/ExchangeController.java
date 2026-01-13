package com.finalProject.ali.exchange;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exchange")
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping
    public ExchangeResult convert(@RequestParam long amount,
                                  @RequestParam String currency) {
        return exchangeService.convert(amount, currency);
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh() {


        return Map.of("message","환율 갱신",
                    "baseDate", exchangeService.refreshRates());
    }

    @GetMapping("/currencies")
    public java.util.Set<String> getCurrencies() {
        return exchangeService.getAvailableCurrencies();
    }
}
