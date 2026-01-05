package com.finalProject.ali.products.dao;

import com.finalProject.ali.products.dto.OptionValueDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OptionsDAO {

    List<OptionValueDTO> optionByProduct(
            @Param("productId") Long productId
    );
}
