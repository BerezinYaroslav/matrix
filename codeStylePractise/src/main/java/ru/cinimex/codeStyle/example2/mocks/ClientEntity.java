package ru.cinimex.codeStyle.example2.mocks;

import java.util.Set;

import ru.cinimex.codeStyle.example1.mocks.SHIFT;

import lombok.Data;

@Data
public class ClientEntity {
    private String id;
    private String name;
    private Set<TaskEntity> tasks;
    private ClientEntity parent;
    private Boolean isActive;
    private Long managerClientId;

    public static ClientEntity getClientGroup(ClientEntity next) {
        return new ClientEntity();
    }

    public SHIFT getType() {
        return null;
    }
}
