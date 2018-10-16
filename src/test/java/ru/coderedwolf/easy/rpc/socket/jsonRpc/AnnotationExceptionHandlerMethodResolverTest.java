package ru.coderedwolf.easy.rpc.socket.jsonRpc;

import org.junit.Test;
import org.springframework.util.ClassUtils;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.JsonRpcController;
import ru.coderedwolf.easy.rpc.socket.jsonRpc.annotation.ExceptionHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;

import static org.junit.Assert.*;

/**
 * @author CodeRedWolf
 * @since 1.0
 */
public class AnnotationExceptionHandlerMethodResolverTest {

    private final AnnotationExceptionHandlerMethodResolver resolver =
            new AnnotationExceptionHandlerMethodResolver(ExceptionController.class);

    @Test
    public void resolveMethodFromAnnotation() {
        IOException exception = new IOException();
        assertEquals("handleIOException", this.resolver.resolveMethod(exception).getName());
    }

    @Test
    public void resolveMethodFromArgument() {
        IllegalArgumentException exception = new IllegalArgumentException();
        assertEquals("handleIllegalArgumentException", this.resolver.resolveMethod(exception).getName());
    }

    @Test
    public void resolveMethodFromArgumentWithErrorType() {
        AssertionError exception = new AssertionError();
        assertEquals("handleAssertionError", this.resolver.resolveMethod(new IllegalStateException(exception)).getName());
    }

    @Test
    public void resolveMethodExceptionSubType() {
        IOException ioException = new FileNotFoundException();
        assertEquals("handleIOException", this.resolver.resolveMethod(ioException).getName());
        SocketException bindException = new BindException();
        assertEquals("handleSocketException", this.resolver.resolveMethod(bindException).getName());
    }

    @Test
    public void resolveMethodBestMatch() {
        SocketException exception = new SocketException();
        assertEquals("handleSocketException", this.resolver.resolveMethod(exception).getName());
    }

    @Test
    public void resolveMethodNoMatch() {
        Exception exception = new Exception();
        assertNull("1st lookup", this.resolver.resolveMethod(exception));
        assertNull("2nd lookup from cache", this.resolver.resolveMethod(exception));
    }

    @Test
    public void resolveMethodInherited() {
        IOException exception = new IOException();
        assertEquals("handleIOException", this.resolver.resolveMethod(exception).getName());
    }

    @Test
    public void resolveMethodAgainstCause() {
        IllegalStateException exception = new IllegalStateException(new IOException());
        assertEquals("handleIOException", this.resolver.resolveMethod(exception).getName());
    }

    @Test(expected = IllegalStateException.class)
    public void ambiguousExceptionMapping() {
        new AnnotationExceptionHandlerMethodResolver(AmbiguousController.class);
    }

    @Test(expected = IllegalStateException.class)
    public void noExceptionMapping() {
        new AnnotationExceptionHandlerMethodResolver(NoExceptionController.class);
    }


    @JsonRpcController
    @SuppressWarnings("unused")
    static class ExceptionController {

        public void handle() {}

        @ExceptionHandler(IOException.class)
        public void handleIOException() {
        }

        @ExceptionHandler(SocketException.class)
        public void handleSocketException() {
        }

        @ExceptionHandler
        public void handleIllegalArgumentException(IllegalArgumentException exception) {
        }

        @ExceptionHandler
        public void handleAssertionError(AssertionError exception) {
        }
    }


    @JsonRpcController
    static class InheritedController extends ExceptionController {

        @Override
        public void handleIOException()	{
        }
    }


    @JsonRpcController
    static class AmbiguousController {

        public void handle() {}

        @ExceptionHandler({BindException.class, IllegalArgumentException.class})
        public String handle1(Exception ex) throws IOException {
            return ClassUtils.getShortName(ex.getClass());
        }

        @ExceptionHandler
        public String handle2(IllegalArgumentException ex) {
            return ClassUtils.getShortName(ex.getClass());
        }
    }


    @JsonRpcController
    static class NoExceptionController {

        @ExceptionHandler
        public void handle() {
        }
    }

}