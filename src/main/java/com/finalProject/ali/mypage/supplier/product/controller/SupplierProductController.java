package com.finalProject.ali.mypage.supplier.product.controller;

import com.finalProject.ali.mypage.supplier.category.dao.SupplierCategoryDAO;
import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import com.finalProject.ali.mypage.supplier.product.dao.SupplierDAO;
import com.finalProject.ali.mypage.supplier.product.dto.ProductDTO;
import com.finalProject.ali.mypage.supplier.product.service.SupplierProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class SupplierProductController extends BaseSupplierController {

    private final SupplierProductService supplierProductService;
    private final SupplierCategoryDAO categoryDAO;
    private final SupplierDAO supplierDAO;

    private String supplierId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        System.out.println("LOGIN userId = " + userId);

        String supplierId = supplierDAO.findSupplierIdByUserId(userId);
        System.out.println("MAPPED supplierId = " + supplierId);

        return supplierId;
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

        model.addAttribute("rootCategories", categoryDAO.findRoot());
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

        ProductDTO product = supplierProductService.get(id, supplierId());
        if (product == null) {
            return "redirect:/mypage/supplier/product?error=notfound";
            }

        model.addAttribute("product", product);
        return "mypage/supplier/product/detail";
    }

    @GetMapping("/mypage/supplier/product/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("pageTitle", "상품 수정");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        ProductDTO form = supplierProductService.get(id, supplierId());
        model.addAttribute("form", form);

        model.addAttribute("rootCategories", categoryDAO.findRoot());

        if (form.getCategoryId() != null && !form.getCategoryId().isBlank()) {
            var leaf = categoryDAO.findById(form.getCategoryId());
            if (leaf != null) {
                var parent = (leaf.getParentId() != null) ? categoryDAO.findById(leaf.getParentId()) : null;
                var root   = (parent != null && parent.getParentId() != null) ? categoryDAO.findById(parent.getParentId()) : parent;

                String cat1 = (root != null) ? root.getCategoryId() : null;
                String cat2 = (parent != null) ? parent.getCategoryId() : null;
                String cat3 = leaf.getCategoryId();

                model.addAttribute("cat1Selected", cat1);
                model.addAttribute("cat2Selected", cat2);
                model.addAttribute("cat3Selected", cat3);


                if (cat1 != null) model.addAttribute("cat2Options", categoryDAO.findChildren(cat1));
                if (cat2 != null) model.addAttribute("cat3Options", categoryDAO.findChildren(cat2));
            }
        }

        return "mypage/supplier/product/edit";
    }

    @PostMapping("/mypage/supplier/product/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @ModelAttribute("form") ProductDTO form) {
        form.setProductId(id);
        supplierProductService.update(form, supplierId());
        return "redirect:/mypage/supplier/product/" + id;
    }

    @PostMapping("/mypage/supplier/product/{id}/toggle")
    public String toggle(@PathVariable("id") Long id) {
        supplierProductService.toggleStatus(id, supplierId());
        return "redirect:/mypage/supplier/product";
    }
}
