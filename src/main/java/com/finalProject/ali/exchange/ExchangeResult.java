package com.finalProject.ali.exchange;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ExchangeResult {

    private long krwAmount;
    private double convertedAmount;
    private String currency;
    private double rate;
    private LocalDate baseDate;
}
