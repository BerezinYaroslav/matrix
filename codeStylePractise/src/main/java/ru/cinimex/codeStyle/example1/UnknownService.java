package ru.cinimex.codeStyle.example1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.cinimex.codeStyle.example1.mocks.DiscountType;
import ru.cinimex.codeStyle.example1.mocks.MarketClient;
import ru.cinimex.codeStyle.example1.mocks.MarketingMapper;
import ru.cinimex.codeStyle.example1.mocks.OptionType;
import ru.cinimex.codeStyle.example1.mocks.SHIFT;
import ru.cinimex.codeStyle.example1.mocks.SIX;
import ru.cinimex.codeStyle.example1.mocks.dto.AppliedDto;
import ru.cinimex.codeStyle.example1.mocks.ClientInfo;
import ru.cinimex.codeStyle.example1.mocks.GetOrderInfoByClientIdAndOrderIdOption;
import ru.cinimex.codeStyle.example1.mocks.Marketing;
import ru.cinimex.codeStyle.example1.mocks.Option;
import ru.cinimex.codeStyle.example1.mocks.OptionAvailability;
import ru.cinimex.codeStyle.example1.mocks.OrderInfo;
import ru.cinimex.codeStyle.example1.mocks.PaymentScheduleInfo;
import ru.cinimex.codeStyle.example1.mocks.ScoringType;
import ru.cinimex.codeStyle.example1.mocks.dto.AvailabilityDto;
import ru.cinimex.codeStyle.example1.mocks.enums.OptionTarget;
import ru.cinimex.codeStyle.example1.mocks.service.OptionService;
import ru.cinimex.codeStyle.example1.mocks.service.impl.OptionAvailabilityService;
import ru.cinimex.codeStyle.example1.mocks.service.impl.OptionCommonsService;
import ru.cinimex.codeStyle.example1.mocks.service.impl.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class UnknownService {
    MarketClient marketingClient;
    ClientInfo clientInfoClient;
    OptionService optionService;
    MarketingMapper marketingMapper;
    OptionAvailabilityService optionAvailabilityService;
    OptionCommonsService optionCommonsService;
    private static OptionTarget PURCHASE_SUCCESS;

    public List<GetOrderInfoByClientIdAndOrderIdOption> getOptionsList(Long clientId, OrderInfo orderInfo) {

        log.info("Получаем список всех возможных опций");
        List<Marketing> options = marketingClient.getAllMarketingOptionsForOrder(orderInfo.getId());
        var optionAvailabilities = marketingClient.checkOptionAvailabilityForClient(clientId);
        OptionAvailability optionAvailability = null;
        if (!optionAvailabilities.isEmpty()) {
            optionAvailability = optionAvailabilities.get(0);
        }

        Boolean isUnpaidOverdueExist = clientInfoClient.getClientOverdue(clientId).getBody().getHasOverdue();
        log.info("isUnpaidOverdueExist: " + isUnpaidOverdueExist);
        log.info("OrderInfo: " + orderInfo);
        log.info("По заказу " + orderInfo.getId() + " " +
                "isSixAvailable = " + (optionAvailability != null ? optionAvailability.getIsSixAvailable() : !isUnpaidOverdueExist) + " " +
                "и IsShiftAvailable = " + (optionAvailability != null ? optionAvailability.getIsShiftAvailable() : !isUnpaidOverdueExist));

        log.info("found options: " + options);
        log.info("Находим примененные опции");
        List<Marketing> appliedOptions = new ArrayList<>(getAppliedOptions(options, orderInfo));
        log.info("Примененные опции: " + appliedOptions);

        log.info("Находим доступные опции");
        List<Marketing> availabilityOptions = new ArrayList<>();

        PaymentScheduleInfo paymentToShift = optionService.getAvailablePaymentForShift(orderInfo.getPaymentSchedules());
        if (paymentToShift != null) {
            Option optionShift = marketingClient.getOption(SHIFT.getTypeCode(), paymentToShift.getPaymentSum());
            log.info("optionShift: " + optionShift);
            if (optionShift != null && options.stream().filter(m -> !m.getIsPaid()).map(Marketing::getOption).noneMatch(o -> o.getId().equals(optionShift.getId()))) {
                Marketing marketingForShift = Marketing.builder().build();
                marketingForShift.setClientId(orderInfo.getClientInfo().getId());
                marketingForShift.setOrderId(orderInfo.getId());
                marketingForShift.setScoringDate(LocalDateTime.now());
                marketingForShift.setRuleBlockNumber("mob");
                marketingForShift.setDecisionId(1L); //approved
                marketingForShift.setOptionAvailability(true);
                marketingForShift.setShowMode(true);
                marketingForShift.setIncludeIntoPayment(1);
                marketingForShift.setIsChosen(false);
                marketingForShift.setIsPaid(false);
                marketingForShift.setIsFinalOption(true);
                marketingForShift.setOption(optionShift);
                marketingForShift.setScoringTypeId(ScoringType.MARKETING.getId());
                Marketing savedMarketing = marketingClient.saveOrUpdateMarketing(marketingMapper.toDto(marketingForShift));
                log.info("Saved marketing: " + savedMarketing);

                options.stream()
                        .filter(o -> o.getOption().getOptionType().equals(SHIFT.getId()))
                        .filter(o -> !Boolean.TRUE.equals(o.getIsChosen()))
                        .peek(o -> log.info("Setting isFinalOption=false in marketing: " + o.getId())) // NOSONAR log
                        .forEach(m -> marketingClient.saveOrUpdateMarketing(marketingMapper.toDto(m.setIsFinalOption(Boolean.FALSE))));
                options = options.stream().filter(o -> !o.getOption().getOptionType().equals(SHIFT.getId())).collect(Collectors.toList());
                options.add(savedMarketing);
            } else {
                log.info(String.format("Опция сдвиг уже привязана к платежу %s с суммой %s", paymentToShift.getId(), paymentToShift.getPaymentSum()));
            }
        } else {
            log.info("Платёж для сдвига не найден. Опция сдвиг недоступна");
        }

        if (optionAvailability != null || !isUnpaidOverdueExist) {
            boolean isAppliedShift = appliedOptions.stream().anyMatch(o -> o.getOption().getOptionType().equals(SHIFT.getId())
                    && o.getScoringTypeId().equals(ScoringType.MARKETING.getId()));
            for (Marketing option : options) {
                log.info("isChosen: " + option.getIsChosen() + ", isAvailable: " + (optionAvailability != null ? optionAvailability.getIsShiftAvailable() : true));
                log.info("option: " + option);
                if (option.getIsPaid() != null
                        && !option.getIsPaid() &&
                        Objects.equals(option.getOption().getOptionType(), OptionType.SHIFT.getId()) &&
                        (optionAvailability == null || Objects.equals(optionAvailability.getIsShiftAvailable(), true))
                        && optionAvailabilityService.checkAvailabilityForOptionShift(orderInfo, option) &&
                        !isUnpaidOverdueExist && option.getScoringTypeId().equals(ScoringType.MARKETING.getId())) {
                    log.info("Опция Сдвиг доступна и еще не оплачена");
                    availabilityOptions.add(option);

                }
                if (DiscountType.isDiscount(option.getOption().getOptionType())) {
                    availabilityOptions.add(option);
                }
            }

            if ((optionAvailability == null || Objects.equals(optionAvailability.getIsSixAvailable(), true))
                    && optionAvailabilityService.checkAvailabilityForOptionSix(orderInfo)
                    && !isAppliedShift) {
                try {
                    log.info("Шестерка доступна, ищем подходящую опцию");
                    var sixOption = marketingClient.getOption(SIX.getTypeCode(), optionCommonsService.getAmountForOptionTariff(orderInfo));
                    var sixMarketing = optionCommonsService.getMarketing(orderInfo, sixOption);

                    log.info("Добавляем опцию Шестерка: " + sixMarketing);
                    availabilityOptions.add(sixMarketing);
                } catch (RuntimeException e) {
                    log.error("Ошибка при обработке опции Шестерка", e);
                    e.printStackTrace();
                }
            } else {
                log.info("Опция Шестерка недоступна");
            }
        }

        List<GetOrderInfoByClientIdAndOrderIdOption> responseOption = new ArrayList<>();
        if (!availabilityOptions.isEmpty()) {
            log.info("Формируем структуру доступных опций для отображения в мобильном приложении");
            responseOption.addAll(availabilityOptions.stream()
                    .filter(availableOptions -> !StringUtils.equals(availableOptions.getOption().getTarget(), PURCHASE_SUCCESS.getValue()))
                    .map(o -> getAvailabilityDTO(o, orderInfo))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        if (!appliedOptions.isEmpty()) {
            log.info("Формируем структуру примененных опций для отображения в мобильном приложении");
            Map<Long, HashSet<Long>> marketingMap = new HashMap<>();
            responseOption.addAll(appliedOptions.stream()
                    .filter(applOptions -> !StringUtils.equals(applOptions.getOption().getTarget(), PURCHASE_SUCCESS.getValue()))
                    .map(o -> getAppliedDTO(o, orderInfo, marketingMap))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        log.info("Available options: " + responseOption.stream()
                .filter(o -> !o.getIsApplied())
                .collect(Collectors.groupingBy(GetOrderInfoByClientIdAndOrderIdOption::getCode)));
        log.info("Applied options: " + responseOption.stream()
                .filter(GetOrderInfoByClientIdAndOrderIdOption::getIsApplied)
                .collect(Collectors.groupingBy(GetOrderInfoByClientIdAndOrderIdOption::getCode)));

        if (!responseOption.isEmpty()) {
            return responseOption;
        }
        log.info("У клиента нет доступных опций");
        return Collections.emptyList();
    }

    private List<Marketing> getAppliedOptions(List<Marketing> markets, OrderInfo orderInfo){

        return new ArrayList<>(Arrays.asList(Marketing.builder().build()));
    }

    private AvailabilityDto getAvailabilityDTO(Marketing marketing, OrderInfo orderInfo){
        return new AvailabilityDto();
    }

    private AppliedDto getAppliedDTO(Marketing marketing, OrderInfo orderInfo, Map<Long, HashSet<Long>> marketingMap){
        return new AppliedDto();
    }
}
