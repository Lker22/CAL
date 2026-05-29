-- 学习路径表
CREATE TABLE IF NOT EXISTS `learning_path` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `path_name` VARCHAR(200) NOT NULL COMMENT '路径名称',
  `total_step` INT DEFAULT 0 COMMENT '总步骤数',
  `current_step` INT DEFAULT 0 COMMENT '当前步骤序号',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态 pending/inProgress/completed/paused',
  `deleted` INT DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径';

-- 学习路径步骤表
CREATE TABLE IF NOT EXISTS `learning_path_step` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `path_id` BIGINT NOT NULL COMMENT '路径ID',
  `step_name` VARCHAR(200) NOT NULL COMMENT '步骤名称',
  `step_content` TEXT COMMENT '步骤内容/要求',
  `resource_ids` VARCHAR(500) COMMENT '关联资源ID(逗号分隔)',
  `sort` INT DEFAULT 0 COMMENT '步骤顺序',
  `finish_status` INT DEFAULT 0 COMMENT '完成状态 0待学习 1学习中 2已完成',
  `finish_time` DATETIME COMMENT '完成时间',
  `deleted` INT DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_path_id` (`path_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径步骤';

-- 学习路径步骤-资源关联表
CREATE TABLE IF NOT EXISTS `learning_path_step_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `step_id` BIGINT NOT NULL COMMENT '步骤ID',
  `resource_id` BIGINT NOT NULL COMMENT '资源ID',
  `sort` INT DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`),
  KEY `idx_step_id` (`step_id`),
  KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习路径步骤资源关联';

-- 学习行为记录表
CREATE TABLE IF NOT EXISTS `learning_behavior` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `resource_id` BIGINT COMMENT '学习资源ID',
  `step_id` BIGINT COMMENT '学习路径步骤ID',
  `behavior_type` VARCHAR(20) COMMENT '行为类型 learning/quiz/view/complete',
  `duration` INT DEFAULT 0 COMMENT '学习时长(秒)',
  `score` INT COMMENT '做题分数',
  `deleted` INT DEFAULT 0 COMMENT '是否删除 0否 1是',
  `behavior_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '行为发生时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_step_id` (`step_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习行为记录';
