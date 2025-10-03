# Modal 弹窗高度控制使用指南

## 📋 问题背景

当弹窗内容过多时，如果不控制高度，会导致整个浏览器页面出现滚动条，用户体验不佳。

**正确做法**：在弹窗内部出现滚动条，弹窗本身保持在视口可见范围内。

---

## ✅ 解决方案

### 方案一：直接使用 bodyStyle（推荐）

最简单直接的方式，适用于大部分场景：

```tsx
import { Modal } from 'antd';

<Modal
  title="编辑用户"
  open={visible}
  onOk={handleOk}
  onCancel={handleCancel}
  width={600}
  destroyOnClose
  bodyStyle={{ 
    maxHeight: 'calc(100vh - 300px)',  // 视口高度减去 300px（标题+按钮+边距）
    overflowY: 'auto',                 // 内容超出时显示滚动条
    paddingRight: '8px'                // 避免滚动条遮挡内容
  }}
>
  <Form>
    {/* 表单内容 */}
  </Form>
</Modal>
```

**高度计算说明**：
- `100vh`：视口高度（浏览器可视区域）
- `-300px`：为标题栏、底部按钮、上下边距预留空间
- 如果内容较少，可以调整为 `-350px`
- 如果内容很多，可以调整为 `-250px`

---

### 方案二：使用工具函数（适合批量调整）

引入预定义的配置工具：

```tsx
import { Modal } from 'antd';
import { getStandardModalProps, getLargeModalProps, getSmallModalProps } from '@/utils/modalConfig';

// 标准尺寸（600px 宽）
<Modal
  title="编辑用户"
  open={visible}
  onOk={handleOk}
  onCancel={handleCancel}
  {...getStandardModalProps()}
>
  <Form>{/* ... */}</Form>
</Modal>

// 大尺寸（800px 宽）
<Modal
  title="编辑详情"
  open={visible}
  onOk={handleOk}
  onCancel={handleCancel}
  {...getLargeModalProps()}
>
  <Form>{/* ... */}</Form>
</Modal>

// 小尺寸（400px 宽）
<Modal
  title="快速操作"
  open={visible}
  onOk={handleOk}
  onCancel={handleCancel}
  {...getSmallModalProps()}
>
  <Form>{/* ... */}</Form>
</Modal>

// 自定义覆盖
<Modal
  title="特殊弹窗"
  open={visible}
  {...getStandardModalProps({ width: 700 })}  // 覆盖宽度
>
  <Form>{/* ... */}</Form>
</Modal>
```

---

### 方案三：直接使用预定义样式常量

```tsx
import { Modal } from 'antd';
import { STANDARD_MODAL_BODY_STYLE } from '@/utils/modalConfig';

<Modal
  title="编辑用户"
  open={visible}
  width={600}
  bodyStyle={STANDARD_MODAL_BODY_STYLE}
>
  <Form>{/* ... */}</Form>
</Modal>
```

---

## 📦 预定义配置说明

| 配置类型 | 宽度 | 最大高度 | 适用场景 |
|---------|------|----------|---------|
| `STANDARD_MODAL_BODY_STYLE` | - | `vh - 300px` | 标准表单（10-15个字段） |
| `LARGE_MODAL_BODY_STYLE` | - | `vh - 250px` | 复杂表单（15+字段） |
| `SMALL_MODAL_BODY_STYLE` | - | `vh - 350px` | 简单表单（5-10个字段） |
| `getStandardModalProps()` | 600px | `vh - 300px` | 标准弹窗 |
| `getLargeModalProps()` | 800px | `vh - 250px` | 大尺寸弹窗 |
| `getSmallModalProps()` | 400px | `vh - 350px` | 小尺寸弹窗 |

---

## 🎯 已应用的页面

以下页面已经应用了弹窗高度控制：

- ✅ `pages/system/menu/index.tsx` - 菜单管理
- ✅ `pages/system/role/index.tsx` - 角色管理
- ✅ **代码生成模板** - `templates/react/List.tsx.vm`

---

## 🚀 代码生成模板

新生成的前端页面会**自动**包含弹窗高度控制，无需手动调整：

```tsx
// 生成的代码（List.tsx.vm）
<Modal
  title={editingRecord ? '编辑XX' : '新增XX'}
  open={formVisible}
  onCancel={handleCancel}
  footer={null}
  width={800}
  destroyOnClose
  bodyStyle={{ 
    maxHeight: 'calc(100vh - 300px)', 
    overflowY: 'auto',
    paddingRight: '8px' 
  }}
>
  <XXForm {...props} />
</Modal>
```

---

## 🔧 调整技巧

### 根据字段数量调整高度

