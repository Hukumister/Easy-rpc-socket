package ru.coderedwolf.easy.rpc.socket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.coderedwolf.easy.rpc.socket.support.MessageHeaderAccessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This a header for message.
 * Class <b>immutable</b>. For change header user assessor {@link MessageHeaderAccessor}.
 *
 * @author CodeRedWolf
 * @since 1.0
 */
public class MessageHeaders implements Map<String, Object> {

    /**
     * The key for message type.
     */
    public static final String MESSAGE_TYPE = "messageType";

    /**
     * The key for message id.
     */
    public static final String MESSAGE_ID = "messageId";

    private final Map<String, Object> headers;

    /**
     * Constructor build empty headers with default key, value.
     * Default message type is Request type, and message id = -1.
     */
    public MessageHeaders() {
        this(null, null);
    }

    /**
     * Construct a header with the given headers.
     *
     * @param headers a map with headers to add.
     */
    public MessageHeaders(@Nullable Map<String, Object> headers) {
        this(headers, headers == null ? null : (MessageType) headers.getOrDefault(MESSAGE_TYPE, MessageType.REQUEST), null);
    }

    /**
     * Construct a header with the given headers and message id.
     *
     * @param headers a map with headers to add.
     * @param id      message id.
     */
    public MessageHeaders(Map<String, Object> headers, Long id) {
        this(headers, headers == null ? null : (MessageType) headers.getOrDefault(MESSAGE_TYPE, MessageType.REQUEST), id);
    }

    /**
     * Construct a header with the given headers, message id and message type.
     *
     * @param headers     a map with headers to add.
     * @param messageType message type.
     * @param id          message id.
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

    /**
     * Gets a message type of message.
     *
     * @return message type.
     */
    public MessageType getMessageType() {
        return (MessageType) headers.get(MESSAGE_TYPE);
    }

    /**
     * Gets a message id of message.
     *
     * @return message id.
     */
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

    /**
     * The call to this method will result in {@link UnsupportedOperationException}.
     */
    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("MessageHeaders is immutable");
    }

    /**
     * The call to this method will result in {@link UnsupportedOperationException}.
     */
    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("MessageHeaders is immutable");
    }

    /**
     * The call to this method will result in {@link UnsupportedOperationException}.
     */
    @Override
    public void putAll(@NotNull Map<? extends String, ?> m) {
        throw new UnsupportedOperationException("MessageHeaders is immutable");
    }

    /**
     * The call to this method will result in {@link UnsupportedOperationException}.
     */
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageHeaders that = (MessageHeaders) o;
        return headers != null ? headers.equals(that.headers) : that.headers == null;
    }

    @Override
    public int hashCode() {
        return headers != null ? headers.hashCode() : 0;
    }
}
