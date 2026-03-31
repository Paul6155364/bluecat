# BlueCat 网吧管理系统

一个基于 Spring Boot + Vue 3 的网吧实时监控系统，用于采集和管理网吧机器状态数据。

## 项目简介

本系统通过调用蓝老板系统API，实时采集网吧门店的机器状态数据，包括：
- 门店信息管理
- 区域配置管理
- 机器状态实时监控
- 历史数据分析
- 数据可视化展示

## 技术栈

### 后端技术栈

| 组件名称 | 推荐版本 | 详细说明 |
| :--- | :--- | :--- |
| **JDK Runtime** | **17** | 本地运行环境，可向下兼容运行 Java 8 编译的代码 |
| **Java Compile** | **1.8** | 编译标准，确保生成的 jar 包可在 JDK 8 服务器运行 |
| **Spring Boot** | **2.6.13** | 2.x 系列的成熟稳定版，支持 Maven 3.5 构建 |
| **MyBatis-Plus** | **3.5.5** | 持久层框架，使用 `mybatis-plus-boot-starter` |
| **Sa-Token** | **1.37.0** | 权限认证框架，使用 `sa-token-spring-boot-starter` |
| **MySQL** | **8** | 数据库驱动 |
| **Redis** | **默认** | 由 Spring Boot 2.6.13 自动管理版本 |
| **Maven** | **3.5+** | 构建工具版本 |

### 前端技术栈

| 组件名称 | 推荐版本 | 详细说明 |
| :--- | :--- | :--- |
| **Node.js** | **22.22.0** | 本地环境，提供极速构建能力 |
| **Vue** | **3.5.12** | 前端核心框架，最新稳定版 |
| **Vite** | **5.4.10** | 下一代构建工具，开发体验极佳 |
| **TypeScript** | **~5.6.0** | 类型支持，配合 Vue 3 使用 |
| **Ant Design Vue** | **4.2.6** | UI 组件库，企业级设计规范 |
| **ECharts** | **5.5.1** | 图表库，功能强大 |
| **Pinia** | **2.2.6** | 状态管理，替代 Vuex |
| **VueRouter** | **4.4.5** | 路由管理 |
| **Axios** | **1.7.7** | HTTP 请求库 |

## 项目结构

```
bluecat/
├── bluecat-backend/          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bluecat/
│   │   │   │   ├── controller/    # 控制器层
│   │   │   │   ├── service/       # 服务层
│   │   │   │   ├── mapper/        # 数据访问层
│   │   │   │   ├── entity/        # 实体类
│   │   │   │   ├── dto/           # 数据传输对象
│   │   │   │   ├── vo/            # 视图对象
│   │   │   │   ├── config/        # 配置类
│   │   │   │   ├── task/          # 定时任务
│   │   │   │   └── util/          # 工具类
│   │   │   └── resources/
│   │   │       ├── mapper/        # MyBatis XML
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
├── bluecat-web/              # 前端项目
│   ├── src/
│   │   ├── api/              # API接口
│   │   ├── components/       # 公共组件
│   │   ├── views/            # 页面视图
│   │   ├── router/           # 路由配置
│   │   ├── store/            # 状态管理
│   │   ├── utils/            # 工具函数
│   │   └── assets/           # 静态资源
│   ├── package.json
│   └── vite.config.ts
├── sql/                      # SQL脚本
│   └── init.sql              # 数据库初始化脚本
└── README.md
```

## 数据库设计

### ER关系图

```
sys_user (管理员登录)
    │
    ▼
shop_config (网吧配置: JWT Token + AppID)
    │
    ├──► shop_info (门店信息: getSnbidInfo原始数据)
    │        │
    │        ├──► shop_image (门店图片)
    │        │
    │        ├──► shop_area (门店区域配置)
    │        │
    │        └──► machine_info (机器信息: get-area-com-set-info原始数据)
    │
    └──► data_collection_task (采集任务记录)
             │
             ├──► shop_status_snapshot (门店快照: general-order-book-area-com-result)
             │        │
             │        ├──► area_status_snapshot (区域快照)
             │        │
             │        ├──► machine_status_history (机器状态历史)
             │        │
             │        └──► area_fee_snapshot (区域费用快照)
             │
             └──► api_call_log (API调用日志)
```

