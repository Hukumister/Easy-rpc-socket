package ru.nikityan.easy.rpc.socket.invocation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.core.MessageSendingOperations;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.RequestMethod;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class ResponseMethodReturnValueHandlerTest {

    @InjectMocks
    private ResponseMethodReturnValueHandler returnValueHandler;

    @Mock
    private MessageSendingOperations messageSendingOperations;

    private MethodParameter annotatedMethodParameter;
    private MethodParameter methodParameter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Method annotatedMethod = ResponseMethodReturnValueHandlerTest.class.getDeclaredMethod("handle");
        Method notAnnotatedMethod = ResponseMethodReturnValueHandlerTest.class.getDeclaredMethod("notHandle");

        this.annotatedMethodParameter = new SynthesizingMethodParameter(annotatedMethod, 0);
        this.methodParameter = new SynthesizingMethodParameter(notAnnotatedMethod, 0);
    }

    @Test
    public void supportReturnType() throws Exception {
        assertTrue(returnValueHandler.supportsReturnType(annotatedMethodParameter));
        assertFalse(returnValueHandler.supportsReturnType(methodParameter));
    }

    @Test
    public void testReturnMessageWithHeader() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", "foo");

        Message<String> message = MessageBuilder.fromPayload("sting").withHeaders(map).build();
        returnValueHandler.handleReturnValue(123L, annotatedMethodParameter, message);

        verify(messageSendingOperations).send(any());
    }

    @Test
    public void testReturnMessageWithoutHeaders() throws Exception {
        Message<String> message = MessageBuilder
                .fromPayload("sting")
                .build();

        returnValueHandler.handleReturnValue(123L, annotatedMethodParameter, message);

        verify(messageSendingOperations).send(any());
    }

    @RequestMethod("handle")
    private void handle() {

    }

    private void notHandle() {

    }
}