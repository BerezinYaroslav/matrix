package ru.cinimex.codeStyle.example1.mocks.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OptionTarget {

    PURCHASE_SUCCESS("purchase_success");

    private final String value;
}
