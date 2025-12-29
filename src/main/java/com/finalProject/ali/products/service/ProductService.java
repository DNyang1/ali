package com.finalProject.ali.products.service;

import com.finalProject.ali.products.dao.ProductDAO;
import com.finalProject.ali.products.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductDAO productDAO;

    public List<ProductDTO> productList(){
        return productDAO.productList();
    }
}
