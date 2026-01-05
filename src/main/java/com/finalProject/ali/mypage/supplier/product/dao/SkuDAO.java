package com.finalProject.ali.mypage.supplier.product.dao;

import com.finalProject.ali.mypage.supplier.product.dto.OptionDTO;
import com.finalProject.ali.mypage.supplier.product.dto.SkuDTO;
import com.finalProject.ali.mypage.supplier.product.dto.SkuPriceDTO;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SkuDAO {

    void insert(SkuDTO sku);
    List<SkuDTO> findByProductId(@Param("productId") Long productId);
    int countByProductId(@Param("productId") Long productId);
    void insertLink(@Param("skuId") String skuId,
                    @Param("optionId") String optionValueId);
    List<OptionDTO> findOptionsBySkuId(@Param("skuId") String skuId);
    void deleteLinksBySkuId(@Param("skuId") String skuId);
    int countOrderItemsBySkuId(@Param("skuId") String skuId);
    Long findProductIdBySkuId(@Param("skuId") String skuId);
    SkuRow findBySkuId(@Param("skuId") String skuId);
    int updateStatus(@Param("skuId") String skuId, @Param("status") String status);
    List<SkuPriceDTO> findPricesBySkuId(@Param("skuId") String skuId);
    @Getter@Setter
    class SkuRow {
        private String skuId;
        private String status;
        private Long stockQuantity;
    }

}
