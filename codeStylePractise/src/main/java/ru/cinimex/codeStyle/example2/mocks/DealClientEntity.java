package ru.cinimex.codeStyle.example2.mocks;

import lombok.Data;

@Data
public class DealClientEntity {
    private ClientEntity client;
    private Boolean isActive;
}
