package ru.cinimex.codeStyle.example3.mocks;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.cinimex.codeStyle.example1.mocks.ClientInfo;

public class ClientAdapter {

    public List<NotificationTemplates> getNotificationTemplates(String type) {
        return Stream.of(new NotificationTemplates(),
                        new NotificationTemplates())
                .collect(Collectors.toList());
    }

    public RiskPhoneInfo getPhoneInfoFromRisk(PhoneInfo phoneInfo) {
        return null;
    }

    public List<PushToken> getPushTokens(String clientId, boolean b) {
        return Stream.of(new PushToken(),
                        new PushToken())
                .collect(Collectors.toList());
    }

    public ClientInfo getClientInfo(Object o, String clientId, Object o1) {
        return new ClientInfo();
    }
}
