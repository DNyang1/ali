package com.finalProject.ali.address.service;

import com.finalProject.ali.address.domain.Address;

import java.util.List;

public interface AddressService {

    List<Address> getMyAddress(String userId);

    void addAddress(Address address, String userId);

    void setDefaultAddress(Long addressId, String userId);

}
