# LIMS
本科毕设 —— 高校实验室管理系统

一个基于 **Spring Boot + Vue3** 前后端分离架构的高校实验室综合管理平台，覆盖人员、设备、安全、预约、资源、报告六大核心业务模块，提供基于 RBAC 的权限控制、JWT 认证、实时消息推送与数据可视化看板。

## 一、项目简介

随着高校实验室规模扩大，设备台账、人员准入、安全巡查、开放预约等事务依靠传统人工管理已难以胜任。本系统面向实验室管理员、教师与学生，提供一体化的线上管理能力，实现"人员可管、设备可查、安全可控、预约可约、数据可视"。

## 二、技术栈

| 端 | 技术 |
| --- | --- |
| 后端框架 | Spring Boot 2.6.13（9 子模块 Maven 聚合工程） |
| 持久层 | MyBatis-Plus 3.5.2 + Druid 连接池 |
| 数据库 | MySQL 5.7+ |
| 缓存 | Redis |
| 安全认证 | Spring Security + JWT（JJWT 0.9.1）+ Kaptcha 验证码 |
| 实时通信 | WebSocket（通知实时推送） |
| 接口文档 | Swagger 2.9.2 |
| 前端框架 | Vue 3 + Vite 5 |
| UI / 样式 | Element Plus + Tailwind CSS |
| 路由 | Vue Router 4 |
| 可视化 | ECharts 5 + vue-echarts |
| 加密 | JSEncrypt（登录密码 RSA 前端加密） |
| 单元测试 | JUnit 5 + Mockito（62 个用例，覆盖 7 个业务模块） |

## 三、功能模块

| 模块 | 主要功能 |
| --- | --- |
| 用户与权限（lab-common） | 登录注册（验证码 + RSA 加密）、用户管理、角色管理、部门管理、通知公告 |
| 人员管理（lab-personnel） | 实验室人员档案、考勤发布与打卡、人员资质认证 |
| 设备管理（lab-equipment） | 设备台账、设备状态维护、领用归还 |
| 安全管理（lab-safety） | 安全事件上报与处理、安全检查记录 |
| 预约管理（lab-reservation） | 实验室信息管理、开放预约、审批与冲突校验 |
| 资源管理（lab-resources） | 实验耗材资源登记、库存管理 |
| 报告管理（lab-reports） | 实验报告提交、批阅与归档 |
| 数据看板（lab-dashboard） | ECharts 可视化统计：设备、预约、安全等多维数据 |

权限采用 RBAC 模型，前端基于路由守卫 + 模块级 permission 控制菜单与页面可见性。

## 四、项目结构

```
高校实验室管理系统/
├── 源代码/
│   └── 源代码_1.0/
│       ├── 前端/
│       │   └── lab-management/          # Vue3 前端工程
│       │       ├── src/
│       │       │   ├── api/services/    # 统一 API 封装（token 注入、Result 解析）
│       │       │   ├── views/           # 10+ 业务页面
│       │       │   ├── router/          # 路由与守卫
│       │       │   ├── composables/     # 组合式函数
│       │       │   └── utils/           # 通知、权限工具
│       │       └── vite.config.js       # 开发代理 → 后端 8082
│       └── 后端/
│           ├── 数据库脚本/
│           │   └── shiyanshi.sql        # 初始化建库脚本
│           └── lab-management/           # Spring Boot 多模块工程
│               ├── lab-common/           # 公共：用户/角色/部门/通知/安全/JWT/WebSocket
│               ├── lab-personnel/        # 人员：考勤/认证
│               ├── lab-equipment/       # 设备
│               ├── lab-safety/          # 安全
│               ├── lab-reservation/     # 预约
│               ├── lab-resources/       # 资源
│               ├── lab-reports/         # 报告
│               ├── lab-dashboard/       # 看板统计
│               └── lab-admin/           # 启动模块（application.yaml）
```

后端分层规范：Controller 只承担 HTTP 职责，业务逻辑与事务（`@Transactional`）位于 ServiceImpl，Mapper 仅做持久化。

## 五、环境要求

| 依赖 | 版本要求 |
| --- | --- |
| JDK | 1.8（推荐 8u300+，高版本 JDK 与 Lombok 可能不兼容） |
| Maven | 3.6+ |
| MySQL | 5.7 及以上 |
| Redis | 任意稳定版 |
| Node.js | 16+ |

## 六、快速开始

### 1. 初始化数据库

```sql
-- 创建数据库并导入脚本
CREATE DATABASE shiyanshi DEFAULT CHARACTER SET utf8mb4;
USE shiyanshi;
SOURCE 后端/数据库脚本/shiyanshi.sql;
```

### 2. 配置并启动后端

编辑 `后端/lab-management/lab-admin/src/main/resources/application.yaml`，按本机环境修改数据库与 Redis 连接信息：

```yaml
spring:
  datasource:
    username: root
    password: <你的数据库密码>
    url: jdbc:mysql://localhost:3306/shiyanshi?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
  redis:
    host: 127.0.0.1
    port: 6379
```

在 `后端/lab-management` 目录下启动：

```bash
mvn spring-boot:run -pl lab-admin
# 或 mvn package 后运行 jar
```

后端启动成功后监听 **8082** 端口，Swagger 文档地址：`http://localhost:8082/swagger-ui.html`

### 3. 启动前端

在 `前端/lab-management` 目录下执行：

```bash
npm install
npm run dev
```

开发服务器运行在 **8081** 端口，浏览器访问：

```
http://localhost:8081
```

> 开发模式下 `/api`、`/login`、`/captcha`、`/ws` 等请求由 Vite 代理转发至后端 8082，无需额外配置跨域。

### 4. 生产构建

```bash
npm run build   # 产物输出至 dist/，可由 Nginx 托管并配置反向代理
```

## 七、单元测试

后端共 9 个测试类、62 个测试方法，覆盖 7 个业务模块的异常分支（重复校验、空值检查、库存校验、增量 diff 策略等）：

```bash
mvn test                        # 全量测试
mvn test -pl lab-safety,lab-resources,lab-reports,lab-reservation -am
```

测试框架为 JUnit 5 + Mockito，对继承 MyBatis-Plus ServiceImpl 的类采用 `@Spy + @InjectMocks` 部分模拟。

## 八、技术亮点

1. **前后端分离**：统一 `Result` 响应格式（code/msg/data），前端 API 层集中封装 token 注入与异常解析；
2. **安全体系**：Spring Security 过滤器链 + JWT 无状态认证 + Kaptcha 图形验证码 + RSA 前端密码加密；
3. **实时推送**：基于 WebSocket 的通知实时下发，无需轮询刷新；
4. **RBAC 权限**：角色-权限-菜单三级控制，前端路由守卫与后端接口双重校验；
5. **数据可视化**：ECharts 多维统计看板，辅助管理决策；
6. **工程规范**：Maven 多模块按业务域拆分，严格三层架构，154 个 Java 源文件、62 个单元测试保障质量。

## 九、常见问题

| 问题 | 解决方案 |
| --- | --- |
| 后端启动报 Redis 连接失败 | 先启动本机 Redis 服务（默认 6379） |
| Lombok 编译报错 | 确认使用 JDK 8，并安装 IDE 的 Lombok 插件 |
| 登录页验证码不显示 | 检查后端 8082 是否启动、Vite 代理 `/captcha` 是否生效 |
| 验证码/Redis 校验失败 | 验证码依赖 Redis 存取，请确认 Redis 可写 |
