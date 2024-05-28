package ru.cinimex.codeStyle.example1.mocks.service.impl;

import ru.cinimex.codeStyle.example1.mocks.PaymentScheduleInfo;
import ru.cinimex.codeStyle.example1.mocks.service.OptionService;

public class OptionServiceImpl implements OptionService {

    @Override
    public PaymentScheduleInfo getAvailablePaymentForShift(PaymentScheduleInfo paymentScheduleInfo){

        return new PaymentScheduleInfo();
    }
}
