import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
  Card,
  Button,
  Space,
  message,
  Tabs,
  Typography,
  Alert,
  Spin,
} from 'antd';
import {
  ArrowLeftOutlined,
  DownloadOutlined,
  CopyOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { codegenApi } from '../../services/codegen';

const { TabPane } = Tabs;
const { Text } = Typography;

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

  // 语言映射
  const getLanguage = (fileName: string): string => {
    if (fileName.endsWith('.java')) return 'java';
    if (fileName.endsWith('.tsx') || fileName.endsWith('.ts')) return 'typescript';
    if (fileName.endsWith('.vue')) return 'vue';
    if (fileName.endsWith('.xml')) return 'xml';
    if (fileName.endsWith('.sql')) return 'sql';
    if (fileName.endsWith('.json')) return 'json';
    return 'text';
  };

  // 文件类型分组
  const groupFiles = (codeMap: Record<string, string>) => {
    const groups: Record<string, { label: string; files: Array<{ key: string; name: string }> }> = {
      backend: { label: '后端代码', files: [] },
      frontend: { label: '前端代码', files: [] },
      sql: { label: 'SQL脚本', files: [] },
      config: { label: '配置文件', files: [] },
    };

    Object.keys(codeMap).forEach(fileName => {
      if (fileName.endsWith('.java')) {
        groups.backend.files.push({ key: fileName, name: fileName });
      } else if (fileName.endsWith('.tsx') || fileName.endsWith('.ts') || fileName.endsWith('.vue')) {
        groups.frontend.files.push({ key: fileName, name: fileName });
      } else if (fileName.endsWith('.sql')) {
        groups.sql.files.push({ key: fileName, name: fileName });
      } else {
        groups.config.files.push({ key: fileName, name: fileName });
      }
    });

    return groups;
  };

  // 使用useMemo优化文件分组计算
  const fileGroups = useMemo(() => groupFiles(codeMap), [codeMap]);

  // 加载代码预览
  const loadPreview = useCallback(async () => {
    if (!params.id) return;

    setLoading(true);
    try {
      const result = await codegenApi.previewCode(Number(params.id));
      setCodeMap(result);
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
      defaultFileKey = fileGroups.backend.files[0].key;
    } else if (fileGroups.frontend.files.length > 0) {
      setActiveGroupTab('frontend');
      defaultFileKey = fileGroups.frontend.files[0].key;
    } else if (fileGroups.sql.files.length > 0) {
      setActiveGroupTab('sql');
      defaultFileKey = fileGroups.sql.files[0].key;
    } else if (fileGroups.config.files.length > 0) {
      setActiveGroupTab('config');
      defaultFileKey = fileGroups.config.files[0].key;
    }

    if (defaultFileKey) {
      setActiveFileTab(defaultFileKey);
    }
  }, [fileGroups, codeMap]);

  // 复制代码
  const handleCopy = async (code: string, fileName: string) => {
    try {
      await navigator.clipboard.writeText(code);
      message.success(`已复制 ${fileName} 的代码`);
    } catch (error) {
      message.error('复制失败');
    }
  };

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
    navigate('/codegen/table');
  };

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

        <Spin spinning={loading}>
          {Object.keys(codeMap).length > 0 ? (
            <Tabs
              activeKey={activeGroupTab}
              onChange={(key) => {
                setActiveGroupTab(key);
                // 切换分组时，自动选择该分组的第一个文件
                const group = fileGroups[key];
                if (group && group.files.length > 0) {
                  setActiveFileTab(group.files[0].key);
                }
              }}
              type="card"
              size="small"
            >
              {Object.entries(fileGroups).map(([groupKey, group]) =>
                group.files.length > 0 && (
                  <TabPane tab={group.label} key={groupKey}>
                    <Tabs
                      activeKey={activeFileTab}
                      onChange={setActiveFileTab}
                      tabPosition="left"
                      style={{ minHeight: 400 }}
                    >
                      {group.files.map(file => (
                        <TabPane
                          tab={
                            <div style={{ textAlign: 'left', maxWidth: 200 }}>
                              <Text ellipsis={{ tooltip: file.name }}>
                                {file.name}
                              </Text>
                            </div>
                          }
                          key={file.key}
                        >
                          <Card
                            size="small"
                            title={
                              <Space>
                                <Text code>{file.name}</Text>
                                <Text type="secondary">
                                  ({codeMap[file.key]?.split('\n').length || 0} 行)
                                </Text>
                              </Space>
                            }
                            extra={
                              <Button
                                size="small"
                                icon={<CopyOutlined />}
                                onClick={() => handleCopy(codeMap[file.key], file.name)}
                              >
                                复制
                              </Button>
                            }
                          >
                            <SyntaxHighlighter
                              language={getLanguage(file.name)}
                              style={oneLight}
                              showLineNumbers
                              wrapLines
                              customStyle={{
                                margin: 0,
                                fontSize: '12px',
                                maxHeight: '600px',
                                overflow: 'auto',
                              }}
                            >
                              {codeMap[file.key] || ''}
                            </SyntaxHighlighter>
                          </Card>
                        </TabPane>
                      ))}
                    </Tabs>
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
