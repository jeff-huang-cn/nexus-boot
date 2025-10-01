-- 系统用户表
DROP TABLE IF EXISTS `system_user`;
CREATE TABLE `system_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(30) NOT NULL COMMENT '用户账号',
  `password` varchar(100) DEFAULT '' COMMENT '密码',
  `nickname` varchar(30) NOT NULL COMMENT '用户昵称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `post_ids` varchar(255) DEFAULT NULL COMMENT '岗位编号数组',
  `email` varchar(50) DEFAULT '' COMMENT '用户邮箱',
  `mobile` varchar(11) DEFAULT '' COMMENT '手机号码',
  `sex` tinyint DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(512) DEFAULT '' COMMENT '头像地址',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '帐号状态（0正常 1停用）',
  `login_ip` varchar(50) DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime DEFAULT NULL COMMENT '最后登录时间',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`,`update_time`,`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 插入测试数据
INSERT INTO `system_user` (`id`, `username`, `password`, `nickname`, `remark`, `dept_id`, `post_ids`, `email`, `mobile`, `sex`, `avatar`, `status`, `login_ip`, `login_date`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSdvVMaN3tIbuHgCQXVHzOFgQcq9dkKn4GZE0XcPbM1i', '系统管理员', '管理员', 103, '[1]', 'admin@beckend.com', '18888888888', 1, '', 0, '127.0.0.1', '2024-01-01 00:00:00', 'admin', '2024-01-01 00:00:00', '', '2024-01-01 00:00:00', b'0', 1),
(2, 'test', '$2a$10$mRMIYLDtRHlf1.rkHe5JkOKwNYUFWxgkLPvDPDQdDOFOwV2CHo3WG', '测试用户', '测试账号', 104, '[2]', 'test@beckend.com', '18888888889', 0, '', 0, '127.0.0.1', '2024-01-01 00:00:00', 'admin', '2024-01-01 00:00:00', '', '2024-01-01 00:00:00', b'0', 1);
