package ru.cinimex.codeStyle.example2.mocks;

public class TaskMapper {
    public TaskShortData entityToData(TaskEntity task) {
        return new TaskShortData();
    }

    public TaskEntity dataToEntity(TaskShortData data) {
        return new TaskEntity();
    }

    public TaskEntity merge(TaskEntity task, TaskShortData data) {
        return new TaskEntity();
    }
}
