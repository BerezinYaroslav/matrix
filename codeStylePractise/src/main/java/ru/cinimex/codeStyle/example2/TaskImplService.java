package ru.cinimex.codeStyle.example2;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.cinimex.codeStyle.example2.mocks.ConstantsEnum.REALIZATION;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.CLIENT_IS_TOP;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.DEAL_SIZE;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.TASK_APPROVAL_SIDE;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.TASK_INDIVIDUAL_DOCUMENTS;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.TASK_INDIVIDUAL_PROCESS;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.TASK_IS_IMPORTANT;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.TASK_ON_BANK_CONTROL;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.TASK_ON_DEPARTMENT_CONTROL;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.TASK_SPECIFIC_CUSTOMER;
import static ru.cinimex.codeStyle.example2.mocks.TaskEnum.TASK_WITH_PROJECT;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ru.cinimex.codeStyle.example2.mocks.ApprovalSideEntity;
import ru.cinimex.codeStyle.example2.mocks.ApprovalSideRepository;
import ru.cinimex.codeStyle.example2.mocks.BusinessCalendarService;
import ru.cinimex.codeStyle.example2.mocks.CRUDTaskService;
import ru.cinimex.codeStyle.example2.mocks.ClientEntity;
import ru.cinimex.codeStyle.example2.mocks.ClientManagerData;
import ru.cinimex.codeStyle.example2.mocks.ClientMapper;
import ru.cinimex.codeStyle.example2.mocks.ClientRepository;
import ru.cinimex.codeStyle.example2.mocks.ClientService;
import ru.cinimex.codeStyle.example2.mocks.ClientShortData;
import ru.cinimex.codeStyle.example2.mocks.ClientTypeEnum;
import ru.cinimex.codeStyle.example2.mocks.CreateDealTaskShortData;
import ru.cinimex.codeStyle.example2.mocks.DealClientEntity;
import ru.cinimex.codeStyle.example2.mocks.DealEntity;
import ru.cinimex.codeStyle.example2.mocks.DealProductEntity;
import ru.cinimex.codeStyle.example2.mocks.DealProductService;
import ru.cinimex.codeStyle.example2.mocks.DealProductShortData;
import ru.cinimex.codeStyle.example2.mocks.DealService;
import ru.cinimex.codeStyle.example2.mocks.EntityConditionNotificationService;
import ru.cinimex.codeStyle.example2.mocks.EntityType;
import ru.cinimex.codeStyle.example2.mocks.EntityValidator;
import ru.cinimex.codeStyle.example2.mocks.NotificationEntityTypeEnum;
import ru.cinimex.codeStyle.example2.mocks.NumberOfWorkingDaysRequest;
import ru.cinimex.codeStyle.example2.mocks.OutContainerDto;
import ru.cinimex.codeStyle.example2.mocks.OverdueRepository;
import ru.cinimex.codeStyle.example2.mocks.PageRequestData;
import ru.cinimex.codeStyle.example2.mocks.ProductEntity;
import ru.cinimex.codeStyle.example2.mocks.ProductHelper;
import ru.cinimex.codeStyle.example2.mocks.ProductRepository;
import ru.cinimex.codeStyle.example2.mocks.ScoringService;
import ru.cinimex.codeStyle.example2.mocks.SskManagerSubstituteStaffData;
import ru.cinimex.codeStyle.example2.mocks.SsksImplementationApiException;
import ru.cinimex.codeStyle.example2.mocks.StaffEntity;
import ru.cinimex.codeStyle.example2.mocks.StaffHelper;
import ru.cinimex.codeStyle.example2.mocks.TaskEntity;
import ru.cinimex.codeStyle.example2.mocks.TaskFilterDtoRq;
import ru.cinimex.codeStyle.example2.mocks.TaskKafkaProducer;
import ru.cinimex.codeStyle.example2.mocks.TaskKpiCalculateService;
import ru.cinimex.codeStyle.example2.mocks.TaskKpiEntity;
import ru.cinimex.codeStyle.example2.mocks.TaskKpiRepository;
import ru.cinimex.codeStyle.example2.mocks.TaskMapper;
import ru.cinimex.codeStyle.example2.mocks.TaskNotificationDto;
import ru.cinimex.codeStyle.example2.mocks.TaskNotificationMapper;
import ru.cinimex.codeStyle.example2.mocks.TaskRepository;
import ru.cinimex.codeStyle.example2.mocks.TaskScoringTLDto;
import ru.cinimex.codeStyle.example2.mocks.TaskScoringTLDtoMapper;
import ru.cinimex.codeStyle.example2.mocks.TaskShortData;
import ru.cinimex.codeStyle.example2.mocks.TaskStaffsFilterDtoRq;
import ru.cinimex.codeStyle.example2.mocks.TaskStateEntity;
import ru.cinimex.codeStyle.example2.mocks.TaskStateRepository;
import ru.cinimex.codeStyle.example2.mocks.TaskSubstituteSskManagerEntity;
import ru.cinimex.codeStyle.example2.mocks.TaskSubstituteSskManagerRepository;
import ru.cinimex.codeStyle.example2.mocks.TaskTLDto;
import ru.cinimex.codeStyle.example2.mocks.TaskTLMapper;
import ru.cinimex.codeStyle.example2.mocks.TaskTypeEntity;
import ru.cinimex.codeStyle.example2.mocks.TaskTypeRepository;
import ru.cinimex.codeStyle.example2.mocks.TaskViewRepository;
import ru.cinimex.codeStyle.example2.mocks.TimeLineService;
import ru.cinimex.codeStyle.example2.mocks.TimelineActionEnum;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

