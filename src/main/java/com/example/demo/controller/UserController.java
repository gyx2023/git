package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 用户接口，标准的 CRUD（增删改查）。
 *
 * 这里演示了 Spring 里最重要的一招：依赖注入（DI）。
 * 构造方法需要一个 UserService，Spring 启动时看到有 @Service 的 UserService，
 * 就自动 new 好塞进来。所以整个项目里你一个 new UserService() 都不用写。
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /** 构造器注入：推荐写法。字段用 final，保证对象一旦创建就不会被偷偷换掉。 */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET http://localhost:8080/users
     * 查询用户列表，返回 JSON 数组。
     */
    @GetMapping
    public List<User> list() {
        return userService.findAll();
    }

    /**
     * GET http://localhost:8080/users/1
     * 查询单个用户；查不到时返回 HTTP 404，而不是一个空的 200。
     */
    @GetMapping("/{id}")
    public User get(@PathVariable Long id,@RequestParam("email") String email) {
        User user = userService.findById(id);
        if (user == null && email == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在：" + id);
        }
        return user;
    }

    /**
     * POST http://localhost:8080/users
     * 请求体是 JSON，例如：{"name":"小刚","age":25}
     *
     * @RequestBody 负责把请求体里的 JSON 自动变成 User 对象。
     * @ResponseStatus(HttpStatus.CREATED) 让返回码是 201（表示"已创建"），这是 REST 的规范做法。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    /**
     * PUT http://localhost:8080/users/1
     * 修改用户，请求体同上。
     */
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.update(id, user);
        if (updated == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在：" + id);
        }
        return updated;
    }

    /**
     * DELETE http://localhost:8080/users/1
     * 删除用户，成功返回 200，没这条数据返回 404。
     */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        if (!userService.delete(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在：" + id);
        }
        return "删除成功";
    }
}
