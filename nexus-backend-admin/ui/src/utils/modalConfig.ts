/**
 * Modal 弹窗全局配置
 * 用于统一管理弹窗样式，避免内容过长导致浏览器滚动
 */

import { ModalProps } from 'antd';

/**
 * 标准 Modal 的 bodyStyle 配置
 * 
 * 特性：
 * - 最大高度：视口高度减去 300px（留出空间给标题、按钮和边距）
 * - 自动出现垂直滚动条
 * - 右侧留出 8px 空间避免滚动条遮挡内容
 */
export const STANDARD_MODAL_BODY_STYLE: React.CSSProperties = {
  maxHeight: 'calc(100vh - 300px)',
  overflowY: 'auto',
  paddingRight: '8px',
};

/**
 * 大尺寸 Modal 的 bodyStyle 配置
 * 适用于内容较多的弹窗
 */
export const LARGE_MODAL_BODY_STYLE: React.CSSProperties = {
  maxHeight: 'calc(100vh - 250px)',
  overflowY: 'auto',
  paddingRight: '8px',
};

/**
 * 小尺寸 Modal 的 bodyStyle 配置
 * 适用于内容较少的弹窗
 */
export const SMALL_MODAL_BODY_STYLE: React.CSSProperties = {
  maxHeight: 'calc(100vh - 350px)',
  overflowY: 'auto',
  paddingRight: '8px',
};

/**
 * 获取标准 Modal 配置
 * @param customProps 自定义属性（会覆盖默认配置）
 * @returns Modal 属性对象
 */
export const getStandardModalProps = (customProps?: Partial<ModalProps>): Partial<ModalProps> => {
  return {
    width: 600,
    destroyOnClose: true,
    bodyStyle: STANDARD_MODAL_BODY_STYLE,
    ...customProps,
  };
};

/**
 * 获取大尺寸 Modal 配置
 * @param customProps 自定义属性
 * @returns Modal 属性对象
 */
export const getLargeModalProps = (customProps?: Partial<ModalProps>): Partial<ModalProps> => {
  return {
    width: 800,
    destroyOnClose: true,
    bodyStyle: LARGE_MODAL_BODY_STYLE,
    ...customProps,
  };
};

/**
 * 获取小尺寸 Modal 配置
 * @param customProps 自定义属性
 * @returns Modal 属性对象
 */
export const getSmallModalProps = (customProps?: Partial<ModalProps>): Partial<ModalProps> => {
  return {
    width: 400,
    destroyOnClose: true,
    bodyStyle: SMALL_MODAL_BODY_STYLE,
    ...customProps,
  };
};

