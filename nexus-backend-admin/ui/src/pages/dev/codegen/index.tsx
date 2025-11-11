import React, { useState, useEffect, useRef } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Input,
  Form,
  Modal,
  Popconfirm,
  Tag,
  Divider,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  EyeOutlined,
  DownloadOutlined,
  SyncOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate } from 'react-router-dom';
import { codegenApi, CodegenTable, PageResult } from '@/services/codegen/codegenApi';
import { globalMessage } from '@/utils/globalMessage';
import { useTableHeight } from '@/hooks/useTableHeight';

/**
 * 代码生成表列表页面
 */
const CodegenTableList: React.FC = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [data, setData] = useState<CodegenTable[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [size, setSize] = useState(10);

  // 创建表格容器的 ref
  const tableContainerRef = useRef<HTMLDivElement>(null);
  // 计算表格高度，有搜索栏+按钮栏+分页器：190px
  const tableHeight = useTableHeight(tableContainerRef, 190);

  // 表格列配置
  const columns: ColumnsType<CodegenTable> = [
    {
      title: '表名',
      dataIndex: 'tableName',
      key: 'tableName',
      width: 200,
      render: (text: string) => <code>{text}</code>,
    },
    {
      title: '表描述',
      dataIndex: 'tableComment',
      key: 'tableComment',
      width: 150,
    },
    {
      title: '实体类名',
      dataIndex: 'className',
      key: 'className',
      width: 150,
      render: (text: string) => <Tag color="blue">{text}</Tag>,
    },
    {
      title: '模块名',
      dataIndex: 'moduleName',
      key: 'moduleName',
      width: 120,
    },
    {
      title: '业务名',
      dataIndex: 'businessName',
      key: 'businessName',
      width: 120,
    },
    {
      title: '前端类型',
      dataIndex: 'frontType',
      key: 'frontType',
      width: 100,
      render: (frontType: number) => {
        const typeMap: Record<number, { text: string; color: string }> = {
          10: { text: 'Vue2', color: 'green' },
          20: { text: 'Vue3', color: 'cyan' },
          30: { text: 'React', color: 'purple' },
        };
        const config = typeMap[frontType] || { text: '未知', color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '模板类型',
      dataIndex: 'templateType',
      key: 'templateType',
      width: 100,
      render: (templateType: number) => {
        const typeMap: Record<number, string> = {
          1: '单表',
          2: '树表',
          3: '主子表',
        };
        return typeMap[templateType] || '未知';
      },
    },
    {
      title: '创建时间',
      dataIndex: 'dateCreated',
      key: 'dateCreated',
      width: 180,
      render: (text: string) => text ? new Date(text).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 280,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => handlePreview(record.id!)}
          >
            预览
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record.id!)}
          >
            编辑
          </Button>
          {/* <Button
            type="link"
            size="small"
            icon={<SyncOutlined />}
            onClick={() => handleSync(record.id!)}
          >
            同步
          </Button> */}
          <Button
            type="link"
            size="small"
            icon={<DownloadOutlined />}
            onClick={() => handleDownload(record.id!)}
          >
            生成
          </Button>
          <Popconfirm
            title="确定要删除这个表配置吗？"
            onConfirm={() => handleDelete(record.id!)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  // 加载数据
  const loadData = async (params?: any) => {
    setLoading(true);
    try {
      const searchParams = {
        current,
        size,
        ...form.getFieldsValue(),
        ...params,
      };
      
      const result: PageResult<CodegenTable> = await codegenApi.getTableList(searchParams);
      setData(result.list);
      setTotal(result.total);
      setCurrent(result.current);
    } catch (error) {
      globalMessage.error('加载数据失败');
      console.error('加载数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始化加载
  useEffect(() => {
    loadData();
  }, [current, size]);

  // 搜索
  const handleSearch = () => {
    setCurrent(1);
    loadData({ current: 1 });
  };

  // 重置
  const handleReset = () => {
    form.resetFields();
    setCurrent(1);
    loadData({ current: 1 });
  };

  // 导入表
  const handleImport = () => {
    navigate('/dev/codegen/import');
  };

  // 编辑表配置
  const handleEdit = (id: number) => {
    navigate(`/dev/codegen/edit/${id}`);
  };

  // 预览代码
  const handlePreview = (id: number) => {
    navigate(`/dev/codegen/preview/${id}`);
  };

  // 同步表结构
  const handleSync = async (id: number) => {
    try {
      await codegenApi.syncTable(id);
      globalMessage.success('同步表结构成功');
      loadData();
    } catch (error) {
      globalMessage.error('同步表结构失败');
      console.error('同步失败:', error);
    }
  };

  // 下载代码
  const handleDownload = async (id: number) => {
    try {
      const blob = await codegenApi.generateCode(id);
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      
      // 从表格中找到对应记录，用于生成文件名
      const record = data.find(item => item.id === id);
      const fileName = `${record?.className || 'code'}_${Date.now()}.zip`;
      link.download = fileName;
      
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      globalMessage.success('代码生成成功');
    } catch (error) {
      globalMessage.error('代码生成失败');
      console.error('代码生成失败:', error);
    }
  };

  // 删除表配置
  const handleDelete = async (id: number) => {
    try {
      await codegenApi.deleteTable(id);
      globalMessage.success('删除表配置成功');
      loadData();
    } catch (error) {
      globalMessage.error('删除表配置失败');
      console.error('删除失败:', error);
    }
  };

  return (
    <div>
      <Card>
        <Form
          form={form}
          layout="inline"
          onFinish={handleSearch}
          style={{ marginBottom: 16 }}
        >
          <Form.Item label="表名" name="tableName">
            <Input placeholder="请输入表名" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item label="表描述" name="tableComment">
            <Input placeholder="请输入表描述" style={{ width: 200 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<SearchOutlined />}>
                搜索
              </Button>
              <Button onClick={handleReset} icon={<ReloadOutlined />}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>

        <div style={{ marginBottom: 16 }}>
          <Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={handleImport}>
              导入表
            </Button>
            <Button icon={<ReloadOutlined />} onClick={() => loadData()}>
              刷新
            </Button>
          </Space>
        </div>

        <div ref={tableContainerRef}>
          <Table
            columns={columns}
            dataSource={data}
            loading={loading}
            rowKey="id"
            scroll={{ y: tableHeight }}
            pagination={{
            current,
            pageSize: size,
            total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
            onChange: (page, pageSize) => {
              setCurrent(page);
              setSize(pageSize || 10);
            },
          }}
          />
        </div>
      </Card>
    </div>
  );
};

export default CodegenTableList;
