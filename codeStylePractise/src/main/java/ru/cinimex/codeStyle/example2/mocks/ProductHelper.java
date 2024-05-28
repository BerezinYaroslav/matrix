package ru.cinimex.codeStyle.example2.mocks;

import lombok.Data;

public class ProductHelper {
    public ProductGroup getProductGroup(int productGroupId){
        return new ProductGroup();
    }

    @Data
    public class ProductGroup{
        private String name;
    }
}