// TODO подумать над объединением с TaskSupportService
@Slf4j
@Service
@AllArgsConstructor
public class TaskImplService implements CRUDTaskService<TaskShortData, CreateDealTaskShortData> {
    public static final String GK = "group";
    public static final String CLIENT = "client";
    public static final String GK_UNDEFINED = "ГК не определена";
    public static final String GROUP_NAME = "clientGroupName";
    public static final String CLIENTS_IN_TASK = "clientsCountInTask";
    public static final String CLIENTS_IN_GROUP = "clientsCountInGroup";
    public static final String IMPLEMENTATION = "Реализация";
    public static final String HAS_IMPL_PLAN = "hasImplPlan";
    public static final Long TASK_TYPE_IMPLEMENTATION = 1L;
    private final DealService dealService;
    private final DealProductService dealProductService;
    private final BusinessCalendarService businessCalendarService;
    private final TaskKpiCalculateService taskKpiCalculateService;
    private final ClientService clientService;
    private final StaffHelper staffHelper;
    private final TaskKafkaProducer taskKafkaProducer;
    private final TimeLineService timeLineService;
    private final EntityConditionNotificationService notificationService;
    private final ScoringService scoringService;
    private final TaskMapper taskMapper;
    private final ClientMapper clientMapper;
    private final TaskTLMapper taskTLMapper;
    private final TaskScoringTLDtoMapper taskScoringTLDtoMapper;
    private final TaskNotificationMapper taskNotificationMapper;
    private final EntityValidator validator;

    private final TaskRepository taskRepository;
    private final TaskViewRepository taskViewRepository;
    private final ClientRepository clientRepository;
    private final TaskStateRepository taskStateRepository;
    private final TaskTypeRepository taskTypeRepository;
    private final ProductRepository productRepository;
    private final ApprovalSideRepository approvalSideRepository;
    private final TaskSubstituteSskManagerRepository taskSubstituteSskManagerRepository;
    private TaskKpiRepository taskKpiRepository;
    private ProductHelper productHelper;
    private OverdueRepository overdueRepository;

