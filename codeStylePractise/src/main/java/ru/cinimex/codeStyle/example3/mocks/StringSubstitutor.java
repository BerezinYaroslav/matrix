package ru.cinimex.codeStyle.example3.mocks;

import java.util.function.Function;

import lombok.Data;

@Data
public class StringSubstitutor {
    private String some;

    public StringSubstitutor(Function<String, String> function) {
    }

    public String replace(String templateText) {
        return null;
    }
}
