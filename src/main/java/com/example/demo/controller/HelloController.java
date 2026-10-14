package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 控制层（Controller）：只负责"接收请求 → 交给 Service → 返回结果"，不写复杂业务逻辑。
 *
 * @RestController = @Controller + @ResponseBody
 *   意思是：这个类的方法返回值直接写回给浏览器（对象会自动变成 JSON）。
 * @RequestMapping("/hello") 给这个类里所有接口加上统一前缀 /hello。
 */
@RestController
@RequestMapping("/hello")
public class HelloController {

    /**
     * GET http://localhost:8080/hello
     * 返回值是 Map，Spring 会自动转成 JSON：{"message":"Hello, Spring Boot!","time":...}
     */
    @PostMapping
    public Map<String, Object> hello() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Hello, GYX!");
        result.put("time", System.currentTimeMillis());
        return result;
    }

    /**
     * GET http://localhost:8080/hello/张三
     *
     * @PathVariable 取的是"路径里的一段"（/hello/{name} 里的 name）
     * @RequestParam 取的是"问号后面的参数"（?times=3），没传就用默认值 0
     */
    @GetMapping("/{name}")
    public String helloName(@PathVariable String name,
                            @RequestParam(defaultValue = "0") int times) {
        return "你好，" + name + "！你传的 times = " + times;
    }
}
