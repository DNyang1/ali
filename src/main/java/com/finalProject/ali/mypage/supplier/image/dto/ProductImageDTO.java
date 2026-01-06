package com.finalProject.ali.mypage.supplier.image.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ProductImageDTO {
    private Long imageId;
    private Long productId;
    private String imageType;   // THUMB(대표), DETAIL(상세)
    private String imagePath;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
