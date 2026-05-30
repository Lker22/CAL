# CLAUDE.md — MediCraft 项目开发记录

## 项目概况

- **项目名**: MediCraft 医学教育智能学习平台
- **架构**: 前后端分离（单体后端 + Vue3 前端）
- **后端**: Spring Boot 3.3.5 / Java 17 / Spring AI 1.0.0-M6 / MyBatis-Plus 3.5.11 / MySQL / Redis
- **前端**: Vue 3 + Vite + Element Plus + Pinia
- **AI**: 阿里百炼 DashScope（OpenAI 兼容接口，模型 qwen3.6-35b-a3b）

## 项目路径

- 后端根目录: `C:\Users\29464\Desktop\shortcut file\MediCraft\MediCraft\educate-server`
- 前端根目录: `C:\Users\29464\Desktop\shortcut file\Media-edu\MediCraft-web`
- 实体类: `MediCraft\educate-pojo\src\main\java\com\education\entity`
- 通用模块: `MediCraft\educate-common\src\main\java\com\education`

## 核心约定

- 用户ID统一通过 `BaseContext.getCurrentId()` 获取（ThreadLocal），不用 `SecurityUtils`
- `Result` 类字段为 `code` / `msg` / `data`（不是 `message`）
- 前端 `request.js` baseURL 为 `/api`，Vite 代理去掉 `/api` 前缀转发到 `http://127.0.0.1:8080`
- MyBatis-Plus `@TableName` 注解指定表名，`@TableLogic` 实现逻辑删除
- `@Async` 自调用不生效（Spring AOP 代理限制），必须跨 Bean 调用

---

## 2026-05-29 修改记录

### 一、学习效果评估模块（从零搭建）

**报错现象**: 评估模块5个Java文件全部编译失败，无法启动项目。

**具体编译错误**:
1. `AssessmentController.java`: `cannot find symbol — SecurityUtils.getUserId()` — 项目中不存在 `SecurityUtils` 类
2. `AssessmentController.java`: `LearningEvaluateService is not imported` — 接口文件不存在
3. `LearningEvaluateServiceImpl.java`: `cannot find symbol — LearningBehaviorService` — 接口文件不存在
4. `LearningEvaluateServiceImpl.java`: `cannot find symbol — QuestionAnswerRecordService` — 接口文件不存在
5. `LearningEvaluateServiceImpl.java`: `cannot find symbol — LearningPathStepService` — 接口文件不存在
6. `LearningEvaluateServiceImpl.java`: `method generateEvaluation() not found in AssessmentAIAgent` — 方法名不匹配，`AssessmentAIAgent` 只有 `generateAssessmentReport()`，没有 `generateEvaluation()`
7. `LearningEvaluateServiceImpl.java`: `cannot resolve method setPathId()` — `LearningEvaluate` 实体类没有 `pathId` 字段
8. `LearningEvaluateServiceImpl.java`: `method getByUserId(Long) not found in StudentProfileService` — 实际接口定义的是 `getByUserId()` 无参方法
9. `LearningEvaluateServiceImpl.java`: `method updateByEvaluate() not found in StudentProfileService` — 方法不存在
10. `AssessmentScheduleTask.java`: `method listActiveUserIds(int) not found in StudentProfileService` — 方法不存在
11. `AssessmentScheduleTask.java`: `method generateAssessmentReport(Long,Long,List) not found` — 接口方法不存在
12. `AssessmentScheduleTask.java`: `method listRecentEvaluates(LocalDateTime) not found` — 接口方法不存在
13. `AssessmentScheduleTask.java`: `method updateProfileByAssessment() not found in StudentProfileService` — 方法不存在

**修改方案**: 新建 11 个文件 + 重写 4 个文件

