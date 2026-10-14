package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务层（Service）：真正干活的地方——判断参数对不对、计算、查数据。
 *
 * @Service 告诉 Spring：请把这个类创建成一个 Bean 并管理起来。
 * 这样 Controller 只要在构造方法里写上参数，Spring 就会自动把对象"塞"进来（依赖注入）。
 *
 * 这里为了让你不装数据库也能跑通，用内存里的 Map 假装是数据库。
 * 缺点很明显：程序一重启，数据就没了。等你学会 Spring Data JPA + MySQL 后，
 * 只要把这里换成 Repository 的调用，Controller 一行都不用改——这就是分层的价值。
 */
@Service
public class UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public UserService() {
        // 预置两条数据，方便你启动后马上就有的看
        create(new User(null, "小明", 20,"xm@test.com"));
        create(new User(null, "小红", 22,"xh@test.com"));
    }

    /** 查询全部用户 */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    /** 按 id 查询一个用户，查不到返回 null */
    public User findById(Long id) {
        return users.get(id);
    }

    /** 新增一个用户：分配 id 并保存 */
    public User create(User user) {
        long id = idGenerator.incrementAndGet();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    /** 修改一个用户：不存在返回 null */
    public User update(Long id, User newUser) {
        User old = users.get(id);
        if (old == null) {
            return null;
        }
        old.setName(newUser.getName());
        old.setAge(newUser.getAge());
        return old;
    }

    /** 删除一个用户：返回是否真的删掉了 */
    public boolean delete(Long id) {
        return users.remove(id) != null;
    }
}
