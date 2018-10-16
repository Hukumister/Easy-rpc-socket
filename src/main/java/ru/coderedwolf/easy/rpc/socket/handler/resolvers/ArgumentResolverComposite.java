package ru.coderedwolf.easy.rpc.socket.handler.resolvers;

import org.springframework.core.MethodParameter;
import ru.coderedwolf.easy.rpc.socket.Message;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class ArgumentResolverComposite implements ArgumentResolver {

    private final List<ArgumentResolver> argumentResolvers = new LinkedList<>();

    public ArgumentResolverComposite addResolver(ArgumentResolver resolver) {
        this.argumentResolvers.add(resolver);
        return this;
    }

    public ArgumentResolverComposite addResolvers(Collection<? extends ArgumentResolver> resolvers) {
        if (resolvers != null) {
            this.argumentResolvers.addAll(resolvers);
        }
        return this;
    }

    public List<ArgumentResolver> getResolvers() {
        return Collections.unmodifiableList(argumentResolvers);
    }

    public void clear() {
        this.argumentResolvers.clear();
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return getArgumentResolver(parameter) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, Message<?> message) throws Exception {
        ArgumentResolver resolver = getArgumentResolver(parameter);
        if (resolver == null) {
            throw new IllegalStateException("Unknown parameter type [" + parameter.getParameterType().getName() + "]");
        }
        return resolver.resolveArgument(parameter, message);
    }

    private ArgumentResolver getArgumentResolver(MethodParameter parameter) {
        ArgumentResolver result = null;
        for (ArgumentResolver resolver : this.argumentResolvers) {
            if (resolver.supportsParameter(parameter)) {
                result = resolver;
                break;
            }
        }
        return result;
    }
}
