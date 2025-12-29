package com.finalProject.ali.mypage.supplier.product.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import com.finalProject.ali.mypage.supplier.product.dto.ProductDTO;
import com.finalProject.ali.mypage.supplier.product.service.SupplierProductService;
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

    @GetMapping("/mypage/supplier/product")
    public String list(Model model) {
        model.addAttribute("pageTitle", "상품 관리");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("products",
                supplierProductService.list(supplierId()));

        return "mypage/supplier/product/index";
    }

    @GetMapping("/mypage/supplier/product/new")
    public String createForm(Model model) {
        model.addAttribute("pageTitle", "상품 등록");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("form", new ProductDTO());
        return "mypage/supplier/product/new";
    }

    @PostMapping("/mypage/supplier/product")
    public String create(@ModelAttribute("form") ProductDTO form) {
        Long newId = supplierProductService.create(form, supplierId());
        return "redirect:/mypage/supplier/product/" + newId;
    }

    @GetMapping("/mypage/supplier/product/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("pageTitle", "상품 상세");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("product", supplierProductService.get(id));
        return "mypage/supplier/product/detail";
    }

    @GetMapping("/mypage/supplier/product/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("pageTitle", "상품 수정");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("form", supplierProductService.get(id));
        return "mypage/supplier/product/edit";
    }

    @PostMapping("/mypage/supplier/product/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @ModelAttribute("form") ProductDTO form) {
        form.setProductId(id);
        supplierProductService.update(form);
        return "redirect:/mypage/supplier/product/" + id;
    }

    @PostMapping("/mypage/supplier/product/{id}/toggle")
    public String toggle(@PathVariable("id") Long id) {
        supplierProductService.toggleStatus(id);
        return "redirect:/mypage/supplier/product";
    }
}
