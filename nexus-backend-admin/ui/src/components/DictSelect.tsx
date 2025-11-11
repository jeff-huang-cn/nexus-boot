/**
 * 字典选择组件
 * 封装了Ant Design的Select组件，自动从字典中加载选项
 * 使用内存缓存，无需Context Provider
 *
 * @author nexus
 */

import React, { useState, useEffect, useMemo } from 'react';
import { Select, SelectProps } from 'antd';
import { getDictData } from '../utils/dictCache';
import type { Dict } from '../services/system/dict/dictApi';

interface DictSelectProps extends Omit<SelectProps, 'options'> {
  /**
   * 字典类型
   */
  dictType: string;

  /**
   * 是否包含禁用的选项
   * @default false
   */
  includeDisabled?: boolean;

  /**
   * 值的类型
   * @default 'string'
   */
  valueType?: 'string' | 'number';
}

interface DictOption {
  label: string;
  value: string | number;
  disabled?: boolean;
}

/**
 * 字典选择组件
 * 使用示例：
 * <DictSelect dictType={DictType.COMMON_STATUS} placeholder="请选择状态" />
 * <DictSelect dictType={DictType.COMMON_STATUS} valueType="number" placeholder="请选择状态" />
 */
const DictSelect: React.FC<DictSelectProps> = ({
  dictType,
  includeDisabled = false,
  valueType = 'string',
  onChange,
  ...selectProps
}) => {
  const [dictList, setDictList] = useState<Dict[]>([]);
  const [loading, setLoading] = useState(false);

  // 加载字典数据
  useEffect(() => {
    setLoading(true);
    getDictData(dictType)
      .then(data => {
        setDictList(data);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [dictType]);

  // 构建选项列表
  const options = useMemo((): DictOption[] => {
    // 过滤数据
    const filteredList = includeDisabled
      ? dictList
      : dictList.filter(d => d.status === 1);

    // 转换为选项格式
    return filteredList.map(d => ({
      label: d.dictLabel,
      value: valueType === 'number' ? Number(d.dictValue) : d.dictValue,
      disabled: d.status === 0,
    }));
  }, [dictList, includeDisabled, valueType]);

  // 包装onChange，确保返回正确的类型
  const handleChange = (value: any, option: any) => {
    if (onChange) {
      onChange(value, option);
    }
  };

  return (
    <Select
      {...selectProps}
      loading={loading}
      options={options}
      fieldNames={{ label: 'label', value: 'value' }}
      onChange={handleChange}
    />
  );
};

export default DictSelect;