package ru.coderedwolf.easy.rpc.socket.handler.resolvers;

import org.jetbrains.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.util.StringUtils;
import ru.coderedwolf.easy.rpc.socket.Message;
import ru.coderedwolf.easy.rpc.socket.core.MessageConverter;
import ru.coderedwolf.easy.rpc.socket.core.SmartMessageConverter;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.Param;

/**
 * Created by Nikit on 17.09.2018.
 */
public class ParamArgumentResolver implements ArgumentResolver {

    private final MessageConverter converter;

    public ParamArgumentResolver(MessageConverter converter) {
        this.converter = converter;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Param.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
        Param parameterAnnotation = parameter.getParameterAnnotation(Param.class);
        if (parameterAnnotation == null) {
            return null;
        }
        String value = parameterAnnotation.value();
        if (StringUtils.isEmpty(value)) {
            return converter.fromMessage(message, parameter.getParameterType());
        }
        if (converter instanceof SmartMessageConverter) {
            SmartMessageConverter smartMessageConverter = (SmartMessageConverter) this.converter;
            return smartMessageConverter.fromMessage(message, parameter.getParameterType(), value);
        }
        return null;
    }

    private boolean isEmptyParams(@Nullable Object params) {
        if (params == null) {
            return true;
        } else if (params instanceof byte[]) {
            return ((byte[]) params).length == 0;
        } else {
            return params instanceof String && !StringUtils.hasText((String) params);
        }
    }
}
