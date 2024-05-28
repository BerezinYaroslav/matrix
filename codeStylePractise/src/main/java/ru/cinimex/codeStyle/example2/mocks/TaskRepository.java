package ru.cinimex.codeStyle.example2.mocks;

public class TaskRepository {
    public long countByDealProduct(DealProductEntity dealProduct){
        return 0L;
    }

    public TaskEntity save(TaskEntity task) {
        return new TaskEntity();
    }
}
