package ru.cinimex.codeStyle.example2.mocks;

import lombok.Getter;

@Getter
public enum ConstantsEnum {
    REALIZATION("Realization");

    private String value;

    ConstantsEnum(String value) {
        this.value = value;
    }
}
