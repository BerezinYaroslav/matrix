package ru.cinimex.codeStyle.example1.mocks;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class Marketing {

    private Long id;
    private Long clientId;
    private Boolean isPaid;
    private Integer orderId;
    private LocalDateTime scoringDate;
    private String ruleBlockNumber;
    private Long decisionId;
    private Boolean optionAvailability;
    private Boolean showMode;
    private Boolean isChosen;
    private Boolean isFinalOption;
    private Long scoringTypeId;
    private Integer includeIntoPayment;
    private Option option;


    public Option getOption(){
        return new Option();
    }

    public Marketing setIsFinalOption(Boolean isFinalOption){
        return this.setIsFinalOption(isFinalOption);
    }

}
