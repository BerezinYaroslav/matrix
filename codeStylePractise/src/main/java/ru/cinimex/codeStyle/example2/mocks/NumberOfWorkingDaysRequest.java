package ru.cinimex.codeStyle.example2.mocks;

import java.time.LocalDate;

import lombok.Data;

@Data
public class NumberOfWorkingDaysRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}
