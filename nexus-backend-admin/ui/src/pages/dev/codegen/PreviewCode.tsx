import React, { useState, useEffect, useCallback, useMemo, memo, useRef } from 'react';
import {
  Card,
  Button,
  Space,
  message,
  Tabs,
  Typography,
  Alert,
  Spin,
  Progress,
  Tree,
} from 'antd';
import {
  ArrowLeftOutlined,
  DownloadOutlined,
  CopyOutlined,
  ReloadOutlined,
  FileOutlined,
  FolderOpenOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { codegenApi } from '../../../services/codegen/codegen';

const { TabPane } = Tabs;
const { Text } = Typography;

// 缓存已渲染的代码组件
const codeCache = new Map<string, React.ReactElement>();

// 优化的代码显示组件
const CodeDisplay = memo(({ 
  fileName, 
  code, 
  onCopy 
}: { 
  fileName: string; 
  code: string; 
  onCopy: (code: string, fileName: string) => void;
}) => {
  const [isRendering, setIsRendering] = useState(true);
  const [renderedContent, setRenderedContent] = useState<React.ReactNode>(null);

  const language = useMemo(() => {
    if (fileName.endsWith('.java')) return 'java';
    if (fileName.endsWith('.tsx') || fileName.endsWith('.ts')) return 'typescript';
    if (fileName.endsWith('.vue')) return 'vue';
    if (fileName.endsWith('.xml')) return 'xml';
    if (fileName.endsWith('.sql')) return 'sql';
    if (fileName.endsWith('.json')) return 'json';
    return 'text';
  }, [fileName]);

  const lineCount = useMemo(() => code.split('\n').length, [code]);

  // 对于大文件，使用简化的渲染配置
  const isLargeFile = lineCount > 500;
  
  const syntaxHighlighterProps = useMemo(() => {
    const baseProps = {
      language,
      style: oneLight,
      showLineNumbers: !isLargeFile, // 大文件不显示行号以提升性能
      wrapLines: false, // 禁用自动换行以提升性能
      lineProps: isLargeFile ? undefined : { style: { display: 'table-row' } }, // 大文件禁用行样式
      customStyle: {
        margin: 0,
        fontSize: '12px',
        maxHeight: isLargeFile ? '400px' : '600px',
        overflow: 'auto',
        backgroundColor: '#fafafa',
      },
      lineNumberStyle: { minWidth: '3em' },
    };

    // 大文件使用更简单的渲染模式
    if (isLargeFile) {
      return {
        ...baseProps,
        useInlineStyles: false, // 禁用内联样式以提升性能
      };
    }

    return baseProps;
  }, [language, isLargeFile]);

  // 异步渲染大文件
  useEffect(() => {
    if (isLargeFile) {
      setIsRendering(true);
      // 使用setTimeout将渲染推迟到下一个事件循环，避免阻塞UI
      const timer = setTimeout(() => {
        setRenderedContent(
          <SyntaxHighlighter {...syntaxHighlighterProps}>
            {code}
          </SyntaxHighlighter>
        );
        setIsRendering(false);
      }, 10);
      return () => clearTimeout(timer);
    } else {
      // 小文件直接渲染
      setRenderedContent(
        <SyntaxHighlighter {...syntaxHighlighterProps}>
          {code}
        </SyntaxHighlighter>
      );
      setIsRendering(false);
    }
  }, [code, syntaxHighlighterProps, isLargeFile]);

  return (
    <Card
      size="small"
      title={
        <Space>
          <Text code>{fileName}</Text>
          <Text type="secondary">
            ({lineCount} 行{isLargeFile ? ' - 大文件已优化显示' : ''})
          </Text>
        </Space>
      }
      extra={
        <Button
          size="small"
          icon={<CopyOutlined />}
          onClick={() => onCopy(code, fileName)}
        >
          复制
        </Button>
      }
    >
      {isLargeFile && (
        <Alert
          message="大文件提示"
          description="为了提升性能，大文件已禁用行号显示和部分高亮功能"
          type="info"
          showIcon
          style={{ marginBottom: 8 }}
        />
      )}
      {isRendering ? (
        <div style={{ 
          height: isLargeFile ? '400px' : '200px', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          backgroundColor: '#fafafa',
          border: '1px solid #d9d9d9',
          borderRadius: '6px'
        }}>
          <Spin size="large" tip="正在渲染代码..." />
        </div>
      ) : (
        renderedContent
      )}
    </Card>
  );
});

CodeDisplay.displayName = 'CodeDisplay';

// 预加载状态管理
interface PreloadState {
  isPreloading: boolean;
  preloadedFiles: Set<string>;
  preloadProgress: number;
}

/**
 * 代码预览页面
 */
const PreviewCode: React.FC = () => {
  const navigate = useNavigate();
  const params = useParams<{ id: string }>();
  const [loading, setLoading] = useState(false);
  const [codeMap, setCodeMap] = useState<Record<string, string>>({});
  const [activeGroupTab, setActiveGroupTab] = useState<string>('backend'); // 外层分组tab
  const [activeFileTab, setActiveFileTab] = useState<string>(''); // 内层文件tab
  
  // 预加载状态
  const [preloadState, setPreloadState] = useState<PreloadState>({
    isPreloading: false,
    preloadedFiles: new Set(),
    preloadProgress: 0,
  });
  
  // 预加载队列引用
  const preloadQueueRef = useRef<string[]>([]);
  const preloadTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // 构建树形数据结构
  const buildTree = (files: string[]) => {
    const tree: any[] = [];
    const map = new Map();

    files.forEach(filePath => {
      const parts = filePath.split('/');
      let currentLevel = tree;
      let currentPath = '';

      parts.forEach((part, index) => {
        currentPath = currentPath ? `${currentPath}/${part}` : part;
        const isFile = index === parts.length - 1;

        if (!map.has(currentPath)) {
          const node: any = {
            title: part,
            key: currentPath,
            isLeaf: isFile,
            icon: isFile ? <FileOutlined /> : undefined,
          };

          if (!isFile) {
            node.children = [];
          }

          map.set(currentPath, node);
          currentLevel.push(node);

          if (!isFile) {
            currentLevel = node.children;
          }
        } else {
          if (!isFile) {
            currentLevel = map.get(currentPath).children;
          }
        }
      });
    });

    return tree;
  };

  // 使用useMemo优化文件分组计算
  const fileGroups = useMemo(() => {
    const groups: Record<string, { label: string; files: string[]; tree: any[] }> = {
      backend: { label: '后端代码', files: [], tree: [] },
      frontend: { label: '前端代码', files: [], tree: [] },
    };

    Object.keys(codeMap).forEach(fileName => {
      if (fileName.startsWith('backend/')) {
        groups.backend.files.push(fileName);
      } else if (fileName.startsWith('frontend/')) {
        groups.frontend.files.push(fileName);
      }
    });

    // 对后端文件排序：resources 相关的文件排在最后
    groups.backend.files.sort((a, b) => {
      const aIsResource = a.includes('/resources/');
      const bIsResource = b.includes('/resources/');
      
      if (aIsResource && !bIsResource) return 1;  // a 是 resources，排后面
      if (!aIsResource && bIsResource) return -1; // b 是 resources，排后面
      return a.localeCompare(b); // 其他情况按字母排序
    });

    // 对前端文件排序
    groups.frontend.files.sort((a, b) => a.localeCompare(b));

    // 构建树形结构
    groups.backend.tree = buildTree(groups.backend.files);
    groups.frontend.tree = buildTree(groups.frontend.files);

    return groups;
  }, [codeMap]);

  // 异步预加载文件渲染
  const preloadFileRender = useCallback(async (fileName: string) => {
    if (preloadState.preloadedFiles.has(fileName) || !codeMap[fileName]) {
      return;
    }

    const code = codeMap[fileName];
    // 模拟渲染过程（实际上是预先创建CodeDisplay组件）
    const cacheKey = `${fileName}_${code?.length || 0}`;
    
    if (!codeCache.has(cacheKey)) {
      // 使用一个简单的函数引用，避免依赖问题
      const copyHandler = (code: string, fileName: string) => {
        navigator.clipboard.writeText(code).then(() => {
          message.success(`已复制 ${fileName} 的代码`);
        }).catch(() => {
          message.error('复制失败');
        });
      };

      const content = (
        <CodeDisplay
          fileName={fileName}
          code={code || ''}
          onCopy={copyHandler}
        />
      );

      // 缓存所有文件的渲染结果
      codeCache.set(cacheKey, content);
    }

    // 更新预加载状态
    setPreloadState(prev => {
      const newPreloadedFiles = new Set(prev.preloadedFiles);
      newPreloadedFiles.add(fileName);
      return {
        ...prev,
        preloadedFiles: newPreloadedFiles,
      };
    });
  }, [codeMap, preloadState.preloadedFiles]);

  // 批量预加载处理
  const processPreloadQueue = useCallback(async () => {
    if (preloadQueueRef.current.length === 0) {
      setPreloadState(prev => ({ ...prev, isPreloading: false, preloadProgress: 100 }));
      return;
    }

    const fileName = preloadQueueRef.current.shift()!;
    await preloadFileRender(fileName);

    // 更新进度
    const totalFiles = Object.keys(codeMap).length;
    const processedFiles = preloadState.preloadedFiles.size + 1;
    const progress = Math.round((processedFiles / totalFiles) * 100);
    
    setPreloadState(prev => ({
      ...prev,
      preloadProgress: progress,
    }));

    // 继续处理下一个文件（使用setTimeout避免阻塞UI）
    preloadTimeoutRef.current = setTimeout(() => {
      processPreloadQueue();
    }, 50); // 50ms间隔，避免阻塞UI
  }, [codeMap, preloadFileRender, preloadState.preloadedFiles.size]);

  // 启动预加载
  const startPreloading = useCallback(() => {
    if (Object.keys(codeMap).length === 0 || preloadState.isPreloading) {
      return;
    }

    // 获取所有文件，排除当前显示的文件
    const allFiles = Object.keys(codeMap);
    const filesToPreload = allFiles.filter(fileName => 
      fileName !== activeFileTab && !preloadState.preloadedFiles.has(fileName)
    );

    if (filesToPreload.length === 0) {
      return;
    }

    // 按优先级排序：Java文件优先
    filesToPreload.sort((a, b) => {
      const aIsJava = a.endsWith('.java');
      const bIsJava = b.endsWith('.java');
      if (aIsJava && !bIsJava) return -1;
      if (!aIsJava && bIsJava) return 1;
      return 0;
    });

    preloadQueueRef.current = filesToPreload;
    setPreloadState(prev => ({
      ...prev,
      isPreloading: true,
      preloadProgress: 0,
    }));

    // 延迟启动预加载，确保首个文件已完全渲染
    setTimeout(() => {
      processPreloadQueue();
    }, 500);
  }, [codeMap, activeFileTab, preloadState.isPreloading, preloadState.preloadedFiles, processPreloadQueue]);

  // 加载代码预览
  const loadPreview = useCallback(async () => {
    if (!params.id) return;

    setLoading(true);
    try {
      const result = await codegenApi.previewCode(Number(params.id));
      setCodeMap(result);
      // 清空缓存和预加载状态，因为数据已更新
      codeCache.clear();
      setPreloadState({
        isPreloading: false,
        preloadedFiles: new Set(),
        preloadProgress: 0,
      });
      preloadQueueRef.current = [];
      if (preloadTimeoutRef.current) {
        clearTimeout(preloadTimeoutRef.current);
        preloadTimeoutRef.current = null;
      }
    } catch (error) {
      message.error('加载代码预览失败');
      console.error('加载代码预览失败:', error);
    } finally {
      setLoading(false);
    }
  }, [params.id]);

  // 初始化加载
  useEffect(() => {
    loadPreview();
  }, [loadPreview]);

  // 当fileGroups变化时，自动设置默认选中
  useEffect(() => {
    if (Object.keys(codeMap).length === 0) return;

    let defaultFileKey = '';

    // 优先选择后端代码
    if (fileGroups.backend.files.length > 0) {
      setActiveGroupTab('backend');
      defaultFileKey = fileGroups.backend.files[0];
    } else if (fileGroups.frontend.files.length > 0) {
      setActiveGroupTab('frontend');
      defaultFileKey = fileGroups.frontend.files[0];
    }

    if (defaultFileKey) {
      setActiveFileTab(defaultFileKey);
    }
  }, [fileGroups, codeMap]);

  // 当第一个文件显示后，启动预加载
  useEffect(() => {
    if (activeFileTab && Object.keys(codeMap).length > 1 && !loading) {
      startPreloading();
    }
  }, [activeFileTab, codeMap, loading, startPreloading]);

  // 组件卸载时清理定时器
  useEffect(() => {
    return () => {
      if (preloadTimeoutRef.current) {
        clearTimeout(preloadTimeoutRef.current);
      }
    };
  }, []);

  // 复制代码
  const handleCopy = useCallback(async (code: string, fileName: string) => {
    try {
      await navigator.clipboard.writeText(code);
      message.success(`已复制 ${fileName} 的代码`);
    } catch (error) {
      message.error('复制失败');
    }
  }, []);

  // 下载代码
  const handleDownload = async () => {
    if (!params.id) return;

    try {
      const blob = await codegenApi.generateCode(Number(params.id));

      // 创建下载链接
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `code_${Date.now()}.zip`;

      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      message.success('代码下载成功');
    } catch (error) {
      message.error('代码下载失败');
      console.error('代码下载失败:', error);
    }
  };

  // 返回上一页
  const handleBack = () => {
    navigate('/dev/codegen');
  };

  // 优化的Tab切换处理
  const handleGroupTabChange = useCallback((key: string) => {
    setActiveGroupTab(key);
    // 切换分组时，自动选择该分组的第一个文件
    const group = fileGroups[key];
    if (group && group.files.length > 0) {
      setActiveFileTab(group.files[0]);
    }
  }, [fileGroups]);

  const handleTreeSelect = useCallback((selectedKeys: React.Key[]) => {
    if (selectedKeys.length > 0) {
      const key = selectedKeys[0] as string;
      // 只有选中文件才切换（非文件夹）
      if (codeMap[key]) {
        setActiveFileTab(key);
      }
    }
  }, [codeMap]);

  // 渲染文件内容，使用缓存优化
  const renderFileContent = useCallback((fileName: string) => {
    const cacheKey = `${fileName}_${codeMap[fileName]?.length || 0}`;
    
    if (codeCache.has(cacheKey)) {
      return codeCache.get(cacheKey);
    }

    const content = (
      <CodeDisplay
        fileName={fileName}
        code={codeMap[fileName] || ''}
        onCopy={handleCopy}
      />
    );

    // 只缓存小文件的渲染结果
    if ((codeMap[fileName]?.length || 0) < 50000) {
      codeCache.set(cacheKey, content);
    }

    return content;
  }, [codeMap, handleCopy]);

  return (
    <div>
      <Card
        title={
          <Space>
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={handleBack}
            >
              返回
            </Button>
            <span>代码预览</span>
          </Space>
        }
        extra={
          <Space>
            <Button
              icon={<ReloadOutlined />}
              onClick={loadPreview}
              loading={loading}
            >
              刷新预览
            </Button>
            <Button
              type="primary"
              icon={<DownloadOutlined />}
              onClick={handleDownload}
              disabled={Object.keys(codeMap).length === 0}
            >
              下载代码
            </Button>
          </Space>
        }
      >
        <Alert
          message="代码预览"
          description="以下是根据表配置生成的代码预览，您可以检查代码质量，确认无误后下载使用。"
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />

        {/* 预加载进度指示器 */}
        {preloadState.isPreloading && (
          <div style={{ marginBottom: 16 }}>
            <Progress
              percent={Math.round(preloadState.preloadProgress)}
              size="small"
              status="active"
              format={(percent) => `预加载中 ${percent}%`}
            />
          </div>
        )}

        <Spin spinning={loading}>
          {Object.keys(codeMap).length > 0 ? (
            <Tabs
              activeKey={activeGroupTab}
              onChange={handleGroupTabChange}
              type="card"
              size="small"
            >
              {Object.entries(fileGroups).map(([groupKey, group]) =>
                group.files.length > 0 && (
                  <TabPane tab={group.label} key={groupKey}>
                    <div style={{ display: 'flex', gap: 16, minHeight: 500 }}>
                      {/* 左侧树形文件列表 */}
                      <div
                        style={{
                          width: 500,
                          minWidth: 500,
                          borderRight: '1px solid #f0f0f0',
                          paddingRight: 16,
                          overflowY: 'auto',
                          overflowX: 'hidden',
                          maxHeight: 'calc(100vh - 300px)',
                        }}
                      >
                        <Tree
                          showIcon
                          defaultExpandAll
                          selectedKeys={[activeFileTab]}
                          onSelect={handleTreeSelect}
                          treeData={group.tree}
                          switcherIcon={<FolderOpenOutlined />}
                          style={{ whiteSpace: 'nowrap' }}
                        />
                      </div>
                      
                      {/* 右侧代码显示区 */}
                      <div style={{ flex: 1, minWidth: 0, overflow: 'hidden' }}>
                        {activeFileTab && codeMap[activeFileTab] ? (
                          renderFileContent(activeFileTab)
                        ) : (
                          <div style={{ 
                            textAlign: 'center', 
                            padding: '100px 0',
                            color: '#999'
                          }}>
                            <FileOutlined style={{ fontSize: 48, marginBottom: 16 }} />
                            <div>请从左侧选择文件查看</div>
                          </div>
                        )}
                      </div>
                    </div>
                  </TabPane>
                )
              )}
            </Tabs>
          ) : (
            !loading && (
              <div style={{ textAlign: 'center', padding: '40px 0' }}>
                <Text type="secondary">暂无代码预览</Text>
              </div>
            )
          )}
        </Spin>
      </Card>
    </div>
  );
};

export default PreviewCode;
