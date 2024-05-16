package com.smgoro.springdemo2.WebScoket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


import static com.smgoro.springdemo2.WebScoket.WebScoketInterceptor.decreaseUserCountAfterDisconnection;
import static java.nio.file.Files.createFile;


public class WebScoketPushHandler extends TextWebSocketHandler {
    static List<WebSocketSession> userList = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(WebScoketPushHandler.class);

    public String name;
    public String id;
    public String roomid;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        System.out.println("渣渣辉");
//        System.out.println(session.getAttributes());
        userList.add(session);
        Map<String, Object> map = session.getAttributes();
        name = (String) map.get("name");
        id = (String) map.get("id");
        roomid = (String) map.get("roomid");
        sendLast50MessagesToUser(session, roomid);
    }

    // 处理用户的发送数据
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // Map -- 字典型 -- 键值对 key:value
        Map<String, Object> map = session.getAttributes();
        name = (String) map.get("name");
        id = (String) map.get("id");
        roomid = (String) map.get("roomid");
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

            // 保存消息到文件
            saveMessageToFile(map, roomid);
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

    // 保存聊天记录
    public void saveMessageToFile(Map<String, Object> message, String roomId) throws IOException {
        // 将 Map 转换为 JSON 对象
        JSONObject jsonObject = new JSONObject(message);

        // 文件路径
        String filePath = "logs/chatrooms/" + roomId + ".json";

        // 创建文件夹
        File directory = new File("logs/chatrooms/");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 读取现有的聊天记录
        JSONArray jsonArray;
        if (!Files.exists(Paths.get(filePath)) || Files.readString(Paths.get(filePath)).isBlank()) {
            // 如果文件不存在或为空，创建一个新的 JSON 数组
            jsonArray = new JSONArray();
        } else {
            // 如果文件存在且不为空，将文件内容解析为 JSON 数组
            jsonArray = JSON.parseArray(Files.readString(Paths.get(filePath)));
        }

        // 将新的聊天记录添加到 JSON 数组中
        jsonArray.add(jsonObject);

        try (FileWriter file = new FileWriter(filePath)) {
            // 将 JSON 数组写回到文件中
            file.write(jsonArray.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendLast50MessagesToUser(WebSocketSession session, String roomId) throws IOException {
        // 文件路径
        String filePath = "logs/chatrooms/" + roomId + ".json";
        File file = new File(filePath);

        // 确保文件存在
        if (file.exists() && !file.isDirectory()) {
            // 读取文件内容
            String fileContent = Files.readString(file.toPath());

            // 将文件内容解析为 JSON 数组
            JSONArray jsonArray = JSONArray.parseArray(fileContent);

            // 选择最后 50 条数据
            int size = jsonArray.size();
            int startIndex = Math.max(0, size - 50);
            List<String> last50Messages = new ArrayList<>();

            for (int i = startIndex; i < size; i++) {
                last50Messages.add(jsonArray.getString(i));
            }

            // 发送数据
            for (String message : last50Messages) {
                TextMessage textMessage = new TextMessage(message);
                if (session.isOpen()) {
                    session.sendMessage(textMessage);
                } else {
                    throw new IllegalStateException("WebSocket is not open");
                }
            }
        } else {
            // 如果文件不存在或不是一个文件，可以发送一个错误消息或者忽略
            System.out.println("No chat history found for room: " + roomId);
        }
    }

}
