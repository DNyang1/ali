package com.finalProject.ali.products.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter @Setter
public class ProductsDTO {

    private Long productId;
    private String categoryId;
    private String productName;
    private String description;
    private Long isCustomizable;
    private Long moq;
    private Date createdAt;
    private Date updatedAt;
}
