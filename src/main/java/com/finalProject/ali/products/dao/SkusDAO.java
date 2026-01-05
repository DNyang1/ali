package com.finalProject.ali.products.dao;

import com.finalProject.ali.products.dto.SkuRowDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SkusDAO {

    List<SkuRowDTO> findSkuRowsByProductId(Long productId);

}
