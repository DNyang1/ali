package com.finalProject.ali.mypage.supplier.product.dao;

import com.finalProject.ali.mypage.supplier.product.dto.OptionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OptionDAO {
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

    int existsProductOptionValue(@Param("optionId") String optionId,
                                 @Param("optionValue") String optionValue);

}


