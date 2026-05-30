-- =============================================
-- 学习路径模块建表SQL
-- 注意：此文件仅包含学习路径相关4张表
-- 完整建表请参考项目根目录 MediCraft.sql
-- 如需单独执行，请先 USE MediCraft;
-- =============================================

-- 删除旧表(按依赖逆序)
DROP TABLE IF EXISTS `learning_behavior`;
DROP TABLE IF EXISTS `learning_path_step_resource`;
DROP TABLE IF EXISTS `learning_path_step`;
DROP TABLE IF EXISTS `learning_path`;

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
