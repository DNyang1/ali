package com.finalProject.ali.products.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter @Setter
public class ProductsDTO {

    private Long productId;
    private String categoryId;
    private String supplierId;
    private String productName;
    private String description;
    private Long isCustomizable;
    private Date createdAt;
    private Date updatedAt;

    private Long moq;
    private Long minPrice;
    private Long maxPrice;
}