### 数据表说明

#### 1. sys_user - 管理员表
用于存储系统管理员账号信息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| username | VARCHAR(50) | 用户名 |
| password | VARCHAR(255) | 密码(BCrypt加密) |
| real_name | VARCHAR(50) | 真实姓名 |
| status | TINYINT | 状态:1正常,0禁用 |

#### 2. shop_config - 网吧配置表
存储不同网吧的JWT Token和AppID配置。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| config_name | VARCHAR(100) | 配置名称 |
| snbid | VARCHAR(50) | 网吧主账号snbid |
| app_id | VARCHAR(50) | AppID |
| jwt_token | TEXT | JWT Token |
| cookie | TEXT | Cookie |
| token_expire_time | DATETIME | Token过期时间 |
| status | TINYINT | 状态:1启用,0禁用 |

#### 3. shop_info - 门店信息表
存储门店基本信息，保留 `getSnbidInfo` 接口原始数据。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| config_id | BIGINT | 网吧配置ID |
| snbid | VARCHAR(50) | 门店编号 |
| name | VARCHAR(200) | 门店名称 |
| address | VARCHAR(500) | 详细地址 |
| raw_json | JSON | getSnbidInfo原始JSON数据 |

#### 4. shop_area - 门店区域表
存储门店区域配置信息。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| shop_id | BIGINT | 门店ID |
| area_name | VARCHAR(100) | 区域名称 |
| allow | TINYINT | 是否允许预订 |

#### 5. machine_info - 机器信息表
存储机器配置信息，保留 `get-area-com-set-info` 接口原始数据。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| shop_id | BIGINT | 门店ID |
| com_name | VARCHAR(50) | 机器名称(S001等) |
| area_name | VARCHAR(100) | 所属区域 |
| raw_json | JSON | get-area-com-set-info原始JSON数据 |

#### 6. data_collection_task - 数据采集任务表
记录每次数据采集任务的执行情况。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| config_id | BIGINT | 网吧配置ID |
| snapshot_time | DATETIME | 快照时间(get-sys-current-seconds) |
| status | TINYINT | 状态:0执行中,1成功,2失败 |

#### 7. shop_status_snapshot - 门店实时状态快照表
存储门店整体状态快照，保留 `general-order-book-area-com-result` 接口原始数据。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| shop_id | BIGINT | 门店ID |
| snapshot_time | DATETIME | 快照时间 |
| total_machines | INT | 机器总数 |
| free_machines | INT | 空闲机器数 |
| busy_machines | INT | 占用机器数 |
| occupancy_rate | DECIMAL(5,2) | 整体上座率(%) |
| raw_json | JSON | general-order-book-area-com-result原始JSON |

#### 8. area_status_snapshot - 区域实时状态快照表
存储各区域的状态快照。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| shop_id | BIGINT | 门店ID |
| area_name | VARCHAR(100) | 区域名称 |
| total_machines | INT | 机器总数 |
| free_machines | INT | 空闲机器数 |
| free_machine_list | JSON | 空闲机器名称列表 |
| occupancy_rate | DECIMAL(5,2) | 上座率(%) |

#### 9. machine_status_history - 机器实时状态历史表
存储每台机器的历史状态记录，**全部保留用于历史分析**。

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| shop_id | BIGINT | 门店ID |
| machine_id | BIGINT | 机器ID |
| com_name | VARCHAR(50) | 机器名称 |
| status | TINYINT | 状态:1空闲,0占用 |
| snapshot_time | DATETIME | 快照时间 |

### 数据采集流程

