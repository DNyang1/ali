package com.finalProject.ali.order.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OrderPreviewResponse {
    private List<OrderPreviewItem> items;
    private Long totalAmount;
}
