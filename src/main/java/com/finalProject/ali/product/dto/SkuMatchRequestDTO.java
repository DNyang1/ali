package com.finalProject.ali.product.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class SkuMatchRequestDTO {

    private List<String> optionValueIds;
}