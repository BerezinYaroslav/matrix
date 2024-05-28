package ru.cinimex.codeStyle.example2.mocks;

import java.math.BigDecimal;
import java.util.Map;

public class ScoringService {
    public TaskEntity calculatedScoring(TaskEntity task) {
        return new TaskEntity();
    }

    public Map<String, BigDecimal> getScoringFactorsByTaskId(long id) {
        return null;
    }
}
