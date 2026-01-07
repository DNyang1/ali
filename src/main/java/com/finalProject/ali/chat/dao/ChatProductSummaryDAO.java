package com.finalProject.ali.chat.dao;

import com.finalProject.ali.chat.dto.ProductSummaryDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatProductSummaryDAO {

    ProductSummaryDTO findProductSummary(Long productId);
}
