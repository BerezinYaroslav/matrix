package ru.cinimex.codeStyle.example2.mocks;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Data;

@Data
public class ClientService {
    private Set<ClientEntity> clientEntities;

    public Set<ClientEntity> getClientEntities(Set<Long> collect) {
        return Stream.of(new ClientEntity(),
                        new ClientEntity())
                .collect(Collectors.toSet());
    }

    public ClientManagerData getClientManager(Long managerClientId) {
        return new ClientManagerData();
    }
}
