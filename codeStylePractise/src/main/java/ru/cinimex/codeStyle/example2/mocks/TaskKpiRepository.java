package ru.cinimex.codeStyle.example2.mocks;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskKpiRepository {
    public List<TaskKpiEntity> findAllByTaskId(long id) {
        return Stream.of(new TaskKpiEntity(),
                        new TaskKpiEntity())
                .collect(Collectors.toList());
    }

    public void save(TaskKpiEntity taskKpiEntity) {
    }
}
