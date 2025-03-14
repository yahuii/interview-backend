package com.guqin.interview.websocket;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.guqin.interview.model.response.DeepSeekResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ChatWebSocketHandler implements WebSocketHandler {

    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("WebSocket 连接已建立: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        System.out.println("WebSocket 连接已关闭: " + session.getId());
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            handlerTextMessage(session, (TextMessage) message);
        } else {
            LOGGER.error("Test Socket 消息处理失败，只接受 文本消息，sessionId：{}", session.getId());
        }
    }


    public void handlerTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String question = message.getPayload(); // 获取前端发送的问题
        System.out.println("收到问题：" + question);
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("role", "system");
        contentMap.put("content", "你是一个编程高手，我会问你很多关于程序员面试题的问题，你需要使用纯 MarkDown 语法返回给我，不能中断");
        messages.add(contentMap);

        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("role", "user");
        messageMap.put("content", question);
        messages.add(messageMap);

        Map<String, Object> map = new HashMap<>();
        map.put("model", "deepseek-chat");
        map.put("messages", messages);
        map.put("stream", true);
        executor.submit(() -> {
            try {
                HttpResponse response = HttpRequest.post("https://api.deepseek.com/chat/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer sk-909ea9bec76043e9800bc23cfeae8cc0")
                        .keepAlive(true)
                        .body(JSONUtil.toJsonStr(map))
                        .executeAsync();

                InputStream inputStream = response.bodyStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

//                    JSONObject jsonObject = JSONUtil.parseObj(line);
                    if(StringUtils.isBlank(line)){
                        continue;
                    }
                    if(line.equals("[DONE]")){
                        break;
                    }
                    line = StringUtils.remove(line,"data: ");
                    DeepSeekResponse deepSeekResponse = JSONUtil.toBean(JSONUtil.toJsonStr(line), DeepSeekResponse.class);
                    session.sendMessage(new TextMessage(deepSeekResponse.getChoices().get(0).getDelta().getContent())); // 逐条推送给前端
                }

                session.sendMessage(new TextMessage("[DONE]")); // 结束标记
            } catch (Exception e) {
                try {
                    session.sendMessage(new TextMessage("ERROR: " + e.getMessage()));
                } catch (Exception ignored) {
                }
            }
        });
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        LOGGER.error("Test Socket 处理异常，sessionId：{}, 异常原因：{}", session.getId(), exception.getMessage());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }


}

