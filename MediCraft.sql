-- 创建数据库
CREATE DATABASE IF NOT EXISTS MediCraft 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE MediCraft;

-- =============================================
-- 删除所有表（按依赖关系逆序删除）
-- =============================================
DROP TABLE IF EXISTS `resource_generate_task`;
DROP TABLE IF EXISTS `smart_tutor`;
DROP TABLE IF EXISTS `learning_evaluate`;
DROP TABLE IF EXISTS `video_watch_progress`;
DROP TABLE IF EXISTS `question_answer_record`;
DROP TABLE IF EXISTS `learning_behavior`;
DROP TABLE IF EXISTS `learning_path_step_resource`;
DROP TABLE IF EXISTS `learning_path_step`;
DROP TABLE IF EXISTS `learning_path`;
DROP TABLE IF EXISTS `learning_resource`;
DROP TABLE IF EXISTS `ai_agent`;
DROP TABLE IF EXISTS `chat_context`;
DROP TABLE IF EXISTS `student_profile`;
DROP TABLE IF EXISTS `sys_user`;

-- =============================================
-- 1. 用户表（核心根表）
-- =============================================
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码(加密)',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint DEFAULT '1' COMMENT '状态 0禁用 1正常',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 2. 学生学习画像表
-- =============================================
CREATE TABLE `student_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `knowledge_base` varchar(50) DEFAULT NULL COMMENT '知识基础 弱/中/强',
  `cognitive_style` varchar(50) DEFAULT NULL COMMENT '认知风格 视觉型/听觉型/动手型',
  `learning_goal` text DEFAULT NULL COMMENT '学习目标',
  `error_prone_points` json DEFAULT NULL COMMENT '易错点(JSON数组)',
  `learning_pace` varchar(20) DEFAULT NULL COMMENT '学习节奏 慢/中/快',
  `resource_preference` varchar(100) DEFAULT NULL COMMENT '资源偏好 文档/视频/题库/实操',
  `learning_habits` text DEFAULT NULL COMMENT '学习习惯补充',
  `update_scene` varchar(50) DEFAULT NULL COMMENT '更新场景 对话/做题/路径调整',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生学习画像';

-- =============================================
-- 3. 对话上下文表
-- =============================================
CREATE TABLE `chat_context` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` varchar(64) NOT NULL COMMENT '对话会话ID',
  `chat_type` varchar(20) NOT NULL DEFAULT 'profile' COMMENT '对话类型 profile/tutor/resource',
  `user_message` text DEFAULT NULL COMMENT '用户输入',
  `ai_reply` text DEFAULT NULL COMMENT 'AI回复',
  `is_extract_profile` tinyint DEFAULT '0' COMMENT '是否抽取画像 0否 1是',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_session` (`user_id`,`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话上下文';

-- =============================================
-- 4. AI智能体配置表
-- =============================================
CREATE TABLE `ai_agent` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `agent_name` varchar(50) NOT NULL COMMENT '智能体名称',
  `agent_role` varchar(50) NOT NULL COMMENT '角色 demand/document/mind/question/case/multimodal',
  `agent_description` varchar(200) DEFAULT NULL COMMENT '智能体描述（前端卡片显示）',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标名称/颜色（前端卡片显示）',
  `prompt_template` text DEFAULT NULL COMMENT '角色Prompt模板',
  `model` varchar(50) DEFAULT 'deepseek-chat' COMMENT '使用的大模型',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态 0禁用 1正常',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI智能体配置';