**新建文件** (11个):
| 文件路径 | 说明 |
|----------|------|
| `educate-server/.../outcome/mapper/LearningBehaviorMapper.java` | 学习行为 Mapper 接口，继承 `BaseMapper<LearningBehavior>` |
| `educate-server/.../outcome/mapper/QuestionAnswerRecordMapper.java` | 答题记录 Mapper 接口 |
| `educate-server/.../outcome/mapper/LearningPathStepMapper.java` | 路径步骤 Mapper 接口 |
| `educate-server/.../outcome/service/LearningBehaviorService.java` | 接口：`getByUserId(Long)` 查询用户学习行为 |
| `educate-server/.../outcome/service/QuestionAnswerRecordService.java` | 接口：`getByUserId(Long)` 查询用户答题记录 |
| `educate-server/.../outcome/service/LearningPathStepService.java` | 接口：`countCompleted(pathId)` / `countByPath(pathId)` |
| `educate-server/.../outcome/service/LearningEvaluateService.java` | 核心接口：`getReport` / `generateReport` / `getStats` / `getWeakPoints` / `getTrend` / `getLatestReport` |
| `educate-server/.../outcome/service/impl/LearningBehaviorServiceImpl.java` | 实现：用 `LambdaQueryWrapper` 按 userId 查询 |
| `educate-server/.../outcome/service/impl/QuestionAnswerRecordServiceImpl.java` | 实现：用 `LambdaQueryWrapper` 按 userId 查询 |
| `educate-server/.../outcome/service/impl/LearningPathStepServiceImpl.java` | 实现：按 pathId 统计完成/总步骤数 |
| `educate-server/.../config/SchedulingConfig.java` | `@EnableScheduling` 配置（主类已有，冗余但无害） |

**修复文件** (4个):

**`AssessmentController.java`** — 6个REST接口全部重写:
- `SecurityUtils.getUserId()` → `BaseContext.getCurrentId()`
- 添加 `import com.education.outcome.service.LearningEvaluateService`
- `@RequestParam` 全部加 `required = false`，缺参数时后端自动计算默认日期范围（近30天）
- `/assessment/report` 新增 `period` 参数支持（week/month/quarter 自动算日期）
- `/assessment/result` 不传 `reportId` 时返回最新一条
- `/assessment/trend` 的 `period` 默认 `"7d"`
- `/assessment/report/generate` 兼容前端传字符串或列表格式的 `includeModules`

**`AssessmentAIAgent.java`** — AI交互逻辑重写:
- 方法名 `generateAssessmentReport()` → `generateEvaluation()`，与Service调用对齐
- 返回类型 `ChatResponse` → `String`，用 `chatClient.prompt().user(prompt).call().content()` 获取纯文本
- 参数从 5 个复杂对象改为 5 个文本字符串（`studentInfo` / `behaviorData` / `answerData` / `pathInfo` / `includeModules`）
- Prompt 重写：要求 AI 输出4个结构化段落（学习概况、知识点掌握度JSON、薄弱点分析、提升建议）
- `extractImproveSuggest(ChatResponse)` → `extractImproveSuggest(String)`，兼容多种标记（`提升建议：` / `【提升建议】`）
- `parseKnowledgeMastery(ChatResponse)` → `parseKnowledgeMastery(String)`，修复 `jsonEnd != -1` 判断逻辑（`lastIndexOf` 找不到返回 -1，-1+1=0 不等于 -1，原判断无效）

**`LearningEvaluateServiceImpl.java`** — 全面重写:
- `generateReport()`: 调用 `studentProfileService.getByUserId()`（无参）获取画像，用 `formatProfile/formatBehaviors/formatAnswers` 格式化为可读文本，调用 AI 生成报告，解析响应构建 `LearningEvaluate` 实体，保存后同步画像
- `getReport()`: `LambdaQueryWrapper` 按 userId + 日期范围查询，按 `createTime` 降序
- `getStats()`: 从 `learning_behavior` 和 `question_answer_record` 表聚合统计（总时长、行为次数、答题数、正确率、平均答题时长、按行为类型分布）
- `getWeakPoints()`: 综合画像易错点 + 最新评估知识点掌握度（低于60分的标记为薄弱）+ 答题错误率
- `getTrend()`: 按天聚合近7/30/90天的学习时长和答题数据
- `getLatestReport()`: 查询最新一条评估报告
- 私有方法 `syncProfileFromEvaluation()`: 将评估中发现的薄弱知识点同步到画像的 `errorPronePoints`，标记 `updateScene` 为"评估触发"

