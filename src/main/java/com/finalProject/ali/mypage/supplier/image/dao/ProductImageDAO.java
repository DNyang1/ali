package com.finalProject.ali.mypage.supplier.image.dao;

import com.finalProject.ali.mypage.supplier.image.dto.ProductImageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductImageDAO {

    ProductImageDTO findThumb(@Param("productId") Long productId);
    ProductImageDTO findById(@Param("imageId") Long imageId);
    List<ProductImageDTO> findByType(@Param("productId") Long productId,
                                     @Param("imageType") String imageType);

    void insert(@Param("productId") Long productId,
                @Param("imageType") String imageType,
                @Param("imagePath") String imagePath,
                @Param("sortOrder") int sortOrder);

    void deleteById(@Param("imageId") Long imageId);

    Integer nextSort(@Param("productId") Long productId,
                     @Param("imageType") String imageType);

}