    public Page<TaskShortData> read(PageRequestData<TaskFilterDtoRq> request) {
        throw new IllegalStateException("Использовать новый путь для получения реестра задач suboimpl/task/search");
        // TODO пока не удаляем, мало ли, аналитика начнет ругаться, хотя этот переезд был согласован еще в марте-апреле
//        String overdueName = getOverdueName(request.getData());
//        Specification<TaskViewEntity> taskViewFilter = TaskViewFilterSpecification.getTaskFilter(request, overdueName);
//        PageableFactory factory = new PageableFactory();
//        Pageable pageable = factory.fromFilter(request, TaskViewFilterSpecification.getFieldsCorrespondence());
//        Page<TaskViewEntity> taskViewEntities = taskViewRepository.findAll(taskViewFilter, pageable);
//        for (TaskViewEntity task : taskViewEntities) {
//            if (!task.getTaskType().getName().equals(IMPLEMENTATION)) {
//                TaskTypeEntity parentTaskType = getParentTaskType(task.getTaskType());
//                task.setTaskType(parentTaskType);
//            }
//        }
//
//        Page<TaskShortData> tasks = taskViewEntities.map(taskViewMapper::entityToData);
//        for (TaskShortData data : tasks) {
//            if (!data.getTaskTypeName().equals(IMPLEMENTATION)) {
//                data.setIsPipeline(null);
//            }
//
//            if (data.getCloseDateTime() == null && data.getExpirationDateTime() != null) {
//                NumberOfWorkingDaysRequest taskDuration = new NumberOfWorkingDaysRequest();
//                taskDuration.setIsInclusive(false);
//                taskDuration.setStartDate(LocalDate.now());
//                taskDuration.setEndDate(data.getExpirationDateTime().toLocalDateTime().toLocalDate());
//                Integer numberOfWorkingDays = businessCalendarService.getNumberOfWorkingDaysExcludingStartDate(taskDuration);
//                data.setDelayInDays(numberOfWorkingDays);
//            }
//
//            fillSubstitute(data);
//            fillProductGroupName(data);
//            fillStaffData(data, data.getUprppManagerId(), data.getSskManagerId());
//        }
//        return tasks;
    }

    @Deprecated
    /**
     * Вместо него использовать read(PageRequestData<TaskFilterDtoRq> taskFilterDtoRq)
     */
    @Override
    public Page<TaskShortData> read(Pageable pageable) {
        throw new UnsupportedOperationException("использовать read(PageRequestData<TaskFilterDtoRq> taskFilterDtoRq)");
    }

    private void fillProductGroupName(TaskShortData data) {
        ofNullable(productHelper.getProductGroup(data.getProductGroupId()))
                .ifPresent(productGroupData -> data.setProductGroupName(productGroupData.getName()));
    }

    @Override
    public TaskShortData read(Long id) {
        TaskEntity task = validator.findById(id, taskRepository);
        if (!task.getTaskType().getName().equals(IMPLEMENTATION)) {
            throw new IllegalStateException(MessageFormat.format(
                    "Task with taskType:{0} and id: {1} not implementation",
                    task.getTaskType().getName(), task.getId()));
        }
//        TaskUtil.extractMostUsedManagerClientId(task).ifPresent(managerClientId -> {
//            task.getClients().forEach(client -> client.setManagerClientId(managerClientId));
//        });
        TaskShortData data = taskMapper.entityToData(task);
        fillProductGroupName(data);
        fillStaffData(data, task.getUprppManagerId(), task.getSskManagerId());
        fillClientInfo(data, task.getClients());
        data.setDealProductTotalTasksNumber(taskRepository.countByDealProduct(task.getDealProduct()));
        NumberOfWorkingDaysRequest taskDuration = new NumberOfWorkingDaysRequest();
        taskDuration.setStartDate(data.getOpenDateTime().toLocalDateTime().toLocalDate());
        if (data.getCloseDateTime() != null) {
            taskDuration.setEndDate(data.getCloseDateTime().toLocalDateTime().toLocalDate());
        } else if (data.getExpirationDateTime() != null) {
            taskDuration.setEndDate(data.getExpirationDateTime().toLocalDateTime().toLocalDate());
        }
        if (taskDuration.getEndDate() != null) {
            Integer numberOfWorkingDays = businessCalendarService.getNumberOfWorkingDays(taskDuration);
            data.setDurationInDays(numberOfWorkingDays);
        }

        fillScoringFactors(data);
        fillSubstitute(data);
        return data;
    }