**`AssessmentScheduleTask.java`** — 定时任务修复:
- `autoGenerateDailyAssessment()`: `listActiveUserIds(7)` → 用 `LambdaQueryWrapper` 查询 `updateTime >= 7天前` 的画像记录作为活跃用户
- `syncAssessmentToProfile()`: `listRecentEvaluates()` → 用 `LambdaQueryWrapper` 查询 `createTime >= 1小时前` 的评估记录；`updateProfileByAssessment()` → 调用 `studentProfileService.saveOrUpdateProfile()` 更新 `updateScene`
- `@Scheduled(fixedRate=3600000)` → `@Scheduled(fixedDelay=3600000, initialDelay=60000)`，避免执行重叠

---

### 二、资源生成 SQL 报错 — 缺少 SET 子句

**报错现象**:
```
java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax;
check the manual that corresponds to your MySQL server version for the right syntax to use near 'WHERE (task_id = ?)'
### SQL: UPDATE resource_generate_task         WHERE  (task_id = ?)
```

**原因**: `ResourceService.java:115` 变量名写错。创建了 `updateWrapper` 做 SET 操作，但 `taskMapper.update()` 传的是旧的 `wrapper`（`LambdaQueryWrapper`，只有 WHERE 没有 SET）:
```java
// 错误代码
LambdaUpdateWrapper<ResourceGenerateTask> updateWrapper = new LambdaUpdateWrapper<>();
updateWrapper.eq(ResourceGenerateTask::getTaskId, taskId)
        .set(ResourceGenerateTask::getResourceId, resource.getId());
taskMapper.update(null, wrapper);  // ← 传了错误的变量
```

**修改文件**: `educate-server/.../resource/service/impl/resourceService.java:115`
- `taskMapper.update(null, wrapper)` → `taskMapper.update(null, updateWrapper)`

---

### 三、前端资源生成失败不提示 / 进度丢失

**报错现象1**: 后端返回失败，前端仍然弹出"资源生成任务已提交"成功提示，用户无法感知失败。

**原因**: `ResourceGenerateView.vue` 的 catch 分支写死了 `ElMessage.success()`:
```js
catch (error) {
  ElMessage.success('资源生成任务已提交')  // 永远成功
  router.push('/generation/progress')
}
```

**修改文件**: `Media-edu/.../views/ai-resource/ResourceGenerateView.vue`
- catch 分支改为读取后端错误信息: `const msg = error?.response?.data?.msg || error?.message || '资源生成失败，请重试'` + `ElMessage.error(msg)`
- handleGenerate 开头增加 agentId 为空时跳转智能体列表的保护

**报错现象2**: 用户在进度页点击其他页面再回来，进度页显示空白，不知道任务还在生成还是已完成。

**原因**: `GenerationProgressView.vue` 依赖 `resourceStore.currentTask`，但这个 ref 在页面刷新或 Pinia 重置后变成 null。同时 store 里 `getGenerationProgress` 用 `t.id` 匹配，但后端返回的任务标识字段是 `taskId`（UUID字符串），不是数据库主键 `id`。

**修改文件**: `Media-edu/.../stores/resource.js`
- 新增 `localStorage` 持久化：`loadTaskFromStorage()` / `saveTaskToStorage()` 两个函数
- `generateResource()` 成功后调用 `saveTaskToStorage(response.data)` 保存到 localStorage
- `getGenerationProgress()` 同步更新 localStorage
- `clearCurrentTask()` 同时清除 localStorage
- 初始化 `currentTask` 从 localStorage 恢复: `ref(loadTaskFromStorage())`
- 任务匹配: `t.id === taskId` → `t.taskId === taskId`

**修改文件**: `Media-edu/.../views/ai-resource/GenerationProgressView.vue`
- 全面重写：无任务时显示空状态引导（"去生成资源" / "查看已有资源"）
- 新增失败状态：红色渐变图标 + 红色错误提示框 + "重新生成"按钮
- 移除前端假进度模拟逻辑，只依赖后端轮询
- `pollFromBackend` 从 `resourceStore.currentTask` 读取 taskId，每次轮询同步更新 UI

---

### 四、前端永远显示"请求失败" — 拦截器吞掉后端错误信息

