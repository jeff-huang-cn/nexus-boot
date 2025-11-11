import { useState, useEffect, RefObject } from 'react';

/**
 * 动态计算表格高度的 Hook
 * @param containerRef 表格容器的 ref
 * @param offsetHeight 需要减去的额外高度（搜索栏、按钮栏等）
 * @returns 计算后的表格高度
 */
export const useTableHeight = (
  containerRef?: RefObject<HTMLDivElement | null>,
  offsetHeight: number = 0
): number => {
  const [tableHeight, setTableHeight] = useState<number>(400);

  useEffect(() => {
    const calculateHeight = () => {
      // 获取视口高度
      const viewportHeight = window.innerHeight;

      if (containerRef?.current) {
        // 如果提供了容器 ref，基于容器位置计算
        const containerTop = containerRef.current.getBoundingClientRect().top;
        // 减去容器顶部距离、底部留白(24px)和额外偏移
        const calculatedHeight = viewportHeight - containerTop - 24 - offsetHeight;
        setTableHeight(Math.max(calculatedHeight, 300)); // 最小高度 300px
      } else {
        // 没有容器 ref 时，使用固定计算方式
        // 假设页面顶部有 64px 导航栏，底部留 24px，加上额外偏移
        const calculatedHeight = viewportHeight - 64 - 24 - offsetHeight;
        setTableHeight(Math.max(calculatedHeight, 300));
      }
    };

    // 初始计算
    calculateHeight();

    // 监听窗口大小变化
    window.addEventListener('resize', calculateHeight);

    // 使用 ResizeObserver 监听容器大小变化（如果支持）
    let resizeObserver: ResizeObserver | null = null;
    if (containerRef?.current && window.ResizeObserver) {
      resizeObserver = new ResizeObserver(calculateHeight);
      resizeObserver.observe(containerRef.current);
    }

    // 清理函数
    return () => {
      window.removeEventListener('resize', calculateHeight);
      if (resizeObserver) {
        resizeObserver.disconnect();
      }
    };
  }, [containerRef, offsetHeight]);

  return tableHeight;
};