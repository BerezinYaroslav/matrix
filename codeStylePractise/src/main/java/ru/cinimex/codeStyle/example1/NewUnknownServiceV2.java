package ru.cinimex.codeStyle.example1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.cinimex.codeStyle.example1.mocks.*;
import ru.cinimex.codeStyle.example1.mocks.dto.AppliedDto;
import ru.cinimex.codeStyle.example1.mocks.dto.AvailabilityDto;
import ru.cinimex.codeStyle.example1.mocks.service.OptionService;
import ru.cinimex.codeStyle.example1.mocks.service.impl.OptionAvailabilityService;
import ru.cinimex.codeStyle.example1.mocks.service.impl.OptionCommonsService;
import ru.cinimex.codeStyle.example1.mocks.service.impl.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.cinimex.codeStyle.example1.mocks.enums.OptionTarget.PURCHASE_SUCCESS;

@Slf4j
@RequiredArgsConstructor
public class NewUnknownServiceV2 {
    private final MarketClient marketingClient;
    private final ClientInfo clientInfoClient;
    private final OptionService optionService;
    private final MarketingMapper marketingMapper;
    private final OptionAvailabilityService optionAvailabilityService;
    private final OptionCommonsService optionCommonsService;

    // void методы заменить на get
    // не забывать про NPE (возвращать Optional, если null - выкинуть исключение)
    // разбить большие методы на подметоды
    // не пересохранять данные в одну и ту же переменную несколько раз
    // не ставить условие в начале метода, выносить перед вызовом
    // методы с логированием - не самый лучший вариант*

    public List<GetOrderInfoByClientIdAndOrderIdOption> getOptionsList(Long clientId, OrderInfo orderInfo) {
        List<Marketing> options = getAllOptions(orderInfo);

        // необходимые условия
        // в logAllOptions и getAvailableOptions есть проверка на null
        OptionAvailability optionAvailability = getOptionAvailability(clientId).orElse(null);
        Boolean isUnpaidOverdueExist = clientInfoClient.getClientOverdue(clientId).getBody().getHasOverdue();

        logAllOptions(orderInfo, options, optionAvailability, isUnpaidOverdueExist);

        List<Marketing> appliedOptions = getAppliedOptions();

        // обновление всех опций (учитываем сдвиги)
        options = getUpdatedAndShiftedOptions(orderInfo, options);

        List<Marketing> availableOptions = getAvailableOptions(options,
                orderInfo, optionAvailability, isUnpaidOverdueExist, appliedOptions);

        List<GetOrderInfoByClientIdAndOrderIdOption> responseOptions = getResponseOptions(orderInfo,
                appliedOptions, availableOptions);

        logResponseOptions(responseOptions);

        return responseOptions;
    }

    private List<Marketing> getUpdatedAndShiftedOptions(OrderInfo orderInfo,
                                                        List<Marketing> options) {
        PaymentScheduleInfo paymentToShift = optionService.getAvailablePaymentForShift(orderInfo.getPaymentSchedules());
        Option optionShift = marketingClient.getOption(SHIFT.getTypeCode(), paymentToShift.getPaymentSum());
        log.info("optionShift: " + optionShift);

        List<Marketing> newOptions = options;

        if (paymentToShift != null) {
            if (optionShift != null && options.stream()
                    .filter(option -> !option.getIsPaid())
                    .map(Marketing::getOption)
                    .noneMatch(option -> option.getId().equals(optionShift.getId()))) {

                newOptions = getShiftedOptions(orderInfo, optionShift, newOptions);
            } else {
                log.info(String.format("Опция сдвиг уже привязана к платежу %s с суммой %s",
                        paymentToShift.getId(), paymentToShift.getPaymentSum()));
            }
        } else {
            log.info("Платёж для сдвига не найден. Опция сдвиг недоступна");
        }

        return newOptions;
    }

    private List<Marketing> getShiftedOptions(OrderInfo orderInfo, Option optionShift, List<Marketing> newOptions) {
        Marketing marketingForShift = getMarketingForShift(orderInfo, optionShift);
        Marketing savedMarketing = marketingClient.saveOrUpdateMarketing(marketingMapper.toDto(marketingForShift));
        log.info("Saved marketing: " + savedMarketing);

        newOptions.stream()
                .filter(option -> option.getOption().getOptionType().equals(SHIFT.getId()))
                .filter(option -> !Boolean.TRUE.equals(option.getIsChosen()))
                .peek(option -> log.info("Setting isFinalOption=false in marketing: " + option.getId()))
                .forEach(option -> marketingClient.saveOrUpdateMarketing(marketingMapper
                        .toDto(option.setIsFinalOption(Boolean.FALSE))));

        newOptions = newOptions.stream()
                .filter(o -> !o.getOption().getOptionType().equals(SHIFT.getId()))
                .collect(Collectors.toList());

        newOptions.add(savedMarketing);
        return newOptions;
    }

