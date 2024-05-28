package ru.cinimex.codeStyle.example2.mocks;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientMapper {
    public ClientShortData entityToShortData(ClientEntity client) {
        return new ClientShortData();
    }
    public List<ClientShortData> entityToShortData(Set<ClientEntity> client) {
        return Stream.of(new ClientShortData()).collect(Collectors.toList());
    }
}
