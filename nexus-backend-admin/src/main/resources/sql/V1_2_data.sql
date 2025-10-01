-- 代码生成管理平台测试数据
-- 用于开发测试的示例数据

USE codegen_admin;

-- =============================================
-- 插入示例代码生成表配置
-- =============================================
INSERT INTO `codegen_table` (
  `table_name`, `table_comment`, `remark`, `module_name`, `business_name`, 
  `class_name`, `class_comment`, `author`, `template_type`, `front_type`, 
  `scene`, `package_name`, `module_package_name`, `business_package_name`, 
  `class_prefix`, `datasource_config_id`, `creator`
) VALUES 
(
  'sys_user', '用户信息表', '系统用户管理', 'system', 'user',
  'User', '用户', 'yourcompany', 1, 30, 1,
  'com.beckend.admin', 'com.beckend.admin.system', 'com.beckend.admin.system.user',
  'Sys', 1, 'admin'
),
(
  'sys_role', '角色信息表', '系统角色管理', 'system', 'role', 
  'Role', '角色', 'yourcompany', 1, 30, 1,
  'com.beckend.admin', 'com.beckend.admin.system', 'com.beckend.admin.system.role',
  'Sys', 1, 'admin'
),
(
  'product_info', '产品信息表', '产品管理', 'business', 'product',
  'Product', '产品', 'yourcompany', 1, 30, 1,
  'com.beckend.admin', 'com.beckend.admin.business', 'com.beckend.admin.business.product',
  '', 1, 'admin'
);

-- =============================================
-- 插入示例字段配置（sys_user表）
-- =============================================
INSERT INTO `codegen_column` (
  `table_id`, `column_name`, `data_type`, `column_comment`, `nullable`, 
  `primary_key`, `auto_increment`, `ordinal_position`, `java_type`, `java_field`,
  `dict_type`, `example`, `create_operation`, `update_operation`, `list_operation`,
  `list_operation_condition`, `list_operation_result`, `html_type`, `creator`
) VALUES 
-- sys_user表字段
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'id', 'bigint', '用户ID', 0, 1, 'auto_increment', 1, 'Long', 'id', '', '1', 0, 0, 1, '=', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'username', 'varchar(50)', '用户名', 0, 0, '', 2, 'String', 'username', '', 'admin', 1, 1, 1, 'LIKE', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'password', 'varchar(100)', '密码', 0, 0, '', 3, 'String', 'password', '', '123456', 1, 1, 0, '=', 0, 'password', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'nickname', 'varchar(50)', '昵称', 1, 0, '', 4, 'String', 'nickname', '', '管理员', 1, 1, 1, 'LIKE', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'email', 'varchar(100)', '邮箱', 1, 0, '', 5, 'String', 'email', '', 'admin@example.com', 1, 1, 1, 'LIKE', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'mobile', 'varchar(20)', '手机号', 1, 0, '', 6, 'String', 'mobile', '', '13888888888', 1, 1, 1, 'LIKE', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'sex', 'tinyint', '性别', 1, 0, '', 7, 'Integer', 'sex', 'sys_user_sex', '1', 1, 1, 1, '=', 1, 'select', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'status', 'tinyint', '状态', 0, 0, '', 8, 'Integer', 'status', 'sys_common_status', '1', 1, 1, 1, '=', 1, 'radio', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'create_time', 'datetime', '创建时间', 0, 0, '', 9, 'LocalDateTime', 'createTime', '', '2024-01-01 00:00:00', 0, 0, 1, 'BETWEEN', 1, 'datetime', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'update_time', 'datetime', '更新时间', 0, 0, '', 10, 'LocalDateTime', 'updateTime', '', '2024-01-01 00:00:00', 0, 0, 0, 'BETWEEN', 1, 'datetime', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'sys_user'), 'deleted', 'bit', '是否删除', 0, 0, '', 11, 'Boolean', 'deleted', '', '0', 0, 0, 0, '=', 0, 'radio', 'admin');

-- =============================================
-- 插入示例字段配置（product_info表）
-- =============================================
INSERT INTO `codegen_column` (
  `table_id`, `column_name`, `data_type`, `column_comment`, `nullable`, 
  `primary_key`, `auto_increment`, `ordinal_position`, `java_type`, `java_field`,
  `dict_type`, `example`, `create_operation`, `update_operation`, `list_operation`,
  `list_operation_condition`, `list_operation_result`, `html_type`, `creator`
) VALUES 
-- product_info表字段
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'id', 'bigint', '产品ID', 0, 1, 'auto_increment', 1, 'Long', 'id', '', '1', 0, 0, 1, '=', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'name', 'varchar(100)', '产品名称', 0, 0, '', 2, 'String', 'name', '', 'iPhone 15', 1, 1, 1, 'LIKE', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'category_id', 'bigint', '分类ID', 0, 0, '', 3, 'Long', 'categoryId', 'product_category', '1', 1, 1, 1, '=', 1, 'select', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'price', 'decimal(10,2)', '价格', 0, 0, '', 4, 'BigDecimal', 'price', '', '999.00', 1, 1, 1, 'BETWEEN', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'stock', 'int', '库存', 0, 0, '', 5, 'Integer', 'stock', '', '100', 1, 1, 1, 'BETWEEN', 1, 'input', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'description', 'text', '产品描述', 1, 0, '', 6, 'String', 'description', '', '高品质产品', 1, 1, 0, 'LIKE', 1, 'textarea', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'status', 'tinyint', '状态', 0, 0, '', 7, 'Integer', 'status', 'product_status', '1', 1, 1, 1, '=', 1, 'radio', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'create_time', 'datetime', '创建时间', 0, 0, '', 8, 'LocalDateTime', 'createTime', '', '2024-01-01 00:00:00', 0, 0, 1, 'BETWEEN', 1, 'datetime', 'admin'),
((SELECT id FROM codegen_table WHERE table_name = 'product_info'), 'update_time', 'datetime', '更新时间', 0, 0, '', 9, 'LocalDateTime', 'updateTime', '', '2024-01-01 00:00:00', 0, 0, 0, 'BETWEEN', 1, 'datetime', 'admin');
