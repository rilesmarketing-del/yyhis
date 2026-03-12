# 2026-03-12 鉴权密码安全补强设计

## 背景

当前系统的密码仍然是演示级实现：

- [AuthService.java](C:/Users/89466/Desktop/亿元项目/backend/src/main/java/com/hospital/patientappointments/service/AuthService.java) 直接用 `account.getPassword().equals(password)` 做明文比对
- [AccountProvisioningService.java](C:/Users/89466/Desktop/亿元项目/backend/src/main/java/com/hospital/patientappointments/service/AccountProvisioningService.java) 在患者注册和管理员建号时直接把原始密码写入 `user_accounts.password`
- [AdminOrgService.java](C:/Users/89466/Desktop/亿元项目/backend/src/main/java/com/hospital/patientappointments/service/AdminOrgService.java) 在密码重置时直接保存明文 `123456`
- [DemoDataInitializer.java](C:/Users/89466/Desktop/亿元项目/backend/src/main/java/com/hospital/patientappointments/service/DemoDataInitializer.java) 也会播种明文演示账号

这会带来两个直接风险：

1. 数据库存储层暴露明文密码
2. 新老账号的密码策略不一致，后续安全升级会越来越难收口

## 目标

在不打断现有演示账号、现有前端协议和现有登录流程的前提下，把密码存储与校验切换到哈希方案，并兼容迁移已有明文账号。

## 选型

本轮采用 `BCrypt` 作为密码哈希算法，通过 `spring-security-crypto` 提供的 `BCryptPasswordEncoder` 完成加密与校验。

选择原因：

- 改动面小，不需要引入整套 Spring Security 登录体系
- 兼容当前 Spring Boot 2.7 项目
- 足以覆盖当前演示环境中“明文存储与明文比对”的核心风险

## 方案概览

### 1. 保持数据库字段不变，内部兼容两种格式

本轮不改 `user_accounts.password` 字段名，也不做表结构迁移。

该列开始允许存两种值：

- 历史遗留的明文密码
- 新生成的 BCrypt 哈希串

识别规则保持最小化：

- 以 `$2a$`、`$2b$`、`$2y$` 开头的，视为 BCrypt 哈希
- 其他旧值，视为历史明文

### 2. 登录时做兼容校验与自动升级

登录流程改成：

- 若账号密码已是 BCrypt：直接用哈希校验
- 若账号密码仍是明文：按旧明文规则比对
- 若旧明文比对成功：立刻把该账号密码升级成 BCrypt 并保存
- 若比对失败：按当前登录失败流程返回统一错误

这样可以保证：

- 现有 `patient / doctor / admin` 演示账号仍可直接登录
- 不需要一次性重置全部账号
- 历史明文账号会随着真实登录逐步完成迁移

### 3. 所有新写入密码的链路统一改成哈希

以下链路从本轮起全部直接写入 BCrypt 哈希：

- 患者注册
- 管理员新建账号
- 管理员重置密码为 `123456`
- 空库播种的演示账号

这样即使旧明文账号还未全部迁完，系统也不会继续制造新的明文密码。

## 组件设计

### PasswordService

新增一个后端密码服务，职责仅限于密码相关逻辑：

- `encode(rawPassword)`：把原始密码转换成 BCrypt 哈希
- `matches(rawPassword, storedPassword)`：统一做密码校验
- `isLegacyPlaintext(storedPassword)`：识别是否为历史明文
- `shouldUpgrade(storedPassword)`：判断登录成功后是否需要升级存储格式

该服务不承载登录 token 逻辑，不处理用户角色与权限，只负责密码的存储格式和校验策略。

### AuthService

[AuthService.java](C:/Users/89466/Desktop/亿元项目/backend/src/main/java/com/hospital/patientappointments/service/AuthService.java) 改为依赖 `PasswordService`：

- 替换当前的明文 `equals` 比对
- 在旧明文账号登录成功后触发升级保存
- 继续保持当前 token 签发与过期清理逻辑不变

### AccountProvisioningService

[AccountProvisioningService.java](C:/Users/89466/Desktop/亿元项目/backend/src/main/java/com/hospital/patientappointments/service/AccountProvisioningService.java) 负责所有“新密码入库”场景：

- `createManagedAccount`
- `registerPatient`

这两条链路都会在写库前调用 `PasswordService.encode(...)`。

### AdminOrgService

[AdminOrgService.java](C:/Users/89466/Desktop/亿元项目/backend/src/main/java/com/hospital/patientappointments/service/AdminOrgService.java) 的 `resetPassword` 逻辑改成：

- 业务口径仍然是“重置为 `123456`”
- 实际写入数据库的是 `123456` 的 BCrypt 哈希

前端与接口响应文案保持不变。

### DemoDataInitializer

[DemoDataInitializer.java](C:/Users/89466/Desktop/亿元项目/backend/src/main/java/com/hospital/patientappointments/service/DemoDataInitializer.java) 在播种空库演示账号时，改为写入哈希后的演示密码：

- `patient123`
- `doctor123`
- `admin123`

这样新的空库初始化默认就是安全存储格式。

## 兼容性与边界

### 保持不变的部分

- 登录接口请求结构不变，前端仍发送原始密码
- 注册接口请求结构不变
- 管理员重置密码的业务口径不变，仍是默认密码 `123456`
- 用户名、角色、token 逻辑不变
- 不新增改密码页面
- 不引入完整 Spring Security 认证框架

### 本轮不做的事情

- 不做密码复杂度规则
- 不做密码找回流程
- 不做强制全员改密
- 不对历史数据库做离线批量迁移脚本
- 不改变前端任何请求协议

## 测试策略

### 服务层测试

新增密码服务单测，至少覆盖：

- 原始密码可编码为非明文值
- 正确密码能匹配哈希
- 错误密码不能匹配哈希
- 明文值会被识别为 legacy

### 鉴权集成测试

扩展 [AuthIntegrationTest.java](C:/Users/89466/Desktop/亿元项目/backend/src/test/java/com/hospital/patientappointments/integration/AuthIntegrationTest.java)：

- 旧明文账号仍可登录
- 登录成功后数据库中的密码已升级成哈希
- 新注册患者能够登录，且数据库里不再保存明文密码

### 组织治理集成测试

扩展 [AdminOrgIntegrationTest.java](C:/Users/89466/Desktop/亿元项目/backend/src/test/java/com/hospital/patientappointments/integration/AdminOrgIntegrationTest.java)：

- 管理员新建账号后数据库存的是哈希
- 管理员重置密码后数据库存的是哈希
- 重置后的 `123456` 可用于登录

### 全量回归

最后运行：

- 聚焦鉴权测试
- `backend` 全量 `mvn test`

## 风险与应对

### 风险 1：历史演示账号失效

应对：采用“明文兼容登录 + 成功后升级”的策略，避免一次性切断现有账号。

### 风险 2：新旧混存期间逻辑分叉

应对：所有密码判断都只通过 `PasswordService` 统一处理，不让业务服务自行判断格式。

### 风险 3：测试环境被种子账号干扰

应对：沿用当前测试配置控制演示播种开关，确保密码测试场景可重复、可隔离。

## 预期结果

完成后系统会达到以下状态：

- 新账号和重置密码全部采用哈希存储
- 历史明文账号仍可正常登录，并会在成功登录后自动升级
- 数据库不再持续新增明文密码
- 前端和演示流程无需改动即可继续使用