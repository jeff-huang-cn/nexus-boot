import React, { useState, useEffect, useCallback } from 'react';
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Space,
  message,
  Table,
  Checkbox,
  Row,
  Col,
  Alert,
} from 'antd';
import {
  SaveOutlined,
  ArrowLeftOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useNavigate, useParams } from 'react-router-dom';
import { codegenApi } from '../../../services/codegen/codegen';
import type { CodegenTable, CodegenColumn } from '../../../types/codegen';

const { Option } = Select;
const { TextArea } = Input;

/**
 * 编辑表配置页面
 */
const EditTable: React.FC = () => {
  const navigate = useNavigate();
  const params = useParams<{ id: string }>();
  const [tableForm] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [tableData, setTableData] = useState<CodegenTable | null>(null);
  const [columns, setColumns] = useState<CodegenColumn[]>([]);

  // 字段表格列配置
  const columnTableColumns: ColumnsType<CodegenColumn> = [
    {
      title: '字段名',
      dataIndex: 'columnName',
      key: 'columnName',
      width: 150,
      render: (text: string) => <code>{text}</code>,
    },
    {
      title: '字段描述',
      dataIndex: 'columnComment',
      key: 'columnComment',
      width: 150,
      render: (text: string, record: CodegenColumn, index: number) => (
        <Input
          value={text}
          onChange={(e) => updateColumn(index, 'columnComment', e.target.value)}
          placeholder="字段描述"
          size="small"
        />
      ),
    },
    {
      title: 'Java类型',
      dataIndex: 'javaType',
      key: 'javaType',
      width: 120,
      render: (text: string, record: CodegenColumn, index: number) => (
        <Select
          value={text}
          onChange={(value) => updateColumn(index, 'javaType', value)}
          size="small"
          style={{ width: '100%' }}
        >
          <Option value="String">String</Option>
          <Option value="Integer">Integer</Option>
          <Option value="Long">Long</Option>
          <Option value="BigDecimal">BigDecimal</Option>
          <Option value="Boolean">Boolean</Option>
          <Option value="LocalDateTime">LocalDateTime</Option>
          <Option value="LocalDate">LocalDate</Option>
        </Select>
      ),
    },
    {
      title: 'Java字段',
      dataIndex: 'javaField',
      key: 'javaField',
      width: 150,
      render: (text: string, record: CodegenColumn, index: number) => (
        <Input
          value={text}
          onChange={(e) => updateColumn(index, 'javaField', e.target.value)}
          placeholder="Java字段名"
          size="small"
        />
      ),
    },
    {
      title: 'TS类型',
      dataIndex: 'typescriptType',
      key: 'typescriptType',
      width: 100,
      render: (text: string, record: CodegenColumn, index: number) => (
        <Select
          value={text}
          onChange={(value) => updateColumn(index, 'typescriptType', value)}
          size="small"
          style={{ width: '100%' }}
        >
          <Option value="string">string</Option>
          <Option value="number">number</Option>
          <Option value="boolean">boolean</Option>
        </Select>
      ),
    },
    {
      title: '表单类型',
      dataIndex: 'htmlType',
      key: 'htmlType',
      width: 120,
      render: (text: string, record: CodegenColumn, index: number) => (
        <Select
          value={text}
          onChange={(value) => updateColumn(index, 'htmlType', value)}
          size="small"
          style={{ width: '100%' }}
        >
          <Option value="input">输入框</Option>
          <Option value="textarea">文本域</Option>
          <Option value="select">下拉框</Option>
          <Option value="radio">单选框</Option>
          <Option value="checkbox">复选框</Option>
          <Option value="datetime">日期时间</Option>
          <Option value="date">日期</Option>
          <Option value="number">数字</Option>
        </Select>
      ),
    },
    {
      title: '查询',
      dataIndex: 'listOperation',
      key: 'listOperation',
      width: 60,
      align: 'center',
      render: (checked: boolean, record: CodegenColumn, index: number) => (
        <Checkbox
          checked={checked}
          onChange={(e) => updateColumn(index, 'listOperation', e.target.checked)}
        />
      ),
    },
    {
      title: '查询方式',
      dataIndex: 'listOperationCondition',
      key: 'listOperationCondition',
      width: 100,
      render: (value: string, record: CodegenColumn, index: number) => (
        <Select
          value={value || 'EQ'}
          size="small"
          style={{ width: '90px' }}
          onChange={(val) => updateColumn(index, 'listOperationCondition', val)}
          disabled={!record.listOperation}
        >
          <Option value="EQ">等于</Option>
          <Option value="LIKE">模糊</Option>
          <Option value="BETWEEN">范围</Option>
          <Option value="GT">大于</Option>
          <Option value="LT">小于</Option>
          <Option value="IN">包含</Option>
        </Select>
      ),
    },
    {
      title: '列表',
      dataIndex: 'listOperationResult',
      key: 'listOperationResult',
      width: 60,
      align: 'center',
      render: (checked: boolean, record: CodegenColumn, index: number) => (
        <Checkbox
          checked={checked}
          onChange={(e) => updateColumn(index, 'listOperationResult', e.target.checked)}
        />
      ),
    },
    {
      title: '新增',
      dataIndex: 'createOperation',
      key: 'createOperation',
      width: 60,
      align: 'center',
      render: (checked: boolean, record: CodegenColumn, index: number) => (
        <Checkbox
          checked={checked}
          onChange={(e) => updateColumn(index, 'createOperation', e.target.checked)}
        />
      ),
    },
    {
      title: '编辑',
      dataIndex: 'updateOperation',
      key: 'updateOperation',
      width: 60,
      align: 'center',
      render: (checked: boolean, record: CodegenColumn, index: number) => (
        <Checkbox
          checked={checked}
          onChange={(e) => updateColumn(index, 'updateOperation', e.target.checked)}
        />
      ),
    },
    {
      title: '必填',
      dataIndex: 'nullable',
      key: 'nullable',
      width: 60,
      align: 'center',
      render: (nullable: boolean, record: CodegenColumn, index: number) => (
        <Checkbox
          checked={!nullable}
          onChange={(e) => updateColumn(index, 'nullable', !e.target.checked)}
        />
      ),
    },
  ];

  // 加载表配置详情
  const loadTableDetail = useCallback(async () => {
    if (!params.id) return;
    
    setLoading(true);
    try {
      const detail = await codegenApi.getTableDetail(Number(params.id));
      console.log('加载表配置成功:', detail.tableName);
      
      // 后端返回的是CodegenTableVO，直接包含了table信息和columns
      setTableData(detail);
      setColumns(detail.columns || []);
    } catch (error) {
      message.error('加载表配置失败');
      console.error('加载表配置失败:', error);
    } finally {
      setLoading(false);
    }
  }, [params.id, tableForm]);

  // 更新字段配置
  const updateColumn = (index: number, field: string, value: any) => {
    const newColumns = [...columns];
    newColumns[index] = { ...newColumns[index], [field]: value };
    setColumns(newColumns);
  };


  // 初始化加载
  useEffect(() => {
    loadTableDetail();
  }, [params.id, loadTableDetail]);

  // 不再需要useEffect来设置表单值，initialValues会自动处理

  // 保存表配置
  const handleSave = async () => {
    try {
      const formValues = await tableForm.validateFields();
      setSaving(true);
      
      const updateData = {
        table: {
          ...tableData,
          ...formValues,
          id: Number(params.id),
        },
        columns,
      };

      await codegenApi.updateTableConfig(Number(params.id), updateData);
      message.success('保存成功');
    } catch (error) {
      message.error('保存失败');
      console.error('保存失败:', error);
    } finally {
      setSaving(false);
    }
  };

  // 同步表结构
  const handleSync = async () => {
    if (!params.id) return;
    
    try {
      await codegenApi.syncTable(Number(params.id));
      message.success('同步成功');
      loadTableDetail(); // 重新加载数据
    } catch (error) {
      message.error('同步失败');
      console.error('同步失败:', error);
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
            <span>配置表：{tableData?.tableName}</span>
          </Space>
        }
        extra={
          <Space>
            <Button
              icon={<ReloadOutlined />}
              onClick={handleSync}
            >
              同步表结构
            </Button>
            <Button
              type="primary"
              icon={<SaveOutlined />}
              onClick={handleSave}
              loading={saving}
            >
              保存配置
            </Button>
          </Space>
        }
        loading={loading}
      >
        <Alert
          message="配置说明"
          description="系统已根据表名自动填充默认配置（参考yudao最佳实践），您可以根据需要调整。这些配置将影响生成的代码结构和功能。"
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
        />

        {/* 表基本信息配置 */}
        <Card title="基本信息" size="small" style={{ marginBottom: 16 }}>
          {tableData ? (
            <Form
              form={tableForm}
              layout="vertical"
              initialValues={{
                tableName: tableData.tableName,
                tableComment: tableData.tableComment,
                className: tableData.className,
                businessName: tableData.businessName,
                moduleName: tableData.moduleName,
                packageName: tableData.packageName,
                author: tableData.author,
                frontType: tableData.frontType || 30,
                templateType: tableData.templateType || 1,
                remark: tableData.remark
              }}
            >
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="表名"
                  name="tableName"
                >
                  <Input disabled />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="表描述"
                  name="tableComment"
                  rules={[{ required: true, message: '请输入表描述' }]}
                >
                  <Input placeholder="请输入表描述" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="实体类名"
                  name="className"
                  rules={[{ required: true, message: '请输入实体类名' }]}
                >
                  <Input placeholder="自动生成，如：UserInfo" />
                </Form.Item>
              </Col>
            </Row>
            
            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="模块名"
                  name="moduleName"
                  rules={[{ required: true, message: '请输入模块名' }]}
                >
                  <Input placeholder="自动生成，如：user" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="业务名"
                  name="businessName"
                  rules={[{ required: true, message: '请输入业务名' }]}
                >
                  <Input placeholder="自动生成，如：userInfo" />
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="包名"
                  name="packageName"
                  rules={[{ required: true, message: '请输入包名' }]}
                >
                  <Input placeholder="自动生成，如：com.beckend.admin.modules.user" />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={8}>
                <Form.Item
                  label="前端类型"
                  name="frontType"
                  rules={[{ required: true, message: '请选择前端类型' }]}
                >
                  <Select placeholder="请选择前端类型">
                    <Option value={30}>React</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="模板类型"
                  name="templateType"
                  rules={[{ required: true, message: '请选择模板类型' }]}
                >
                  <Select placeholder="请选择模板类型">
                    <Option value={1}>单表CRUD</Option>
                    <Option value={2}>树表CRUD</Option>
                    <Option value={3}>主子表CRUD</Option>
                  </Select>
                </Form.Item>
              </Col>
              <Col span={8}>
                <Form.Item
                  label="作者"
                  name="author"
                >
                  <Input placeholder="默认：beckend" />
                </Form.Item>
              </Col>
            </Row>

            <Row gutter={16}>
              <Col span={24}>
                <Form.Item
                  label="备注"
                  name="remark"
                >
                  <TextArea 
                    placeholder="请输入备注"
                    rows={3}
                  />
                </Form.Item>
              </Col>
            </Row>
            </Form>
          ) : (
            <div>加载中...</div>
          )}
        </Card>

        {/* 字段配置 */}
        <Card title="字段配置" size="small">
          <Table
            columns={columnTableColumns}
            dataSource={columns}
            rowKey="id"
            pagination={false}
            scroll={{ x: 1000 }}
            size="small"
          />
        </Card>
      </Card>
    </div>
  );
};

export default EditTable;
