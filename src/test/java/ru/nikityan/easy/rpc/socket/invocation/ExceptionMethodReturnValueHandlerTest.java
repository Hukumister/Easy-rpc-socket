package ru.nikityan.easy.rpc.socket.invocation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.core.MessageSendingOperations;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.ExceptionHandler;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.RequestMethod;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class ExceptionMethodReturnValueHandlerTest {

    @InjectMocks
    private ExceptionMethodReturnValueHandler returnValueHandler;

    @Mock
    private MessageSendingOperations messageSendingOperations;

    private MethodParameter exceptionParameter;
    private MethodParameter methodParameter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Method annotatedMethod = ExceptionMethodReturnValueHandlerTest.class.getDeclaredMethod("handle");
        Method notAnnotatedMethod = ExceptionMethodReturnValueHandlerTest.class.getDeclaredMethod("notHandle");

        this.exceptionParameter = new SynthesizingMethodParameter(annotatedMethod, 0);
        this.methodParameter = new SynthesizingMethodParameter(notAnnotatedMethod, 0);
    }

    @Test
    public void supportsReturnType() throws Exception {
        Assert.assertTrue(returnValueHandler.supportsReturnType(exceptionParameter));
        Assert.assertFalse(returnValueHandler.supportsReturnType(methodParameter));
    }

    @Test
    public void resolveParam() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", "foo");

        Message<String> message = MessageBuilder.fromPayload("sting")
                .withHeaders(map)
                .build();

        returnValueHandler.handleReturnValue("message", exceptionParameter, message);

        verify(messageSendingOperations).send(any());
    }

    @ExceptionHandler(Exception.class)
    private void handle() {

    }

    @RequestMethod("")
    private void notHandle() {

    }
}

