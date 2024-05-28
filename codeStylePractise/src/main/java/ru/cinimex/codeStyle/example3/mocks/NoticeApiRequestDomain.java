package ru.cinimex.codeStyle.example3.mocks;

import java.util.List;

import lombok.Data;

@Data
public class NoticeApiRequestDomain {
    private String type;
    private List<NoticeApiParamDomain> params;
    private String name;
    private String clientId;
    private String orderId;
    private String paymentId;
    private String phone;
    private String email;
}
