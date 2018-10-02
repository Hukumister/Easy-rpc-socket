package ru.nikityan.easy.rpc.socket.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageType;
import ru.nikityan.easy.rpc.socket.exceptions.JsonRequestException;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcRequest;

import static org.junit.Assert.assertEquals;

/**
 * Created by Nikit on 02.10.2018.
 */
public class JsonRpcIncomingConverterTest {

    private final Gson gson = new GsonBuilder().create();

    private final JsonRpcIncomingConverter incomingConverter = new JsonRpcIncomingConverter();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test(expected = UnsupportedOperationException.class)
    public void fromMessageUnsupported() throws Exception {
        incomingConverter.fromMessage(null, null);
    }

    @Test
    public void toMessage() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(1L, "method", null);
        String toJson = gson.toJson(jsonRpcRequest);

        Message<?> message = incomingConverter.toMessage(toJson);
        JsonRpcRequest rpcRequest = (JsonRpcRequest) message.getPayload();

        assertEquals(message.getMessageHeader().getMessageType(), MessageType.REQUEST);
        assertEquals(rpcRequest.getId(), 1L);
        assertEquals(rpcRequest.getMethod(), "method");
    }

    @Test
    public void failParse() throws Exception {
        String json = "{id='1', {method = 12}";
        expectedException.expect(JsonRequestException.class);
        try {
            incomingConverter.toMessage(json);
        } catch (JsonRequestException ex) {
            assertEquals(ex.getRpcError(), JsonRpcError.parseError());
            throw ex;
        }
    }

    @Test
    public void failRequest() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(1L, null, null);
        String toJson = gson.toJson(jsonRpcRequest);

        expectedException.expect(JsonRequestException.class);
        try {
            incomingConverter.toMessage(toJson);
        } catch (JsonRequestException ex) {
            assertEquals(ex.getRpcError(), JsonRpcError.badRequest());
            throw ex;
        }
    }
}