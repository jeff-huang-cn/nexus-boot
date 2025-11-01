/**
 * 字典数据Hook
 * 提供便捷的方法来使用字典数据（按需加载）
 *
 * @author nexus
 */

import { useMemo, useState, useEffect } from 'react';
import { useDict } from '../contexts/DictContext';
import type { Dict } from '../services/system/dict/dictApi';

/**
 * 字典选项接口（用于Select、Radio等组件）
 */
export interface DictOption {
  label: string;
  value: string | number;
  colorType?: string;
  disabled?: boolean;
}

/**
 * 使用字典数据的Hook（按需加载）
 * @param dictType 字典类型
 * @returns 字典数据和相关方法
 */
export function useDictData(dictType: string) {
  const { getDictByType, getDictLabel, dictData, loadingStates } = useDict();
  const [dictList, setDictList] = useState<Dict[]>([]);

  // 按需加载字典数据
  useEffect(() => {
    // 如果缓存中已有数据，直接使用
    if (dictData[dictType]) {
      setDictList(dictData[dictType]);
      return;
    }

    // 否则异步加载
    getDictByType(dictType).then(data => {
      setDictList(data);
    });
  }, [dictType, getDictByType, dictData]);

  // 转换为Select选项格式
  const dictOptions = useMemo((): DictOption[] => {
    return dictList
      .filter(d => d.status === 1) // 只返回启用的字典项
      .map(d => ({
        label: d.dictLabel,
        value: d.dictValue,
        colorType: d.colorType,
        disabled: false,
      }));
  }, [dictList]);

  // 转换为Select选项格式（包含禁用项）
  const allDictOptions = useMemo((): DictOption[] => {
    return dictList.map(d => ({
      label: d.dictLabel,
      value: d.dictValue,
      colorType: d.colorType,
      disabled: d.status === 0,
    }));
  }, [dictList]);

  /**
   * 根据字典值获取字典标签（同步，从已加载的数据中查找）
   * @param value 字典值
   * @returns 字典标签，如果未找到返回字典值本身
   */
  const getLabel = (value: string): string => {
    const dict = dictList.find(d => d.dictValue === value);
    return dict?.dictLabel || value;
  };

  /**
   * 根据字典值获取完整的字典对象
   * @param value 字典值
   * @returns 字典对象
   */
  const getDictByValue = (value: string): Dict | undefined => {
    return dictList.find(d => d.dictValue === value);
  };

  /**
   * 检查字典值是否存在
   * @param value 字典值
   * @returns 是否存在
   */
  const hasValue = (value: string): boolean => {
    return dictList.some(d => d.dictValue === value);
  };

  return {
    // 原始数据
    dictList,

    // 选项数据
    dictOptions,      // 仅启用的选项
    allDictOptions,   // 所有选项（包括禁用）

    // 工具方法
    getLabel,
    getDictByValue,
    hasValue,
  };
}

export default useDictData;