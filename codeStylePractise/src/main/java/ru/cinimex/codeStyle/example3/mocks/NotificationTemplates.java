package ru.cinimex.codeStyle.example3.mocks;

import lombok.Data;

@Data
public class NotificationTemplates {
    private Long id;
    private String sendingType;
    private Long order;
    private Integer pushWaitingTime;
    private Boolean isEnabled;
    private String templateText;
    private String topic;
    private boolean isDaytime;
    private Integer templateCode;
    private String pushType;
    private String pushExternalId;
}
