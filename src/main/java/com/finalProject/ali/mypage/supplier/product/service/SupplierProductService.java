package com.finalProject.ali.mypage.supplier.product.service;

import com.finalProject.ali.mypage.supplier.product.dao.ProductDAO;
import com.finalProject.ali.mypage.supplier.product.dto.ProductDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierProductService {

    private final ProductDAO productDAO;

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
}
