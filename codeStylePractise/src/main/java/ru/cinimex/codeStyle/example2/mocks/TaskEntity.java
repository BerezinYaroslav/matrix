package ru.cinimex.codeStyle.example2.mocks;

import java.util.Set;

import lombok.Data;

@Data
public class TaskEntity {
    private long id;
    private TaskTypeEntity taskType;
    private long uprppManagerId;
    private long sskManagerId;
    private Set<ClientEntity> clients;
    private DealProductEntity dealProduct;
    private ProductEntity product;
    private TaskStateEntity taskState;
    private ApprovalSideEntity approvalSide;
    private Object withProjectElements;
    private Object complexityOfIndividualProcess;
    private Object complexityOfIndividualDocuments;

    public void clearTaskSalesOffices() {
    }

    public void addSalesOffice(Long salesOfficeId) {
    }

    public void addClient(ClientEntity client) {
    }
}
