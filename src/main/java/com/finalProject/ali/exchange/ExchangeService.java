package com.finalProject.ali.exchange;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final RestTemplate restTemplate;

    @Value("${exchange.exim.api-key}")
    private String apiKey;

    private Map<String, Double> rateMap = new HashMap<>();
    private LocalDate baseDate;

    public synchronized LocalDate refreshRates() {
        String today = LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String url =
                "https://oapi.koreaexim.go.kr/site/program/financial/exchangeJSON"
                        + "?authkey=" + apiKey
                        + "&searchdate=" + today
                        + "&data=AP01";

        ExchangeRateDTO[] response =
                restTemplate.getForObject(url, ExchangeRateDTO[].class);

        Map<String, Double> newRates = new HashMap<>();

        for (ExchangeRateDTO dto : response) {
            if (dto.getDealBasR() == null) continue;

            double rate = Double.parseDouble(dto.getDealBasR().replace(",",""));
            newRates.put(dto.getCurUnit(),rate);
        }

        this.rateMap = newRates;
        this.baseDate = LocalDate.now();
        return baseDate;
    }

    public ExchangeResult convert(long krwAmount, String currency) {
        Double rate = rateMap.get(currency);
        if (rate == null) {
            throw new IllegalArgumentException("rate 없어요");
        }

        return new ExchangeResult(
                krwAmount,
                krwAmount / rate,
                currency,
                rate,
                baseDate
        );
    }

    public java.util.Set<String> getAvailableCurrencies() {
        return rateMap.keySet();
    }
}
