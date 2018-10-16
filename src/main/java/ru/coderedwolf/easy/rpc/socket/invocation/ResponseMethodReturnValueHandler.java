package ru.coderedwolf.easy.rpc.socket.invocation;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.MessageHeaders;
import ru.coderedwolf.easy.rpc.socket.core.MessageSendingOperations;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcError;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.RequestMethod;
import ru.coderedwolf.easy.rpc.socket.support.MessageBuilder;
import ru.coderedwolf.easy.rpc.socket.support.MessageHeaderAccessor;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class ResponseMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final MessageSendingOperations messageSendingOperations;

    public ResponseMethodReturnValueHandler(MessageSendingOperations messageSendingOperations) {
        this.messageSendingOperations = messageSendingOperations;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(RequestMethod.class);
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  Message<?> message) throws Exception {
        Assert.notNull(returnType, "Method parameter is required");
        MessageHeaders messageHeader = message.getMessageHeader();
        MessageHeaderAccessor accessor = MessageHeaderAccessor.ofHeaders(messageHeader);
        RequestMethod annotation = returnType.getMethodAnnotation(RequestMethod.class);
        accessor.setMessageMethod(annotation.value());
        messageHeader = accessor.getMessageHeaders();
        JsonRpcResponse rpcResponse;
        if (returnValue instanceof JsonRpcError) {
            rpcResponse = new JsonRpcResponse(messageHeader.getId(), (JsonRpcError) returnValue);
        } else {
            rpcResponse = new JsonRpcResponse(messageHeader.getId(), returnValue);
        }
        Message<JsonRpcResponse> responseMessage = MessageBuilder.fromPayload(rpcResponse)
                .withHeaders(messageHeader)
                .build();
        messageSendingOperations.send(responseMessage);
    }
}
