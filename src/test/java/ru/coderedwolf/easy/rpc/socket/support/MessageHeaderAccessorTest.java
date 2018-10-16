package ru.coderedwolf.easy.rpc.socket.support;

import org.junit.Test;
import ru.coderedwolf.easy.rpc.socket.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Nikit on 29.09.2018.
 */
public class MessageHeaderAccessorTest {

    @Test
    public void existingHeaders() throws InterruptedException {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", "baz");

        GenericMessage<String> message = new GenericMessage<>("payload", map);

        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofMessage(message);
        MessageHeaders actual = accessor.getMessageHeaders();

        assertEquals(3, actual.size());
        assertEquals("bar", actual.get("foo"));
        assertEquals("baz", actual.get("bar"));
    }

    @Test
    public void muttable() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", "baz");

        GenericMessage<String> message = new GenericMessage<>("payload", map);
        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofMessage(message);

        accessor.setMessageMethod("subcribe");
        MessageHeaders messageHeaders = accessor.getMessageHeaders();

        assertEquals(messageHeaders.size(), 4);
    }

    @Test(expected = IllegalStateException.class)
    public void setImmutable() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", "baz");

        GenericMessage<String> message = new GenericMessage<>("payload", map);
        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofMessage(message);

        accessor.setMessageMethod("subcribe");
        accessor.setImmutable();
        accessor.setMessageMethod("method");
    }
}