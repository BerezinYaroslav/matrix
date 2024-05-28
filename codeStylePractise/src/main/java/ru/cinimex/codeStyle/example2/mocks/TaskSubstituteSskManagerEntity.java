package ru.cinimex.codeStyle.example2.mocks;

import java.util.Date;

import lombok.Data;

@Data
public class TaskSubstituteSskManagerEntity {
    private long staffId;
    private Date dateFrom;
    private Date dateTo;
}
