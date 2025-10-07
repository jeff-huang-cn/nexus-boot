import React from 'react';
import { Card, Row, Col, Statistic, Typography, Space } from 'antd';
import {
  DatabaseOutlined,
  ToolOutlined,
  FileTextOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';

const { Title, Paragraph } = Typography;

const Dashboard: React.FC = () => {
  return (
    <div>
      <Typography style={{ marginBottom: 24 }}>
        <Title level={2}>代码生成管理平台</Title>
        <Paragraph>
          欢迎使用代码生成管理平台！这是一个基于Spring Boot + React的代码生成工具，
          可以快速生成CRUD代码和前端页面，提高开发效率。
        </Paragraph>
      </Typography>

      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="已导入表数量"
              value={0}
              prefix={<DatabaseOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已生成代码数"
              value={0}
              prefix={<ToolOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="可用模板数"
              value={6}
              prefix={<FileTextOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="系统运行时间"
              value="0小时"
              prefix={<ClockCircleOutlined />}
              valueStyle={{ color: '#fa8c16' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={16}>
        <Col span={12}>
          <Card title="功能特性" bordered={false}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <div>
                <strong>数据库表导入和管理</strong>
                <p>支持从MySQL数据库导入表结构，自动解析字段类型和注释</p>
              </div>
              <div>
                <strong>可视化字段配置</strong>
                <p>提供友好的界面配置字段属性、验证规则和显示类型</p>
              </div>
              <div>
                <strong>多种代码模板支持</strong>
                <p>支持生成Java后端代码和React前端页面</p>
              </div>
              <div>
                <strong>实时代码预览</strong>
                <p>在生成前可以预览代码内容，确保生成结果符合预期</p>
              </div>
            </Space>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="快速开始" bordered={false}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <div>
                <strong>1. 导入数据库表</strong>
                <p>在"代码生成 → 导入表"中选择需要生成代码的数据库表</p>
              </div>
              <div>
                <strong>2. 配置生成参数</strong>
                <p>在"代码生成 → 表管理"中配置类名、字段属性等参数</p>
              </div>
              <div>
                <strong>3. 预览和生成</strong>
                <p>预览生成的代码内容，确认无误后下载代码文件</p>
              </div>
              <div>
                <strong>4. 集成到项目</strong>
                <p>将生成的代码集成到您的项目中，加速开发进程</p>
              </div>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default Dashboard;
