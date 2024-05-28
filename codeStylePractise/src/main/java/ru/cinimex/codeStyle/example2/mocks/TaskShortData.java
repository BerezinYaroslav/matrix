package ru.cinimex.codeStyle.example2.mocks;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.Data;

@Data
public class TaskShortData {
    private long id;
    private Integer productGroupId;
    private String productGroupName;
    private long dealProductTotalTasksNumber;
    private Timestamp openDateTime;
    private Timestamp closeDateTime;
    private Timestamp expirationDateTime;
    private int durationInDays;
    private long uprppManagerId;
    private long sskManagerId;
    private List<Integer> approvalSideId;
    private Set<ClientShortData> clients;
    private List<Long> salesOfficeIds;
    private BigDecimal estimatedComplexityOfIndividualDocuments;
    private BigDecimal estimatedComplexityOfIndividualProcess;
    private BigDecimal estimatedComplexityWithApprovalSide;
    private BigDecimal estimatedComplexityWithProjectElements;
    private BigDecimal estimatedImportant;
    private BigDecimal estimatedOnBankManagementControl;
    private BigDecimal estimatedOnDepartmentManagementControl;
    private BigDecimal estimatedSpecificCustomer;
    private BigDecimal estimatedTopClient;
    private BigDecimal estimatedDealSize;
    private long substituteManagerId;
    private String substituteManagerShortName;
    private Date substituteDateFrom;
    private Date substituteDateTo;
    private String sskManagerShortName;
    private String uprppManagerShortName;
    private Long taskTypeId;
    private ClientShortData clientsGroup;
    private long productId;
    private String taskStateId;
}