```
1. 登录系统 → 获取所有配置的网吧
2. 遍历每个网吧配置:
   ├── 调用 getSnbidList → 更新 shop_info (门店列表)
   ├── 遍历每个门店:
   │   ├── 调用 getSnbidInfo → 更新 shop_info (门店详情)
   │   ├── 调用 get-area-com-set-info → 更新 machine_info (机器信息)
   │   ├── 调用 get-sys-current-seconds → 获取服务器时间
   │   ├── 调用 general-order-book-area-com-result → 获取空闲机器
   │   └── 保存快照数据到:
   │       ├── shop_status_snapshot (门店快照)
   │       ├── area_status_snapshot (区域快照)
   │       └── machine_status_history (机器状态历史)
```

### API原始数据对应表

| 表名 | 用途 | 原始数据来源 |
|------|------|------------|
| `shop_info` | 门店基础信息 | `getSnbidInfo` |
| `shop_area` | 区域配置 | `get-book-seat-config.areaList` |
| `machine_info` | 机器配置 | `get-area-com-set-info` |
| `shop_status_snapshot` | 门店实时状态 | `general-order-book-area-com-result` |
| `machine_status_history` | 机器状态历史 | 计算得出 (空闲机器列表对比) |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 22.22.0+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.5+

### 后端启动

```bash
# 1. 创建数据库
mysql -u root -p
CREATE DATABASE bluecat DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 2. 执行SQL脚本
mysql -u root -p bluecat < sql/init.sql

# 3. 修改配置文件
cd bluecat-backend
vim src/main/resources/application.yml
# 修改数据库连接信息和Redis配置

# 4. 启动项目
mvn spring-boot:run
```

### 前端启动

```bash
# 1. 安装依赖
cd bluecat-web
npm install

# 2. 启动开发服务器
npm run dev

# 3. 构建生产版本
npm run build
```

### 访问系统

- 前端地址: http://localhost:5173
- 后端地址: http://localhost:8080
- 默认账号: admin / admin123

## 功能模块

### 1. 系统管理
- 管理员登录/登出
- 个人信息修改
- 密码修改

### 2. 网吧配置管理
- 网吧配置增删改查
- JWT Token 配置
- Token 过期提醒

### 3. 门店管理
- 门店列表查看
- 门店详情查看
- 门店图片管理

### 4. 区域管理
- 区域列表查看
- 区域配置管理

### 5. 机器管理
- 机器列表查看
- 机器状态监控
- 机器历史状态查询

### 6. 数据采集
- 手动触发采集
- 定时自动采集（每小时）
- 采集任务日志查看

### 7. 数据分析
- 门店上座率统计
- 区域热度分析
- 机器使用率分析
- 历史趋势图表

## 开发说明

### 后端开发

```bash
# 编译项目
mvn clean compile

# 打包项目
mvn clean package

# 运行测试
mvn test
```

### 前端开发

```bash
# 开发模式
npm run dev

# 构建生产版本
npm run build

# 代码检查
npm run lint

# 代码格式化
npm run format
```

## 部署说明

### Docker 部署

```bash
# 构建镜像
docker-compose build

# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f
```

### 传统部署

1. 打包后端项目: `mvn clean package`
2. 上传 jar 包到服务器
3. 启动服务: `java -jar bluecat-backend.jar`
4. 打包前端项目: `npm run build`
5. 部署 dist 目录到 Nginx

## 注意事项

1. **Token 管理**: JWT Token 有效期一般为2小时，请及时更新
2. **数据采集**: 建议每小时采集一次，避免频繁调用API
3. **历史数据**: 机器状态历史数据全部保留，注意数据库存储空间
4. **安全配置**: 生产环境请修改默认密码和数据库连接信息

## 更新日志

### v1.0.0 (2026-03-30)
- 初始版本发布
- 完成基础功能开发
- 支持网吧配置管理
- 支持门店和机器状态监控
- 支持数据采集和历史分析

## 许可证

MIT License

## 联系方式

如有问题或建议，请联系项目维护人员。
