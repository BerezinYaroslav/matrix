package ru.cinimex.codeStyle.example2.mocks;

import lombok.Getter;

@Getter
public enum TaskEnum {
    TASK_INDIVIDUAL_DOCUMENTS("Documents"),
    TASK_INDIVIDUAL_PROCESS("Process"),
    TASK_APPROVAL_SIDE("Side"),
    TASK_WITH_PROJECT("Project"),
    TASK_IS_IMPORTANT("Important"),
    TASK_ON_BANK_CONTROL("Bank-Control"),
    TASK_ON_DEPARTMENT_CONTROL("Department-Control"),
    TASK_SPECIFIC_CUSTOMER("Customer"),
    CLIENT_IS_TOP("Top"),
    DEAL_SIZE("Side");

    private String name;

    TaskEnum(String name) {
        this.name = name;
    }
}
