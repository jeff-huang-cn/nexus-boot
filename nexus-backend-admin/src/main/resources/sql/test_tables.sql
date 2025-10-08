 -- ================================================
-- 代码生成测试表
-- 用于测试树表和主子表代码生成功能
-- ================================================

-- 1. 树表测试：分类表
DROP TABLE IF EXISTS `test_category`;
CREATE TABLE `test_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试分类表（树表）';

-- 初始化树表测试数据
INSERT INTO `test_category` (`id`, `parent_id`, `name`, `sort`, `status`) VALUES
(1, 0, '电子产品', 1, 1),
(2, 1, '手机', 1, 1),
(3, 1, '电脑', 2, 1),
(4, 2, '苹果手机', 1, 1),
(5, 2, '华为手机', 2, 1),
(6, 3, '笔记本', 1, 1),
(7, 3, '台式机', 2, 1),
(8, 0, '家居用品', 2, 1),
(9, 8, '家具', 1, 1),
(10, 8, '家电', 2, 1);


-- 2. 主子表测试：订单表（主表）
DROP TABLE IF EXISTS `test_order`;
CREATE TABLE `test_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `customer_name` VARCHAR(100) NOT NULL COMMENT '客户姓名',
    `customer_phone` VARCHAR(20) DEFAULT NULL COMMENT '客户电话',
    `total_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付 1-已支付 2-已发货 3-已完成 4-已取消',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_customer_name` (`customer_name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试订单表（主表）';


-- 3. 主子表测试：订单明细表（子表）
DROP TABLE IF EXISTS `test_order_item`;
CREATE TABLE `test_order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `product_code` VARCHAR(50) DEFAULT NULL COMMENT '商品编码',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '单价',
    `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '小计',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `date_created` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `creator` VARCHAR(64) DEFAULT '' COMMENT '创建者',
    `last_updated` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updater` VARCHAR(64) DEFAULT '' COMMENT '更新者',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    CONSTRAINT `fk_order_item_order` FOREIGN KEY (`order_id`) REFERENCES `test_order` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试订单明细表（子表）';


-- 初始化主子表测试数据
INSERT INTO `test_order` (`id`, `order_no`, `customer_name`, `customer_phone`, `total_amount`, `status`) VALUES
(1, 'ORD20251008001', '张三', '13800138000', 5999.00, 1),
(2, 'ORD20251008002', '李四', '13800138001', 3299.00, 0);

INSERT INTO `test_order_item` (`order_id`, `product_name`, `product_code`, `quantity`, `price`, `amount`) VALUES
(1, 'iPhone 15 Pro', 'IP15PRO', 1, 5999.00, 5999.00),
(2, 'MacBook Air', 'MBA13', 1, 7999.00, 7999.00),
(2, '妙控键盘', 'MK01', 1, 899.00, 899.00);


-- 4. 添加代码生成配置（如果需要手动添加）
-- INSERT INTO `codegen_table` (...) VALUES (...);
