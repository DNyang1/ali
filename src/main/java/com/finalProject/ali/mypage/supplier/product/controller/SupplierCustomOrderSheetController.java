package com.finalProject.ali.mypage.supplier.product.controller;

import com.finalProject.ali.product.dao.SupplierDAO;
import com.finalProject.ali.product.dto.CustomOrderSheetForm;
import com.finalProject.ali.product.service.ProductOptionSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage/supplier/product/{productId}/custom-sheet")
public class SupplierCustomOrderSheetController {

    private final ProductOptionSkuService service;
    private final SupplierDAO supplierDAO;

    private String supplierId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        String userId = auth.getName();

        String supplierId = supplierDAO.findSupplierIdByUserId(userId);
        if (supplierId == null) {
            throw new IllegalStateException("공급자 정보가 없습니다.");
        }
        return supplierId;
    }


    @GetMapping("/new")
    public String form(@PathVariable Long productId,
                       @RequestParam(required = false) Long inquiryId,
                       Model model) {
        CustomOrderSheetForm form = new CustomOrderSheetForm();
        form.setInquiryId(inquiryId);

        model.addAttribute("productId", productId);
        model.addAttribute("form", form);
        return "mypage/supplier/product/custom_sheet_form";
    }

    @PostMapping
    public String create(@PathVariable Long productId,
                         @ModelAttribute("form") CustomOrderSheetForm form,
                         RedirectAttributes ra) {

        try {
            String skuId = service.createCustomOrderSheet(productId, supplierId(), form);
            ra.addFlashAttribute("createdSkuId", skuId);
            ra.addFlashAttribute("saved", true);
            return "redirect:/mypage/supplier/product/" + productId + "/custom-sheet/new";
        } catch (IllegalStateException | IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/supplier/product/" + productId + "/custom-sheet/new";
        }
    }
}
