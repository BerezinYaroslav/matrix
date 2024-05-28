package ru.cinimex.codeStyle.example1.mocks.service;

import ru.cinimex.codeStyle.example1.mocks.PaymentScheduleInfo;

public interface OptionService {
    public PaymentScheduleInfo getAvailablePaymentForShift(PaymentScheduleInfo paymentScheduleInfo);
}
