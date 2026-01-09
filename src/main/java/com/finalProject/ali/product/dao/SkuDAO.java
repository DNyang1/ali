package com.finalProject.ali.product.dao;

import com.finalProject.ali.product.dto.OptionDTO;
import com.finalProject.ali.product.dto.SkuDTO;
import com.finalProject.ali.product.dto.SkuRowDTO;
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
    void deleteLinksBySkuId(@Param("skuId") String skuId);
    int countOrderItemsBySkuId(@Param("skuId") String skuId);
    Long findProductIdBySkuId(@Param("skuId") String skuId);
    SkuRowDTO findBySkuId(@Param("skuId") String skuId);
    int updateStatus(@Param("skuId") String skuId, @Param("status") String status);
    int updateMoq(@Param("skuId") String skuId, @Param("moq") Long moq);
    void updateStock(@Param("skuId") String skuId,
                     @Param("stock") Long stock);
    int countDuplicateSkuCombination(Long productId, List<String> optionValueIds, int cnt);
    List<OptionDTO> findOptionsBySkuIdV2(@Param("skuId") String skuId);
    int countDuplicateSkuCombinationExcludingSku(Long productId, String skuId, List<String> optionValueIds, int cnt);
    int countBySkuPrefix(@Param("prefix") String prefix);

    List<SkuRowDTO> findSkuRowsByProductId(Long productId);

    SkuDTO findSkuByOptionValues(
            @Param("optionValueIds") List<String> optionValueIds,
            @Param("optionCount") int optionCount
    );

}
