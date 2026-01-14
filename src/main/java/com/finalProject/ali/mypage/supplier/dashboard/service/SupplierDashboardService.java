package com.finalProject.ali.mypage.supplier.dashboard.service;

import com.finalProject.ali.mypage.supplier.inquiry.dao.SupplierInquiryDAO;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SupplierDashboardService {

    private final SupplierInquiryDAO inquiryDAO;



    public long countOpenInquiries(String supplierId) {
        return inquiryDAO.countBySupplierAndStatus(supplierId, 0);
    }

    public long countInProgressInquiries(String supplierId) {
        return inquiryDAO.countBySupplierAndStatus(supplierId, 1);
    }

}
