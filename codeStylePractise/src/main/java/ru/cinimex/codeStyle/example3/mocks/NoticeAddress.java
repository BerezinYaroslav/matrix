package ru.cinimex.codeStyle.example3.mocks;

import java.util.List;

import lombok.Data;

@Data
public class NoticeAddress<T> {
    private String stub;
    private List<PushToken> pushTokens;
    private boolean isPresent;
    private T address;

    public NoticeAddress() {
    }

    public NoticeAddress(String stub) {
        this.stub = stub;
    }

    public NoticeAddress(List<PushToken> pushTokens) {
        this.pushTokens = pushTokens;
    }
}
