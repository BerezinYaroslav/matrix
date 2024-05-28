package ru.cinimex.codeStyle.example2.mocks;

import lombok.Data;

@Data
public class TaskTypeEntity {
    private long id;
    private String name;
    private TaskTypeEntity parent;
}
