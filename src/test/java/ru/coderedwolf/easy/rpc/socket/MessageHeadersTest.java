package ru.coderedwolf.easy.rpc.socket;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class MessageHeadersTest {

    @Test
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public void testType() throws Exception {
        MessageHeaders messageHeaders = new MessageHeaders(null, -1L);
        MessageType messageType = messageHeaders.getMessageType();

        assertEquals(messageType, MessageType.REQUEST);
    }

    @Test
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    public void testId() throws Exception {
        MessageHeaders messageHeaders = new MessageHeaders(null, 123L);
        Long messageHeadersId = messageHeaders.getId();

        assertEquals(messageHeadersId.longValue(), 123L);
    }

    @Test
    public void testMessageOverwritten() throws Exception {
        MessageHeaders first = new MessageHeaders(null, MessageType.NOTIFICATION, -1L);
        MessageHeaders second = new MessageHeaders(first, -1L);

        assertEquals(second.getMessageType(), MessageType.NOTIFICATION);
    }

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Test
    public void testSimpleCreate() throws Exception {
        MessageHeaders headers = new MessageHeaders();

        assertEquals((long) headers.getId(), -1);
        assertEquals(headers.getMessageType(), MessageType.REQUEST);
    }

    @Test
    public void fromMapCreate() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", "foo");

        MessageHeaders messageHeaders = new MessageHeaders(map);

        assertEquals(messageHeaders.get("foo"), map.get("foo"));
        assertEquals(messageHeaders.get("bar"), map.get("bar"));
    }
}