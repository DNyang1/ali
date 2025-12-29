package com.finalProject.ali.supplier_mypage.product.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import com.finalProject.ali.supplier_mypage.product.dto.ProductDTO;
import com.finalProject.ali.supplier_mypage.product.service.SupplierProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class SupplierProductController extends BaseSupplierController {

    private final SupplierProductService supplierProductService;

    // 임시 supplier_id (로그인 붙이면 여기만 교체)
    private String supplierId() {
        return "TEST_SUPPLIER";
    }

    @GetMapping("/supplier_mypage/product")
    public String list(Model model) {
        model.addAttribute("pageTitle", "상품 관리");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("products",
                supplierProductService.list(supplierId()));

        return "supplier_mypage/product/index";
    }

    @GetMapping("/supplier_mypage/product/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "상품 등록");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("form", new ProductDTO());
        return "supplier_mypage/product/new";
    }

    @PostMapping("/supplier_mypage/product")
    public String create(@ModelAttribute("form") ProductDTO form) {
        Long newId = supplierProductService.create(form, supplierId());
        return "redirect:/supplier_mypage/product/" + newId;
    }

    @GetMapping("/supplier_mypage/product/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("pageTitle", "상품 상세");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("product", supplierProductService.get(id));
        return "supplier_mypage/product/detail";
    }

    @GetMapping("/supplier_mypage/product/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("pageTitle", "상품 수정");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("form", supplierProductService.get(id));
        return "supplier_mypage/product/edit";
    }

    @PostMapping("/supplier_mypage/product/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @ModelAttribute("form") ProductDTO form) {
        form.setProductId(id);
        supplierProductService.update(form);
        return "redirect:/supplier_mypage/product/" + id;
    }

    @PostMapping("/supplier_mypage/product/{id}/toggle")
    public String toggle(@PathVariable("id") Long id) {
        supplierProductService.toggleStatus(id);
        return "redirect:/supplier_mypage/product";
    }
}
