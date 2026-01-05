package com.finalProject.ali.mypage.supplier.product.controller;

import com.finalProject.ali.mypage.supplier.common.controller.BaseSupplierController;
import com.finalProject.ali.mypage.supplier.product.dao.SkuPriceDAO;
import com.finalProject.ali.mypage.supplier.product.dao.SupplierDAO;
import com.finalProject.ali.mypage.supplier.product.dto.OptionDTO;
import com.finalProject.ali.mypage.supplier.product.dto.SkuForm;
import com.finalProject.ali.mypage.supplier.product.dto.SkuPriceDTO;
import com.finalProject.ali.mypage.supplier.product.dto.SkuPriceRequestDTO;
import com.finalProject.ali.mypage.supplier.product.service.ProductOptionSkuService;
import com.finalProject.ali.mypage.supplier.product.service.SkuPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SupplierProductOptionSkuController extends BaseSupplierController {

    private final ProductOptionSkuService service;
    private final SkuPriceDAO skuPriceDAO;
    private final SupplierDAO supplierDAO;
    private final SkuPriceService skuPriceService;

    private String supplierId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        String userId = auth.getName();
        String supplierId = supplierDAO.findSupplierIdByUserId(userId);
        if (supplierId == null) throw new IllegalStateException("공급자 정보가 없습니다.");
        return supplierId;
    }



    @GetMapping("/mypage/supplier/product/{productId}/option")
    public String optionPage(@PathVariable Long productId, Model model) {

        service.ensureCategoryOptionsSeeded(productId, supplierId());

        model.addAttribute("pageTitle", "옵션 관리");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("productId", productId);
        model.addAttribute("options", service.options(productId, supplierId()));
        model.addAttribute("form", new OptionDTO());

        return "mypage/supplier/product/option";
    }

    @PostMapping("/mypage/supplier/product/{productId}/option")
    public String addOption(@PathVariable Long productId,
                            @ModelAttribute("form") OptionDTO form) {
        service.addOption(productId, supplierId(), form);
        return "redirect:/mypage/supplier/product/" + productId + "/option";
    }

    @GetMapping("/mypage/supplier/product/{productId}/sku")
    public String skuPage(@PathVariable Long productId, Model model) {

        service.ensureCategoryOptionsSeeded(productId, supplierId());

        model.addAttribute("pageTitle", "SKU 관리");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("productId", productId);
        model.addAttribute("skus", service.getSkusWithEditable(productId, supplierId()));

        var options = service.options(productId, supplierId());
        model.addAttribute("options", options);

        Map<String, List<OptionDTO>> optionGroups = options.stream()
                .collect(Collectors.groupingBy(
                        OptionDTO::getOptionName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("optionGroups", optionGroups);

        model.addAttribute("skuForm", new SkuForm());

        return "mypage/supplier/product/sku";
    }

    @PostMapping("/mypage/supplier/product/{productId}/sku")
    public String addSku(@PathVariable Long productId,
                         @ModelAttribute("skuForm") SkuForm skuForm,
                         @RequestParam(value = "optionValueIds", required = false)
                         List<String> optionValueIds) {

        skuForm.setOptionValueIds(optionValueIds);

        service.createSku(productId, supplierId(), skuForm);

        return "redirect:/mypage/supplier/product/" + productId + "/sku";
    }


    @GetMapping("/mypage/supplier/product/{productId}/sku/{skuId}/edit-options")
    public String editSkuOptions(@PathVariable Long productId,
                                 @PathVariable String skuId,
                                 Model model) {
        if (!service.canEditSkuOptions(skuId)) {
            return "redirect:/mypage/supplier/product/" + productId + "/sku?error=sold";
        }

        model.addAttribute("pageTitle", "SKU 옵션 수정");
        model.addAttribute("activeMenu", "product");
        addCommonAttributes(model);

        model.addAttribute("productId", productId);
        model.addAttribute("skuId", skuId);

        var allOptions = service.options(productId, supplierId());
        var linked = service.findOptionsBySkuId(productId, supplierId(), skuId);

        Set<String> selected = new HashSet<>();
        for (var o : linked) selected.add(o.getOptionId());

        model.addAttribute("options", allOptions);
        model.addAttribute("selectedOptionIds", selected);
        Map<String, List<OptionDTO>> optionGroups = allOptions.stream()
                .collect(Collectors.groupingBy(
                        OptionDTO::getOptionName,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("optionGroups", optionGroups);

        return "mypage/supplier/product/sku-edit-options";
    }

    @PostMapping("/mypage/supplier/product/{productId}/sku/{skuId}/edit-options")
    public String updateSkuOptions(@PathVariable Long productId,
                                   @PathVariable String skuId,
                                   @RequestParam(value = "optionValueIds", required = false)
                                   List<String> optionValueIds) {

        try {
            service.updateSkuOptions(productId, supplierId(), skuId, optionValueIds);
            return "redirect:/mypage/supplier/product/" + productId + "/sku?updated=1";
        } catch (IllegalStateException e) {
            return "redirect:/mypage/supplier/product/" + productId + "/sku?error=sold";
        } catch (IllegalArgumentException e) {
            return "redirect:/mypage/supplier/product/" + productId + "/sku/" + skuId + "/edit-options?error=empty";
        }
    }
    @GetMapping("/mypage/supplier/sku/{skuId}/prices")
    @ResponseBody
    public List<SkuPriceDTO> getSkuPrices(@PathVariable String skuId) {
        return skuPriceDAO.findBySkuId(skuId);
    }

    @PostMapping("/mypage/supplier/sku/{skuId}/status")
    public String updateSkuStatus(@PathVariable String skuId,
                                  @RequestParam String status,
                                  @RequestParam Long productId){
        service.updateStatus(skuId,status);
        return "redirect:/mypage/supplier/product/" + productId + "/sku";

    }
    @PostMapping("/mypage/supplier/sku/{skuId}/prices")
    @ResponseBody
    public String saveSkuPrices(@PathVariable String skuId,
                                @RequestBody SkuPriceRequestDTO req) {

        skuPriceService.replacePrices(skuId, req);
        return "OK";
    }


}
