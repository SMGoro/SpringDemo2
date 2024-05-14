package com.smgoro.springdemo2.WebScoket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.InetSocketAddress;
import java.util.*;

import static com.smgoro.springdemo2.WebScoket.WebScoketInterceptor.decreaseUserCountAfterDisconnection;


public class WebScoketPushHandler extends TextWebSocketHandler {
    private static List<WebSocketSession> userList = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(WebScoketPushHandler.class);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        System.out.println("渣渣辉");
//        System.out.println(session.getAttributes());
        userList.add(session);
    }

    // 处理用户的发送数据
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // Map -- 字典型 -- 键值对 key:value
        Map<String, Object> map = session.getAttributes();
        String name = (String) map.get("name");
        String id = (String) map.get("id");
        String roomid = (String) map.get("roomid");
        map.put("message", message.getPayload());

        logger.info("[聊天室ID：" + roomid + "] [用户 " + name + " (id: " + id + ") ] 发送内容：" + message.getPayload());
//        System.out.println("[聊天室ID：" + roomid + "] [用户" + name + " (id: " + id + ") ] 发送内容：" + message.getPayload());

        // JSON格式字符串
        TextMessage textMessage = new TextMessage(JSON.toJSONString(map));

        // 获取信息
        for (int i = 0; i < userList.size(); i++) {
            WebSocketSession user = userList.get(i);
            String userRoomId = (String) user.getAttributes().get("roomid");
            if (session.isOpen()) {
                if (roomid.equals(userRoomId)) {
                    user.sendMessage(textMessage);
                }
            } else {
                throw new IllegalStateException("WebSocket is not open");
            }
//            userList.get(i).sendMessage(textMessage);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        // 获取用户名称和id
        Map<String, Object> map = session.getAttributes();
        String name = (String) map.get("name");
        String id = (String) map.get("id");
        String roomid = (String) map.get("roomid");
        // 获取用户ip
        InetSocketAddress remoteAddress = session.getRemoteAddress();
        String ip = remoteAddress.getHostString();

        if (name != null && id != null && roomid != null) {
            logger.info("[用户名：" + name + " (id：" + id + ") (ip: " + ip + ") ] 退出了 " + roomid + " 聊天室");
        } else {
            throw new IllegalArgumentException("Invalid map values");
        }

        userList.remove(session);
        decreaseUserCountAfterDisconnection(session);
    }
}
