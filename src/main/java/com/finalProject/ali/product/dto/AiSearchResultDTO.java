package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiSearchResultDTO {
    private String intent;
    private String strategy;
    private String match;
}