**报错现象**: 后端返回 `Result.fail(500, "任务创建失败：xxx")`，前端弹窗显示"请求失败"，看不到具体原因。

**原因** (两处bug):
1. 后端 `Result` 类的字段名是 `msg`，但 `request.js` 响应拦截器读的是 `res.message`（undefined），所以 `ElMessage.error(res.message || '请求失败')` 总是走到 `"请求失败"`
2. 拦截器 `return Promise.reject(new Error(res.message || '请求失败'))` 创建了新的 Error 对象，丢失了 `response` 属性。业务代码 `error?.response?.data?.msg` 永远是 undefined

**修改文件**: `Media-edu/.../api/request.js`
- 读取字段: `res.message` → `res.msg || res.message || '请求失败'`
- 附加 response: `return Promise.reject(new Error(...))` → 创建 err 对象后 `err.response = { data: res }` 再 reject

---

### 五、前端请求超时 30s — `@Async` 自调用不生效

**报错现象**:
```
[API] 请求失败: timeout of 30000ms exceeded
[Resource] 生成资源失败: timeout of 30000ms exceeded
```

**原因**: `ResourceService.createGenerateTask()` 和 `executeGenerateTask()` 在同一个类中。`createGenerateTask` 用 `this.executeGenerateTask(taskId)` 调用，Spring AOP 代理无法拦截类内部自调用（`this.xxx()`），导致 `@Async` 不生效，`executeGenerateTask` 变成同步执行。AI 生成需要几十秒，前端 30 秒超时。

**修改方案**: 新建 1 个文件 + 修改 1 个文件

**新建文件**: `educate-server/.../resource/service/impl/ResourceAsyncExecutor.java`
- 独立的 `@Component` Bean，包含原 `ResourceService.executeGenerateTask()` 的全部逻辑
- 方法加 `@Async` + `@Transactional(rollbackFor = Exception.class)`
- 异常处理: 失败时更新任务状态为 `failed`，不向上抛异常（避免异步线程异常丢失）

**修改文件**: `educate-server/.../resource/service/impl/ResourceService.java`
- 删除 `executeGenerateTask()` 方法和 `updateTaskStatus()` 私有方法
- 删除不再需要的 `resourceMapper`、`agentStrategyFactory` 依赖注入
- 新增注入 `ResourceAsyncExecutor resourceAsyncExecutor`
- `createGenerateTask()` 中 `this.executeGenerateTask(taskId)` → `resourceAsyncExecutor.executeGenerateTask(taskId)`

---

### 六、智能体策略缺失 — demand 和 multimodal 没有策略实现

**报错现象**:
```
java.lang.RuntimeException: 资源生成失败：未找到策略：demand
Caused by: java.lang.RuntimeException: 未找到策略：demand
```

**原因**: 项目使用策略模式，`AgentStrategyFactory` 根据 `agentRole` 分发到对应 `AgentGenerateStrategy` 实现。6 个智能体角色只有 4 个有实现：
- ✅ `document` → `DocumentAgentStrategy`
- ✅ `mind` → `MindAgentStrategy`
- ✅ `question` → `QuestionAgentStrategy`
- ✅ `case` → `CaseAgentStrategy`
- ❌ `demand` → 无实现
- ❌ `multimodal` → 无实现（`VideoAgent` 继承 `BaseAgent` 而非实现 `AgentGenerateStrategy`，不会被工厂扫描到）

**修改方案**: 新建 2 个文件 + 修改 1 个文件

**新建文件**:
- `educate-server/.../resource/agent/DemandAgentStrategy.java` — `getSupportRole()` 返回 `"demand"`，Prompt 要求 AI 输出学习目标拆解、知识点清单、推荐学习路径、前置知识、预计时长、推荐资源类型，生成结果存为 `document` 类型
- `educate-server/.../resource/agent/MultimodalAgentStrategy.java` — `getSupportRole()` 返回 `"multimodal"`，Prompt 要求 AI 输出教学视频脚本（视频标题、分镜脚本≥5个含画面描述和旁白、知识点图解、总结与思考题），生成结果存为 `document` 类型（系统没有独立的"视频"资源类型）

