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
import java.util.stream.Stream;

import static ru.cinimex.codeStyle.example1.mocks.enums.OptionTarget.PURCHASE_SUCCESS;

@Slf4j
@RequiredArgsConstructor
public class NewUnknownServiceV2 {
    private final MarketClient marketingClient;
    private final ClientInfo clientInfo;
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
        List<Marketing> appliedOptions = getAppliedOptions();

        // обновление всех опций (учитываем сдвиги)
        List<Marketing> updatedOptions = Optional
                .ofNullable(optionService.getAvailablePaymentForShift(orderInfo.getPaymentSchedules()))
                .map(payment -> getUpdatedAndShiftedOptions(orderInfo, options, payment))
                .orElseGet(() -> {
                    log.info("Платёж для сдвига не найден. Опция сдвиг недоступна");
                    return options;
                });

        List<Marketing> availableOptions = getAvailableOptions(updatedOptions, orderInfo, clientId, appliedOptions);

        List<GetOrderInfoByClientIdAndOrderIdOption> responseOptions = getResponseOptions(orderInfo, appliedOptions,
                availableOptions);

        logResponseOptions(responseOptions);

        return responseOptions;
    }

    private List<Marketing> getUpdatedAndShiftedOptions(OrderInfo orderInfo,
                                                        List<Marketing> options,
                                                        PaymentScheduleInfo paymentToShift) {
        Option optionShift = marketingClient.getOption(SHIFT.getTypeCode(), paymentToShift.getPaymentSum());
        log.info("optionShift: " + optionShift);

        return Optional.of(marketingClient.getOption(SHIFT.getTypeCode(), paymentToShift.getPaymentSum()))
                .filter(option -> options.stream()
                        .filter(inOption -> !inOption.getIsPaid())
                        .map(Marketing::getOption)
                        .noneMatch(inOption -> inOption.getId().equals(option.getId())))
                .map(option -> getShiftedOptions(orderInfo, optionShift, options))
                .orElseGet(() -> {
                    log.info(String.format("Опция сдвиг уже привязана к платежу %s с суммой %s",
                            paymentToShift.getId(), paymentToShift.getPaymentSum()));
                    return options;
                });
    }

    private List<Marketing> getShiftedOptions(OrderInfo orderInfo, Option optionShift, List<Marketing> options) {
        Marketing marketingForShift = buildMarketingForShift(orderInfo, optionShift);
        Marketing savedMarketing = marketingClient.saveOrUpdateMarketing(marketingMapper.toDto(marketingForShift));
        log.info("Saved marketing: " + savedMarketing);

        List<Marketing> filteredOptions = options.stream()
                .filter(option -> option.getOption().getOptionType().equals(SHIFT.getId()))
                .filter(option -> !Boolean.TRUE.equals(option.getIsChosen()))
                .collect(Collectors.toList());

        filteredOptions.stream()
                .peek(option -> log.info("Setting isFinalOption=false in marketing: " + option.getId()))
                .forEach(option -> marketingClient.saveOrUpdateMarketing(marketingMapper
                        .toDto(option.setIsFinalOption(Boolean.FALSE))));

        filteredOptions = filteredOptions.stream()
                .filter(option -> !option.getOption().getOptionType().equals(SHIFT.getId()))
                .collect(Collectors.toList());

        filteredOptions.add(savedMarketing);
        return filteredOptions;
    }

    private Marketing buildMarketingForShift(OrderInfo orderInfo, Option optionShift) {
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
        return marketingClient.checkOptionAvailabilityForClient(clientId).stream().findFirst();
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
                                                Long clientId,
                                                List<Marketing> appliedOptions) {
        log.info("Находим доступные опции");
        List<Marketing> availabilityOptions = new ArrayList<>();
        OptionAvailability optionAvailability = getOptionAvailability(clientId).orElse(null);
        Inform inform = Optional.of(clientInfo.getClientOverdue(clientId).getBody()).orElse(null);

        return Stream.of(optionAvailability, inform)
                .filter(Objects::nonNull)
                .filter(option -> !inform.getHasOverdue())
                .peek(option -> logAllOptions(orderInfo, options, optionAvailability, inform.getHasOverdue()))
                .findFirst()
                .map(option -> {
                    availabilityOptions.addAll(getOptions(options, orderInfo, optionAvailability, inform.getHasOverdue()));
                    getSixMarketingOption(orderInfo, optionAvailability, appliedOptions).ifPresent(availabilityOptions::add);
                    return availabilityOptions;
                })
                .orElse(availabilityOptions);
    }

    private List<Marketing> getOptions(List<Marketing> options,
                                       OrderInfo orderInfo,
                                       OptionAvailability optionAvailability,
                                       Boolean isUnpaidOverdueExist) {
        return options.stream()
                .peek(option -> log.info("isChosen: " + option.getIsChosen() + ", isAvailable: "
                        + (optionAvailability != null ? optionAvailability.getIsShiftAvailable() : true)))
                .peek(option -> log.info("option: " + option))
                .filter(option -> filterOption(orderInfo, optionAvailability, isUnpaidOverdueExist, option))
                .peek(option -> log.info("Опция Сдвиг доступна и еще не оплачена"))
                .collect(Collectors.toList());
    }

    private boolean filterOption(OrderInfo orderInfo, OptionAvailability optionAvailability, Boolean isUnpaidOverdueExist, Marketing option) {
        return (option.getIsPaid() != null
                && !option.getIsPaid()
                && Objects.equals(option.getOption().getOptionType(), OptionType.SHIFT.getId())
                && (optionAvailability == null || Objects.equals(optionAvailability.getIsShiftAvailable(), true))
                && optionAvailabilityService.checkAvailabilityForOptionShift(orderInfo, option)
                && !isUnpaidOverdueExist && option.getScoringTypeId().equals(ScoringType.MARKETING.getId()))
                || DiscountType.isDiscount(option.getOption().getOptionType());
    }

    private Optional<Marketing> getSixMarketingOption(OrderInfo orderInfo,
                                                      OptionAvailability optionAvailability,
                                                      List<Marketing> appliedOptions) {
        if (isAvailableSixMarketing(orderInfo, optionAvailability, appliedOptions)) {
            try {
                log.info("Шестерка доступна, ищем подходящую опцию");
                var sixOption = marketingClient.getOption(SIX.getTypeCode(),
                        optionCommonsService.getAmountForOptionTariff(orderInfo));
                var sixMarketing = optionCommonsService.getMarketing(orderInfo, sixOption);

                log.info("Добавляем опцию Шестерка: " + sixMarketing);
                return Optional.ofNullable(sixMarketing);
            } catch (RuntimeException e) {
                log.error("Ошибка при обработке опции Шестерка", e);
                return Optional.empty();
            }
        } else {
            log.info("Опция Шестерка недоступна");
        }

        return Optional.empty();
    }

    private boolean isAvailableSixMarketing(OrderInfo orderInfo,
                                            OptionAvailability optionAvailability,
                                            List<Marketing> appliedOptions) {
        boolean isAppliedShift = appliedOptions.stream()
                .anyMatch(option -> option.getOption().getOptionType().equals(SHIFT.getId())
                        && option.getScoringTypeId().equals(ScoringType.MARKETING.getId()));

        boolean isSixAvailable = optionAvailability == null
                || Objects.equals(optionAvailability.getIsSixAvailable(), true);

        return isSixAvailable && optionAvailabilityService.checkAvailabilityForOptionSix(orderInfo) && !isAppliedShift;
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

        Optional.of(availabilityOptions)
                .filter(availabilityOption -> !availabilityOptions.isEmpty())
                .map(availabilityOption -> responseOptions.addAll(getAvailabilityOptionsForResponse(orderInfo, availabilityOptions)));

        Optional.of(appliedOptions)
                .filter(appliedOption -> !appliedOptions.isEmpty())
                .map(appliedOption -> responseOptions.addAll(getAppliedOptionsForResponse(orderInfo, appliedOptions)));

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
