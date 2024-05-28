package ru.cinimex.codeStyle.example3.mocks;

import lombok.Data;

@Data
public class PushToken {
    private Integer id;
    private Boolean isActive;
    private String token;
}
