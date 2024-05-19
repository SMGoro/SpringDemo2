package com.smgoro.springdemo2.WebScoket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WebScoketInterceptor implements HandshakeInterceptor {
    public static final int MAX_USER_PER_IP = 3;
    public static Map<String, Integer> ipUserCountMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(WebScoketInterceptor.class);
    public String name;
    public String id;
    public String roomid;
    public static String ip;
    public String time;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 获取用户名称和id
        ServletServerHttpRequest ServletServerHttpRequest = (ServletServerHttpRequest) request;
        name = ServletServerHttpRequest.getServletRequest().getParameter("name");
        id = ServletServerHttpRequest.getServletRequest().getParameter("id");
        roomid = ServletServerHttpRequest.getServletRequest().getParameter("roomid");
        // 获取用户ip
        InetSocketAddress remoteAddress = request.getRemoteAddress();
//        ip = remoteAddress.getHostString();
        ip = getRealIp(request);
        // 获取当前时间为time变量
        time = getCurrentTime();

//        System.out.println("[用户名：" + name + " (id：" + id + ") (ip:" + ip + ") ] 加入了" + roomid + "聊天室");
        logger.info(time + " [用户名：" + name + " (id：" + id + ") (ip: " + ip + ") ] 加入了 " + roomid + " 聊天室");
        // 将对应的name和id放到用户会话中
        attributes.put("name", name);
        attributes.put("id", id);
        attributes.put("roomid", roomid);
        attributes.put("ip", ip);
        attributes.put("time", time);

        // 检查当前IP地址对应的用户数量是否超过最大值
        int userCount = ipUserCountMap.getOrDefault(ip, 0);
        if (userCount >= MAX_USER_PER_IP) {
//            System.out.println("IP地址：" + ip + " 已经有超过" + MAX_USER_PER_IP + "个用户连接，禁止该用户连接");
            logger.warn("IP地址：" + ip + " 已经有超过" + MAX_USER_PER_IP + "个用户连接，禁止该用户连接");
            // web控制台输出提示
            ServletServerHttpRequest.getServletRequest().getSession().setAttribute("message", "当前IP地址已经有超过" + MAX_USER_PER_IP + "个用户连接，禁止该用户连接");
            return false;
        }
        // 将该用户的IP地址添加到HashMap中
        ipUserCountMap.put(ip, userCount + 1);
        return true;
    }

    // 获取当前时间
    public String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return sdf.format(date);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    public static void decreaseUserCountAfterDisconnection(WebSocketSession request) {
//        InetSocketAddress remoteAddress = request.getRemoteAddress();
//        String ip = remoteAddress.getHostString();
        int userCount = ipUserCountMap.getOrDefault(ip, 0);
        if (userCount > 0) {
            ipUserCountMap.put(ip, userCount - 1);
            logger.info("IP: " + ip + "断开连接");
        }
    }

    // 获取真实IP
    public static String getRealIp(ServerHttpRequest request) {
        String realIp = request.getHeaders().getFirst("X-Real-IP");
        if (realIp == null) {
            realIp = request.getRemoteAddress().getHostString();
        }
        return realIp;
    }
}
