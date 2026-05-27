CREATE DATABASE IF NOT EXISTS MediCraft 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE MediCraft;

-- 1. 用户表
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码(加密)',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint DEFAULT '1' COMMENT '状态 0禁用 1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 学生学习画像表
CREATE TABLE `student_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `knowledge_base` varchar(50) DEFAULT NULL COMMENT '知识基础 弱/中/强',
  `cognitive_style` varchar(50) DEFAULT NULL COMMENT '认知风格 视觉型/听觉型/动手型',
  `learning_goal` text DEFAULT NULL COMMENT '学习目标',
  `error_prone_points` text DEFAULT NULL COMMENT '易错点(JSON数组)',
  `learning_pace` varchar(20) DEFAULT NULL COMMENT '学习节奏 慢/中/快',
  `resource_preference` varchar(100) DEFAULT NULL COMMENT '资源偏好 文档/视频/题库/实操',
  `learning_habits` text DEFAULT NULL COMMENT '学习习惯补充',
  `update_scene` varchar(50) DEFAULT NULL COMMENT '更新场景 对话/做题/路径调整',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生学习画像';

-- 3. 对话上下文表
CREATE TABLE `chat_context` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `session_id` varchar(64) NOT NULL COMMENT '对话会话ID',
  `user_message` text DEFAULT NULL COMMENT '用户输入',
  `ai_reply` text DEFAULT NULL COMMENT 'AI回复',
  `is_extract_profile` tinyint DEFAULT '0' COMMENT '是否抽取画像 0否 1是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_session` (`user_id`,`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话上下文';

-- 4. AI智能体配置表
CREATE TABLE `ai_agent` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `agent_name` varchar(50) NOT NULL COMMENT '智能体名称',
  `agent_role` varchar(50) NOT NULL COMMENT '角色 文档/思维导图/题库/实操/视频/解析',
  `prompt_template` text DEFAULT NULL COMMENT '角色Prompt模板',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI智能体配置';

-- 5. 多模态学习资源表
CREATE TABLE `learning_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `agent_id` bigint NOT NULL COMMENT '生成智能体ID',
  `resource_type` varchar(30) NOT NULL COMMENT '资源类型 document/mind/question/video/case',
  `resource_title` varchar(200) NOT NULL COMMENT '资源标题',
  `resource_content` longtext DEFAULT NULL COMMENT '资源内容/URL',
  `course_name` varchar(100) DEFAULT NULL COMMENT '关联课程',
  `knowledge_point` varchar(100) DEFAULT NULL COMMENT '关联知识点',
  `difficulty` varchar(20) DEFAULT NULL COMMENT '难度 简单/中等/困难',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='多模态学习资源';

-- 6. 学习路径表
CREATE TABLE `learning_path` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `path_name` varchar(200) NOT NULL COMMENT '路径名称',
  `total_step` int DEFAULT '0' COMMENT '总步骤数',
  `current_step` int DEFAULT '0' COMMENT '当前步骤',
  `status` varchar(20) DEFAULT 'doing' COMMENT '状态 doing/finish',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径';

-- 7. 学习路径步骤表
CREATE TABLE `learning_path_step` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `path_id` bigint NOT NULL COMMENT '路径ID',
  `step_name` varchar(200) NOT NULL COMMENT '步骤名称',
  `step_content` text DEFAULT NULL COMMENT '步骤要求',
  `resource_ids` varchar(500) DEFAULT NULL COMMENT '关联资源ID(逗号分隔)',
  `sort` int DEFAULT '0' COMMENT '步骤顺序',
  `finish_status` tinyint DEFAULT '0' COMMENT '完成状态',
  `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  KEY `idx_path_id` (`path_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径步骤';

-- 8. 学习行为记录表
CREATE TABLE `learning_behavior` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `resource_id` bigint DEFAULT NULL COMMENT '学习资源ID',
  `behavior_type` varchar(50) DEFAULT NULL COMMENT '行为类型 学习/做题/查看',
  `duration` int DEFAULT '0' COMMENT '学习时长(秒)',
  `score` int DEFAULT NULL COMMENT '做题分数',
  `behavior_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习行为记录';

-- 9. 学习效果评估表
CREATE TABLE `learning_evaluate` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `evaluate_content` text DEFAULT NULL COMMENT '评估报告',
  `improve_suggest` text DEFAULT NULL COMMENT '提升建议',
  `knowledge_mastery` text DEFAULT NULL COMMENT '知识点掌握度(JSON)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习效果评估';

-- 10. 智能辅导答疑表
CREATE TABLE `smart_tutor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `question` text NOT NULL COMMENT '学生问题',
  `text_answer` text DEFAULT NULL COMMENT '文字解答',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图解URL',
  `video_url` varchar(500) DEFAULT NULL COMMENT '讲解视频URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能辅导答疑';