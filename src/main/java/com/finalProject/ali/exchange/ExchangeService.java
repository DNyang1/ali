package com.finalProject.ali.exchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Getter
    private LocalDate baseDate;

    public synchronized LocalDate refreshRates() {

        for (int i = 0; i < 5; i++) {
            LocalDate targetDate = LocalDate.now().minusDays(i);
            String dateStr = targetDate.format(DateTimeFormatter.BASIC_ISO_DATE);

            String url =
                    "https://oapi.koreaexim.go.kr/site/program/financial/exchangeJSON"
                            + "?authkey=" + apiKey
                            + "&searchdate=" + dateStr
                            + "&data=AP01";


            ExchangeRateDTO[] response = restTemplate.getForObject(url, ExchangeRateDTO[].class);

            String raw =
                    restTemplate.getForObject(url, String.class);

            System.out.println("RAW RESPONSE = " + raw);


            if (response == null || response.length == 0) {
                continue;
            }
            
            Map<String, Double> newRates = new HashMap<>();
            for (ExchangeRateDTO dto : response) {
                if (dto.getDealBasR() != null && !dto.getDealBasR().isEmpty()) {
                    double rate = Double.parseDouble(dto.getDealBasR().replace(",", ""));
                    newRates.put(dto.getCurUnit(), rate);
                }
            }
            
            if (!newRates.isEmpty()) {
                this.rateMap = newRates;
                this.baseDate = targetDate;
                System.out.println("로딩성공 : " + targetDate);
                return baseDate;
            }
        }
        
        throw new IllegalStateException("최근 5일간 환율 데이터가 없습니다.");
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
