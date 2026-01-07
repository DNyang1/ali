package com.finalProject.ali.product.dao;

import com.finalProject.ali.product.dto.OptionDTO;
import com.finalProject.ali.product.dto.StockOptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OptionDAO {
    //태민
    List<OptionDTO> findByProductId(Long productId);

    int existsProductOption(@Param("productId") Long productId,
                            @Param("optionName") String optionName);

    String findProductOptionId(@Param("productId") Long productId,
                               @Param("optionName") String optionName);

    void insertProductOption(@Param("optionId") String optionId,
                             @Param("productId") Long productId,
                             @Param("categoryId") String categoryId,
                             @Param("optionName") String optionName);

    void insertProductOptionValue(@Param("optionValueId") String optionValueId,
                                  @Param("optionId") String optionId,
                                  @Param("optionValue") String optionValue,
                                  @Param("sortOrder") Integer sortOrder);

    void updateOptionValue(@Param("optionId") String optionId,
                           @Param("optionValue") String optionValue);
    // 현성
    List<OptionDTO> optionByProduct(
            @Param("productId") Long productId
    );

    List<StockOptionDTO> findOptionsByProductStock(@Param("productId") Long productId);


}


