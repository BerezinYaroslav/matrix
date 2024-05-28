package ru.cinimex.codeStyle.example2.mocks;

import java.util.Optional;

public class TaskSubstituteSskManagerRepository {
    public Optional<TaskSubstituteSskManagerEntity> findFirstByTaskIdAndIsActiveTrue(Long taskId) {
        return Optional.of(new TaskSubstituteSskManagerEntity());
    }
}
