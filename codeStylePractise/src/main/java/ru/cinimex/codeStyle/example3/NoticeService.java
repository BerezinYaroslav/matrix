package ru.cinimex.codeStyle.example3;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import ru.cinimex.codeStyle.example1.mocks.ClientInfo;
import ru.cinimex.codeStyle.example3.mocks.ClientAdapter;
import ru.cinimex.codeStyle.example3.mocks.ClientNotFoundException;
import ru.cinimex.codeStyle.example3.mocks.NoValidChannelException;
import ru.cinimex.codeStyle.example3.mocks.NoticeAddress;
import ru.cinimex.codeStyle.example3.mocks.NoticeApiParamDomain;
import ru.cinimex.codeStyle.example3.mocks.NoticeApiRequestDomain;
import ru.cinimex.codeStyle.example3.mocks.NoticeApiResponse;
import ru.cinimex.codeStyle.example3.mocks.NotificationTemplates;
import ru.cinimex.codeStyle.example3.mocks.PhoneInfo;
import ru.cinimex.codeStyle.example3.mocks.PushCreate;
import ru.cinimex.codeStyle.example3.mocks.PushInner;
import ru.cinimex.codeStyle.example3.mocks.PushNotificationService;
import ru.cinimex.codeStyle.example3.mocks.PushToken;
import ru.cinimex.codeStyle.example3.mocks.QueueAdapter;
import ru.cinimex.codeStyle.example3.mocks.RiskPhoneInfo;
import ru.cinimex.codeStyle.example3.mocks.StringSubstitutor;
import ru.cinimex.codeStyle.example3.mocks.SubstitutionException;
import ru.cinimex.codeStyle.example3.mocks.TemplateAndRequestOfNotice;
import ru.cinimex.codeStyle.example3.mocks.TemplateNotFoundException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class NoticeService {
    private static final String NOTIFICATION_TEMPLATES_SMS = "sms";
    private static final String NOTIFICATION_TEMPLATES_EMAIL = "email";
    private static final String NOTIFICATION_TEMPLATES_PUSH = "push";
    private static final int smsAndPushTemplatesCount = 2;
    private static final long UNSUCCESSFUL = 2L;

    private final ClientAdapter feignAdapter;
    private final QueueAdapter queueAdapter;
    private final PushNotificationService pushNotificationService;

    public NoticeApiResponse sendNotification(NoticeApiRequestDomain request) {
        List<NotificationTemplates> templates = feignAdapter.getNotificationTemplates(request.getType());
        if (templates == null || templates.isEmpty()) {
            throw new TemplateNotFoundException("no template found for type: " + request.getType());
        }

        // Если был передан параметр channel, то рассылка только переданным каналам. Если параметра нет, то рассылка по всем каналам
        List<String> channels = request.getParams()
                .stream()
                .filter(p -> p.getName().equals("channel"))
                .flatMap(p -> Arrays.stream(p.getValue().split(",")))
                .collect(toList());
        log.info("Channels for notification: {}", channels);
        if (!channels.isEmpty()) {
            templates = templates.stream()
                    .filter(t -> channels.contains(t.getSendingType()))
                    .collect(toList());
        }
        ClientInfo clientInfo = getClientInfo(request);
        var addressMap = templates.stream()
                .collect(toMap(Function.identity(),
                        template -> getAddressesForTemplate(template, request, clientInfo)));

        if (addressMap.values().stream().noneMatch(NoticeAddress::isPresent)) {
            log.error("Could not find addresses for any of the templates");
            addressMap.entrySet()
                    .stream()
                    .sorted(Comparator.comparingLong(entry -> entry.getKey().getOrder()))
                    .forEach(entry -> sendErrorNotice(request, entry));
            throw new NoValidChannelException();
        }

        Map<NotificationTemplates, ? extends NoticeAddress<?>> pushAndSmsMap = addressMap.entrySet()
                .stream()
                .filter(entry -> ((NOTIFICATION_TEMPLATES_PUSH.equals(entry.getKey().getSendingType())
                        && !((List<PushToken>) entry.getValue().getAddress()).isEmpty())
                        || (NOTIFICATION_TEMPLATES_SMS.equals(entry.getKey().getSendingType())
                        && entry.getKey().getPushWaitingTime() != null)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        Integer isPushWaitingTime = addressMap.keySet()
                .stream()
                .filter(nt -> NOTIFICATION_TEMPLATES_SMS.equals(nt.getSendingType()))
                .map(NotificationTemplates::getPushWaitingTime)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        Boolean isSMSRequired = addressMap.keySet()
                .stream()
                .filter(nt -> NOTIFICATION_TEMPLATES_SMS.equals(nt.getSendingType()))
                .filter(NotificationTemplates::getIsEnabled)
                .map(nt -> Boolean.TRUE)
                .findFirst()
                .orElse(Boolean.FALSE);
        Map<Integer, String> tokenAndUuidMap = addressMap.entrySet()
                .stream()
                .filter(entry -> NOTIFICATION_TEMPLATES_PUSH.equals(entry.getKey().getSendingType()))
                .map(Map.Entry::getValue)
                .flatMap(na -> Stream.of((List<PushToken>) na.getAddress()))
                .flatMap(List::stream)
                .collect(toMap(PushToken::getId, value -> UUID.randomUUID().toString()));
        PushCreate pushCreate = getPushCreate(addressMap, request, isPushWaitingTime, isSMSRequired,
                getClientPhone(request, clientInfo), tokenAndUuidMap);
        pushNotificationService.push(pushCreate);
        if (pushAndSmsMap.size() == smsAndPushTemplatesCount) {
            addressMap.entrySet()
                    .stream()
                    .filter(entry -> !NOTIFICATION_TEMPLATES_SMS.equals(entry.getKey().getSendingType()))
                    .filter(entry -> entry.getKey().getIsEnabled() && entry.getValue().isPresent())
                    .sorted(Comparator.comparingLong(entry -> entry.getKey().getOrder()))
                    .forEach(entry -> saveAndPushNotification(entry, request, tokenAndUuidMap));
        } else {
            addressMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().getIsEnabled() && entry.getValue().isPresent())
                    .sorted(Comparator.comparingLong(entry -> entry.getKey().getOrder()))
                    .forEach(entry -> saveAndPushNotification(entry, request, tokenAndUuidMap));
        }
        return new NoticeApiResponse();
    }

    private void saveAndPushNotification(Map.Entry<NotificationTemplates, ? extends NoticeAddress<?>> entry,
                                         NoticeApiRequestDomain request, Map<Integer, String> tokenAndUuidMap) {
        if (NOTIFICATION_TEMPLATES_PUSH.equals(entry.getKey().getSendingType())) {
            List<PushToken> address = (List<PushToken>) entry.getValue().getAddress();
            address.stream()
                    .filter(PushToken::getIsActive)
                    .forEach(pt -> sendPush(request, entry.getKey(), tokenAndUuidMap.get(pt.getId()), pt));
        } else {
            processAndSend(request, entry.getKey(), entry.getValue());
        }
    }


    private void processAndSend(NoticeApiRequestDomain request, NotificationTemplates template,
                                NoticeAddress<?> address) {
        switch (template.getSendingType()) {
            case NOTIFICATION_TEMPLATES_SMS -> sendSms(request, template, (String) address.getAddress());
            case NOTIFICATION_TEMPLATES_EMAIL -> sendEmail(request, template, (String) address.getAddress());
            default -> log.error("Couldn't send message to channel: {}", template.getSendingType());
        }
    }

    private NoticeAddress<?> getAddressesForTemplate(NotificationTemplates template, NoticeApiRequestDomain request,
                                                     ClientInfo clientInfo) {
        switch (template.getSendingType()) {
            case NOTIFICATION_TEMPLATES_SMS -> {
                return new NoticeAddress<>(getClientPhone(request, clientInfo));
            }
            case NOTIFICATION_TEMPLATES_EMAIL -> {
                return new NoticeAddress<>(getClientEmail(request, clientInfo));
            }
            case NOTIFICATION_TEMPLATES_PUSH -> {
                return new NoticeAddress<>(getPushTokensFromRequest(request));
            }
            default -> {
                log.error("Could not find address for channel '{}'", template.getSendingType());
                return new NoticeAddress<>();
            }
        }
    }

    private void sendSms(NoticeApiRequestDomain request, NotificationTemplates template, String phone) {
        RiskPhoneInfo phoneInfoFromRisk = feignAdapter.getPhoneInfoFromRisk(new PhoneInfo(phone));
        TemplateAndRequestOfNotice.TemplateAndRequestOfNoticeBuilder templateAndRequestOfNoticeBuilder =
                getTemplateAndRequestOfNoticeBuilder(request, template.getTemplateText(), template.getId());
        if (phoneInfoFromRisk != null && phoneInfoFromRisk.getIsInBlackList()) {
            templateAndRequestOfNoticeBuilder
                    .pushId(null)
                    .statusId(UNSUCCESSFUL)
                    .sendingType(template.getSendingType())
                    .description("Сообщение не отправлено: Номер телефона в черном списке");

            queueAdapter.sendNotificationToTable(templateAndRequestOfNoticeBuilder.build());
            log.info("Сообщение не отправлено: Номер телефона в черном списке, {}", phone);
        } else {
            templateAndRequestOfNoticeBuilder
                    .phone(phone)
                    .isDayTime(template.isDaytime())
                    .template(request.getType());

            queueAdapter.sendSms(templateAndRequestOfNoticeBuilder.build());
            log.info("Sent sms of type '{}' to number: {}", template.getTemplateCode(), phone);
        }
    }

    private void sendEmail(NoticeApiRequestDomain request, NotificationTemplates template, String email) {
        TemplateAndRequestOfNotice templateAndRequestOfNotice =
                getTemplateAndRequestOfNoticeBuilder(request, template.getTemplateText(), template.getId())
                        .email(email)
                        .topic(makeSubstitution(template.getTopic(), request.getParams()))
                        .isDayTime(template.isDaytime())
                        .template(request.getType())
                        .build();

        queueAdapter.sendEmail(templateAndRequestOfNotice);
        log.info("Sent email of type '{}' to address: {}", request.getType(), email);
    }

    private void sendPush(NoticeApiRequestDomain request, NotificationTemplates template, String pushExternalId,
                          PushToken token) {
        TemplateAndRequestOfNotice templateAndRequestOfNotice =
                getTemplateAndRequestOfNoticeBuilder(request, template.getTemplateText(), template.getId())
                        .token(token.getToken())
                        .tokenId(token.getId())
                        .topic("")
                        .template(request.getType())
                        .pushType(template.getPushType())
                        .pushExternalId(pushExternalId)
                        .build();

        queueAdapter.sendPush(templateAndRequestOfNotice);
        log.info("Sent push of type '{}' to token id: {}", request.getType(), token.getId());
    }

    private void sendErrorNotice(NoticeApiRequestDomain request,
                                 Map.Entry<NotificationTemplates, ? extends NoticeAddress<?>> entry) {
        TemplateAndRequestOfNotice templateAndRequestOfNotice =
                getTemplateAndRequestOfNoticeBuilder(request, entry.getKey().getTemplateText(), entry.getKey().getId())
                        .pushId(null)
                        .statusId(UNSUCCESSFUL)
                        .sendingType(entry.getKey().getSendingType())
                        .description("Could not find address for sending type")
                        .build();

        queueAdapter.sendNotificationToTable(templateAndRequestOfNotice);
    }

    private TemplateAndRequestOfNotice.TemplateAndRequestOfNoticeBuilder getTemplateAndRequestOfNoticeBuilder(
            NoticeApiRequestDomain request, String templateText, Long templateId) {
        return TemplateAndRequestOfNotice.builder()
                .clientId(request.getClientId())
                .orderId(request.getOrderId())
                .paymentId(request.getPaymentId())
                .text(makeSubstitution(templateText, request.getParams()))
                .templateId(templateId);
    }

    private String getClientPhone(NoticeApiRequestDomain request, ClientInfo clientInfo) {
        if (StringUtils.isNotBlank(request.getPhone())) {
            log.info("Found phone number in notice request");
            return request.getPhone();
        }
        log.info("Could not find phone in notice request");

        if (clientInfo == null || clientInfo.getPhone() == null) {
            log.error("Could not find client id in notice api request, or client info does not contain phone number");
            return null;
        }
        log.info("Found phone number in client info with client id = {}", clientInfo.getId());
        return clientInfo.getPhone();
    }

    private String getClientEmail(NoticeApiRequestDomain request, ClientInfo clientInfo) {
        if (StringUtils.isNotBlank(request.getEmail())) {
            log.info("Found email in notice request");
            return request.getEmail();
        }
        log.info("Could not find email in notice request");

        if (clientInfo == null || clientInfo.getEmail() == null) {
            log.error("Could not find client id in notice api request, or client info does not contain email");
            return null;
        }
        return clientInfo.getEmail();
    }

    private List<PushToken> getPushTokensFromRequest(NoticeApiRequestDomain request) {
        if (request.getClientId() == null) {
            log.error("Client id not found in notice request");
            return Collections.emptyList();
        }
        List<PushToken> pushTokensResponse = Collections.emptyList();
        try {
            pushTokensResponse = feignAdapter.getPushTokens(request.getClientId(), true);
            if (pushTokensResponse == null || pushTokensResponse.size() == 0) {
                log.error("Could not find push tokens for client id = {}", request.getClientId());
            }
        } catch (Exception e) {
            log.error("Could not find push tokens for client id = {}\n{}", request.getClientId(), e.getMessage());
        }
        return pushTokensResponse;
    }

    private String makeSubstitution(String templateText, List<NoticeApiParamDomain> params) {
        if (StringUtils.isBlank(templateText)) {
            throw new SubstitutionException("substitution template is null or empty");
        }
        Map<String, String> parameters = new HashMap<>();
        for (NoticeApiParamDomain param : params) {
            parameters.put(param.getName(), param.getValue());
        }
        StringSubstitutor substitutor = new StringSubstitutor(key -> {
            if (!parameters.containsKey(key)) {
                throw new SubstitutionException("No replacement value for variable: " + key);
            }
            return parameters.get(key);
        });

        return substitutor.replace(templateText);
    }

    private ClientInfo getClientInfo(NoticeApiRequestDomain request) {
        if (request.getClientId() != null) {
            var clientInfoResponse = feignAdapter.getClientInfo(null, request.getClientId(), null);
            if (clientInfoResponse == null) {
                log.error("Invalid client id = {}", request.getClientId());
                throw new ClientNotFoundException();
            }
            log.info("Found client info with supplied client id = {}", request.getClientId());
            return clientInfoResponse;
        }
        return null;
    }

    private PushCreate getPushCreate(Map<NotificationTemplates, ? extends NoticeAddress<?>> map,
                                     NoticeApiRequestDomain request, Integer isPushWaitingTime, Boolean isSMSRequired,
                                     String phone, Map<Integer, String> tokenAndUuidMap) {
        List<PushToken> pushTokens = map.entrySet()
                .stream()
                .filter(entry -> NOTIFICATION_TEMPLATES_PUSH.equals(entry.getKey().getSendingType()))
                .map(Map.Entry::getValue)
                .flatMap(na -> Stream.of((List<PushToken>) na.getAddress()))
                .flatMap(List::stream)
                .collect(toList());
        NotificationTemplates pushNotificationTemplates = map.keySet()
                .stream()
                .filter(noticeAddress -> NOTIFICATION_TEMPLATES_PUSH.equals(noticeAddress.getSendingType()))
                .findFirst()
                .orElse(null);
        NotificationTemplates smsNotificationTemplates = map.keySet()
                .stream()
                .filter(noticeAddress -> NOTIFICATION_TEMPLATES_SMS.equals(noticeAddress.getSendingType()))
                .findFirst()
                .orElse(null);
        return PushCreate.builder()
                .clientId(request.getClientId())
                .orderId(request.getOrderId())
                .phone(phone)
                .pushMessageText(pushNotificationTemplates != null
                        && NOTIFICATION_TEMPLATES_PUSH.equals(pushNotificationTemplates.getSendingType())
                        ? makeSubstitution(pushNotificationTemplates.getTemplateText(), request.getParams())
                        : null)
                .smsMessageText(smsNotificationTemplates != null
                        && NOTIFICATION_TEMPLATES_SMS.equals(smsNotificationTemplates.getSendingType())
                        ? makeSubstitution(smsNotificationTemplates.getTemplateText(), request.getParams())
                        : null)
                .templateCode(request.getType())
                .isSMSRequired(isSMSRequired)
                .pushWaitingTime(isPushWaitingTime)
                .pushTokens(pushTokens.stream()
                        .map(pt -> new PushInner(pt.getId(), tokenAndUuidMap.get(pt.getId())))
                        .collect(toList()))
                .build();
    }
}
