package com.config;

import com.beans.Player;
import com.service.PlayerService;
import com.util.JwtUtil;
import com.util.StringUtil;
import com.websocket.Source;
import com.websocket.Subscribles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * EnableWebSocketMessageBroker 此注解表示开启WebSocket支持。通过此注解开启使用STOMP协议来传输基于代理（message broker）的消息。
 * @author lgh
 * @date 2023.1.3
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private PlayerService playerService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        //注册一个名为/endpointChat的节点，并指定使用SockJS协议。
        stompEndpointRegistry.addEndpoint("/ws/").setAllowedOrigins("*").withSockJS();
        stompEndpointRegistry.addEndpoint("/app").addInterceptors(new TokenInterceptorByUrl()).setAllowedOrigins("*");
    }

    /**
     * 配置消息代理（Message Broker），可以理解为信息传输的通道
     * @param messageBrokerRegistry MessageBrokerRegistry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry messageBrokerRegistry) {
        //点对点式应增加一个/queue的消息代理。相应的如果是广播室模式可以设置为"/topic"
        messageBrokerRegistry.enableSimpleBroker("/queue","/topic");
        // 客户端向服务端发送消息需有/app 前缀
        //messageBrokerRegistry.setApplicationDestinationPrefixes("/app");
        // 指定用户发送（一对一）的前缀 /user/
        messageBrokerRegistry.setUserDestinationPrefix("/user");

    }

    /**
     * 拦截器方式2
     *
     * @param registration
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new TokenInterceptor());
    }


    /**
     * 获取连接时候的用户id
     */
    public class TokenInterceptorByUrl implements HandshakeInterceptor  {


        @Override
        public boolean beforeHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
            String querytoken = serverHttpRequest.getURI().getQuery();
            if (querytoken!=null && querytoken.indexOf("=")>-1)
            {
                map.put("token",querytoken.split("=")[1]);
            }

            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

        }
    }


    /**
     * 获取连接时候的用户id
     */
    public class TokenInterceptor implements  ChannelInterceptor{
        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {

//            System.out.println("================");

            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            //1、判断是否首次连接
            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                //2、判断token
                //List<String> simpSessionAttributes = accessor.getNativeHeader("simpSessionAttributes");
               //List<String> nativeHeader = accessor.getNativeHeader("Authorization");
               List<String> nativeHeader = accessor.getNativeHeader("Authorization");
               String openId =  (String)accessor.getSessionAttributes().get("openId");
               String source = Source.app.name(); //pc

               // System.out.println(simpSessionAttributes.get(0));
                if (nativeHeader != null && !nativeHeader.isEmpty() )
                {
                    openId = nativeHeader.get(0);
                    source =    Source.pc.name();
                }
//                System.out.println("====================openId="+openId);
                if( openId!=null) {
                    String loginUser = openId;
                    Principal principal = new Principal() {
                        @Override
                        public String getName() {
                            return loginUser;
                        }
                    };
                    accessor.setUser(principal);
                    return message;
//                    String token = appToken;
//                    if (token!=null && token.length()>0) {
//                        //todo,通过token获取用户信息，下方用loginUser来代替
//                        //   pc_username
//                        //   app_username
//                        String loginUser = source + "_" + JwtUtil.getUsername(token); //---------------------这里要改
//                        System.out.println(loginUser);
//                        if (loginUser != null) {
//                            //如果存在用户信息，将用户名赋值，后期发送时，可以指定用户名即可发送到对应用户
//                            Principal principal = new Principal() {
//                                @Override
//                                public String getName() {
//                                    return loginUser;
//                                }
//                            };
//                            accessor.setUser(principal);
//                            return message;
//                        }
//                    }
                }
                return message;
            }
            //不是首次连接，已经登陆成功
            return message;
        }

        // 在消息发送后立刻调用，boolean值参数表示该调用的返回值
        @Override
        public void postSend(Message<?> message, MessageChannel messageChannel, boolean b) {

            StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
            Principal principal = accessor.getUser();
            // 忽略心跳消息等非STOMP消息
            if(accessor.getCommand() == null)
            {
                return;
            }
//            switch (accessor.getCommand())
//            {
//                // 首次连接
//                case CONNECT:
//                    System.out.println("首次连接成功");
//                    if(principal!=null){
//                        String userName = principal.getName();
//                        System.out.println(principal.getName());
//                        String uid = userName.split("_")[1];
//                        if(StringUtil.isNotNull(uid)){
//                            Player player = adminService.getById(uid);
//                            if(null!=admin && StringUtil.isNotNull(admin.getParentId())){
//                                //非顶层角色，进行强制下线
//                                simpMessagingTemplate.convertAndSendToUser(principal.getName(), Subscribles.LOGOUT,"强制下线");
//                                System.out.println(principal.getName());
//                                Integer appOnline = admin.getAppOnlineStatus();
//                                Integer pcOnline = admin.getPcOnlineStatus();
//                                if(null==appOnline){
//                                    appOnline = 1;
//                                }
//                                if(null==pcOnline){
//                                    pcOnline = 1;
//                                }
//                                if(userName.startsWith("pc")){
//                                    pcOnline =1;
//                                }else if(userName.startsWith("app")){
//                                    appOnline = 1;
//                                }
//                                Integer onlineStatus = (appOnline==0&&pcOnline==0)?0:1;
//                                adminService.updateOnlineStatus(uid,pcOnline,appOnline,onlineStatus);
//                            }
//                        }
//                    }
//                    break;
//                // 连接中
//                case CONNECTED:
//                    System.out.println("连接成功");
//                    if(principal!=null){
//                        // webSocketManager.deleteUser(principal.getName());
//                        System.out.println(principal.getName());
//                    }
//                    break;
//                // 下线
//                case DISCONNECT:
//                    System.out.println("下线了");
//                    if(principal!=null){
//                        System.out.println(principal.getName());
//                        String userName = principal.getName();
//                        String uid = userName.split("_")[1];
//                        if(StringUtil.isNotNull(uid)){
//                            Admin admin = adminService.getById(uid);
//                            if(null!=admin){
//                                Integer appOnline = admin.getAppOnlineStatus();
//                                Integer pcOnline = admin.getPcOnlineStatus();
//                                if(StringUtil.isNull(admin.getParentId())){
//                                    //父级ID为空，表示顶层角色
//                                    //多端登录次数减1
//                                    Integer multiLogin = admin.getMultiLogin();
//                                    if(null!=multiLogin){
//                                        multiLogin--;
//                                        if(multiLogin<1){
//                                            pcOnline=0;
//                                        }else{
//                                            pcOnline=1;
//                                        }
//                                    }else{
//                                        pcOnline=0;
//                                        multiLogin=0;
//                                    }
//                                    Integer onlineStatus = (appOnline==0&&pcOnline==0)?0:1;
//                                    adminService.updateOnlineStatus(uid,pcOnline,appOnline,onlineStatus,multiLogin);
//
//                                }else{
//
//                                    if(null==appOnline){
//                                        appOnline = 0;
//                                    }
//                                    if(null==pcOnline){
//                                        pcOnline = 0;
//                                    }
//                                    if(userName.startsWith("pc")){
//                                        pcOnline =0;
//                                    }else if(userName.startsWith("app")){
//                                        appOnline = 0;
//                                    }
//                                    Integer onlineStatus = (appOnline==0&&pcOnline==0)?0:1;
//                                    adminService.updateOnlineStatus(uid,pcOnline,appOnline,onlineStatus);
//                                }
//
//                            }
//                        }
//                       // webSocketManager.deleteUser(principal.getName());
//                    }
//                    break;
//                default:
//                    break;
//            }
        }
    }
}