    private Marketing getMarketingForShift(OrderInfo orderInfo, Option optionShift) {
        return Marketing.builder()
                .clientId(orderInfo.getClientInfo().getId())
                .orderId(orderInfo.getId())
                .scoringDate(LocalDateTime.now())
                .ruleBlockNumber("mob")
                .decisionId(1L)
                .optionAvailability(true)
                .showMode(true)
                .includeIntoPayment(1)
                .isChosen(false)
                .isPaid(false)
                .isFinalOption(true)
                .option(optionShift)
                .scoringTypeId(ScoringType.MARKETING.getId()).build();
    }

    private Optional<OptionAvailability> getOptionAvailability(Long clientId) {
        return Optional.ofNullable(marketingClient.checkOptionAvailabilityForClient(clientId).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("OptionAvailabilityForClient is null")));
    }

    private List<Marketing> getAllOptions(OrderInfo orderInfo) {
        log.info("Получаем список всех возможных опций");
        return marketingClient.getAllMarketingOptionsForOrder(orderInfo.getId());
    }

    private List<Marketing> getAppliedOptions() {
        log.info("Находим примененные опции");
        List<Marketing> appliedOptions = List.of(Marketing.builder().build());
        log.info("Примененные опции: " + appliedOptions);

        return appliedOptions;
    }

    private List<Marketing> getAvailableOptions(List<Marketing> options,
                                                OrderInfo orderInfo,
                                                OptionAvailability optionAvailability,
                                                Boolean isUnpaidOverdueExist,
                                                List<Marketing> appliedOptions) {
        log.info("Находим доступные опции");
        List<Marketing> availabilityOptions = new ArrayList<>();

        if (optionAvailability != null || !isUnpaidOverdueExist) {
            availabilityOptions.addAll(getOptionsForAvailable(options, orderInfo, optionAvailability, isUnpaidOverdueExist));
            availabilityOptions.add(getSixMarketingOptionsForAvailable(orderInfo, optionAvailability, appliedOptions).orElse(null));
        }

        return availabilityOptions;
    }

    private List<Marketing> getOptionsForAvailable(List<Marketing> options,
                                                   OrderInfo orderInfo,
                                                   OptionAvailability optionAvailability,
                                                   Boolean isUnpaidOverdueExist) {
        // возможно, лучше вынести этот стрим-блок в отдельный метод, так как он весьма громоздкий
        List<Marketing> basicOptions = options.stream()
                .peek(option -> log.info("isChosen: " + option.getIsChosen() + ", isAvailable: "
                        + (optionAvailability != null ? optionAvailability.getIsShiftAvailable() : true)))
                .peek(option -> log.info("option: " + option))
                .filter(option -> option.getIsPaid() != null
                        && !option.getIsPaid()
                        && Objects.equals(option.getOption().getOptionType(), OptionType.SHIFT.getId())
                        && (optionAvailability == null || Objects.equals(optionAvailability.getIsShiftAvailable(), true))
                        && optionAvailabilityService.checkAvailabilityForOptionShift(orderInfo, option)
                        && !isUnpaidOverdueExist && option.getScoringTypeId().equals(ScoringType.MARKETING.getId()))
                .peek(option -> log.info("Опция Сдвиг доступна и еще не оплачена"))
                .collect(Collectors.toList());

        List<Marketing> discountOptions = options.stream()
                .filter(option -> DiscountType.isDiscount(option.getOption().getOptionType()))
                .toList();

        basicOptions.addAll(discountOptions);
        return basicOptions;
    }

    private Optional<Marketing> getSixMarketingOptionsForAvailable(OrderInfo orderInfo,
                                                                   OptionAvailability optionAvailability,
                                                                   List<Marketing> appliedOptions) {
        Optional<Marketing> sixMarketing = Optional.empty();

        boolean isAppliedShift = appliedOptions.stream()
                .anyMatch(option -> option.getOption().getOptionType().equals(SHIFT.getId())
                        && option.getScoringTypeId().equals(ScoringType.MARKETING.getId()));

        boolean isSixAvailable = optionAvailability == null
                || Objects.equals(optionAvailability.getIsSixAvailable(), true);

        if (isSixAvailable && optionAvailabilityService.checkAvailabilityForOptionSix(orderInfo) && !isAppliedShift) {
            // выдаст либо sixMarketing, либо Optional.empty()
            sixMarketing = getSixMarketing(orderInfo);

            if (sixMarketing.isEmpty()) {
                log.error("Ошибка при обработке опции Шестерка");
            }
        } else {
            log.info("Опция Шестерка недоступна");
        }

        return sixMarketing;
    }

    private Optional<Marketing> getSixMarketing(OrderInfo orderInfo) {
        log.info("Шестерка доступна, ищем подходящую опцию");
        var sixOption = marketingClient.getOption(SIX.getTypeCode(), optionCommonsService.getAmountForOptionTariff(orderInfo));
        var sixMarketing = optionCommonsService.getMarketing(orderInfo, sixOption);
        log.info("Добавляем опцию Шестерка: " + sixMarketing);

        return Optional.ofNullable(sixMarketing);
    }

    List<GetOrderInfoByClientIdAndOrderIdOption> getResponseOptions(OrderInfo orderInfo,
                                                                    List<Marketing> appliedOptions,
                                                                    List<Marketing> availabilityOptions) {
        List<GetOrderInfoByClientIdAndOrderIdOption> responseOptions = new ArrayList<>();

        if (!availabilityOptions.isEmpty()) {
            responseOptions.addAll(getAvailabilityOptionsForResponse(orderInfo, availabilityOptions));
        }
        if (!appliedOptions.isEmpty()) {
            responseOptions.addAll(getAppliedOptionsForResponse(orderInfo, appliedOptions));
        }

        return responseOptions;
    }

    private List<GetOrderInfoByClientIdAndOrderIdOption> getAvailabilityOptionsForResponse(OrderInfo orderInfo,
                                                                                           List<Marketing> availabilityOptions) {
        log.info("Формируем структуру доступных опций для отображения в мобильном приложении");

        return availabilityOptions.stream()
                .filter(availableOptions -> !StringUtils.equals(availableOptions.getOption().getTarget(),
                        PURCHASE_SUCCESS.getValue()))
                .map(option -> getAvailabilityDTO(option, orderInfo))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<GetOrderInfoByClientIdAndOrderIdOption> getAppliedOptionsForResponse(OrderInfo orderInfo,
                                                                                      List<Marketing> appliedOptions) {
        log.info("Формируем структуру примененных опций для отображения в мобильном приложении");
        Map<Long, HashSet<Long>> marketingMap = new HashMap<>();

        return appliedOptions.stream()
                .filter(option -> !StringUtils.equals(option.getOption().getTarget(), PURCHASE_SUCCESS.getValue()))
                .map(option -> getAppliedDTO(option, orderInfo, marketingMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void logAllOptions(OrderInfo orderInfo,
                               List<Marketing> options,
                               OptionAvailability optionAvailability,
                               Boolean isUnpaidOverdueExist) {
        log.info("isUnpaidOverdueExist: " + isUnpaidOverdueExist);
        log.info("OrderInfo: " + orderInfo);
        log.info("По заказу " + orderInfo.getId() + " " + "isSixAvailable = " +
                (optionAvailability != null ? optionAvailability.getIsSixAvailable() : !isUnpaidOverdueExist) +
                " " + "и IsShiftAvailable = " +
                (optionAvailability != null ? optionAvailability.getIsShiftAvailable() : !isUnpaidOverdueExist));
        log.info("found options: " + options);
    }


    private void logResponseOptions(List<GetOrderInfoByClientIdAndOrderIdOption> responseOptions) {
        log.info("Available options: " + responseOptions.stream()
                .filter(o -> !o.getIsApplied())
                .collect(Collectors.groupingBy(GetOrderInfoByClientIdAndOrderIdOption::getCode)));
        log.info("Applied options: " + responseOptions.stream()
                .filter(GetOrderInfoByClientIdAndOrderIdOption::getIsApplied)
                .collect(Collectors.groupingBy(GetOrderInfoByClientIdAndOrderIdOption::getCode)));

        if (responseOptions.isEmpty()) {
            log.info("У клиента нет доступных опций");
        }
    }


    private AvailabilityDto getAvailabilityDTO(Marketing marketing, OrderInfo orderInfo) {
        return new AvailabilityDto();
    }

    private AppliedDto getAppliedDTO(Marketing marketing,
                                     OrderInfo orderInfo,
                                     Map<Long, HashSet<Long>> marketingMap) {
        return new AppliedDto();
    }
}
