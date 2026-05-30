-- 学习路径表添加 duration 字段（用于存储学习周期描述）
-- 如果 MediCraft 数据库已存在，请执行此迁移
-- 如果是全新建库，直接用 MediCraft.sql 即可，无需执行此文件

ALTER TABLE `learning_path`
ADD COLUMN `duration` varchar(50) DEFAULT NULL COMMENT '学习周期描述 如2周/1个月/2个月/3个月' AFTER `status`;