```tsx
// 字段少（<10个）
bodyStyle={{ maxHeight: 'calc(100vh - 350px)', ... }}

// 字段中等（10-15个）
bodyStyle={{ maxHeight: 'calc(100vh - 300px)', ... }}  // 默认

// 字段多（>15个）
bodyStyle={{ maxHeight: 'calc(100vh - 250px)', ... }}
```

### 特殊场景

```tsx
// 全屏弹窗（占满屏幕）
<Modal
  title="数据详情"
  open={visible}
  width="90vw"
  bodyStyle={{ 
    maxHeight: 'calc(100vh - 200px)', 
    overflowY: 'auto' 
  }}
>
  {/* ... */}
</Modal>

// 固定高度弹窗
<Modal
  bodyStyle={{ 
    height: '500px',      // 固定 500px
    overflowY: 'auto' 
  }}
>
  {/* ... */}
</Modal>
```

---

## ✏️ 实际示例

### 示例1：菜单管理弹窗（字段多）

```tsx
<Modal
  title={editingRecord ? '编辑菜单' : '新增菜单'}
  open={formVisible}
  onOk={handleSave}
  onCancel={() => setFormVisible(false)}
  width={600}
  destroyOnClose
  bodyStyle={{ 
    maxHeight: 'calc(100vh - 300px)', 
    overflowY: 'auto',
    paddingRight: '8px' 
  }}
>
  <Form form={form} layout="vertical">
    <Form.Item label="父级菜单" name="parentId">...</Form.Item>
    <Form.Item label="菜单名称" name="name">...</Form.Item>
    <Form.Item label="菜单类型" name="type">...</Form.Item>
    {/* 更多字段... */}
  </Form>
</Modal>
```

### 示例2：角色管理弹窗（字段少）

```tsx
<Modal
  title={editingRecord ? '编辑角色' : '新增角色'}
  open={formVisible}
  onOk={handleSave}
  onCancel={() => setFormVisible(false)}
  width={600}
  destroyOnClose
  bodyStyle={{ 
    maxHeight: 'calc(100vh - 300px)', 
    overflowY: 'auto',
    paddingRight: '8px' 
  }}
>
  <Form form={form} layout="vertical">
    <Form.Item label="角色名称" name="name">...</Form.Item>
    <Form.Item label="角色编码" name="code">...</Form.Item>
    <Form.Item label="显示顺序" name="sort">...</Form.Item>
    <Form.Item label="备注" name="remark">...</Form.Item>
  </Form>
</Modal>
```

---

## ⚠️ 注意事项

1. **必须设置 `overflowY: 'auto'`**  
   否则内容超出时不会出现滚动条

2. **建议添加 `paddingRight: '8px'`**  
   避免滚动条遮挡右侧内容

3. **配合 `destroyOnClose` 使用**  
   关闭弹窗时销毁内容，避免缓存问题

4. **响应式设计**  
   使用 `calc(100vh - XXpx)` 而不是固定像素值，适配不同屏幕高度

5. **测试不同分辨率**  
   在小屏幕（如笔记本 1366x768）和大屏幕上都要测试

---

## 🎨 效果对比

### ❌ 修改前（浏览器滚动）
```
┌─────────────────────────────────┐
│ 浏览器窗口                        │
│                                 │
│  ┌──────────────────────┐       │
│  │ Modal Title          │       │
│  ├──────────────────────┤       │
│  │ 字段1                 │       │
│  │ 字段2                 │       │
│  │ 字段3                 │       │
│  │ ...                  │       │
└──┼──────────────────────┼───────┘
   │ 字段15                │   ↑
   │ 字段16                │   │ 浏览器滚动条
   ├──────────────────────┤   ↓
   │ [确定] [取消]         │
   └──────────────────────┘
```

### ✅ 修改后（弹窗内滚动）
```
┌─────────────────────────────────┐
│ 浏览器窗口                        │
│                                 │
│  ┌──────────────────────┐       │
│  │ Modal Title          │       │
│  ├──────────────────────┤       │
│  │ 字段1                 │ ↑     │
│  │ 字段2                 │ │     │
│  │ 字段3                 │ │弹窗 │
│  │ ...                  │ │滚动 │
│  │ 字段15                │ │条   │
│  │ 字段16                │ ↓     │
│  ├──────────────────────┤       │
│  │ [确定] [取消]         │       │
│  └──────────────────────┘       │
│                                 │
└─────────────────────────────────┘
```

---

## 📚 相关资源

- [Ant Design Modal 文档](https://ant.design/components/modal-cn)
- [CSS calc() 函数](https://developer.mozilla.org/zh-CN/docs/Web/CSS/calc)
- [视口单位 vh](https://developer.mozilla.org/zh-CN/docs/Web/CSS/length#%E8%A7%86%E5%8F%A3%E5%8D%95%E4%BD%8D)

---

**最后更新**: 2025-10-03

