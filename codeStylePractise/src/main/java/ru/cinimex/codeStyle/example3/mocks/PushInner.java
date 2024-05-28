package ru.cinimex.codeStyle.example3.mocks;

import lombok.Data;

@Data
public class PushInner {
    private int id;
    private String some;

    public PushInner(int id, String some) {
        this.id = id;
        this.some = some;
    }
}
