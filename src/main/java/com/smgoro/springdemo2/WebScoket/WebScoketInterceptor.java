package com.smgoro.springdemo2.WebScoket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class WebScoketInterceptor implements HandshakeInterceptor {
    public static final int MAX_USER_PER_IP = 3;
    private Map<String, Integer> ipUserCountMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(WebScoketInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 获取用户名称和id
        ServletServerHttpRequest ServletServerHttpRequest = (ServletServerHttpRequest) request;
        String name = ServletServerHttpRequest.getServletRequest().getParameter("name");
        String id = ServletServerHttpRequest.getServletRequest().getParameter("id");
        String roomid = ServletServerHttpRequest.getServletRequest().getParameter("roomid");
        // 获取用户ip
        InetSocketAddress remoteAddress = (InetSocketAddress) request.getRemoteAddress();
        String ip = remoteAddress.getHostString();

//        System.out.println("[用户名：" + name + " (id：" + id + ") (ip:" + ip + ") ] 加入了" + roomid + "聊天室");
        logger.info("[用户名：" + name + " (id：" + id + ") (ip: " + ip + ") ] 加入了" + roomid + "聊天室");
        // 将对应的name和id放到用户会话中
        attributes.put("name", name);
        attributes.put("id", id);
        attributes.put("roomid", roomid);
        attributes.put("ip", ip);
        // 检查当前IP地址对应的用户数量是否超过十个
        int userCount = ipUserCountMap.getOrDefault(ip, 0);
        if (userCount >= MAX_USER_PER_IP) {
//            System.out.println("IP地址：" + ip + " 已经有超过十个用户连接，禁止该用户连接");
            logger.warn("IP地址：" + ip + " 已经有超过" + MAX_USER_PER_IP + "个用户连接，禁止该用户连接");
            // web控制台输出提示
            ServletServerHttpRequest.getServletRequest().getSession().setAttribute("error", "当前IP地址已经有超过" + MAX_USER_PER_IP + "个用户连接，禁止该用户连接");
            return false;
        }
        // 将该用户的IP地址添加到HashMap中
        ipUserCountMap.put(ip, userCount + 1);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 获取用户名称和id
        ServletServerHttpRequest ServletServerHttpRequest = (ServletServerHttpRequest) request;
        String name = ServletServerHttpRequest.getServletRequest().getParameter("name");
        String id = ServletServerHttpRequest.getServletRequest().getParameter("id");
        String roomid = ServletServerHttpRequest.getServletRequest().getParameter("roomid");
        // 获取用户ip
        InetSocketAddress remoteAddress = (InetSocketAddress) request.getRemoteAddress();
        String ip = remoteAddress.getHostString();
        logger.info("[用户名：" + name + " (id：" + id + ") (ip: " + ip + ") ] 退出了" + roomid + "聊天室");

        decreaseUserCountAfterDisconnection(request);
    }

    // 断开连接操作
    private void decreaseUserCountAfterDisconnection(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = (InetSocketAddress) request.getRemoteAddress();
        String ip = remoteAddress.getHostString();

        int userCount = ipUserCountMap.getOrDefault(ip, 0);
        if (userCount > 0) {
            ipUserCountMap.put(ip, userCount - 1);
//            logger.info("IP: " + ip + "断开连接");
        }
    }
}
