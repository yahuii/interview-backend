package com.guqin.interview.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // 异步处理


    @PostMapping("/question")
    public ResponseBodyEmitter getAIResponseByQuestion(String question) {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
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
                        .header("Authorization", "Bearer sk-909ea9bec76043e9800bc23cfeae8cc0") // 替换为你的 API Key
                        .body(JSONUtil.toJsonStr(map))
                        .executeAsync(); // 关键：异步执行，防止阻塞

                InputStream inputStream = response.bodyStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    emitter.send(line + "\n"); // 逐条发送到前端
                }

                emitter.complete(); // 结束流
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

}


