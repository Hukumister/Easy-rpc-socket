package ru.nikityan.easy.rpc.socket.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception used in jsonrpc protocol.
 */
public class JsonRequestException extends RuntimeException {
    /**
     * Code use like in HTTP code error
     */
    private int code;

    public JsonRequestException(String message) {
        super(message);
    }

    public JsonRequestException(String message, HttpStatus code) {
        super(message);
        this.code = code.value();
    }

    public JsonRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getCode() {
        return code;
    }
}
