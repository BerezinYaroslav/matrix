package ru.cinimex.codeStyle.example1.mocks.service.impl;

import ru.cinimex.codeStyle.example1.mocks.Marketing;
import ru.cinimex.codeStyle.example1.mocks.OrderInfo;

public class OptionAvailabilityService {

    public boolean checkAvailabilityForOptionShift(OrderInfo orderInfo, Marketing marketing){
        return true;
    }

    public Boolean checkAvailabilityForOptionSix(OrderInfo orderInfo){
        return Boolean.TRUE;
    }
}
