package ru.cinimex.codeStyle.example1.mocks.service.impl;

import ru.cinimex.codeStyle.example1.mocks.Marketing;
import ru.cinimex.codeStyle.example1.mocks.OrderInfo;

public class OptionCommonsService {

    public Integer getAmountForOptionTariff(OrderInfo orderInfo){
        return 1;
    }

    public Marketing getMarketing(OrderInfo orderInfo, Object obj){
        return Marketing.builder().build();
    }
}
