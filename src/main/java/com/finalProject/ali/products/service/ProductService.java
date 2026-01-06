package com.finalProject.ali.products.service;

import com.finalProject.ali.products.dao.ProductsDAO;
import com.finalProject.ali.products.dto.CategoryDTO;
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

    public List<ProductsDTO> getProductsByCustom(boolean custom) {
        return productDAO.selectByCustom(custom ? 1L : 0L);
    }

    public List<ProductsDTO> getProductsByRootCategory(String categoryId) {
        return productDAO.selectByRootCategory(categoryId);
    }

    public List<ProductsDTO> getProductsByCategoryAndCustom(String categoryId, boolean custom) {
        return productDAO.selectByCategoryAndCustom(
                categoryId,
                custom ? 1L : 0L
        );
    }

    public List<CategoryDTO> getRootCategories() {
        return productDAO.selectRootCategories();
    }

    public List<ProductsDTO> searchProducts(String keyword) {
        return productDAO.searchByKeyword(keyword);
    }

    public List<ProductsDTO> getProductsByCategory(String categoryId) {
        return productDAO.findByCategoryId(categoryId);
    }

}
