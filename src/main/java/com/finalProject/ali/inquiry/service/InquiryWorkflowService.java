package com.finalProject.ali.inquiry.service;

import com.finalProject.ali.inquiry.dao.InquiryDAO;
import com.finalProject.ali.inquiry.dto.InquiryDTO;
import com.finalProject.ali.product.dao.CustomOrderSheetDAO;
import com.finalProject.ali.product.dto.CustomOrderSheetDTO;
import com.finalProject.ali.product.sheet.status.SheetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InquiryWorkflowService {

    private final InquiryDAO inquiryDAO;             
    private final CustomOrderSheetDAO sheetDAO;


    @Transactional
    public void userCancelInquiry(Long inquiryId, String userId) {
        InquiryDTO inq = inquiryDAO.findById(inquiryId);
        if (inq == null) throw new IllegalStateException("문의 없음");
        if (!userId.equals(inq.getUserId())) throw new SecurityException("권한 없음");

        var sheet = sheetDAO.findByInquiryId(inquiryId);

        if (sheet == null) {
            if (inq.getStatus() != 0L) throw new IllegalStateException("이미 처리된 문의는 취소 불가");
            inquiryDAO.updateStatus(inquiryId, 2L);
            return;
        }

        if (!"SENT".equals(String.valueOf(sheet.getStatus()))) {
            throw new IllegalStateException("현재 상태에서는 취소할 수 없습니다. (" + sheet.getStatus() + ")");
        }

        sheetDAO.updateStatus(sheet.getSheetId(), SheetStatus.CANCELLED);

        inquiryDAO.updateStatus(inquiryId, 2L);
    }

    @Transactional
    public void userRejectQuote(Long inquiryId, String userId) {

        InquiryDTO inq = inquiryDAO.findById(inquiryId);
        if (inq == null) throw new IllegalStateException("문의가 없습니다.");
        if (!userId.equals(inq.getUserId())) throw new SecurityException("권한이 없습니다.");

        CustomOrderSheetDTO sheet = sheetDAO.findByInquiryId(inquiryId);
        if (sheet == null) throw new IllegalStateException("견적(주문서)이 없습니다.");

        if (sheet.getStatus() == null || !"SENT".equals(sheet.getStatus().toString())) {
            throw new IllegalStateException("현재 상태에서는 견적 거절이 불가합니다. (" + sheet.getStatus() + ")");
        }

        sheetDAO.updateStatus(sheet.getSheetId(), SheetStatus.REJECTED);

        inquiryDAO.updateStatus(inquiryId, 1L);
    }

    @Transactional
    public void cancelInquiryBySupplier(Long inquiryId, String supplierId) {
        InquiryDTO inq = inquiryDAO.findById(inquiryId);
        if (inq == null) throw new IllegalStateException("문의 없음");

        if (!supplierId.equals(inq.getSupplierId())) {
            throw new SecurityException("권한 없음");
        }

        CustomOrderSheetDTO sheet = sheetDAO.findByInquiryId(inquiryId);
        if (sheet != null) {
            sheetDAO.updateStatus(sheet.getSheetId(), SheetStatus.CANCELLED);
        }

        inquiryDAO.updateStatus(inquiryId, 2L);
    }




}
