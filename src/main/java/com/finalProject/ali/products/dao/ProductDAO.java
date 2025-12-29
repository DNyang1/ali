package com.finalProject.ali.products.dao;

import com.finalProject.ali.products.dto.ProductDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductDAO {

    List<ProductDTO> productList();
}
