package ru.cinimex.codeStyle.example2.mocks;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CRUDTaskService<O, I> {//TaskShortData, CreateDealTaskShortData
    Page<O> read(Pageable pageable);

    TaskShortData read(Long id);

    TaskShortData update(TaskShortData data);

    TaskShortData delete(Long id);

    CreateDealTaskShortData create(CreateDealTaskShortData data);

    TaskShortData create(TaskShortData data);


}
