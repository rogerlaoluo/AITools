# 开发者工具市场前端结构

## 📁 目录结构

```
static/
├── index.html                      # 主页 - 工具市场首页
├── css/
│   └── common.css                  # 共享样式文件
├── js/
│   └── common.js                   # 共享JavaScript函数
└── tools/                         # 工具页面目录
    ├── base64.html                # Base64 编解码工具
    ├── json-formatter.html        # JSON 格式化工具
    ├── qrcode-generator.html      # 二维码生成器
    ├── contract-renewal-evaluator.html  # 续签评估系统
    ├── encryption-tool.html       # 在线加密解密
    └── ascii-table.html           # ASCII 对照表
```

## 🏗️ 架构设计

### 1. 主页 (index.html)
- 显示所有工具的卡片式布局
- 左侧分类导航
- 点击工具卡片跳转到对应的工具页面

### 2. 共享资源

#### CSS (css/common.css)
- 全局样式
- 侧边栏样式
- 工具卡片样式
- 表单和按钮样式
- 响应式设计

#### JavaScript (js/common.js)
- 通用工具函数
- API 调用函数
- 结果显示函数
- 针对不同工具类型的结果渲染：
  - 二维码图片
  - ASCII 表格
  - 续签评估结果

### 3. 工具页面 (tools/*.html)

每个工具都有独立的 HTML 文件，包含：
- 返回首页的链接
- 工具特定的表单
- 工具特定的 JavaScript 逻辑

## 🎯 优势

### 可维护性
- ✅ 每个工具独立维护，互不影响
- ✅ 共享样式和逻辑，避免重复代码
- ✅ 修改某个工具不影响其他工具

### 可扩展性
- ✅ 添加新工具只需创建新的 HTML 文件
- ✅ 可以独立测试每个工具
- ✅ 支持自定义每个工具的页面逻辑

### 性能
- ✅ 首页加载更快（只加载工具列表）
- ✅ 按需加载工具页面
- ✅ 静态资源可以缓存

## 📝 添加新工具

### 步骤：

1. **创建工具页面**
   ```bash
   touch src/main/resources/static/tools/your-tool.html
   ```

2. **编写页面内容**
   - 参考 `tools/base64.html` 的模板
   - 设置正确的 `TOOL_ID`
   - 添加工具特定的表单字段

3. **后端实现**
   - 创建 Java 类实现 `DeveloperTool` 接口
   - 添加 `@Component` 注解

4. **自动注册**
   - 工具会自动出现在首页
   - 点击即可使用

## 🔧 维护指南

### 修改全局样式
编辑 `css/common.css`

### 修改某个工具
编辑对应的 `tools/工具名.html`

### 添加共享函数
编辑 `js/common.js`

### 修改首页
编辑 `index.html`

## 📊 工具列表

当前共有 6 个工具：

1. **Base64 编解码** (base64.html)
2. **JSON 格式化** (json-formatter.html)
3. **二维码生成器** (qrcode-generator.html)
4. **续签评估系统** (contract-renewal-evaluator.html)
5. **在线加密解密** (encryption-tool.html)
6. **ASCII 对照表** (ascii-table.html)
