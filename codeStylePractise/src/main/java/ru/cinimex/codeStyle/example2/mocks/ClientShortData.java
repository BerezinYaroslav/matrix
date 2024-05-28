package ru.cinimex.codeStyle.example2.mocks;

import java.util.List;

import lombok.Data;

@Data
public class ClientShortData {
    private long id;
    private long managerClientId;
    private long industryId;
    private List<ClientShortData> branches;
    private String industryName;
    private String managerClientShortName;
}
