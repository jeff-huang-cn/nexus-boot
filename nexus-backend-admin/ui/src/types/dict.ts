/**
 * 系统字典类型常量
 * 定义系统中使用的所有字典类型，避免硬编码字符串
 *
 * 注意：只定义字典类型，具体的字典值从数据库动态读取
 *
 * @author nexus
 */

/**
 * 字典类型枚举
 */
export enum DictType {
  /** 用户性别 */
  USER_SEX = 'sys_user_sex',

  /** 用户状态 */
  USER_STATUS = 'sys_user_status',

  /** 菜单类型 */
  MENU_TYPE = 'sys_menu_type',

  /** 菜单状态 */
  MENU_STATUS = 'sys_menu_status',

  /** 角色状态 */
  ROLE_STATUS = 'sys_role_status',

  /** 通用状态 */
  COMMON_STATUS = 'sys_common_status',

  /** 是否 */
  YES_NO = 'sys_yes_no',
}