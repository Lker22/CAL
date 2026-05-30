# CLAUDE.md — 学习路径模块开发备忘

## 项目概述

MediCraft 是一个 AI 学习辅助系统，本目录 `dm/` 包含学习路径规划模块的后端代码，可直接复制到项目中使用。

## 项目技术栈

- 后端：Spring Boot 3.3.5 + MyBatis-Plus 3.5.11 + Spring AI (DashScope) + MySQL + Redis
- 前端：Vue 3 (`<script setup>`) + Element Plus + Pinia + Axios
- Java 17，构建工具 Maven 多模块

## 模块结构

```
dm/
├── educate-pojo/.../education/path/vo/     ← 7个请求/响应VO
├── educate-server/.../education/path/
│   ├── controller/                          ← 1个控制器，8个接口
│   ├── mapper/                              ← 4个Mapper（MyBatis-Plus BaseMapper）
│   └── service/ + service/impl/             ← Service接口 + 实现
└── sql/                                     ← 建表SQL（参考用，以MediCraft.sql为准）
```

## 关键约束

### 数据库状态值（不可改）
- `learning_path.status`：`doing` / `finish`（不是 pending/completed）
- `learning_path_step.finish_status`：`tinyint` 的 `0`（未完成）/ `1`（已完成）（不是三态）
- 前端需要的 `pending / inProgress / completed` 三态由 VO 层映射推导

### 状态映射规则（在 `LearningPathServiceImpl.buildPathDetailVO()` 中）
```
数据库 doing  +  有已完成步骤  →  前端 inProgress
数据库 doing  +  无已完成步骤  →  前端 pending
数据库 finish  →  前端 completed

步骤 finish_status=1  →  completed
步骤 finish_status=0 + 排序第一个未完成  →  inProgress
步骤 finish_status=0 + 非第一个  →  pending
```

### 前后端字段映射
后端 `pathName` → 前端 `title`（VO中用 `@JsonProperty("title")` 别名）
后端 `totalStep` → 前端 `totalSteps`（VO中用 `@JsonProperty("totalSteps")` 别名）
后端 `createTime` → 前端 `createdAt`（VO中用 `@JsonProperty("createdAt")` 别名）

## 前端数据流

```
前端 API 调用 → Axios拦截器(return response.data 即Result对象)
→ Store(response.data.records 取分页数组)
→ computed 派生 → template 渲染
```

**易错点**：Axios 拦截器返回的是 `response.data`（后端 Result 对象），不是原始 axios response。分页数据在 `response.data.records`，不是 `response.data`。

## 代码规范

- 实体：`@Data` + `@Accessors(chain=true)` + `@TableName` + `@TableId(IdType.AUTO)` + `@TableLogic`
- 控制器：`@RestController` + `@RequestMapping` + 返回 `Result<T>`
- 服务：接口继承 `IService<Entity>`，实现 `extends ServiceImpl<Mapper, Entity>`
- DI：使用 `@Resource`（或构造器注入）
- 事务：`@Transactional(rollbackFor = Exception.class)`
- 用户上下文：`BaseContext.getCurrentId()` 获取 ThreadLocal 中的 userId

## 路径调整接口参数

前端 `params` 字段为 JSON 字符串，不同调整类型结构不同：
- `extend/compress`：`{ duration: "7天" }`
- `reorder`：`{ steps: [{ id, sort }] }` — 直接更新DB，不走AI
- `add`：`{ contentName, contentDetail }` — AI生成新步骤
- `remove`：`{ removeStepIds: [id1, id2] }` — 软删除+重编号

## 已知坑

1. **不要自己生成SQL**：以 `MediCraft.sql` 为准，我方SQL只做参考
2. **前端 store 取分页数据**：`response.data.records`，不是 `response.data`
3. **PathListView 模板**：用 store 的 `paths`，不要用 `mockPaths`
4. **Vue 双 script 块**：`<script setup>` 和 `<script>` 作用域隔离，computed 必须在 setup 中定义
5. **包名**：`com.education.path`（不要改成 study 或其他）
6. **页面不要依赖URL参数加载数据**：从菜单导航进入时 query 参数为空，必须有 fallback（默认选中第一个、dropdown 选择器）
7. **错误处理要透传后端信息**：catch 块不能只显示固定文案，要读取 `error?.response?.data?.msg`
8. **多表操作后重新请求数据**：打卡等操作涉及多表更新，前端不要手动拼凑状态，应重新请求后端获取完整数据
9. **v-for 遍历后端数据必须校验**：`Array.isArray(response.data) ? response.data : []`，防止 null/对象导致崩溃
10. **400 错误信息在 error.response.data 中**：Axios 的 `response` 拦截器只对 2xx 生效，400/500 走 `error` 拦截器。后端验证错误的具体信息在 `error.response.data.message`，不在 `error.message`
11. **Result 类的字段是 `msg` 不是 `message`**：后端 `Result` 类用 `msg` 字段，Axios 响应拦截器中必须读 `res.msg`，不能读 `res.message`。`message` 是 HTTP 协议层字段，`msg` 是自定义 Result 类字段，两者不同
