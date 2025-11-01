/**
 * 字典上下文
 * 管理系统字典数据的缓存和访问（按需加载）
 *
 * @author nexus
 */

import React, { createContext, useContext, useState, ReactNode, useCallback } from 'react';
import { dictApi, Dict } from '../services/system/dict/dictApi';

interface DictContextType {
  // 状态
  dictData: Record<string, Dict[]>; // 按dictType分组的字典数据
  loadingStates: Record<string, boolean>; // 各字典类型的加载状态

  // 方法
  getDictByType: (dictType: string) => Promise<Dict[]>;
  getDictLabel: (dictType: string, dictValue: string) => Promise<string>;
  refreshDict: (dictType: string) => Promise<void>;
  clearCache: (dictType?: string) => void;
}

const DictContext = createContext<DictContextType | undefined>(undefined);

interface DictProviderProps {
  children: ReactNode;
}

export const DictProvider: React.FC<DictProviderProps> = ({ children }) => {
  // 字典数据缓存（前端内存缓存）
  const [dictData, setDictData] = useState<Record<string, Dict[]>>({});
  // 各字典类型的加载状态
  const [loadingStates, setLoadingStates] = useState<Record<string, boolean>>({});

  /**
   * 根据字典类型获取字典列表（按需加载）
   * 如果缓存中没有，则从后端加载并缓存
   * @param dictType 字典类型
   * @returns 字典列表
   */
  const getDictByType = useCallback(async (dictType: string): Promise<Dict[]> => {
    // 如果缓存中已有数据，直接返回
    if (dictData[dictType]) {
      return dictData[dictType];
    }

    // 如果正在加载中，等待加载完成
    if (loadingStates[dictType]) {
      // 等待加载完成后返回
      return new Promise((resolve) => {
        const checkInterval = setInterval(() => {
          if (!loadingStates[dictType] && dictData[dictType]) {
            clearInterval(checkInterval);
            resolve(dictData[dictType]);
          }
        }, 100);
      });
    }

    // 开始加载
    try {
      setLoadingStates(prev => ({ ...prev, [dictType]: true }));

      if (process.env.NODE_ENV === 'development') {
        console.log(`按需加载字典: ${dictType}`);
      }

      const data = await dictApi.getListByType(dictType);

      // 缓存数据
      setDictData(prev => ({
        ...prev,
        [dictType]: data,
      }));

      if (process.env.NODE_ENV === 'development') {
        console.log(`字典 ${dictType} 加载完成:`, data);
      }

      return data;
    } catch (error) {
      console.error(`加载字典 ${dictType} 失败:`, error);
      return [];
    } finally {
      setLoadingStates(prev => ({ ...prev, [dictType]: false }));
    }
  }, [dictData, loadingStates]);

  /**
   * 根据字典类型和字典值获取字典标签
   * @param dictType 字典类型
   * @param dictValue 字典值
   * @returns 字典标签，如果未找到返回字典值本身
   */
  const getDictLabel = useCallback(async (dictType: string, dictValue: string): Promise<string> => {
    const dicts = await getDictByType(dictType);
    const dict = dicts.find(d => d.dictValue === dictValue);
    return dict?.dictLabel || dictValue;
  }, [getDictByType]);

  /**
   * 刷新指定字典类型的数据
   * @param dictType 字典类型
   */
  const refreshDict = useCallback(async (dictType: string) => {
    try {
      setLoadingStates(prev => ({ ...prev, [dictType]: true }));

      if (process.env.NODE_ENV === 'development') {
        console.log(`刷新字典: ${dictType}`);
      }

      const data = await dictApi.getListByType(dictType);

      // 更新缓存
      setDictData(prev => ({
        ...prev,
        [dictType]: data,
      }));

      if (process.env.NODE_ENV === 'development') {
        console.log(`字典 ${dictType} 刷新完成:`, data);
      }
    } catch (error) {
      console.error(`刷新字典 ${dictType} 失败:`, error);
      throw error;
    } finally {
      setLoadingStates(prev => ({ ...prev, [dictType]: false }));
    }
  }, []);

  /**
   * 清除缓存
   * @param dictType 字典类型，不传则清除所有缓存
   */
  const clearCache = useCallback((dictType?: string) => {
    if (dictType) {
      // 清除指定字典类型的缓存
      setDictData(prev => {
        const newData = { ...prev };
        delete newData[dictType];
        return newData;
      });

      if (process.env.NODE_ENV === 'development') {
        console.log(`清除字典缓存: ${dictType}`);
      }
    } else {
      // 清除所有缓存
      setDictData({});

      if (process.env.NODE_ENV === 'development') {
        console.log('清除所有字典缓存');
      }
    }
  }, []);

  const value: DictContextType = {
    dictData,
    loadingStates,
    getDictByType,
    getDictLabel,
    refreshDict,
    clearCache,
  };

  return (
    <DictContext.Provider value={value}>
      {children}
    </DictContext.Provider>
  );
};

/**
 * 使用字典上下文的Hook
 * @returns DictContextType
 * @throws Error 如果在DictProvider外部使用
 */
export const useDict = (): DictContextType => {
  const context = useContext(DictContext);

  if (context === undefined) {
    throw new Error('useDict must be used within a DictProvider');
  }

  return context;
};

export default DictContext;