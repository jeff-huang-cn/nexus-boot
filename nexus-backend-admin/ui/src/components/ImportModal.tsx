import React, { useState } from 'react';
import { Modal, Upload, Button, Alert } from 'antd';
import { InboxOutlined, DownloadOutlined } from '@ant-design/icons';
import type { UploadFile, UploadProps } from 'antd';
import { globalMessage } from '../utils/globalMessage';
import request from '../utils/request';

const { Dragger } = Upload;

interface ImportModalProps {
  visible: boolean;
  title?: string;
  templateUrl?: string;
  onImport: (file: File) => Promise<boolean>;
  onCancel: () => void;
  accept?: string;
}

/**
 * 通用导入对话框组件
 * 支持下载模板和上传文件
 */
const ImportModal: React.FC<ImportModalProps> = ({
  visible,
  title = '数据导入',
  templateUrl,
  onImport,
  onCancel,
  accept = '.xlsx,.xls',
}) => {
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const [uploading, setUploading] = useState(false);

  // 下载模板
  const handleDownloadTemplate = async () => {
    if (!templateUrl) {
      globalMessage.warning('模板下载地址未配置');
      return;
    }
    
    try {
      // 使用 axios request 下载文件（自动处理认证）
      const blob = await request.get(templateUrl, {
        responseType: 'blob',
      });
      
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `导入模板_${new Date().getTime()}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      
      globalMessage.success('模板下载成功');
    } catch (error) {
      globalMessage.error('模板下载失败');
      console.error('下载模板失败:', error);
    }
  };

  // 文件选择前的处理
  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    const isExcel = 
      file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
      file.type === 'application/vnd.ms-excel';
    
    if (!isExcel) {
      globalMessage.error('只能上传 Excel 文件！');
      return Upload.LIST_IGNORE;
    }

    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      globalMessage.error('文件大小不能超过 10MB！');
      return Upload.LIST_IGNORE;
    }

    setFileList([file]);
    return false; // 阻止自动上传
  };

  // 移除文件
  const handleRemove = () => {
    setFileList([]);
  };

  // 执行导入
  const handleImport = async () => {
    if (fileList.length === 0) {
      globalMessage.warning('请先选择要导入的文件');
      return;
    }

    setUploading(true);
    try {
      const file = fileList[0] as any;
      const success = await onImport(file);
      
      if (success !== false) {
        globalMessage.success('导入成功');
        setFileList([]);
        onCancel();
      }
    } catch (error: any) {
      globalMessage.error(error.message || '导入失败');
    } finally {
      setUploading(false);
    }
  };

  // 关闭对话框
  const handleCancel = () => {
    setFileList([]);
    onCancel();
  };

  const draggerProps: UploadProps = {
    name: 'file',
    multiple: false,
    fileList,
    beforeUpload,
    onRemove: handleRemove,
    accept,
    maxCount: 1,
  };

  return (
    <Modal
      title={title}
      open={visible}
      onCancel={handleCancel}
      footer={[
        <Button key="cancel" onClick={handleCancel}>
          取消
        </Button>,
        <Button
          key="import"
          type="primary"
          loading={uploading}
          onClick={handleImport}
          disabled={fileList.length === 0}
        >
          开始导入
        </Button>,
      ]}
      width={600}
      destroyOnClose
    >
      <div style={{ marginTop: 16 }}>
        {/* 拖拽上传区域 */}
        <Dragger {...draggerProps}>
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
          <p className="ant-upload-hint">
            支持单个文件上传，严禁上传公司数据或其他违禁文件
          </p>
        </Dragger>

        {/* 说明信息 */}
        <Alert
          message="导入说明"
          description={
            <ul style={{ margin: 0, paddingLeft: 20 }}>
              <li>请先下载导入模板，按照模板格式填写数据</li>
              <li>支持 .xlsx 和 .xls 格式的 Excel 文件</li>
              <li>单次导入文件大小不超过 10MB</li>
              <li>导入数据将会覆盖同名记录</li>
            </ul>
          }
          type="info"
          showIcon
          style={{ marginTop: 16 }}
        />

        {/* 下载模板按钮 */}
        {templateUrl && (
          <Button
            type="dashed"
            icon={<DownloadOutlined />}
            onClick={handleDownloadTemplate}
            block
            size="large"
            style={{ marginTop: 16 }}
          >
            下载导入模板
          </Button>
        )}
      </div>
    </Modal>
  );
};

export default ImportModal;

