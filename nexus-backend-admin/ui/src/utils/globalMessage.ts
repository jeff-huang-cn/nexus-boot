import { App } from 'antd';

// 全局 message 实例
let messageInstance: ReturnType<typeof App.useApp>['message'] | null = null;

export const setGlobalMessage = (instance: ReturnType<typeof App.useApp>['message']) => {
  messageInstance = instance;
};

export const globalMessage = {
  success: (content: string) => {
    if (messageInstance) {
      messageInstance.success(content);
    } else {
      console.error('Message instance not initialized');
    }
  },
  error: (content: string) => {
    if (messageInstance) {
      messageInstance.error(content);
    } else {
      console.error('Message instance not initialized, error:', content);
    }
  },
  warning: (content: string) => {
    if (messageInstance) {
      messageInstance.warning(content);
    } else {
      console.error('Message instance not initialized');
    }
  },
  info: (content: string) => {
    if (messageInstance) {
      messageInstance.info(content);
    } else {
      console.error('Message instance not initialized');
    }
  },
};

