# CyberPulse 项目启动指南

## 项目已创建完成！

项目基础结构已经创建完成，包含完整的前后端框架。

## 快速启动

### 1. 初始化数据库

```bash
# 连接MySQL并执行SQL脚本
mysql -u root -p < sql/init.sql
```

### 2. 启动后端

```bash
cd bluecat-backend

# 方式1: 使用Maven启动
mvn spring-boot:run

# 方式2: 先编译再运行
mvn clean package
java -jar target/cyberpulse-backend-1.0.0.jar
```

后端启动成功后访问: http://localhost:8080/api/doc.html

### 3. 启动前端

```bash
cd bluecat-web

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动成功后访问: http://localhost:5173

## 默认账号

- 用户名: `admin`
- 密码: `admin123`

## 配置说明

### 后端配置

修改 `bluecat-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cyberpulse
    username: root
    password: 你的MySQL密码
  
  redis:
    host: localhost
    port: 6379
    password: 你的Redis密码(如果有)
```

### 前端配置

前端已配置好代理，默认代理到 `http://localhost:8080`

如需修改，编辑 `bluecat-web/vite.config.ts`

## 项目结构

```
bluecat/
├── bluecat-backend/        # 后端项目
│   ├── src/main/java/com/bluecat/
│   │   ├── controller/     # 控制器
│   │   ├── service/        # 服务层
│   │   ├── mapper/         # 数据访问
│   │   ├── entity/         # 实体类
│   │   └── config/         # 配置类
│   └── pom.xml
├── bluecat-web/            # 前端项目
│   ├── src/
│   │   ├── views/          # 页面
│   │   ├── router/         # 路由
│   │   └── utils/          # 工具类
│   └── package.json
└── sql/
    └── init.sql            # 数据库脚本
```

## 下一步开发建议

### 后端需要完善:

1. **实体类**: 创建剩余的实体类 (ShopInfo, MachineInfo等)
2. **Service层**: 完善业务逻辑
3. **Controller层**: 添加完整的API接口
4. **定时任务**: 实现数据采集定时任务
5. **API客户端**: 封装蓝老板系统API调用

### 前端需要完善:

1. **API接口**: 在 `src/api/` 目录下创建API调用函数
2. **页面开发**: 完善各个页面的功能
3. **状态管理**: 使用Pinia管理全局状态
4. **图表展示**: 使用ECharts展示数据分析

## 技术支持

如有问题，请查看:
- 后端日志: `bluecat-backend/logs/cyberpulse.log`
- 浏览器控制台
- 数据库连接是否正常
- Redis是否启动
