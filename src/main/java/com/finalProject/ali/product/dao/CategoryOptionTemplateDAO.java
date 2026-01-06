package com.finalProject.ali.product.dao;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryOptionTemplateDAO {

    List<TemplateRow> findTemplatesByCategoryId(@Param("categoryId") String categoryId);
    List<TemplateRow> findTemplatesByCategoryIds(@Param("categoryIds") List<String> categoryIds);
    List<String> findTemplateValues(@Param("templateId") String templateId);
    List<TemplateValueRow> findTemplateValueRows(@Param("templateId") String templateId);

    @Getter @Setter
    class TemplateRow {
        private String templateId;
        private String categoryId;
        private String optionName;
        private String inputType;
        private int isRequired;
        private int sortOrder;
    }
    @SuppressWarnings("unused")
    @Getter @Setter
    class TemplateValueRow {
        private String optionValue;
        private int sortOrder;
    }
}
