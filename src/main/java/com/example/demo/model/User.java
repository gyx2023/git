package com.example.demo.model;

/**
 * 实体类（也叫 POJO / Model / DTO）：
 * 它就是一份"数据长什么样"的说明书。
 *
 * 后端把对象返回给前端时，Spring 会自动把它转换成 JSON，例如：
 *   { "id": 1, "name": "小明", "age": 20 }
 * 前端传来 JSON 时，Spring 也会自动把它变成这个类的对象。
 *
 * 之所以必须写 getter/setter：JSON 转换工具是通过 getXxx/setXxx 方法读写字段的。
 */
public class User {

    private Long id;
    private String name;
    private Integer age;
    private String email;

    public User() {
    }

    public User(Long id, String name, Integer age,String email) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', age=" + age + ", email='" + email + "'" + '}';
    }
}
