package com.finalProject.ali.supplier_mypage.product.controller;

import com.finalProject.ali.supplier_mypage.common.controller.BaseSupplierController;
import com.finalProject.ali.supplier_mypage.product.dto.OptionDTO;
import com.finalProject.ali.supplier_mypage.product.dto.SkuDTO;
import com.finalProject.ali.supplier_mypage.product.service.ProductOptionSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class SupplierProductOptionSkuController extends BaseSupplierController {

    private final ProductOptionSkuService service;

    @GetMapping("/supplier_mypage/product/{productId}/option")
    public String optionPage(@PathVariable Long productId, Model model) {
        model.addAttribute("pageTitle", "옵션 관리");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("productId", productId);
        model.addAttribute("options", service.options(productId));
        model.addAttribute("form", new OptionDTO());
        return "supplier_mypage/product/option";
    }

    @PostMapping("/supplier_mypage/product/{productId}/option")
    public String addOption(@PathVariable Long productId,
                            @ModelAttribute("form") OptionDTO form) {
        service.addOption(productId, form);
        return "redirect:/supplier_mypage/product/" + productId + "/option";
    }

    @GetMapping("/supplier_mypage/product/{productId}/sku")
    public String skuPage(@PathVariable Long productId, Model model) {
        model.addAttribute("pageTitle", "SKU 관리");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("productId", productId);
        model.addAttribute("skus",service.getSkusWithEditable(productId));
        model.addAttribute("options", service.options(productId));
        model.addAttribute("skuForm", new SkuDTO());
        return "supplier_mypage/product/sku";
    }

    @PostMapping("/supplier_mypage/product/{productId}/sku")
    public String addSku(@PathVariable Long productId,
                         @ModelAttribute("skuForm") SkuDTO skuForm,
                         @RequestParam(value = "optionIds", required = false) java.util.List<String> optionIds) {

        String skuId = service.addSku(skuForm);

        // 생성과 동시에 옵션 연결(그래야 SKU 목록 조회에 잡힘)
        if (optionIds != null) {
            for (String optionId : optionIds) {
                service.link(skuId, optionId);
            }
        }

        return "redirect:/supplier_mypage/product/" + productId + "/sku";
    }

    @GetMapping("/supplier_mypage/product/{productId}/sku/{skuId}/edit-options")
    public String editSkuOptions(@PathVariable Long productId,
                                 @PathVariable String skuId,
                                 Model model) {
        if (!service.canEditSkuOptions(skuId)) {
            return "redirect:/supplier_mypage/product/" + productId + "/sku?error=sold";
        }
        model.addAttribute("pageTitle", "SKU 옵션 수정");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("productId", productId);
        model.addAttribute("skuId", skuId);

        var allOptions = service.options(productId);

        var linked = service.findOptionsBySkuId(skuId);
        java.util.Set<String> selected = new java.util.HashSet<>();
        for (var o : linked) selected.add(o.getOptionId());

        model.addAttribute("options", allOptions);
        model.addAttribute("selectedOptionIds", selected);

        return "supplier_mypage/product/sku-edit-options";
    }

    @PostMapping("/supplier_mypage/product/{productId}/sku/{skuId}/edit-options")
    public String updateSkuOptions(@PathVariable Long productId,
                                   @PathVariable String skuId,
                                   @RequestParam(value = "optionIds", required = false)
                                   java.util.List<String> optionIds) {

        try {
            service.updateSkuOptions(skuId, optionIds);
            return "redirect:/supplier_mypage/product/" + productId + "/sku?updated=1";
        } catch (IllegalStateException e) {
            return "redirect:/supplier_mypage/product/" + productId + "/sku?error=sold";
        } catch (IllegalArgumentException e) {
            return "redirect:/supplier_mypage/product/" + productId + "/sku/{skuId}/edit-options?error=empty";
        }
    }




}