    @SneakyThrows
    @Override
    public TaskShortData update(TaskShortData data) {
        TaskEntity task = validator.checkIfNotExists(data, taskRepository);

        TaskTLDto oldTask = taskTLMapper.toDto(task);
        TaskNotificationDto oldTaskNotificationDto = taskNotificationMapper.toDto(task);

        oldTask.setTaskScoring(getTaskScoringTLDto(task));
        if (checkIfNeedsKpiUpdate(task, taskMapper.dataToEntity(data))) {
            List<TaskKpiEntity> taskKpiEntities = taskKpiRepository.findAllByTaskId(task.getId());
            for (TaskKpiEntity taskKpiEntity : taskKpiEntities) {
                taskKpiCalculateService.calculateKpi(taskKpiEntity);
                taskKpiRepository.save(taskKpiEntity);
            }
        }
        task = taskMapper.merge(task, data);
        fillTask(data, task);

        StaffEntity urppStaff = staffHelper.getStaffById(data.getUprppManagerId());

        task.setUprppManagerId(urppStaff.getId());
        task = taskRepository.save(task);
        task = calculatedScoring(task);

        TaskTLDto newTask = taskTLMapper.toDto(task);
        newTask.setTaskScoring(getTaskScoringTLDto(task));
        timeLineService.process(EntityType.TASK, TimelineActionEnum.UPDATE, newTask, oldTask);

        TaskNotificationDto newTaskNotificationDto = taskNotificationMapper.toDto(task);
        notificationService.process(NotificationEntityTypeEnum.TASK, oldTaskNotificationDto, newTaskNotificationDto);

        TaskShortData taskShortData = taskMapper.entityToData(task);
        fillProductGroupName(data);
        fillClientInfo(taskShortData, task.getClients());
        fillStaffData(taskShortData, taskShortData.getUprppManagerId(), taskShortData.getSskManagerId());
        fillScoringFactors(taskShortData);
        fillSubstitute(taskShortData);
        if (!Objects.isNull(taskKafkaProducer)) {
            taskKafkaProducer.send(new OutContainerDto<>(List.of(taskShortData)));
        }

        return taskShortData;
    }

    @Override
    public TaskShortData delete(Long id) {
        throw new UnsupportedOperationException();
    }

    public Map<Long, SskManagerSubstituteStaffData> getTaskWithStaff(TaskStaffsFilterDtoRq taskFilter) {
        Set<TaskEntity> tasks = validator.checkIfNotExists(taskFilter.getTaskIds(), taskRepository);
        if (tasks != null) {
            return tasks.stream()
                    .collect(Collectors.toMap(TaskEntity::getId, this::createManagerStaffData));
        }
        return Map.of();
    }

    private SskManagerSubstituteStaffData createManagerStaffData(TaskEntity task) {
        SskManagerSubstituteStaffData managerStaffData = new SskManagerSubstituteStaffData();
        managerStaffData.setSskManagerId(task.getSskManagerId());
        managerStaffData.setSubstituteStaffId(findActiveSubstituteStaffId(task));
        return managerStaffData;
    }

    private Long findActiveSubstituteStaffId(TaskEntity task) {
        TaskSubstituteSskManagerEntity activeSubstituteStaff = findActiveSubstituteStaff(task.getId());
        if (isNull(activeSubstituteStaff)) {
            return null;
        }
        return activeSubstituteStaff.getStaffId();
    }

    private TaskSubstituteSskManagerEntity findActiveSubstituteStaff(Long taskId) {
        Optional<TaskSubstituteSskManagerEntity> substituteSskManager = taskSubstituteSskManagerRepository
                .findFirstByTaskIdAndIsActiveTrue(taskId);
        if (substituteSskManager.isEmpty()) {
            return null;
        }
        return substituteSskManager.get();
    }


    @Override
    public CreateDealTaskShortData create(CreateDealTaskShortData data) {
        DealEntity dealEntity = dealService.getAndSaveDealById(data.getId());
        if (Objects.isNull(dealEntity)) {
            throw new IllegalStateException(MessageFormat.format(
                    "Not found deal with id: {0}",
                    data.getId()));
        }
        Set<DealProductShortData> createdDealProducts = new HashSet<>();
        for (DealProductShortData dealProductShortData : data.getDealProductData()) {
            DealProductEntity dealProductEntity = dealProductService.getDealProductEntity(dealProductShortData.getId());
            if (Objects.isNull(dealProductEntity)) {
                throw new IllegalStateException(MessageFormat.format(
                        "Not found deal product with id: {0}",
                        dealProductShortData.getId()));
            }
            // FIXME: 05.05.2023 Облегчить проверку, проверка на null сущностей есть выше, но сонар все равно ругается что может быть нпе
            if (nonNull(dealEntity.getId()) && nonNull(dealProductEntity.getDeal()) && nonNull(dealProductEntity.getDeal().getId()) &&
                    !dealEntity.getId().equals(dealProductEntity.getDeal().getId())) {
                throw new IllegalStateException(MessageFormat.format(
                        "Deal product with id: {0} not related to the deal with id={1} ",
                        dealProductShortData.getId(), dealEntity.getId()));
            }
            Set<TaskShortData> createdTasks = new HashSet<>();
            for (TaskShortData taskShortData : dealProductShortData.getImplTasks()) {
                validator.checkIfExists(taskShortData.getId(), taskRepository);
                TaskShortData createTaskShortData = createTask(taskShortData, dealEntity, dealProductEntity);
                fillScoringFactors(createTaskShortData);
                createdTasks.add(createTaskShortData);
            }
            dealProductShortData.setImplTasks(createdTasks);
            createdDealProducts.add(dealProductShortData);
        }
        data.setDealProductData(createdDealProducts);
        if (!Objects.isNull(taskKafkaProducer)) {
            List<TaskShortData> taskDataList = ofNullable(data.getDealProductData())
                    .stream()
                    .flatMap(Collection::stream)
                    .map(DealProductShortData::getImplTasks)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
            taskKafkaProducer.send(new OutContainerDto<>(taskDataList));
        }

        return data;
    }

