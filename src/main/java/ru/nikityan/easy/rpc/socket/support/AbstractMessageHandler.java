package ru.nikityan.easy.rpc.socket.support;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import ru.nikityan.easy.rpc.socket.Message;
import ru.nikityan.easy.rpc.socket.MessageHandler;
import ru.nikityan.easy.rpc.socket.exceptions.MessagingException;
import ru.nikityan.easy.rpc.socket.handler.AbstractExceptionHandlerMethodResolver;
import ru.nikityan.easy.rpc.socket.handler.HandlerMethod;
import ru.nikityan.easy.rpc.socket.handler.InvocableHandlerMethod;
import ru.nikityan.easy.rpc.socket.handler.resolvers.ArgumentResolver;
import ru.nikityan.easy.rpc.socket.handler.resolvers.ArgumentResolverComposite;
import ru.nikityan.easy.rpc.socket.invocation.HandlerMethodReturnValueHandler;
import ru.nikityan.easy.rpc.socket.invocation.HandlerMethodReturnValueHandlerComposite;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nikit on 26.08.2018.
 */
public abstract class AbstractMessageHandler implements MessageHandler, ApplicationContextAware, InitializingBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SCOPED_TARGET_NAME_PREFIX = "scopedTarget.";

    @Nullable
    private ApplicationContext applicationContext;

    private final ArgumentResolverComposite argumentResolverComposite
            = new ArgumentResolverComposite();

    private final Map<Class<?>, AbstractExceptionHandlerMethodResolver> exceptionHandlerCache
            = new ConcurrentHashMap<>(64);

    private final Map<String, HandlerMethod> handlerMethods = new LinkedHashMap<>(64);

    private final HandlerMethodReturnValueHandlerComposite returnValueHandlers =
            new HandlerMethodReturnValueHandlerComposite();

    @Override
    public void afterPropertiesSet() throws Exception {
        if (argumentResolverComposite.getResolvers().isEmpty()) {
            argumentResolverComposite.addResolvers(initArgumentResolvers());
        }

        if (returnValueHandlers.getHandlers().isEmpty()) {
            returnValueHandlers.addHandlers(initMethodReturnValue());
        }

        if (applicationContext == null) {
            return;
        }

        for (String beanName : applicationContext.getBeanNamesForType(Object.class)) {
            if (beanName.contains(SCOPED_TARGET_NAME_PREFIX)) {
                continue;
            }
            Class<?> type = null;
            try {
                type = applicationContext.getType(beanName);
            } catch (Throwable throwable) {
                logger.debug("Could not get bean type for bean with name {}", beanName);
            }

            if (type != null && isHandler(type)) {
                resolveHandlerMethods(beanName);
            }
        }
    }

    protected abstract List<HandlerMethodReturnValueHandler> initMethodReturnValue();

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        String destination = getDestination(message);
        if (destination == null) {
            return;
        }
        logger.debug("Searching methods to handle {} , destination='{}'", message, destination);
        handleMessageInternal(message, destination);
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    protected abstract String getMappingForMethod(Method method, Class<?> userType);

    protected abstract List<? extends ArgumentResolver> initArgumentResolvers();

    protected abstract boolean isHandler(Class<?> beanType);

    protected abstract String getDestination(Message<?> message);

    protected void handleMatch(HandlerMethod handlerMethod, String destination, Message<?> message) {
        logger.debug("Invoking {}", handlerMethod.getShortLogMessage());
        handlerMethod = handlerMethod.createWithResolvedBean();
        InvocableHandlerMethod invocable = new InvocableHandlerMethod(handlerMethod);
        invocable.setArgumentResolvers(this.argumentResolverComposite);

        try {
            Object returnValue = invocable.invoke(message);
            MethodParameter returnType = handlerMethod.getReturnType();
            if (void.class == returnType.getParameterType()) {
                return;
            }
            this.returnValueHandlers.handleReturnValue(returnValue, returnType, message);
        } catch (Exception ex) {
            processHandlerMethodException(handlerMethod, ex, message);
        }
    }

    protected void processHandlerMethodException(HandlerMethod handlerMethod, Exception exception, Message<?> message) {
        InvocableHandlerMethod invocable = getExceptionHandlerMethod(handlerMethod, exception);
        if (invocable == null) {
            logger.error("Unhandled exception from message handler method", exception);
            return;
        }
        invocable.setArgumentResolvers(this.argumentResolverComposite);
        logger.debug("Invoking {}", invocable.getShortLogMessage());
        try {
            Throwable cause = exception.getCause();
            Object returnValue = (cause != null ?
                    invocable.invoke(message, exception, cause, handlerMethod) :
                    invocable.invoke(message, exception, handlerMethod));
            MethodParameter returnType = invocable.getReturnType();
            if (void.class == returnType.getParameterType()) {
                return;
            }
            this.returnValueHandlers.handleReturnValue(returnValue, returnType, message);
        } catch (Throwable throwable) {
            logger.error("Error while processing handler method exception", throwable);
        }
    }

    private InvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
        if (logger.isDebugEnabled()) {
            logger.debug("Searching methods to handle " + exception.getClass().getSimpleName());
        }
        Class<?> beanType = handlerMethod.getBeanType();
        AbstractExceptionHandlerMethodResolver resolver = this.exceptionHandlerCache
                .computeIfAbsent(beanType, key -> createExceptionHandlerMethodResolverFor(beanType));
        Method method = resolver.resolveMethod(exception);
        if (method != null) {
            return new InvocableHandlerMethod(handlerMethod.getBean(), method);
        }
        return null;
    }

    protected abstract AbstractExceptionHandlerMethodResolver createExceptionHandlerMethodResolverFor(Class<?> beanType);

    protected void handleMessageInternal(Message<?> message, String destination) {
        HandlerMethod handlerMethod = this.handlerMethods.get(destination);
        if (handlerMethod == null) {
            handleNotFoundMethod(message, destination);
            return;
        }
        logger.debug("Invoking {}", handlerMethod.getShortLogMessage());
        handleMatch(handlerMethod, destination, message);
    }

    protected abstract void handleNotFoundMethod(Message<?> message, String destination);

    protected void resolveHandlerMethods(Object handler) {
        Class<?> handlerType;
        if (handler instanceof String) {
            ApplicationContext context = getApplicationContext();
            Assert.state(context != null, "ApplicationContext is required for resolving handler bean names");
            handlerType = context.getType((String) handler);
        } else {
            handlerType = handler.getClass();
        }
        if (handlerType == null) {
            return;
        }

        final Class<?> userType = ClassUtils.getUserClass(handlerType);
        Map<Method, String> methods = MethodIntrospector.selectMethods(userType,
                (MethodIntrospector.MetadataLookup<String>) method -> getMappingForMethod(method, userType));
        if (logger.isDebugEnabled()) {
            logger.debug(methods.size() + " message handler methods found on " + userType + ": " + methods);
        }
        methods.forEach((key, value) -> registerHandlerMethod(handler, key, value));
    }

    protected void registerHandlerMethod(Object handler, Method method, String mapping) {
        Assert.notNull(mapping, "Mapping must not be null");
        HandlerMethod newHandlerMethod = createHandlerMethod(handler, method);
        HandlerMethod oldHandlerMethod = this.handlerMethods.get(mapping);

        if (oldHandlerMethod != null && !oldHandlerMethod.equals(newHandlerMethod)) {
            throw new IllegalStateException("Ambiguous mapping found. Cannot map '" + newHandlerMethod.getBean() +
                    "' bean method \n" + newHandlerMethod + "\nto " + mapping + ": There is already '" +
                    oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped.");
        }

        this.handlerMethods.put(mapping, newHandlerMethod);
        logger.debug("Mapped \"{}\", onto {}", mapping, newHandlerMethod);
    }

    protected HandlerMethod createHandlerMethod(Object handler, Method method) {
        HandlerMethod handlerMethod;
        if (handler instanceof String) {
            ApplicationContext context = getApplicationContext();
            Assert.state(context != null, "ApplicationContext is required for resolving handler bean names");
            String beanName = (String) handler;
            handlerMethod = new HandlerMethod(beanName, context.getAutowireCapableBeanFactory(), method);
        } else {
            handlerMethod = new HandlerMethod(handler, method);
        }
        return handlerMethod;
    }
}
