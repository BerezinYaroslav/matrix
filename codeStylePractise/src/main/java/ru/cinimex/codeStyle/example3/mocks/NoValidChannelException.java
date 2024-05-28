package ru.cinimex.codeStyle.example3.mocks;

public class NoValidChannelException extends RuntimeException {
    public NoValidChannelException() {
    }

    public NoValidChannelException(String message) {
        super(message);
    }

    public NoValidChannelException(String message, Throwable cause) {
        super(message, cause);
    }
}