    @Override
    public TaskShortData create(TaskShortData data) {
        throw new UnsupportedOperationException();
    }

    private TaskShortData createTask(TaskShortData taskData, DealEntity deal, DealProductEntity dealProductEntity) {
        validator.checkIfExists(taskData.getId(), taskRepository);
        TaskEntity taskEntity = taskMapper.dataToEntity(taskData);
        //TODO не заполняется 1 клиент, не хочет перезаписывать, для этого очищаем список
        fillTask(taskData, taskEntity);
        if (isNull(deal.getUprppManagerId())) {
            throw new SsksImplementationApiException(String.format("Not filled UPRPP manager id fro deal with id: %s", deal.getId()));
        }
        taskEntity.setUprppManagerId(deal.getUprppManagerId());
        taskEntity.setDealProduct(dealProductEntity);
        taskEntity = taskRepository.save(taskEntity);
        taskEntity = calculatedScoring(taskEntity);

        TaskTLDto newTaskTLDto = taskTLMapper.toDto(taskEntity);
        newTaskTLDto.setTaskScoring(getTaskScoringTLDto(taskEntity));
        timeLineService.process(EntityType.TASK, TimelineActionEnum.CREATE, newTaskTLDto, new TaskTLDto());

        TaskNotificationDto newTaskNotificationDto = taskNotificationMapper.toDto(taskEntity);
        notificationService.process(NotificationEntityTypeEnum.TASK, new TaskNotificationDto(), newTaskNotificationDto);

        TaskShortData taskShortData = taskMapper.entityToData(taskEntity);
        fillClientInfo(taskShortData, taskEntity.getClients());
        fillSubstitute(taskShortData);
        return taskShortData;
    }

    private void fillTask(TaskShortData taskData, TaskEntity taskEntity) {
        TaskTypeEntity taskType = validator.checkIfNotExists(taskData.getTaskTypeId(), taskTypeRepository);
        if (!taskType.getName().equals(IMPLEMENTATION)) {
            throw new IllegalStateException(MessageFormat.format(
                    "TaskType:{0} for implementation task not supported",
                    taskType.getName()));
        }
        ProductEntity productTask = validator.checkIfNotExists(taskData.getProductId(), productRepository);
        TaskStateEntity taskStateTask = validator.checkIfNotExists(taskData.getTaskStateId(), taskStateRepository);
        if (Objects.nonNull(taskData.getSskManagerId())) {
            var taskSskManager = staffHelper.getStaffById(taskData.getSskManagerId());
            taskEntity.setSskManagerId(taskSskManager.getId());
        }
        if (isNull(taskData.getApprovalSideId())) {
            throw new IllegalStateException("Approval side not filled for task");
        }
        ApprovalSideEntity approvalSide = validator.checkIfNotExists(taskData.getApprovalSideId(), approvalSideRepository);

        if (taskData.getClients() == null) {
            throw new IllegalStateException(MessageFormat.format(
                    "Clients not found for task with taskType:{0} and id: {1}",
                    taskType.getName(), taskData.getId()));
        }
        taskEntity.setClients(new HashSet<>());
        taskEntity.clearTaskSalesOffices();
        if (nonNull(taskData.getSalesOfficeIds())) {
            for (Long salesOfficeId : taskData.getSalesOfficeIds()) {
                taskEntity.addSalesOffice(salesOfficeId);
            }
        }

        Set<ClientEntity> clients = clientService.getClientEntities(taskData.getClients().stream().map(ClientShortData::getId)
                .collect(Collectors.toSet()));

        for (ClientEntity client : clients) {
            if (Objects.isNull(client.getTasks())) {
                client.setTasks(new HashSet<>());
            }
            taskEntity.addClient(client);
        }
        for (ClientShortData clientShortData : taskData.getClients()) {
            if (!isNull(clientShortData.getBranches()) && !clientShortData.getBranches().isEmpty()) {
                Set<ClientEntity> branches = clientService.getClientEntities(clientShortData.getBranches().stream().map(ClientShortData::getId)
                        .collect(Collectors.toSet()));
                for (ClientEntity client : branches) {
                    taskEntity.addClient(client);
                }
            }
        }
        taskEntity.setTaskType(taskType);
        taskEntity.setProduct(productTask);
        taskEntity.setTaskState(taskStateTask);
        taskEntity.setApprovalSide(approvalSide);
    }

