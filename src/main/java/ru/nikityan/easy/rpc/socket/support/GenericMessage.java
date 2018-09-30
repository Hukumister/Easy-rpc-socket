package ru.nikityan.easy.rpc.socket.support;

import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;

import java.util.Map;

/**
 * Created by Nikit on 25.08.2018.
 */
public class GenericMessage<T> implements Message<T> {

    private final T payload;

    private final MessageHeaders messageHeaders;

    public GenericMessage(T payload, Map<String, Object> messageHeaders) {
        this.payload = payload;
        this.messageHeaders = new MessageHeaders(messageHeaders);
    }

    @Override
    public T getPayload() {
        return this.payload;
    }

    @Override
    public MessageHeaders getMessageHeader() {
        return this.messageHeaders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenericMessage<?> that = (GenericMessage<?>) o;

        return (payload != null ? payload.equals(that.payload) : that.payload == null)
                && (messageHeaders != null ? messageHeaders.equals(that.messageHeaders) : that.messageHeaders == null);
    }

    @Override
    public int hashCode() {
        int result = payload != null ? payload.hashCode() : 0;
        result = 31 * result + (messageHeaders != null ? messageHeaders.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GenericMessage{" +
                "payload=" + payload +
                ", messageHeaders=" + messageHeaders +
                '}';
    }
}
