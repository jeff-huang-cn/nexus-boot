-- 为 system_user 表添加 Spring Security 所需的账户状态字段
-- 用于支持账户过期、锁定、密码过期等功能
-- 设计思路：使用时间字段判断状态，更灵活且符合业务场景

-- 1. 添加账户过期时间字段
-- 当前时间 > expired_time 时，账户过期；NULL 表示永不过期
ALTER TABLE `system_user` ADD COLUMN `expired_time` datetime DEFAULT NULL 
  COMMENT '账户过期时间（NULL=永不过期，当前时间>此值=账户已过期）' AFTER `status`;

-- 2. 添加账户锁定时间字段
-- 当前时间 < locked_time 时，账户被锁定；NULL 表示未锁定
-- 适用于：密码错误多次后锁定 1 小时等场景
ALTER TABLE `system_user` ADD COLUMN `locked_time` datetime DEFAULT NULL 
  COMMENT '账户锁定截止时间（NULL=未锁定，当前时间<此值=账户被锁定）' AFTER `expired_time`;

-- 3. 添加密码过期时间字段
-- 当前时间 > password_expire_time 时，密码过期；NULL 表示永不过期
ALTER TABLE `system_user` ADD COLUMN `password_expire_time` datetime DEFAULT NULL 
  COMMENT '密码过期时间（NULL=永不过期，当前时间>此值=密码已过期）' AFTER `locked_time`;

-- 4. 添加密码最后修改时间字段
-- 用于计算密码过期时间、密码修改历史等
ALTER TABLE `system_user` ADD COLUMN `password_update_time` datetime DEFAULT NULL 
  COMMENT '密码最后修改时间' AFTER `password_expire_time`;

-- 说明：
-- status 字段：0=正常启用，1=停用
-- expired_time：账户有效期，适用于临时账户场景
-- locked_time：锁定截止时间，适用于安全锁定场景（如密码错误多次）
-- password_expire_time：密码有效期，适用于定期修改密码策略

-- 添加索引（用于定时任务清理过期账户）
CREATE INDEX `idx_expired_time` ON `system_user` (`expired_time`);
CREATE INDEX `idx_locked_time` ON `system_user` (`locked_time`);
CREATE INDEX `idx_password_expire` ON `system_user` (`password_expire_time`);

