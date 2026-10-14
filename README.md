# 最小 Spring Boot 后端示例

一个能跑起来、能看懂、能改的小后端：1 个打招呼接口 + 一套用户增删改查接口。
不连数据库，数据放在内存里，所以不用装 MySQL 就能直接运行。

## 一、先搞清这几个概念

| 概念 | 一句话解释 |
| --- | --- |
| 后端程序 | 一个一直开着的程序，监听某个端口，等别人来"提问"，然后给出答案 |
| HTTP 请求 | 浏览器 / App 发来的"提问"，由三部分组成：请求方法 + 网址 + 数据 |
| 请求方法 | GET 拿数据、POST 新增、PUT 修改、DELETE 删除，合称 CRUD |
| 接口 / API | 一个能回答问题的具体地址，例如 `GET /users/1` 就是"查 id 为 1 的用户" |
| REST | 一套给接口起名字的约定：用网址表示资源，用方法表示动作 |
| JSON | 前后端之间传数据的通用格式：`{"name":"小明","age":20}` |
| Spring | 一个 Java 框架，核心作用是帮你创建和管理对象 |
| Spring Boot | 套在 Spring 外面的省心外壳：自动配置 + 内嵌 Tomcat，不用写 XML 就能跑 |
| Bean | 交给 Spring 管理的对象；加了 `@Service`、`@RestController` 的类启动时会被自动创建 |
| IoC 控制反转 | 以前是你自己 `new` 对象，现在是 Spring 帮你 new 好并保管 |
| DI 依赖注入 | Spring 把保管好的对象塞进需要它的地方，见 `UserController` 的构造方法 |
| Starter 依赖 | 一包打包票的依赖；`spring-boot-starter-web` 里含 Spring MVC、内嵌 Tomcat、JSON 工具 |
| Maven | 负责下载 jar 包、编译、打包、运行的工具，靠 `pom.xml` 干活 |
| 分层 | Controller 收请求、Service 干业务、Repository 碰数据库，各管一段 |

## 二、一次请求是怎么走完全程的

```
浏览器 / Apifox / 前端页面
        |
        |  GET http://localhost:8080/users/1
        v
(1) 内嵌 Tomcat           负责接电话，把 HTTP 请求交给 Spring
        v
(2) DispatcherServlet     查路由表：/users/1 归哪个方法管
        v
(3) UserController.get()  把网址里的 1 取出来（@PathVariable）
        v
(4) UserService.findById()真正干活的地方，以后换成查数据库
        v
(5) 返回 User 对象        Spring 自动把它转成 JSON
        v
浏览器收到：{"id":1,"name":"小明","age":20}
```

## 三、代码地图

```
springboot-demo/
├── pom.xml                                  依赖清单：要什么包、怎么打包
├── run.cmd                                  启动脚本（借用 IDEA 自带的 Maven）
└── src
    ├── main/java/com/example/demo
    │   ├── DemoApplication.java             程序入口，@SpringBootApplication
    │   ├── controller/HelloController.java  最简单的接口：路径参数、查询参数
    │   ├── controller/UserController.java   用户增删改查，演示依赖注入
    │   ├── model/User.java                  数据长什么样，会自动转成 JSON
    │   └── service/UserService.java         业务逻辑，内存版"数据库"
    ├── main/resources/application.properties 配置：端口等
    └── test/java/com/example/demo/DemoApplicationTests.java  自动化测试
```

## 四、怎么跑起来

方式 A：用 IntelliJ IDEA（推荐）

1. `File` → `Open`，选中本项目的 `pom.xml`，选择 `Open as Project`；
2. 等右下角 Maven 把依赖下载完（第一次几分钟）；
3. 打开 `DemoApplication.java`，点 `main` 方法左边的绿色三角。

方式 B：命令行

```cmd
cd outputs\springboot-demo
run.cmd spring-boot:run
```

日志里出现 `Tomcat started on port(s): 8080` 和 `Started DemoApplication` 就是成功了。
按 `Ctrl + C` 停止。也可以先 `run.cmd clean package` 打包，再 `java -jar target\springboot-demo-0.0.1-SNAPSHOT.jar` 启动。

## 五、怎么测试接口

浏览器只方便测 GET；新增、修改、删除建议用 Apifox / Postman，或者下面的命令行。

| 接口 | 作用 | 浏览器能测吗 |
| --- | --- | --- |
| `GET /hello` | 返回一段 JSON 问候 | 能 |
| `GET /hello/Tom?times=3` | 演示路径参数和查询参数 | 能 |
| `GET /users` | 用户列表 | 能 |
| `GET /users/1` | 查单个用户 | 能 |
| `POST /users` | 新增用户，请求体传 JSON | 不能，用 Apifox |
| `PUT /users/1` | 修改用户 | 不能，用 Apifox |
| `DELETE /users/1` | 删除用户 | 不能，用 Apifox |

命令行示例（Windows PowerShell）：

```powershell
# 查列表
Invoke-RestMethod http://localhost:8080/users

# 新增用户（注意：中文要用 UTF-8 字节发送，否则会乱码）
$body = [Text.Encoding]::UTF8.GetBytes('{"name":"小刚","age":25}')
Invoke-RestMethod -Method Post -Uri http://localhost:8080/users -ContentType 'application/json; charset=utf-8' -Body $body

# 修改用户
$body = [Text.Encoding]::UTF8.GetBytes('{"name":"小明改","age":21}')
Invoke-RestMethod -Method Put -Uri http://localhost:8080/users/1 -ContentType 'application/json; charset=utf-8' -Body $body

# 删除用户
Invoke-RestMethod -Method Delete -Uri http://localhost:8080/users/1
```

## 六、新手最容易踩的 5 个坑

1. 接口 404：`DemoApplication` 是不是放在最外层包？`@GetMapping` 路径有没有写错？
2. 端口被占用：报 `Port 8080 was already in use`，改 `application.properties` 里的 `server.port`。
3. 改了代码没生效：IDEA 里要重新运行；命令行要 `Ctrl + C` 后重新启动。
4. 字段不显示：`User` 忘了写 getter/setter，JSON 就转不出来。
5. POST 报 415：Apifox 里要选 `Body` → `JSON`，让它带上 `Content-Type: application/json`。

## 七、练手任务（按顺序做）

完整的分关卡练习题（含接口设计、验收命令、卡住了往哪想）见同目录下的 [EXERCISES.md](EXERCISES.md)。

1. 把端口改成 `9090`，重新启动，用新地址访问 `/hello`。
2. 给 `User` 加一个 `email` 字段，观察新增接口不传它时返回什么。
3. 加一个"按名字模糊查询"的接口 `GET /users/search?name=小`，提示：用 `@RequestParam String name` 再遍历过滤。
4. 加一个 `GET /users/count` 接口，返回用户总数。
5. 把测试里的断言故意改错，运行 `run.cmd test`，看看失败信息怎么报。
6. 进阶：把 `UserService` 换成 Spring Data JPA + MySQL（Controller 一行都不用改）。

## 八、下一步学什么

1. 接数据库：`spring-boot-starter-data-jpa` + MySQL，把 `UserService` 里的 Map 换成 `JpaRepository`。
2. 参数校验：`spring-boot-starter-validation` + `@Valid` + `@NotBlank`，让非法参数自动返回 400。
3. 统一返回格式：用 `@RestControllerAdvice` 包一个 `{code, message, data}` 结构。
4. 登录鉴权：Spring Security 或 JWT。
5. 前后端联调：写个简单 HTML 页面用 `fetch` 调用这些接口；跨域问题用 `@CrossOrigin` 解决。
