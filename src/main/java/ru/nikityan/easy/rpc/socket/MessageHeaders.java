package ru.nikityan.easy.rpc.socket;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MessageHeaders implements Map<String, Object> {

    /**
     *
     */
    public static final String MESSAGE_TYPE = "messageType";

    public static final String MESSAGE_ID = "messageId";

    private final Map<String, Object> headers;

    public MessageHeaders() {
        this(null, null);
    }

    public MessageHeaders(Map<String, Object> headers) {
        this(headers, headers == null ? null : (MessageType) headers.getOrDefault(MESSAGE_TYPE, MessageType.REQUEST), null);
    }

    /**
     * @param headers
     * @param id
     */
    public MessageHeaders(Map<String, Object> headers, Long id) {
        this(headers, headers == null ? null : (MessageType) headers.getOrDefault(MESSAGE_TYPE, MessageType.REQUEST), id);
    }

    /**
     * @param headers
     * @param messageType
     * @param id
     */
    public MessageHeaders(Map<String, Object> headers, MessageType messageType, Long id) {
        this.headers = (headers != null ? new HashMap<>(headers) : new HashMap<>());
        if (messageType != null) {
            this.headers.put(MESSAGE_TYPE, messageType);
        } else {
            this.headers.put(MESSAGE_TYPE, MessageType.REQUEST);
        }
        if (id != null) {
            this.headers.put(MESSAGE_ID, id);
        }
    }

    public MessageType getMessageType() {
        return (MessageType) headers.get(MESSAGE_TYPE);
    }

    public Long getId() {
        return (Long) headers.getOrDefault(MESSAGE_ID, -1L);
    }

    protected Map<String, Object> getRawHeader() {
        return this.headers;
    }

    @Override
    public int size() {
        return headers.size();
    }

    @Override
    public boolean isEmpty() {
        return headers.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return headers.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return headers.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return headers.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("MessageHeaders is immutable");
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("MessageHeaders is immutable");
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException("MessageHeaders is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("MessageHeaders is immutable");
    }

    @Override
    public Set<String> keySet() {
        return headers.keySet();
    }

    @Override
    public Collection<Object> values() {
        return headers.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return headers.entrySet();
    }
}
