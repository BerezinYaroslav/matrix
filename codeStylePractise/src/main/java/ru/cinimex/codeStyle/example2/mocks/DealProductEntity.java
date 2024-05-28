package ru.cinimex.codeStyle.example2.mocks;

import java.util.Set;

import lombok.Data;

@Data
public class DealProductEntity {
    private Deal deal;

    @Data
    public static class Deal{
        private long id;
        private Set<DealClientEntity> dealClients;
    }
}
