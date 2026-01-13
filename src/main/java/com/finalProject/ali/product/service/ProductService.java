package com.finalProject.ali.product.service;

import com.finalProject.ali.product.category.dto.CategoryDTO;
import com.finalProject.ali.product.dao.ProductDAO;
import com.finalProject.ali.product.dto.ProductDTO;
import com.finalProject.ali.product.dto.ProductSearchSummaryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {
    // 현성
    private final ProductDAO productDAO;

    public List<ProductDTO> productList(){
        return productDAO.productList();
    }

    public ProductDTO productDetail(Long productId) {
        return productDAO.productDetail(productId);
    }

    public List<ProductDTO> getProductsByCustom(boolean custom) {
        return productDAO.selectByCustom(custom ? 1L : 0L);
    }

    public List<ProductDTO> getProductsByRootCategory(String categoryId) {
        return productDAO.selectByRootCategory(categoryId);
    }

    public List<ProductDTO> getProductsByCategoryAndCustom(String categoryId, boolean custom) {
        return productDAO.selectByCategoryAndCustom(
                categoryId,
                custom ? 1L : 0L
        );
    }

    public Map<String, List<ProductDTO>> getRecommendedProducts(int limitPerCategory) {

        List<CategoryDTO> rootCategories = productDAO.selectRootCategories();

        Map<String, List<ProductDTO>> result = new java.util.LinkedHashMap<>();

        for (CategoryDTO category : rootCategories) {
            List<ProductDTO> products =
                    productDAO.selectMainRecommendedByCategory(
                            category.getCategoryId(),
                            limitPerCategory
                    );

            result.put(category.getCategoryName(), products);
        }

        return result;
    }

    public List<CategoryDTO> getRootCategories() {
        return productDAO.selectRootCategories();
    }

    public List<ProductDTO> searchProducts(String keyword) {
        return productDAO.searchByKeyword(keyword);
    }

    public List<ProductDTO> getProductsByCategory(String categoryId) {
        return productDAO.findByCategoryId(categoryId);
    }

    private boolean isCategorySearch(List<ProductSearchSummaryDTO> products) {

        if (products == null || products.isEmpty()) {
            return false;
        }

        String firstCategory = products.get(0).getCategoryName();

        // 🔒 null 방어
        if (firstCategory == null) {
            return false;
        }

        return products.stream()
                .map(ProductSearchSummaryDTO::getCategoryName)
                .allMatch(c -> firstCategory.equals(c));
    }


    //태민
    public List<ProductDTO> list(String supplierId) {
        return productDAO.findBySupplierId(supplierId);
    }

    public ProductDTO get(Long productId, String supplierId) {
        return productDAO.findById(productId, supplierId);
    }

    public Long create(ProductDTO product, String supplierId) {
        product.setSupplierId(supplierId);
        product.setCreatedAt(LocalDate.now());
        product.setUpdatedAt(LocalDate.now());
        productDAO.insert(product);
        return product.getProductId();
    }

    public void update(ProductDTO product, String supplierId) {
        product.setUpdatedAt(LocalDate.now());
        int updated = productDAO.update(product, supplierId);
        if (updated == 0) {
            throw new IllegalArgumentException("권한 없음 또는 상품이 존재하지 않습니다.");
        }
    }

    public void toggleStatus(Long productId, String supplierId) {
        ProductDTO p = productDAO.findById(productId, supplierId);
        if (p == null) {
            throw new IllegalArgumentException("권한 없음 또는 상품이 존재하지 않습니다.");
        }

        String next = "ACTIVE";
        if ("ACTIVE".equalsIgnoreCase(p.getStatus())) {
            next = "INACTIVE";
        }

        int updated = productDAO.updateStatus(productId, supplierId, next, LocalDate.now());
        if (updated == 0) {
            throw new IllegalStateException("상태 변경 실패");
        }


    }

    public String findSupplierIdByProductId(Long productId) {
        return productDAO.findSupplierIdByProductId(productId);
    }

}
