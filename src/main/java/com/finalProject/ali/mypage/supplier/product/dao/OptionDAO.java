package com.finalProject.ali.mypage.supplier.product.dao;

import com.finalProject.ali.mypage.supplier.product.dto.OptionDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OptionDAO {
    List<OptionDTO> findByProductId(Long productId);
    void insert(OptionDTO option);
}