    /**
     * Метод для заполнения группы клиентов, клиентов и связанных с ними филиалов
     *
     * @param data       дто задачи
     * @param allClients клиенты задачи
     */
    private void fillClientInfo(TaskShortData data, @Nullable Set<ClientEntity> allClients) {
        if (isNullOrEmpty(allClients)) {
            throw new SsksImplementationApiException(String.format("Invalid database state. No clients for task with taskId = '%d' found", data.getId()));
        }
        ClientEntity parent = ClientEntity.getClientGroup(allClients.iterator().next());
        if (isNull(parent.getType())) {
            throw new IllegalStateException(MessageFormat.format(
                    "Client type not filled for client with id:{0} in task with id: {1}",
                    parent.getId(), data.getId()));
        }

        data.setClientsGroup(!isClientCheckName(parent, GK) ? null : clientMapper.entityToShortData(parent));
        Set<ClientEntity> clients = allClients.stream()
                .filter(e -> isClientCheckName(e, CLIENT))
                .collect(Collectors.toSet());
        if (clients.isEmpty()) {
            clients = allClients;
        }
        Set<ClientShortData> clientShortDataSet = new HashSet<>();
        for (ClientEntity client : clients) {
            Set<ClientEntity> branches = allClients.stream()
                    .filter(e -> e.getParent() != null)
                    .filter(e -> client.getId().equals(e.getParent().getId()))
                    .filter(e -> isClientCheckName(e, "branch")).collect(Collectors.toSet());
            ClientShortData clientShortData = clientMapper.entityToShortData(client);
            clientShortData.setBranches(clientMapper.entityToShortData(branches));
            clientShortData.setIndustryId(1L);
            clientShortData.setIndustryName("Неизменная Отрасль");
            if (client.getManagerClientId() != null) {
                ClientManagerData clientManager = clientService.getClientManager(client.getManagerClientId());
                clientShortData.setManagerClientId(clientManager.getId());
                clientShortData.setManagerClientShortName(clientManager.getShortName());
            }
            clientShortDataSet.add(clientShortData);
        }
        data.setClients(clientShortDataSet);
    }

    private void fillStaffData(TaskShortData data, Long uprppManagerId, Long sskManagerId) {
        if (Objects.nonNull(sskManagerId)) {
            ofNullable(staffHelper.getStaffById(sskManagerId))
                    .ifPresent(sskManager -> data.setSskManagerShortName(sskManager.getShortName()));
        }
        if (Objects.nonNull(uprppManagerId)) {
            ofNullable(staffHelper.getStaffById(uprppManagerId))
                    .ifPresent(uprppManager -> data.setUprppManagerShortName(uprppManager.getShortName()));
        } else if (Long.valueOf(REALIZATION.getValue()).equals(data.getTaskTypeId())) {
            log.warn(String.format("Not filled UPRPP manager id for task with id: %s and type implementation", data.getId()));
        }
    }

