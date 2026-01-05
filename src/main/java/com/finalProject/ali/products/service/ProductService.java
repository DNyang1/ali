package com.finalProject.ali.products.service;

import com.finalProject.ali.products.dao.ProductsDAO;
import com.finalProject.ali.products.dto.ProductsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductsDAO productDAO;

    public List<ProductsDTO> productList(){
        return productDAO.productList();
    }

    public ProductsDTO productDetail(Long productId) {
        return productDAO.productDetail(productId);
    }

}
