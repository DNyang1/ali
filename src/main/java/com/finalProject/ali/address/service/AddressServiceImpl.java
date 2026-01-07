package com.finalProject.ali.address.service;

import com.finalProject.ali.address.domain.Address;
import com.finalProject.ali.address.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService{
    private final AddressMapper addressMapper;

    @Override
    public List<Address> getMyAddress(String userId) {
        return addressMapper.findByUserId(userId);
    }

    @Override
    public void addAddress(Address address, String userId) {
        address.setUserId(userId);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            addressMapper.clearDefault(userId);
        }

        addressMapper.insert(address);
    }

    @Override
    public void setDefaultAddress(Long addressId, String userId) {
        addressMapper.clearDefault(userId);
        addressMapper.setDefault(addressId);
    }
}