    /**
     * Метод заполнения информации по клиентам
     *
     * @param task    - задача {@link TaskEntity}
     * @param parent  - родительский клиент {@link ClientEntity}
     * @param clients - список дочерних клиентов {@link Set<ClientEntity>}
     * @return - мапа с заполненной информацией по клиентам,
     * где key - название поля в taskShortData, value - рассчитанное значение
     */
    private Map<String, String> getClientInfo(TaskEntity task, ClientEntity parent, Set<ClientEntity> clients) {
        Map<String, String> clientInfoMap = new HashMap<>();
        if (isNull(parent.getType())) {
            throw new IllegalStateException(MessageFormat.format(
                    "Client type not filled for client with id:{0}",
                    parent.getId()));
        }

        clientInfoMap.put(GROUP_NAME, Boolean.TRUE.equals(isClientCheckName(parent, GK)) ? parent.getName() : GK_UNDEFINED);
        long countClients = clients.stream()
                .filter(Objects::nonNull)
                .filter(client -> nonNull(client.getType()))
                .filter(client -> nonNull(client.getType().getId()))
                .filter(client -> nonNull(client.getIsActive()))
                .filter(ClientEntity::getIsActive)
                .filter(client -> ClientTypeEnum.CLIENT.getId().equals(client.getType().getId()))
                .count();

        clientInfoMap.put(CLIENTS_IN_TASK, String.valueOf(countClients));
        //TODO ждем ответа от аналитики
        Set<ClientEntity> clientsByParent = clientRepository.findByParentAndIsActiveTrue(parent);
        clientInfoMap.put(CLIENTS_IN_GROUP, getClientsCountInGroup(clientsByParent, task));
        return clientInfoMap;
    }

    /**
     * Метод для получения параметра ClientsCountInGroup
     *
     * @param clientsByParent клиенты родителя
     * @param task            задача
     * @return ClientsCountInGroup
     */
    private String getClientsCountInGroup(Set<ClientEntity> clientsByParent, TaskEntity task) {
        final String COUNT_PARENT_NULL = "1";
        Long taskType = task.getTaskType().getId();

        if (TASK_TYPE_IMPLEMENTATION.equals(taskType)) {
            Set<DealClientEntity> clients = task.getDealProduct().getDeal().getDealClients();
            return String.valueOf(clients.stream()
                    .filter(Objects::nonNull)
                    .filter(dealClient -> nonNull(dealClient.getClient()))
                    .filter(dealClient -> nonNull(dealClient.getClient().getType()))
                    .filter(dealClient -> nonNull(dealClient.getClient().getType().getId()))
                    .filter(dealClient -> nonNull(dealClient.getClient().getIsActive()))
                    .filter(DealClientEntity::getIsActive)
                    .filter(dealClient -> ClientTypeEnum.CLIENT.getId().equals(dealClient.getClient().getType().getId()))
                    .count());
        }

        if (isEmpty(clientsByParent)) {
            return COUNT_PARENT_NULL;
        }

        return String.valueOf(clientsByParent.stream()
                .filter(Objects::nonNull)
                .filter(client -> nonNull(client.getType()))
                .filter(client -> nonNull(client.getType().getId()))
                .filter(client -> nonNull(client.getIsActive()))
                .filter(ClientEntity::getIsActive)
                .filter(client -> ClientTypeEnum.GROUP.getId().equals(client.getType().getId()))
                .count());
    }

    /**
     * Метод для получения родительского типа задачи по дочернему
     *
     * @param taskType дочерний тип задачи
     * @return родительский тип задачи
     */
    private TaskTypeEntity getParentTaskType(TaskTypeEntity taskType) {
        while (taskType.getParent() != null) {
            taskType = taskType.getParent();
        }
        return taskType;
    }

    /**
     * Метод рассчета скоринговых факторов по задаче
     *
     * @param task - задача
     * @return - задача с рассчитанным totalScoringValue
     */
    private TaskEntity calculatedScoring(TaskEntity task) {
        // FIXME: 24.05.2023 переделать на явную установку значения totalScoringValue
        return taskRepository.save(scoringService.calculatedScoring(task));
    }

    private TaskScoringTLDto getTaskScoringTLDto(TaskEntity task) {
        return taskScoringTLDtoMapper.getTaskScoringTLDto(task);
    }