**修改文件**: `educate-server/.../resource/agent/AgentStrategyFactory.java`
- `init()` 方法加 `@PostConstruct` 注解，应用启动时自动初始化策略映射（原来无注解，靠首次调用 `getStrategy()` 时懒加载）

---

### 七、StringTemplate 语法冲突 — JSON 花括号被当变量

**报错现象**:
```
java.lang.IllegalArgumentException: The template string is not valid.
Caused by: org.stringtemplate.v4.compiler.STException
  1:104: mismatched input ',' expecting LPAREN
```

**原因**: `QuestionAgentStrategy` 的默认 Prompt 模板包含 JSON 数组示例:
```java
"格式为JSON数组：[{\"id\":1,\"title\":\"题干\",\"options\":[],\"answer\":\"答案\",\"analysis\":\"解析\"}]。"
```
Spring AI 的 `PromptTemplate` 底层使用 StringTemplate 4 (ST4) 引擎，`{` 和 `}` 是模板变量语法。JSON 中的 `{` 被 ST4 解析为变量开始标记，导致解析失败。

**修改文件**: `educate-server/.../resource/agent/QuestionAgentStrategy.java`
- JSON 示例中的花括号用双括号转义:
```
// 修改前: [{\"id\":1,...}]
// 修改后: [{{\"id\":1,...}}]
```

---

### 八、`el-progress` 颜色报错 — `color.map is not a function`

**报错现象**:
```
TypeError: color.map is not a function
  at getColors (progress.vue_vue_type_script_setup_true_lang.mjs:93)
  at getCurrentColor (progress.vue_vue_type_script_setup_true_lang.mjs:106)
```

**原因**: 当前 Element Plus 版本的 `el-progress` 组件的 `color` 属性内部调用 `color.map()`，期望传入数组或函数。项目中多处直接传字符串或三元表达式（结果为字符串），导致 `.map()` 报错。

**修改文件** (5个，共8处):

1. `Media-edu/.../components/common/ProgressTracker.vue:56`
   - `:color="progressColor"` → `:color="() => progressColor"`

2. `Media-edu/.../views/learning-assessment/LearningStatsView.vue:131`
   - `:color="index === 0 ? '#409EFF' : ..."` → `:color="() => ['#409EFF','#67C23A','#E6A23C','#909399','#B37FEB'][index] || '#909399'"`

3. `Media-edu/.../views/learning-assessment/LearningStatsView.vue:160`
   - `:color="subject.progress >= 80 ? '#67C23A' : ..."` → `:color="(p) => p >= 80 ? '#67C23A' : p >= 50 ? '#409EFF' : '#E6A23C'"`

4. `Media-edu/.../views/learning-assessment/AssessmentReportView.vue:155`
   - `:color="dim.score >= 80 ? '#67C23A' : ..."` → `:color="(p) => p >= 80 ? '#67C23A' : p >= 60 ? '#409EFF' : '#F56C6C'"`

5. `Media-edu/.../views/learning-assessment/AssessmentResultView.vue:77`
   - `:color="{ '0%': '#409EFF', '100%': '#67C23A' }"` → `:color="() => '#67C23A'"`

6. `Media-edu/.../views/learning-assessment/AssessmentResultView.vue:135`
   - `:color="point.mastery < 60 ? '#F56C6C' : '#E6A23C'"` → `:color="(p) => p < 60 ? '#F56C6C' : '#E6A23C'"`

7. `Media-edu/.../views/user/ProfileDisplayView.vue:95`
   - `:color="completionRate === 100 ? '#67C23A' : '#409EFF'"` → `:color="() => completionRate === 100 ? '#67C23A' : '#409EFF'"`

---

## 常见坑备忘

