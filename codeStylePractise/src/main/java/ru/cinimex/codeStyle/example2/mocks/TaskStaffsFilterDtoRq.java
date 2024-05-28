package ru.cinimex.codeStyle.example2.mocks;

import java.util.List;

import lombok.Data;

@Data
public class TaskStaffsFilterDtoRq {
    private List<Integer> taskIds;
}
