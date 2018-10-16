# easy-rpc-socket
This library is designed to simplify a work with the jsonRpc protocol (a link to the specification 
https://www.jsonrpc.org/specification), the websocket protocol is used as a transport protocol.

## Installing
The library works in the spring framework ecosystem. 
To use the library, you must enable sping dependencies for working with a websocket.

    compile "org.springframework:spring-websocket:$springVersion"
    compile "org.springframework:spring-context-support:$springVersion"

Where `$springVersion ` set spring version 4.3.+

Then you need to create a configuration file, which implements the WebSocketConfigurer interface. In this configuration 
file, connect the easy-socket-rpc library by adding the annotation `@EnableEasySocketRpc`. After that all beans of this 
library will be available.

Further, by overriding the registerWebSocketHandlers method, you need to configure the path and specify the 
TransportWebSocketHandler bean as the handle. It is also necessary to configure HandshakeHandler. Configure it in 
different ways depending on the container used.

Example for jetty:

    ``` java
    @Configuration
    @EnableWebSocket
    @EnableEasySocketRpc
    public class WebSocketConfig implements WebSocketConfigurer {
 
        private final ServletContext context;
        private final TransportWebSocketHandler handler;
 
        @Autowired
        public WebSocketConfig(ServletContext context, TransportWebSocketHandler handler) {
            this.context = context;
            this.handler = handler;
        }
 
        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(handler, "/jsonRpcHandler")
                    .setHandshakeHandler(handshakeHandler())
                    .setAllowedOrigins("*");
        }
 
        @Bean
        public DefaultHandshakeHandler handshakeHandler() {
            WebSocketPolicy policy = new WebSocketPolicy(WebSocketBehavior.SERVER);
            policy.setIdleTimeout(120_000);
            return new DefaultHandshakeHandler(new JettyRequestUpgradeStrategy(new WebSocketServerFactory(context, policy)));
        }
    }
    ```

## Getting Started

After the library is connected, the controller may be created.
Example:

    @JsonRpcController
    public class HelloWorld {

        @RequestMethod("helloWorld")
        public String helloWorld(@Param("id") Long idNumber) {
            System.out.println(idNumber);
            return "HelloWorld";
        }

        @Subscribe("subscribe")
        public String subscribe(@Param("name") String str) {
            System.out.println(str);
            return "ok";
        }
    }

Sample request model:

    {
        "id":123,
        "method":"helloWorld", 
        "params":{
            "id":123
        }
    }
     
Response Model

   
    {
   	    "id":123,
   	    "result":"HelloWorld", 
   	    "error":null
    }
   
Also your can return another object.

Example:  
 
        ``` java       
         private class Answer {
            private final long id;
            private final String title;
    
            public Answer(long id, String title) {
                this.id = id;
                this.title = title;
            }
    
            public long getId() {
                return id;
            }
    
            public String getTitle() {
                return title;
            }
        }
        
        @Controller
        public class HelloWorld {
            
            @RequestMethod("helloWorld")
            public Answer helloWorld(@Param("id") Long idNumber) {
                return new Answer(idNumber, "testTitle");
            }
        }
        ```
        
Response Model

    {
        "id":123,
        "result":{
            "id":123, 
            "title":"testTitle"
        },
        "error":null
    }       
        
Further, methods are divided into two types: method, as it is, and subscription. The method operates on the principle 
of query-response. The subscription works in the similar way as the method does, except that the client’s session is 
memorized, by means of that you can send notifications to all clients, who subscribe to this method, using the 
`MessageSendingOperations` bean.

Example of using MessageSendingOperations:

    @Controller
     public class HelloWorld {
    
        private final MessageSendingOperations sendingOperations;
    
        public HelloWorld(MessageSendingOperations sendingOperations) {
            this.sendingOperations = sendingOperations;
        }
    
        @Scheduled(fixedDelay = 2000)
        public void schedule() {
            sendingOperations.convertAndSend("subscribe", "ok");
        }
    
        @Subscribe("subscribe")
        public String subscribe(@Param("name") String str) {
            System.out.println(str);
            return "ok";
        }
    }
      

Request model:

    {
        "id":45,
        "method":"subscribe", 
        "params":{
           "name":"Test"
        }
    }
  
Response model:

    {
   	    "id":45,
   	    "error":null
   	    "result":"ok", 
    }
   
Notification model:   
    
    {
        "method":"subscribe",
        "params":"ok"
    }
    
In addition, it is possible to catch exceptions in a method by marking the method with the ExceptionHandler annotation 
and passing to the annotation the type
of the exception being caught. If there are no methods in the controller marked with the ExceptionHandler annotation, 
then a standard error will be sent to the client (error model).

Example:
    
    @Controller
    public class HelloWorld {
    
        @ExceptionHandler(Exception.class)
        public String handleException() {
            System.out.println("Exception");
            return "Exception";
        }
    
        @ExceptionHandler(IllegalStateException.class)
        public String handle() {
            System.out.println("IllegalStateException");
            return "IllegalStateException";
        }
    
        @RequestMethod("helloWorld")
        public String helloWorld(@Param("id") Long idNumber) throws Exception {
            throw new Exception("error");
        }
    
    }
    
Response model:

    {
   	    "id":45,
   	    "error":null
   	    "result":"Exception", 
    }    

If you want return error object throw exception or return JsonRpcError object.

Example:

    @Controller
    public class HelloWorld {
        
        @ExceptionHandler(IllegalStateException.class)
        public JsonRpcError handle() {
            return new JsonRpcError(-32001, "custom error");
        }
    }



Response model:

    {
        "id":123,
        "result":null, 
        "error":{
            "code":-32001,
            "message":"custom error"
        }
    }


## Authors

* CodeRedWolf
