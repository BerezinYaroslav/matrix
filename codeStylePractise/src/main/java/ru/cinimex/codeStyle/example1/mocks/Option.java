package ru.cinimex.codeStyle.example1.mocks;

import lombok.Data;

@Data
public class Option {

    private Integer id;
    private OptionType optionType;

    public Object getTarget() {
        return new Object();
    }
}
