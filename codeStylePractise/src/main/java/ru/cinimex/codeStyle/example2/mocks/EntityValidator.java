package ru.cinimex.codeStyle.example2.mocks;

import java.util.List;
import java.util.Set;

public class EntityValidator {
    public TaskEntity findById(Long id, TaskRepository taskRepository) {
        return new TaskEntity();
    }

    public TaskEntity checkIfNotExists(TaskShortData data, TaskRepository taskRepository) {
        return new TaskEntity();
    }

    public Set<TaskEntity> checkIfNotExists(List<Integer> taskIds, TaskRepository taskRepository) {
        return Set.of(new TaskEntity(), new TaskEntity());
    }

    public void checkIfExists(long id, TaskRepository repository) {
    }

    public ApprovalSideEntity checkIfNotExists(List<Integer> approvalSideId, ApprovalSideRepository approvalSideRepository) {
        return new ApprovalSideEntity();
    }

    public ApprovalSideEntity checkIfNotExists(String overdueId, OverdueRepository overdueRepository) {
        return new ApprovalSideEntity();
    }

    public ProductEntity checkIfNotExists(long productId, ProductRepository productRepository) {
        return new ProductEntity();
    }

    public TaskTypeEntity checkIfNotExists(long taskTypeId, TaskTypeRepository productRepository) {
        return new TaskTypeEntity();
    }

    public TaskStateEntity checkIfNotExists(String taskStateId, TaskStateRepository productRepository) {
        return new TaskStateEntity();
    }
}
