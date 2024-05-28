package ru.cinimex.codeStyle.example2.mocks;

import lombok.Getter;

@Getter
public enum ClientTypeEnum {
    CLIENT("One"),
    GROUP("Group");

    private String id;

    ClientTypeEnum(String id) {
        this.id = id;
    }
}
