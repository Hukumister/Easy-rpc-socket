package ru.nikityan.easy.rpc.socket.invocation;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHeaders;
import ru.nikityan.easy.rpc.socket.core.MessageSendingOperations;
import ru.nikityan.easy.rpc.socket.jsonRpc.JsonRpcResponse;
import ru.nikityan.easy.rpc.socket.jsonRpc.annotation.RequestMapping;
import ru.nikityan.easy.rpc.socket.support.MessageBuilder;

/**
 * Created by Nikit on 30.09.2018.
 */
public class ResponseMethodReturnValueHandler implements HandlerMethodReturnValueHandler {

    private final MessageSendingOperations messageSendingOperations;

    public ResponseMethodReturnValueHandler(MessageSendingOperations messageSendingOperations) {
        this.messageSendingOperations = messageSendingOperations;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        return returnType.hasMethodAnnotation(RequestMapping.class);
    }

    @Override
    public void handleReturnValue(@Nullable Object returnValue, MethodParameter returnType,
                                  Message<?> message) throws Exception {

        MessageHeaders messageHeader = message.getMessageHeader();
        if (messageHeader != null) {
            JsonRpcResponse rpcResponse = new JsonRpcResponse(messageHeader.getId(), returnValue);
            Message<JsonRpcResponse> responseMessage = MessageBuilder.fromPayload(rpcResponse)
                    .withHeaders(messageHeader)
                    .build();
            messageSendingOperations.send(responseMessage);
            return;
        }
        JsonRpcResponse rpcResponse = new JsonRpcResponse(-1, returnValue);
        Message<JsonRpcResponse> responseMessage = MessageBuilder.fromPayload(rpcResponse)
                .build();
        messageSendingOperations.send(responseMessage);
    }
}
