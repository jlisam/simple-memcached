package com.slack.memcached.exception;

public class BadDataChunkException extends IllegalArgumentException {

    public BadDataChunkException(Throwable cause) {
        super(cause);
    }

    public BadDataChunkException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadDataChunkException(String s) {
        super(s);
    }

    public BadDataChunkException() {
        super();
    }
}
