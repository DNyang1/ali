package com.finalProject.ali.mypage.supplier.product.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryOptionTemplateDAO {

    List<TemplateRow> findTemplatesByCategoryId(@Param("categoryId") String categoryId);
    List<TemplateRow> findTemplatesByCategoryIds(@Param("categoryIds") List<String> categoryIds);
    List<String> findTemplateValues(@Param("templateId") String templateId);
    List<TemplateValueRow> findTemplateValueRows(@Param("templateId") String templateId);

    class TemplateRow {
        private String templateId;
        private String categoryId;
        private String optionName;
        private String inputType;
        private int isRequired;
        private int sortOrder;

        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }

        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

        public String getOptionName() { return optionName; }
        public void setOptionName(String optionName) { this.optionName = optionName; }

        public String getInputType() { return inputType; }
        public void setInputType(String inputType) { this.inputType = inputType; }

        public int getIsRequired() { return isRequired; }
        public void setIsRequired(int isRequired) { this.isRequired = isRequired; }

        public int getSortOrder() { return sortOrder; }
        public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    }

    class TemplateValueRow {
        private String optionValue;
        private int sortOrder;

        public String getOptionValue() { return optionValue; }
        public void setOptionValue(String optionValue) { this.optionValue = optionValue; }

        public int getSortOrder() { return sortOrder; }
        public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    }
}