-- 插入6个智能体初始化数据（对应你截图的智能体中心）
INSERT INTO `ai_agent` (`id`, `agent_name`, `agent_role`, `agent_description`, `icon`, `prompt_template`, `sort`, `status`) VALUES
(1, '需求解析智能体', 'demand', '分析学习需求，拆解学习目标，生成个性化学习方案', 'demand', '', 1, 1),
(2, '文档生成智能体', 'document', '根据学习需求生成结构化的学习文档和笔记', 'document', '', 2, 1),
(3, '思维导图智能体', 'mind', '将知识点以思维导图形式呈现，帮助建立知识体系', 'mind', '', 3, 1),
(4, '题库智能体', 'question', '根据知识点生成针对性练习题和测试卷', 'question', '', 4, 1),
(5, '实操案例智能体', 'case', '生成实操案例和项目实践任务', 'case', '', 5, 1),
(6, '多模态智能体', 'multimodal', '生成视频脚本、音频讲解等多模态学习资源', 'multimodal', '', 6, 1);

-- =============================================
-- 5. 多模态学习资源表（核心优化：三字段分离）
-- =============================================
CREATE TABLE `learning_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `agent_id` bigint NOT NULL COMMENT '生成智能体ID',
  `resource_type` varchar(30) NOT NULL COMMENT '资源类型 document/mind/question/video/case',
  `content_format` varchar(20) NOT NULL DEFAULT 'text' COMMENT '内容格式 text/markdown/json/url',
  `resource_title` varchar(200) NOT NULL COMMENT '资源标题',
  `resource_content` longtext DEFAULT NULL COMMENT '可直接渲染的文本内容',
  `metadata` json DEFAULT NULL COMMENT '资源元数据（不同类型存储不同字段）',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件/视频/图片URL',
  `task_id` varchar(64) DEFAULT NULL COMMENT '生成任务ID（关联异步任务）',
  `version` int DEFAULT 1 COMMENT '资源版本号',
  `course_name` varchar(100) DEFAULT NULL COMMENT '关联课程',
  `knowledge_point` varchar(100) DEFAULT NULL COMMENT '关联知识点',
  `difficulty` varchar(20) DEFAULT NULL COMMENT '难度 简单/中等/困难',
  `status` tinyint DEFAULT '1' COMMENT '状态 0禁用 1正常',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_agent_id` (`agent_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多模态学习资源';

-- =============================================
-- 6. 学习路径表
-- =============================================
CREATE TABLE `learning_path` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `path_name` varchar(200) NOT NULL COMMENT '路径名称',
  `total_step` int DEFAULT '0' COMMENT '总步骤数',
  `current_step` int DEFAULT '0' COMMENT '当前步骤',
  `status` varchar(20) DEFAULT 'doing' COMMENT '状态 doing/finish',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径';

-- =============================================
-- 7. 学习路径步骤表
-- =============================================
CREATE TABLE `learning_path_step` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `path_id` bigint NOT NULL COMMENT '路径ID',
  `step_name` varchar(200) NOT NULL COMMENT '步骤名称',
  `step_content` text DEFAULT NULL COMMENT '步骤要求',
  `resource_ids` varchar(500) DEFAULT NULL COMMENT '关联资源ID(逗号分隔，兼容旧代码)',
  `sort` int DEFAULT '0' COMMENT '步骤顺序',
  `finish_status` tinyint DEFAULT '0' COMMENT '完成状态 0未完成 1已完成',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  PRIMARY KEY (`id`),
  KEY `idx_path_id` (`path_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径步骤';

-- =============================================
-- 8. 学习路径步骤-资源关联表（多对多关系）
-- =============================================
CREATE TABLE `learning_path_step_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `step_id` bigint NOT NULL COMMENT '步骤ID',
  `resource_id` bigint NOT NULL COMMENT '资源ID',
  `sort` int DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_step_resource` (`step_id`, `resource_id`),
  KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径步骤-资源关联表';

-- =============================================
-- 9. 学习行为记录表
-- =============================================
CREATE TABLE `learning_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint DEFAULT NULL COMMENT '学习资源ID',
  `step_id` bigint DEFAULT NULL COMMENT '学习路径步骤ID',
  `behavior_type` varchar(50) DEFAULT NULL COMMENT '行为类型 学习/做题/查看/完成',
  `duration` int DEFAULT '0' COMMENT '学习时长(秒)',
  `score` int DEFAULT NULL COMMENT '做题分数',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `behavior_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_step_id` (`step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习行为记录';

-- =============================================
-- 10. 题库答题记录表（错题本功能依赖）
-- =============================================
CREATE TABLE `question_answer_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '题库资源ID',
  `question_id` int NOT NULL COMMENT '题目ID（对应metadata中的questions数组索引）',
  `user_answer` text DEFAULT NULL COMMENT '用户答案',
  `correct_answer` text DEFAULT NULL COMMENT '正确答案',
  `is_correct` tinyint DEFAULT 0 COMMENT '是否正确 0否 1是',
  `spend_time` int DEFAULT 0 COMMENT '答题时长(秒)',
  `answer_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_resource` (`user_id`, `resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库答题记录表';

-- =============================================
-- 11. 视频观看进度表（断点续传功能依赖）
-- =============================================
CREATE TABLE `video_watch_progress` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `resource_id` bigint NOT NULL COMMENT '视频资源ID',
  `current_position` int DEFAULT 0 COMMENT '当前观看位置(秒)',
  `total_duration` int DEFAULT 0 COMMENT '视频总时长(秒)',
  `watch_count` int DEFAULT 1 COMMENT '观看次数',
  `last_watch_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '最后观看时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_resource` (`user_id`, `resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频观看进度表';

-- =============================================
-- 12. 学习效果评估表
-- =============================================
CREATE TABLE `learning_evaluate` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `path_id` bigint DEFAULT NULL COMMENT '关联学习路径ID',
  `evaluate_content` text DEFAULT NULL COMMENT '评估报告',
  `improve_suggest` text DEFAULT NULL COMMENT '提升建议',
  `knowledge_mastery` json DEFAULT NULL COMMENT '知识点掌握度(JSON)',
  `start_time` datetime DEFAULT NULL COMMENT '评估开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '评估结束时间',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_path_id` (`path_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习效果评估';

-- =============================================
-- 13. 智能辅导答疑表
-- =============================================
CREATE TABLE `smart_tutor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` varchar(64) NOT NULL COMMENT '答疑会话ID',
  `question` text NOT NULL COMMENT '学生问题',
  `text_answer` text DEFAULT NULL COMMENT '文字解答',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图解URL',
  `video_url` varchar(500) DEFAULT NULL COMMENT '讲解视频URL',
  `deleted` tinyint DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_session` (`user_id`, `session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能辅导答疑';

-- =============================================
-- 14. 资源生成任务表（RabbitMQ异步生成依赖）
-- =============================================
CREATE TABLE `resource_generate_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` varchar(64) NOT NULL COMMENT '任务唯一ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `agent_id` bigint NOT NULL COMMENT '生成智能体ID',
  `topic` varchar(200) NOT NULL COMMENT '生成主题',
  `params` json DEFAULT NULL COMMENT '生成参数',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '任务状态 pending/running/success/failed',
  `progress` int DEFAULT 0 COMMENT '生成进度 0-100',
  `error_msg` text DEFAULT NULL COMMENT '错误信息',
  `resource_id` bigint DEFAULT NULL COMMENT '生成成功后关联的资源ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源生成任务表';


INSERT INTO `ai_agent` (
`agent_name`,
`agent_role`,
`agent_description`,
`icon`,
`prompt_template`,  -- 空着
`model`,           -- 模型动态生效：deepseek / qwen / glm / gpt
`sort`,
`status`
) VALUES (
'学习效果评估智能体',
'evaluation',
'分析学习行为、答题、路径完成度，生成AI评估报告',
'evaluation',
'',   -- 提示词不入库！
'deepseek-chat',  -- 模型可随时改
7,
1
);

 ALTER TABLE `learning_path`
  ADD COLUMN `duration` varchar(50) DEFAULT NULL COMMENT '学习周期描述 如2周/1个月/2个月/3个月' AFTER `status`;
