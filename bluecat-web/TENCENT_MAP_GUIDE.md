# 腾讯地图 Key 配置指南

## 问题排查步骤

### 1. 检查 Key 是否正确

请确认您已在代码中配置正确的 Key：
- 文件位置：`src/config/map.ts`
- 当前 Key：`Q5ABZ-HSL34-JTBUE-D2PW4-ZKK76-MCBJA`

### 2. 在腾讯地图控制台检查配置

访问：https://lbs.qq.com/

#### 2.1 检查 Key 的服务类型
进入【控制台】→【应用管理】→【我的应用】，找到您的 Key，确保勾选了：
- ✅ **JavaScript API**（必须）
- ✅ WebService API（建议）

#### 2.2 检查域名白名单
在 Key 设置中，检查**域名白名单**：
- 开发环境：添加 `localhost` 或 `127.0.0.1`
- 如果不限制域名，可以设置为 `*`（不推荐生产环境）

### 3. 检查浏览器控制台

打开浏览器开发者工具（F12），查看 Console 标签页：

#### 常见错误信息：

**错误1：Invalid Key**
```
Invalid Key
```
**解决方案**：
- Key 配置错误或未生效
- 等待几分钟让 Key 生效
- 检查 Key 是否被禁用

**错误2：域名白名单限制**
```
RefererNotAllowed
```
**解决方案**：
- 在腾讯地图控制台添加当前域名到白名单
- 开发环境添加 `localhost`

**错误3：Key 没有权限**
```
Key没有调用该服务的权限
```
**解决方案**：
- 在控制台勾选 "JavaScript API" 服务
- 保存并等待几分钟生效

**错误4：配额超限**
```
超出调用限制
```
**解决方案**：
- 检查 API 调用配额
- 升级配额或等待重置

### 4. 测试 Key 是否有效

在浏览器中直接访问以下 URL（替换 YOUR_KEY）：
```
https://apis.map.qq.com/ws/location/v1/ip?key=Q5ABZ-HSL34-JTBUE-D2PW4-ZKK76-MCBJA
```

如果返回 JSON 数据包含 `status: 0`，说明 Key 有效。

### 5. 重新生成 Key

如果以上步骤都无法解决，建议：
1. 在腾讯地图控制台删除当前 Key
2. 重新创建一个新的 Key
3. 勾选所有需要的服务类型
4. 配置正确的域名白名单
5. 更新代码中的 Key 配置

### 6. 临时解决方案

如果您急需使用，可以暂时使用以下公开测试 Key（仅限开发测试）：
```
OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77
```

⚠️ 注意：测试 Key 有调用限制，不建议生产环境使用。

## 配置文件位置

- Key 配置：`src/config/map.ts`
- 地图组件：`src/views/site-selection/index.vue`

## 联系支持

如果问题依然存在：
- 腾讯位置服务官方文档：https://lbs.qq.com/webDemoCenter/glAPI/glAPI/map
- 技术支持：在腾讯地图控制台提交工单

## 调试技巧

在浏览器控制台输入以下命令查看地图对象：
```javascript
console.log(window.TMap)  // 应该输出 TMap 对象
```

如果输出 `undefined`，说明 SDK 未加载成功。
