package ru.cinimex.codeStyle.example1.mocks;

import lombok.Data;

@Data
public class OrderInfo {
    private Integer id;
    private PaymentScheduleInfo paymentSchedules;
    private ClientInfo clientInfo;

}