1. **Spring `@Async` 自调用失效**: 同一类内 `this.asyncMethod()` 不走代理，`@Async` 不生效。必须抽到独立 Bean 通过注入调用
2. **MyBatis-Plus `LambdaUpdateWrapper`**: 必须有 `.set()` 调用，否则生成的 SQL 是 `UPDATE table WHERE ...` 缺少 SET 子句
3. **Spring AI StringTemplate `{ }` 冲突**: `PromptTemplate` 底层用 ST4 引擎，模板中字面量花括号需双括号转义 `{{` `}}`（JSON 示例、正则表达式等含花括号的内容）
4. **Element Plus `el-progress` color**: 当前版本必须传函数 `(percentage) => string`，不能传字符串或三元表达式
5. **`Result` 类字段名**: 是 `msg` 不是 `message`，前端 axios 拦截器读 `res.msg`
6. **Axios 拦截器 Error 对象**: `Promise.reject(new Error(msg))` 创建新 Error 丢失 `response` 属性，需要手动 `err.response = { data: res }` 附加
7. **`StudentProfileService.getByUserId()`**: 无参方法，内部用 `BaseContext.getCurrentId()` 取用户ID，不能传 userId
8. **前后端字段名不一致**: 前端 `PROFILE_DIMENSIONS` 使用 `learningGoals / learningRhythm / errorPoints`，后端 `StudentProfile` 实体字段为 `learningGoal / learningPace / errorPronePoints`，`JSON.parseObject` 按字段名映射，名不匹配则值为 null
9. **AI Prompt 字段名必须与实体一致**: `extractProfileFromChat` 的 prompt 让 AI 返回 JSON，字段名必须和 `StudentProfile` 实体字段完全一致（`learningGoal` 不是 `learningGoals`，`learningPace` 不是 `learningRhythm`），否则 `parseObject` 映射失败
10. **`spring-boot-maven-plugin` 对非应用模块的影响**: 继承 `spring-boot-starter-parent` 的多模块项目中，非应用模块（common/pojo 等纯 JAR 库）需显式跳过 repackage，否则 `install` 阶段报 artifact 未赋值
11. **`target/classes` 残留 class 文件**: 移动 Java 类到不同包后，旧包路径下的编译产物不会自动清理，`mvn compile` 不会删除它们，需 `mvn clean compile` 清除

---

## 2026-05-30 修改记录

### 一、画像构建 — 字段名不匹配导致数据丢失

**报错现象**: 用户在对话式画像智能体中提供了学习节奏、学习目标等信息，但画像展示页仍显示"未完善"。

**原因**: 前端和后端字段名不一致，数据无法正确映射：

| 前端 PROFILE_DIMENSIONS key | 后端 StudentProfile 实体字段 |
|---|---|
| `knowledgeBase` | `knowledgeBase` ✓ |
| `cognitiveStyle` | `cognitiveStyle` ✓ |
| `learningGoals` | `learningGoal` ✗ (少个s) |
| `learningRhythm` | `learningPace` ✗ (完全不同) |
| `errorPoints` | `errorPronePoints` ✗ (完全不同) |
| `resourcePreference` | `resourcePreference` ✓ |

`AIChatServiceImpl.extractProfileFromChat()` 的 AI Prompt 用了正确的实体字段名（`learningGoal`、`learningPace`），AI 返回的 JSON 能正确 `parseObject` 保存到数据库。但前端 `ProfileDisplayView` 和 `ProfileQueryView` 用 `profile.learningGoals` / `profile.learningRhythm` 读取，数据库返回的是 `learningGoal` / `learningPace`，匹配不上，永远显示 null。

另外 `ProfileDisplayView` 和 `ProfileQueryView` 在 `catch` 中填充了硬编码假数据，掩盖了真实问题——用户看到的是假数据而非真实画像。

**修改文件** (4个):

**`Media-edu/.../stores/profile.js`**:
- `updateDimensions()`: 增加前后端字段名双向映射，`learningGoals` 优先读 `learningGoal`，`learningRhythm` 优先读 `learningPace`，`errorPoints` 优先读 `errorPronePoints`
- `updateProfile()`: 发送前将前端字段名映射为后端实体字段名（`learningGoals` → `learningGoal`，`learningRhythm` → `learningPace`，`errorPoints` → `errorPronePoints`），避免后端 `JSON.parseObject` 无法识别字段

**`Media-edu/.../views/user/ProfileDisplayView.vue`**:
- `fieldMap` 修正为正确的后端字段名：`learningGoals` → `learningGoal`，`learningRhythm` → `learningPace`，`errorPoints` → `errorPronePoints`
- 移除 `loadProfile` 的 catch 假数据 fallback，失败时保持 null 显示"暂无数据"

