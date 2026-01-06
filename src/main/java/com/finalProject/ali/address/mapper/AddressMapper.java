package com.finalProject.ali.address.mapper;

import com.finalProject.ali.address.domain.Address;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AddressMapper {
    List<Address> findByUserId(String userId);
    void insert(Address address);
    void clearDefault(String userId);
    void setDefault(@Param("id") Long id);
}
