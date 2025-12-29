package com.finalProject.ali.supplier_mypage.product.service;

import com.finalProject.ali.supplier_mypage.product.dao.ProductDAO;
import com.finalProject.ali.supplier_mypage.product.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierProductService {

    private final ProductDAO productDao;

    public List<ProductDTO> list(String supplierId) {
        return productDao.findBySupplierId(supplierId);
    }

    public Long  create(ProductDTO product, String supplierId) {
        product.setSupplierId(supplierId);
        product.setCreatedAt(LocalDate.now());
        product.setUpdatedAt(LocalDate.now());
        productDao.insert(product);

        return product.getProductId();
    }

    public ProductDTO get(Long productId) {
        return productDao.findById(productId);
    }

    public void update(ProductDTO product) {
        product.setUpdatedAt(LocalDate.now());
        productDao.update(product);
    }

    public void toggleStatus(Long productId) {
        ProductDTO p = productDao.findById(productId);
        if (p == null) return;

        String next = "ACTIVE";
        if ("ACTIVE".equalsIgnoreCase(p.getStatus())) {
            next = "INACTIVE";
        }

        productDao.updateStatus(productId, next, LocalDate.now());
    }
}
