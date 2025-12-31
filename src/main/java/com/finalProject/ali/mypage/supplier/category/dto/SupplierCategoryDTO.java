package com.finalProject.ali.mypage.supplier.category.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class SupplierCategoryDTO {
    private String categoryId;
    private String parentId;
    private String categoryName;
    private LocalDate createdAt;
    private LocalDate updatedAt;
}
