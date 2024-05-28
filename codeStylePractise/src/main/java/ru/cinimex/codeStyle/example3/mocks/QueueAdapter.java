package ru.cinimex.codeStyle.example3.mocks;

import lombok.Data;

@Data
public class QueueAdapter {
    public void sendNotificationToTable() {
    }

    public void sendNotificationToTable(TemplateAndRequestOfNotice templateAndRequestOfNotice) {
    }

    public void sendEmail(TemplateAndRequestOfNotice templateAndRequestOfNotice) {
    }

    public void sendPush(TemplateAndRequestOfNotice templateAndRequestOfNotice) {
    }

    public void sendSms(TemplateAndRequestOfNotice build) {
    }
}
