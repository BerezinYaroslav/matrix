package ru.cinimex.codeStyle.example2.mocks;

import java.util.Set;

import lombok.Data;

@Data
public class DealProductShortData {
    private long id;
    private Set<TaskShortData> implTasks;
}
