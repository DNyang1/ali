package com.finalProject.ali.products.dao;

import com.finalProject.ali.products.dto.ProductsDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductsDAO {

    List<ProductsDTO> productList();
}
