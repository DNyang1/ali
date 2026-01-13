package com.finalProject.ali.shipping.controller;

import com.finalProject.ali.shipping.dto.ShippingResponse;
import com.finalProject.ali.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/order-item/{id}/shipping")
    public ShippingResponse shipping(@PathVariable Long id) {
        return shippingService.getShipping(id);
    }

    @GetMapping("/shipping/carriers")
    public List<String> getCarriers() {
        return shippingService.getCarrierNames();
    }
}
