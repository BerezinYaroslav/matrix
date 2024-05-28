package ru.cinimex.codeStyle.example3.mocks;

import java.util.List;

import lombok.Builder;

@Builder
public class PushCreate {
    private String clientId;
    private String orderId;
    private String phone;
    private String pushMessageText;
    private String smsMessageText;
    private String templateCode;
    private boolean isSMSRequired;
    private int pushWaitingTime;
    private List<PushInner> pushTokens;
}
