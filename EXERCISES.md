# CRUD 接口练习手册

从易到难共 7 关，前 4 关只用到你现有的知识（不需要额外装软件）。
每关都写清了：要做什么、接口长什么样、怎么自己验收、卡住了往哪想。

开始前先看一眼你的项目结构，建议照着 `User` 那套"抄一遍再改"，这是最快的学法：

```
model/User.java              -> 数据的形状
service/UserService.java     -> 业务逻辑（内存 Map 假装数据库）
controller/UserController.java -> 接口
```

## 0. 准备：两个小工具

**工具一：PowerShell 里发请求的快捷函数**（解决中文乱码问题，先复制到窗口里执行一次）

```powershell
function Post-Json($url, $json) {
    $body = [Text.Encoding]::UTF8.GetBytes($json)
    Invoke-RestMethod -Method Post -Uri $url -ContentType 'application/json; charset=utf-8' -Body $body
}
function Put-Json($url, $json) {
    $body = [Text.Encoding]::UTF8.GetBytes($json)
    Invoke-RestMethod -Method Put -Uri $url -ContentType 'application/json; charset=utf-8' -Body $body
}
```

**工具二：看清楚状态码**。默认情况下请求失败会抛异常，看不到 404/400 的具体内容，用这个包装一下：

```powershell
function Try-Call($block) {
    try { $block.Invoke() } catch {
        $code = [int]$_.Exception.Response.StatusCode
        Write-Host "HTTP $code" -ForegroundColor Yellow
    }
}
```

## 1. 热身：给 User 加一个字段（约 10 分钟）

**要求**

1. 给 `User` 增加 `email` 字段（String）；
2. 让它能通过 `POST /users` 保存进去，并在 `GET /users` 里返回出来。

**验收**

```powershell
Post-Json http://localhost:8080/users '{"name":"小刚","age":25,"email":"xg@test.com"}'
Invoke-RestMethod http://localhost:8080/users
# 期望：返回的 JSON 里能看到 email 字段
```

**卡住了往哪想**：model 的字段、getter、setter 三样都要加，缺一个 JSON 里就看不到这个字段。

## 2. 核心关：从零写一套图书 CRUD（约 40 分钟）

这是本手册的重点。新建三个文件，照着 User 那套写，但**不要复制粘贴完就不管**，每一行都想想为什么。

**数据模型 Book**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Long | 主键，新增时自动分配 |
| title | String | 书名 |
| author | String | 作者 |
| price | Double | 价格 |
| stock | Integer | 库存数量 |

**要实现的 5 个接口**

| 方法 | 路径 | 作用 | 成功状态码 | 失败情形 |
| --- | --- | --- | --- | --- |
| GET | `/books` | 查全部 | 200 | — |
| GET | `/books/{id}` | 查一个 | 200 | 不存在 → 404 |
| POST | `/books` | 新增 | 201 | — |
| PUT | `/books/{id}` | 修改 | 200 | 不存在 → 404 |
| DELETE | `/books/{id}` | 删除 | 200 | 不存在 → 404 |

**新建文件**

1. `src/main/java/com/example/demo/model/Book.java`
2. `src/main/java/com/example/demo/service/BookService.java`
3. `src/main/java/com/example/demo/controller/BookController.java`

**验收**

```powershell
# 1) 新增两本书
Post-Json http://localhost:8080/books '{"title":"Java 编程思想","author":"Bruce","price":108.0,"stock":5}'
Post-Json http://localhost:8080/books '{"title":"算法导论","author":"Cormen","price":128.0,"stock":3}'

# 2) 查列表，期望 2 条、都带 id
Invoke-RestMethod http://localhost:8080/books

# 3) 查一个，期望拿到 id=1 的那本
Invoke-RestMethod http://localhost:8080/books/1

# 4) 修改，期望价格变成 99，其它字段不变
Put-Json http://localhost:8080/books/1 '{"title":"Java 编程思想","author":"Bruce","price":99.0,"stock":5}'

# 5) 删除，期望返回"删除成功"
Invoke-RestMethod -Method Delete http://localhost:8080/books/2

# 6) 再查 id=2，期望 HTTP 404
Try-Call { Invoke-RestMethod http://localhost:8080/books/2 }
```

**卡住了往哪想**

1. 接口返回 404 但地址没错 → `DemoApplication` 必须在 `com.example.demo` 这个最外层包，新建的类要放在它的子包里；
2. 列表查不出来 → `@Service`、`@RestController` 注解忘了加，或者 Controller 里的构造方法没写 `private final BookService bookService;`；
3. POST 报 415 → 请求头必须是 `application/json`；
4. 想让 Service 像 UserService 一样"不再写重复代码" → 参考 `UserService` 里的 `Map` + `AtomicLong` 写法。

## 3. 查询增强关：筛选、排序、分页（约 40 分钟）

真实项目里"列表"几乎从不会一次返回全部，这关就是练这个。

**接口设计**

```
GET /books?author=Cormen&minPrice=100&maxPrice=200&sort=price,desc&page=0&size=5
```

所有参数都是可选的，不传就是"不筛选/默认排序/第一页"。

**返回结构**（别直接返回数组了，前端需要知道总数）

```json
{
  "content": [ { "id": 2, "title": "算法导论", "price": 128.0 } ],
  "page": 0,
  "size": 5,
  "total": 1
}
```

