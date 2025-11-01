/**
 * 字典缓存工具
 * 在浏览器会话期间缓存字典数据，减少网络请求
 *
 * @author nexus
 */

import { dictApi, Dict } from '../services/system/dict/dictApi';

// 全局缓存对象（存储在内存中）
const dictCache: Record<string, Dict[]> = {};

// 正在加载的Promise，避免重复请求
const loadingPromises: Record<string, Promise<Dict[]> | undefined> = {};

/**
 * 获取字典数据（带缓存）
 * @param dictType 字典类型
 * @returns 字典数据
 */
export async function getDictData(dictType: string): Promise<Dict[]> {
  // 1. 如果缓存中有，直接返回
  if (dictCache[dictType]) {
    if (process.env.NODE_ENV === 'development') {
      console.log(`从缓存读取字典: ${dictType}`);
    }
    return dictCache[dictType];
  }

  // 2. 如果正在加载中，返回同一个Promise（避免重复请求）
  const existingPromise = loadingPromises[dictType];
  if (existingPromise) {
    if (process.env.NODE_ENV === 'development') {
      console.log(`等待字典加载: ${dictType}`);
    }
    return existingPromise;
  }

  // 3. 开始加载
  if (process.env.NODE_ENV === 'development') {
    console.log(`从后端加载字典: ${dictType}`);
  }

  const loadPromise = dictApi.getListByType(dictType).then(data => {
    // 缓存数据
    dictCache[dictType] = data;
    // 清除加载Promise
    delete loadingPromises[dictType];

    if (process.env.NODE_ENV === 'development') {
      console.log(`字典加载完成: ${dictType}`, data);
    }

    return data;
  }).catch(error => {
    // 加载失败，清除加载Promise
    delete loadingPromises[dictType];
    console.error(`字典加载失败: ${dictType}`, error);
    return [];
  });

  // 保存加载Promise
  loadingPromises[dictType] = loadPromise;

  return loadPromise;
}

/**
 * 刷新指定字典的缓存
 * @param dictType 字典类型
 */
export async function refreshDictCache(dictType: string): Promise<void> {
  // 清除缓存
  delete dictCache[dictType];
  delete loadingPromises[dictType];

  // 重新加载
  await getDictData(dictType);

  if (process.env.NODE_ENV === 'development') {
    console.log(`字典缓存已刷新: ${dictType}`);
  }
}

/**
 * 清除所有字典缓存
 */
export function clearAllDictCache(): void {
  Object.keys(dictCache).forEach(key => delete dictCache[key]);
  Object.keys(loadingPromises).forEach(key => delete loadingPromises[key]);

  if (process.env.NODE_ENV === 'development') {
    console.log('所有字典缓存已清除');
  }
}

/**
 * 获取字典标签
 * @param dictType 字典类型
 * @param dictValue 字典值
 * @returns 字典标签
 */
export async function getDictLabel(dictType: string, dictValue: string): Promise<string> {
  const data = await getDictData(dictType);
  const dict = data.find(d => d.dictValue === dictValue);
  return dict?.dictLabel || dictValue;
}