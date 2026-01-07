package com.finalProject.ali.checkout.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CheckoutRequest {

    private List<CheckoutItemRequest> items;
    private Long addressId;

    @Getter @Setter
    public static class CheckoutItemRequest {
        private String skuId;
        private Long quantity;
    }
}
