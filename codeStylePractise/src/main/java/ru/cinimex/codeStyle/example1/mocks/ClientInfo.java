package ru.cinimex.codeStyle.example1.mocks;

import lombok.Data;

@Data
public class ClientInfo {
    private Long id;
    private Inform body;
    private String phone;
    private String email;

    public ClientInfo getClientOverdue(Long id) {
        return new ClientInfo();
    }
}
