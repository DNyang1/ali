package com.finalProject.ali.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ExchangeRateDTO {

    @JsonProperty("cur_unit")
    private String curUnit;

    @JsonProperty("deal_bas_r")
    private String dealBasR;

}
