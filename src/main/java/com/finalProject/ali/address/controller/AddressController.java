package com.finalProject.ali.address.controller;

import com.finalProject.ali.address.domain.Address;
import com.finalProject.ali.address.service.AddressService;
import com.finalProject.ali.user.dto.UserDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/address")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public List<Address> getAddresses(HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        return addressService.getMyAddress(userId);
    }

    @PostMapping
    public void addAddress(@RequestBody Address address, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        addressService.addAddress(address, userId);
    }

    @PatchMapping("/{id}/default")
    public void setDefaultAddress(@PathVariable Long id, HttpSession session){
        UserDTO user = (UserDTO) session.getAttribute("loginUser");
        String userId = user.getUserId();
        addressService.setDefaultAddress(id, userId);

    }
}
