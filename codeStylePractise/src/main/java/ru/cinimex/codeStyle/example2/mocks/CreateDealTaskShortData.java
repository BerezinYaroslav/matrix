package ru.cinimex.codeStyle.example2.mocks;

import java.util.Set;

import lombok.Data;

@Data
public class CreateDealTaskShortData {
    private long id;
    private Set<DealProductShortData> dealProductData;
}
