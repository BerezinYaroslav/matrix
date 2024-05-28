package ru.cinimex.codeStyle.example2.mocks;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientRepository {
    public Set<ClientEntity> findByParentAndIsActiveTrue(ClientEntity parent) {
        return Stream.of(new ClientEntity(),
                        new ClientEntity())
                .collect(Collectors.toSet());
    }
}
