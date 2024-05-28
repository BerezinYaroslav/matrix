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
public class NewUnknownServiceV1 {
    private final MarketClient marketingClient;
    private final ClientInfo clientInfoClient;
    private final OptionService optionService;
    private final MarketingMapper marketingMapper;
    private final OptionAvailabilityService optionAvailabilityService;
    private final OptionCommonsService optionCommonsService;

    public List<GetOrderInfoByClientIdAndOrderIdOption> getOptionsList(Long clientId, OrderInfo orderInfo) {
        // возможно, лучше вынести логирование из основного метода в более мелкие, в данном случае в getAllOptions
        log.info("Получаем список всех возможных опций");
        List<Marketing> options = getAllOptions(orderInfo);

        // необходимый для заполнения опций код
        OptionAvailability optionAvailability = getOptionAvailability(clientId);
        Boolean isUnpaidOverdueExist = clientInfoClient.getClientOverdue(clientId).getBody().getHasOverdue();

        // логирование всех опций
        logAllOptions(orderInfo, options, optionAvailability, isUnpaidOverdueExist);

        log.info("Находим примененные опции");
        List<Marketing> appliedOptions = getAppliedOptions();
        log.info("Примененные опции: " + appliedOptions);

        log.info("Находим доступные опции");
        // возможно, лучше заменить availability на available
        List<Marketing> availabilityOptions = getAvailabilityOptions(options,
                orderInfo, optionAvailability, isUnpaidOverdueExist, appliedOptions);

        // получение списка искомых опцией
        List<GetOrderInfoByClientIdAndOrderIdOption> responseOptions = getResponseOptions(orderInfo,
                appliedOptions, availabilityOptions);

        // логирование искомых опций
        logResponseOptions(responseOptions);

        return responseOptions;
    }

    private void shiftPayment(OrderInfo orderInfo,
                              List<Marketing> options,
                              PaymentScheduleInfo paymentToShift,
                              Option optionShift) {
        log.info("optionShift: " + optionShift);

        if (paymentToShift != null) {
            if (optionShift != null && options.stream()
                    .filter(m -> !m.getIsPaid())
                    .map(Marketing::getOption)
                    .noneMatch(o -> o.getId().equals(optionShift.getId()))) {
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
                        .forEach(m -> marketingClient.saveOrUpdateMarketing(marketingMapper
                                .toDto(m.setIsFinalOption(Boolean.FALSE))));

                options = options.stream()
                        .filter(o -> !o.getOption().getOptionType().equals(SHIFT.getId()))
                        .collect(Collectors.toList());

                options.add(savedMarketing);
            } else {
                log.info(String.format("Опция сдвиг уже привязана к платежу %s с суммой %s",
                        paymentToShift.getId(), paymentToShift.getPaymentSum()));
            }
        } else {
            log.info("Платёж для сдвига не найден. Опция сдвиг недоступна");
        }
    }

    private OptionAvailability getOptionAvailability(Long clientId) {
        var optionAvailabilities = marketingClient.checkOptionAvailabilityForClient(clientId);
        OptionAvailability optionAvailability = null;

        if (!optionAvailabilities.isEmpty()) {
            optionAvailability = optionAvailabilities.get(0);
        }

        return optionAvailability;
    }

    List<Marketing> getAllOptions(OrderInfo orderInfo) {
        return marketingClient.getAllMarketingOptionsForOrder(orderInfo.getId());
    }

    List<Marketing> getAppliedOptions() {
        return new ArrayList<>(List.of(Marketing.builder().build()));
    }

    List<Marketing> getAvailabilityOptions(List<Marketing> options,
                                           OrderInfo orderInfo,
                                           OptionAvailability optionAvailability,
                                           Boolean isUnpaidOverdueExist,
                                           List<Marketing> appliedOptions) {
        List<Marketing> availabilityOptions = new ArrayList<>();
        fillAvailabilityOptions(options, orderInfo, optionAvailability, isUnpaidOverdueExist, appliedOptions,
                availabilityOptions);

        return availabilityOptions;
    }

    private void fillAvailabilityOptions(List<Marketing> options,
                                         OrderInfo orderInfo,
                                         OptionAvailability optionAvailability,
                                         Boolean isUnpaidOverdueExist,
                                         List<Marketing> appliedOptions,
                                         List<Marketing> availabilityOptions) {
        if (optionAvailability != null || !isUnpaidOverdueExist) {
            boolean isAppliedShift = appliedOptions.stream()
                    .anyMatch(o -> o.getOption().getOptionType().equals(SHIFT.getId())
                            && o.getScoringTypeId().equals(ScoringType.MARKETING.getId()));

            for (Marketing option : options) {
                log.info("isChosen: " + option.getIsChosen() + ", isAvailable: "
                        + (optionAvailability != null ? optionAvailability.getIsShiftAvailable() : true));
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
                    var sixOption = marketingClient.getOption(SIX.getTypeCode(),
                            optionCommonsService.getAmountForOptionTariff(orderInfo));
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

        // сдвиг платежей
        PaymentScheduleInfo paymentToShift = optionService.getAvailablePaymentForShift(orderInfo.getPaymentSchedules());
        Option optionShift = marketingClient.getOption(SHIFT.getTypeCode(), paymentToShift.getPaymentSum());
        log.info("optionShift: " + optionShift);
        shiftPayment(orderInfo, options, paymentToShift, optionShift);
    }

    List<GetOrderInfoByClientIdAndOrderIdOption> getResponseOptions(OrderInfo orderInfo,
                                                                    List<Marketing> appliedOptions,
                                                                    List<Marketing> availabilityOptions) {
        List<GetOrderInfoByClientIdAndOrderIdOption> responseOptions = new ArrayList<>();
        fillResponseOptions(orderInfo, appliedOptions, availabilityOptions, responseOptions);
        return responseOptions;
    }

    private void fillResponseOptions(OrderInfo orderInfo,
                                     List<Marketing> appliedOptions,
                                     List<Marketing> availabilityOptions,
                                     List<GetOrderInfoByClientIdAndOrderIdOption> responseOptions) {
        if (!availabilityOptions.isEmpty()) {
            log.info("Формируем структуру доступных опций для отображения в мобильном приложении");

            responseOptions.addAll(availabilityOptions.stream()
                    .filter(availableOptions -> !StringUtils.equals(availableOptions.getOption().getTarget(),
                            PURCHASE_SUCCESS.getValue()))
                    .map(o -> getAvailabilityDTO(o, orderInfo))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
        if (!appliedOptions.isEmpty()) {
            log.info("Формируем структуру примененных опций для отображения в мобильном приложении");
            Map<Long, HashSet<Long>> marketingMap = new HashMap<>();

            responseOptions.addAll(appliedOptions.stream()
                    .filter(applOptions -> !StringUtils.equals(applOptions.getOption().getTarget(),
                            PURCHASE_SUCCESS.getValue()))
                    .map(o -> getAppliedDTO(o, orderInfo, marketingMap))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        }
    }

    private static void logAllOptions(OrderInfo orderInfo,
                                      List<Marketing> options,
                                      OptionAvailability optionAvailability,
                                      Boolean isUnpaidOverdueExist) {
        log.info("isUnpaidOverdueExist: " + isUnpaidOverdueExist);
        log.info("OrderInfo: " + orderInfo);
        log.info("По заказу " + orderInfo.getId() + " " +
                "isSixAvailable = " +
                (optionAvailability != null ? optionAvailability.getIsSixAvailable() : !isUnpaidOverdueExist) + " " +
                "и IsShiftAvailable = " +
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
