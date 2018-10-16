package ru.coderedwolf.easy.rpc.socket.handler.resolvers;

import com.google.gson.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcRequest;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.Param;
import ru.coderedwolf.easy.rpc.socket.core.JsonRequestMessageConverter;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class ParamArgumentResolverTest {

    private ParamArgumentResolver resolver;

    private MethodParameter stringParam;
    private MethodParameter primitiveParam;
    private MethodParameter listParam;
    private MethodParameter mapParam;
    private MethodParameter notAnnotatedParam;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        Method payloadMethod = ParamArgumentResolverTest.class.getDeclaredMethod("handleMessage", String.class,
                Long.class, List.class, Map.class, String.class);

        this.stringParam = new SynthesizingMethodParameter(payloadMethod, 0);
        this.primitiveParam = new SynthesizingMethodParameter(payloadMethod, 1);
        this.listParam = new SynthesizingMethodParameter(payloadMethod, 2);
        this.mapParam = new SynthesizingMethodParameter(payloadMethod, 3);
        this.notAnnotatedParam = new SynthesizingMethodParameter(payloadMethod, 4);

        resolver = new ParamArgumentResolver(new JsonRequestMessageConverter());
    }

    @Test
    public void supportsParameter() throws Exception {
        assertTrue(resolver.supportsParameter(stringParam));
        assertTrue(resolver.supportsParameter(mapParam));
        assertTrue(resolver.supportsParameter(listParam));
        assertTrue(resolver.supportsParameter(primitiveParam));

        assertFalse(resolver.supportsParameter(notAnnotatedParam));
    }

    @Test
    public void resolveString() throws Exception {
        JsonPrimitive primitive = new JsonPrimitive("string");
        JsonRpcRequest rpcRequest = new JsonRpcRequest(1L, "method", primitive);
        Message<JsonRpcRequest> message = MessageBuilder.fromPayload(rpcRequest).build();

        Object argument = resolver.resolveArgument(stringParam, message);
        assertEquals(argument, "string");
    }

    @Test
    public void resolvePrimitive() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("x", 123);

        JsonRpcRequest rpcRequest = new JsonRpcRequest(1L, "method", jsonObject);
        Message<JsonRpcRequest> message = MessageBuilder.fromPayload(rpcRequest).build();

        Object argument = resolver.resolveArgument(primitiveParam, message);
        assertEquals(argument, 123L);
    }

    @Test
    public void resolveCollection() throws Exception {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(1);
        jsonArray.add(2);
        jsonArray.add(3);

        JsonRpcRequest rpcRequest = new JsonRpcRequest(1L, "method", jsonArray);
        Message<JsonRpcRequest> message = MessageBuilder.fromPayload(rpcRequest).build();

        Object argument = resolver.resolveArgument(listParam, message);

        assertEquals(argument, Arrays.asList(1, 2, 3));
    }

    @Test
    public void resolveMap() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        map.put("bar", "foo");

        Gson gson = new GsonBuilder().create();
        JsonElement jsonElement = gson.toJsonTree(map);
        JsonRpcRequest rpcRequest = new JsonRpcRequest(2L, "method", jsonElement);
        Message<?> requestMessage = MessageBuilder.fromPayload(rpcRequest).build();

        Object argument = resolver.resolveArgument(mapParam, requestMessage);

        assertEquals(argument, map);
    }

    private void handleMessage(@Param String param,
                               @Param("x") Long id,
                               @Param List<Integer> list,
                               @Param Map<String, String> map,
                               String name) {

    }
}