    /**
     * Метод заполнения скоринговых факторов по задаче
     *
     * @param taskData - задача
     * @return - задача с заполненными скоринговыми факторами
     */
    private TaskShortData fillScoringFactors(TaskShortData taskData) {
        Map<String, BigDecimal> scoringFactors = scoringService.getScoringFactorsByTaskId(taskData.getId());
        taskData.setEstimatedComplexityOfIndividualDocuments(scoringFactors.get(TASK_INDIVIDUAL_DOCUMENTS.getName()));
        taskData.setEstimatedComplexityOfIndividualProcess(scoringFactors.get(TASK_INDIVIDUAL_PROCESS.getName()));
        taskData.setEstimatedComplexityWithApprovalSide(scoringFactors.get(TASK_APPROVAL_SIDE.getName()));
        taskData.setEstimatedComplexityWithProjectElements(scoringFactors.get(TASK_WITH_PROJECT.getName()));
        taskData.setEstimatedImportant(scoringFactors.get(TASK_IS_IMPORTANT.getName()));
        taskData.setEstimatedOnBankManagementControl(scoringFactors.get(TASK_ON_BANK_CONTROL.getName()));
        taskData.setEstimatedOnDepartmentManagementControl(scoringFactors.get(TASK_ON_DEPARTMENT_CONTROL.getName()));
        taskData.setEstimatedSpecificCustomer(scoringFactors.get(TASK_SPECIFIC_CUSTOMER.getName()));
        taskData.setEstimatedTopClient(scoringFactors.get(CLIENT_IS_TOP.getName()));
        taskData.setEstimatedDealSize(scoringFactors.get(DEAL_SIZE.getName()));

        return taskData;
    }

    /**
     * Проверяет нужно ли пересчитывать КПЭ при обновлении задачи
     */
    private boolean checkIfNeedsKpiUpdate(TaskEntity oldTask, TaskEntity newTask) {
        if (isNull(oldTask) || isNull(newTask)) {
            return false;
        }
        if (!isNull(oldTask.getProduct()) && !isNull(newTask.getProduct())) {
            if (!oldTask.getProduct().getProductGroupId().equals(newTask.getProduct().getProductGroupId())) {
                return true;
            }
        } else {
            if (!isNull(newTask.getProduct())) {
                return true;
            }
        }
        if (oldTask.getWithProjectElements() != null) {
            if (!oldTask.getWithProjectElements().equals(newTask.getWithProjectElements())) {
                return true;
            }
        } else if (!isNull(newTask.getWithProjectElements())) {
            return true;
        }
        if (oldTask.getComplexityOfIndividualProcess() != null) {
            if (!oldTask.getComplexityOfIndividualProcess().equals(newTask.getComplexityOfIndividualProcess())) {
                return true;
            }
        } else if (!isNull(newTask.getComplexityOfIndividualProcess())) {
            return true;
        }
        if (oldTask.getComplexityOfIndividualDocuments() != null) {
            return !oldTask.getComplexityOfIndividualDocuments().equals(newTask.getComplexityOfIndividualDocuments());
        } else {
            return !isNull(newTask.getComplexityOfIndividualDocuments());
        }
    }

    private String getOverdueName(TaskFilterDtoRq filterDtoRq) {
        if (nonNull(filterDtoRq) && nonNull(filterDtoRq.getOverdueId())) {
            return validator.checkIfNotExists(filterDtoRq.getOverdueId(), overdueRepository).getName();
        } else {
            return null;
        }
    }

    private TaskShortData fillSubstitute(TaskShortData taskShortData) {
        TaskSubstituteSskManagerEntity activeSubstituteStaff = findActiveSubstituteStaff(taskShortData.getId());
        if (nonNull(activeSubstituteStaff)) {
            StaffEntity substituteStaff = staffHelper.getStaffById(activeSubstituteStaff.getStaffId());
            if (nonNull(substituteStaff)) {
                taskShortData.setSubstituteManagerId(activeSubstituteStaff.getStaffId());
                taskShortData.setSubstituteManagerShortName(substituteStaff.getShortName());
                taskShortData.setSubstituteDateFrom(activeSubstituteStaff.getDateFrom());
                taskShortData.setSubstituteDateTo(activeSubstituteStaff.getDateTo());
            } else {
                log.warn(String.format("active substitute staff with id: %s, not present in dictionary service",
                        activeSubstituteStaff.getStaffId()));
            }
        }
        return taskShortData;
    }


    private boolean isClientCheckName(ClientEntity e, String branch) {
        return false;
    }

    private boolean isNullOrEmpty(Set<ClientEntity> allClients) {
        return false;
    }
}