**要求**

1. `author` 支持模糊匹配（包含即可，忽略大小写）；
2. `minPrice` / `maxPrice` 支持区间过滤；
3. `sort` 形如 `price,desc` 或 `title,asc`，不传按 id 升序；
4. `page` 从 0 开始，`size` 默认 5；越界时返回空数组而不是报错。

**提示**：用 Java 8 的 Stream API 会短很多——
`books.stream().filter(...).sorted(...).skip(page * size).limit(size).collect(...)`；
把参数接进来用 `@RequestParam(required = false) String author` 这种写法。

**验收**：至少能证明这四件事——筛选生效、排序生效、`total` 是筛选后的总数、`page=99` 时 `content` 是空数组。

## 4. 健壮性关：参数校验 + 统一错误返回（约 40 分钟）

现在的接口有个毛病：你发 `{"title":"","price":-5}` 它也会照单全收。这关把它补上。

**第一步：在 `pom.xml` 的 `<dependencies>` 里加**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

**第二步：给 `Book` 加校验注解**

| 字段 | 规则 | 注解 |
| --- | --- | --- |
| title | 不能为空 | `@NotBlank(message = "书名不能为空")` |
| author | 不能为空 | `@NotBlank(message = "作者不能为空")` |
| price | 必须大于 0 | `@Positive(message = "价格必须大于 0")` |
| stock | 不能为负 | `@Min(value = 0, message = "库存不能为负")` |

然后在 Controller 的参数上加 `@Valid`，例如 `public Book create(@Valid @RequestBody Book book)`。

**第三步：统一返回结构 + 统一异常处理**

新建 `common/ApiResponse.java`（`{code, message, data}` 三段式）和
`common/GlobalExceptionHandler.java`，加上 `@RestControllerAdvice` 注解，集中处理三类错误：

| 异常 | 返回状态码 | message |
| --- | --- | --- |
| 参数校验失败（`MethodArgumentNotValidException`） | 400 | 具体哪个字段、为什么不合法 |
| 找不到资源（`ResponseStatusException`） | 404 | 资源不存在 |
| 其它未预料的异常 | 500 | 服务器内部错误 |

**验收**

```powershell
Try-Call { Post-Json http://localhost:8080/books '{"title":"","price":-5}' }
# 期望：HTTP 400，并且能看出是书名空了、价格不是正数
```

**卡住了往哪想**：`@RestControllerAdvice` 里用 `@ExceptionHandler(MethodArgumentNotValidException.class)` 捕获，
异常的 `getBindingResult().getFieldErrors()` 里能拿到每个字段的错误信息。

## 5. 业务规则关：借书与还书（约 30 分钟）

这关练的是**业务逻辑该写在哪**——答案是 Service，不是 Controller。

**接口**

| 方法 | 路径 | 作用 | 特殊要求 |
| --- | --- | --- | --- |
| POST | `/books/{id}/borrow` | 借一本，库存 -1 | 库存为 0 → 409 |
| POST | `/books/{id}/return` | 还一本，库存 +1 | 书不存在 → 404 |

**验收**：库存 3 的书连借 3 次成功，第 4 次返回 409。

**想一想**：为什么"判断库存够不够"必须写在 Service 里，而不是 Controller 里？
（提示：以后换成数据库，两个请求同时来借最后一本，会发生什么？）

## 6. 进阶关：接上数据库，接口一行不改（约 1 小时）

**要求**

1. `pom.xml` 加 `spring-boot-starter-data-jpa` 和 `mysql-connector-j`；
2. `application.properties` 配数据库地址、账号密码；
3. `Book` 加上 `@Entity`、`@Id`、`@GeneratedValue`；
4. 新建 `repository/BookRepository.java`：`public interface BookRepository extends JpaRepository<Book, Long> { }`；
5. 把 `BookService` 里所有 `Map` 操作换成 repository 调用（`findAll()`、`findById()`、`save()`、`deleteById()`）。

**验收**：`BookController` 一个字都不用改，所有老接口照常工作，而且**重启程序后数据还在**。

## 7. 挑战关：订单 + 事务（约 1.5 小时）

**数据模型**：订单 `Order`（id、下单时间、总价、状态）和订单项 `OrderItem`（书、数量、小计），一个订单对多个订单项。

**接口**：`POST /orders`，请求体形如

```json
{ "items": [ { "bookId": 1, "quantity": 2 }, { "bookId": 2, "quantity": 1 } ] }
```

**要求**

1. 校验每本书库存是否足够，不够就整单失败并返回 409；
2. 扣减库存、计算总价、生成订单和订单项，全部成功才提交；
3. 中途有任何异常，整单回滚（`@Transactional`）；
4. `GET /orders/{id}` 能查到订单及其明细。

**验收**：一次下单买 2 本书，库存正确减少、总价正确；其中一本书库存不足时，另一本的库存也**不能**被扣掉。

## 自检清单

写完每一关都对照一遍：

- [ ] 路径符合 REST 约定（用网址表示资源，用方法表示动作），没有 `/getBookById` 这类命名
- [ ] 状态码语义正确：新增 201、查到 200、找不到 404、参数错 400、业务冲突 409
- [ ] 业务逻辑写在 Service，Controller 只做收发
- [ ] 每个接口都用真实请求测过一遍，包括出错的情况
- [ ] 出错时返回的信息能让人看懂是哪里不对
