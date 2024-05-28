package ru.cinimex.codeStyle.example3.mocks;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TemplateAndRequestOfNotice {
    private String pushId;
    private long statusId;
    private String phone;
    private String sendingType;
    private boolean isDayTime;
    private String description;
    private String template;
    private String token;
    private String topic;
    private int tokenId;
    private String clientId;
    private String orderId;
    private String paymentId;
    private String text;
    private Long templateId;
    private String email;
    private String pushType;
    private String pushExternalId;
}