**`Media-edu/.../views/user/ProfileQueryView.vue`**:
- `fieldMap` 同上修正
- 新增 `getFieldValue(profile, key)` 函数，兼容读取前后端字段名
- 移除 `loadProfile` 的 catch 假数据 fallback
- `startEdit()` 用 `getFieldValue` 读取，正确填充编辑表单
- 模板显示用 `getFieldValue` 替代直接读取 `profile[dim.key]`

---

### 二、资源生成 — 前端进度条 status 类型比较失败

**报错现象**: 后端任务实际已完成（status="success"），但前端进度页一直卡在"生成中"不跳转完成状态。

**原因**: `GenerationProgressView.vue` 的 `pollFromBackend` 中 status 比较只处理了 `=== 'success'` 和 `=== 'failed'` 两种字符串。如果后端返回其他格式（如 `"completed"`、数字 `2`/`3`），前端无法识别，任务永远卡在处理中状态。

**修改文件**: `Media-edu/.../views/ai-resource/GenerationProgressView.vue`
- status 比较兼容多种格式：成功 `=== 2 || === 'success' || === 'completed'`，失败 `=== 3 || === 'failed'`

---

### 三、评估报告 — 硬编码假数据不渲染后端结果

**报错现象**: 点击"生成新报告"后，页面仍然显示固定的示例数据（综合评分85、学习时长1260分钟等），看不到真实评估数据。

**原因**: `AssessmentReportView.vue` 和 `AssessmentResultView.vue` 的报告数据用 `ref()` 定义了硬编码示例数据。即使 `assessmentStore.generateReport()` / `getAssessmentResult()` 从后端成功获取数据并存入 store，页面模板仍然渲染本地 `ref` 的假数据，从未使用 store 中的真实数据。

**修改文件** (2个):

**`Media-edu/.../views/learning-assessment/AssessmentReportView.vue`**:
- `report` 从 `ref(假数据对象)` 改为 `computed()`，从 `assessmentStore.assessmentReport` 读取
- 没有后端数据时显示空结构（评分0、空数组），不再显示假数据
- `generateReport()` 成功后 store 自动更新，computed 重新渲染页面

**`Media-edu/.../views/learning-assessment/AssessmentResultView.vue`**:
- `result` 从 `ref(假数据对象)` 改为 `computed()`，从 `assessmentStore.assessmentResult` 读取
- `radarData` 从 `ref(固定数组)` 改为 `computed()`，优先从 `assessmentResult.radarData` 读取，其次从 `learningStats` 构建
- `onMounted` 同时调用 `getAssessmentResult()` 和 `getLearningStats()` 获取完整数据

---

### 四、Maven 构建失败 — `educate-common` 的 `spring-boot-maven-plugin` 冲突

**报错现象**:
```
[ERROR] The packaging plugin for project educate-common did not assign a file to the build artifact
```

**原因**: 父 POM 继承 `spring-boot-starter-parent`，默认将 `spring-boot-maven-plugin` 绑定到所有子模块。`educate-common` 和 `educate-pojo` 是普通 JAR 库（不可执行应用），`spring-boot-maven-plugin:repackage` 在这些模块上执行时替换 artifact 文件，导致 `maven-install-plugin` 找不到原始 artifact。

**修改文件** (2个):
- `educate-common/pom.xml` — 添加 `spring-boot-maven-plugin` 并设置 `<skip>true</skip>` 跳过 repackage
- `educate-pojo/pom.xml` — 同上

---

### 五、Bean 名冲突 — 旧编译产物残留

**报错现象**:
```
ConflictingBeanDefinitionException: Annotation-specified bean name 'learningEvaluateServiceImpl'
conflicts with existing, non-compatible bean definition of same name and class
```

**原因**: `target/classes/com/education/outcome/service/LearningEvaluateServiceImpl.class` 是旧代码在 `service` 包下时编译的残留文件。当前代码已移到 `service.impl` 包，但旧的 class 文件仍在 target 目录中，Spring 扫描时发现两个同名 Bean。

**修复**: 执行 `mvn clean compile` 清除 target 目录后重新编译。
