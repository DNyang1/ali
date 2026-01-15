package com.finalProject.ali.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.finalProject.ali.inquiry.dao.InquiryDAO;
import com.finalProject.ali.inquiry.dto.InquiryCreateRequest;
import com.finalProject.ali.inquiry.dto.InquiryDTO;
import com.finalProject.ali.product.dao.ProductDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryDAO inquiryDAO;
    private final ProductDAO productDAO; // product_id → supplier_id 조회용

    private String normalizeUserId(String id) {
        if (id == null) return null;
        return id.startsWith("s_") ? id.substring(2) : id;
    }

    // 구매자 문의 등록
    @Transactional
    public Long createInquiry(String userId, InquiryCreateRequest request) {

        if (request.getProductId() == null) {
            throw new IllegalArgumentException("productId는 필수입니다.");
        }

        String supplierId = productDAO.findSupplierIdByProductId(request.getProductId());
        if (supplierId == null) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }

        String u = normalizeUserId(userId);
        String s = normalizeUserId(supplierId);
        if (u.equals(s)) {
            throw new IllegalStateException("본인 상품에는 문의를 등록할 수 없습니다.");
        }

        InquiryDTO inquiry = new InquiryDTO();
        inquiry.setUserId(userId);
        inquiry.setSupplierId(supplierId);
        inquiry.setProductId(request.getProductId());
        inquiry.setQuantity(request.getQuantity());
        inquiry.setContent(request.getContent());
        inquiry.setStatus(0L); // 답변대기

        inquiryDAO.insertInquiry(inquiry);
        return inquiry.getInquiryId();
    }

    // 구매자 문의 목록
    public List<InquiryDTO> findMyInquiries(String userId, Integer status) {
        if (status == null) return inquiryDAO.findMyInquiries(userId); // 기존 SQL 사용
        return inquiryDAO.findMyInquiriesByStatus(userId, status);     // 새 SQL 사용
    }

    // 문의 단건 조회
    public InquiryDTO findById(Long inquiryId) {
        return inquiryDAO.findById(inquiryId);
    }

    // 판매자 답변대기 개수
    public long countPendingBySupplier(String supplierId) {
        return inquiryDAO.countBySupplierAndStatus(supplierId, 0L);
    }

    // 판매자 문의 목록
    public List<InquiryDTO> findBySupplier(String supplierId, Long status) {
        return inquiryDAO.findBySupplierAndStatus(supplierId, status);
    }

    // 판매자 문의 상태 변경
    @Transactional
    public void updateStatus(Long inquiryId, String supplierId, Long status) {

        InquiryDTO inquiry = inquiryDAO.findById(inquiryId);
        if (inquiry == null) {
            throw new IllegalArgumentException("문의가 존재하지 않습니다.");
        }

        if (!supplierId.equals(inquiry.getSupplierId())) {
            throw new SecurityException("권한이 없습니다.");
        }

        if (status != 1L && status != 2L) {
            throw new IllegalArgumentException("status는 1(완료) 또는 2(반려)만 가능합니다.");
        }

        inquiryDAO.updateStatus(inquiryId, status);
    }

    public List<InquiryDTO> findBySupplierAll(String supplierId) {
        return inquiryDAO.findBySupplierAll(supplierId);
    }
    public List<InquiryDTO> findRecentBySupplier(String supplierId, int limit) {
        List<InquiryDTO> all = inquiryDAO.findBySupplierAll(supplierId);
        if (all == null) return java.util.List.of();
        return all.size() > limit ? all.subList(0, limit) : all;
    }
}



