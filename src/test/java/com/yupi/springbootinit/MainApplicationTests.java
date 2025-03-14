package com.yupi.springbootinit;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.guqin.interview.MainApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主类测试
 */
@SpringBootTest(classes = MainApplication.class)
class MainApplicationTests {

    @Test
    void contextLoads() {
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> contentMap = new HashMap<>();
        contentMap.put("role", "system");
        contentMap.put("content", "你是一个编程高手，我会问你很多关于程序员面试题的问题，你需要使用纯 MarkDown 语法返回给我，不能中断");
        messages.add(contentMap);

        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("role", "user");
        messageMap.put("content", "什么是 Java 的反射？");
        messages.add(messageMap);

        Map<String, Object> map = new HashMap<>();
        map.put("model", "deepseek-chat");
        map.put("messages", messages);
        map.put("stream", true);
        HttpResponse response = HttpRequest.post("https://api.deepseek.com/chat/completions")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer sk-909ea9bec76043e9800bc23cfeae8cc0")
                .keepAlive(true)
                .body(JSONUtil.toJsonStr(map)).executeAsync(); // 关键点：使用 executeAsync 让请求异步执行

        // 获取响应流
        InputStream inputStream = response.bodyStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            // 逐行读取流式响应
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(response.body());

    }

}
