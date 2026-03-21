# 管理端组织与人员管理 + 患者自助注册设计

## 背景

当前系统已经有最小鉴权、真实账号登录、管理端总览与报表，但 [AdminOrg.vue](/C:/Users/89466/Desktop/yy/frontend/src/views/admin/AdminOrg.vue) 仍然是静态组织页，不能维护真实科室和人员；登录页也只有演示账号登录，没有患者自助创建账号能力。

## 目标

这一步将账号底座继续做实：

- 管理端可以新增科室分布
- 管理端可以新增人员账号，并且新增后可立即登录
- 患者端登录页可以自助注册账号
- 系统自动生成 `patientId`
- 所有新增账号统一进入现有登录体系

## 方案选择

### 方案 A：统一账号模型 + Department + 患者独立注册接口

- `UserAccount` 继续作为统一登录主表
- 新增 `Department` 表管理科室分布
- 管理端可新增科室和人员
- 登录页新增患者注册入口，走独立注册接口

优点：

- 一套账号体系覆盖管理端建人与患者注册
- 改动集中
- 与现有最小鉴权体系保持一致

缺点：

- `UserAccount` 继续承载账号与基础人员字段

### 方案 B：人员档案表与账号表分离

优点：

- 结构更标准

缺点：

- 当前阶段范围过大，联动过多

### 方案 C：管理端真做，患者注册临时前端模拟

优点：

- 快

缺点：

- 会把账号体系做散

最终采用方案 A。

## 范围

### 本轮包含

- 新增 `Department` 数据模型
- 扩展 `UserAccount` 支持 `departmentId / title / mobile`
- 管理端查看真实科室树与人员列表
- 管理端新增科室
- 管理端新增 `patient / doctor / admin` 人员账号
- 登录页新增患者自助注册入口
- 患者注册成功后可以立即使用新账号登录

### 本轮不包含

- 编辑和删除科室
- 编辑和删除人员
- 自动登录
- 更复杂的人事档案

## 数据模型

### Department

最小字段：

- `id`
- `name`
- `parentId`

先支持一层父子结构即可。

### UserAccount 扩展

新增字段：

- `departmentId`
- `title`
- `mobile`

其中：

- `patient` 可无科室
- `doctor / admin` 需要科室
- `patientId` 仅对患者自动生成

## 接口设计

### 管理端

- `GET /api/admin/org/summary`
- `POST /api/admin/departments`
- `POST /api/admin/accounts`

`summary` 返回：

- `departments`
- `staffs`
- `roleStats`

### 认证端

- `POST /api/auth/register`

仅允许创建 `patient` 账号，系统自动生成 `patientId`。

## 页面设计

### AdminOrg

- 左侧：真实科室树
- 右侧：真实人员表
- 顶部操作：`新增科室`、`新增人员`
- 支持按角色、科室筛选，支持按用户名/显示名搜索

### LoginPage

- 保留演示账号卡片
- 增加 `患者注册` 入口
- 以弹窗或抽屉完成注册
- 注册成功后提示并回填登录表单

## 关键业务规则

- 用户名必须唯一
- 患者注册与管理端新增患者账号，都自动生成 `patientId`
- 管理端新增 `doctor / admin / patient` 后都可立即登录
- 患者注册只允许创建 `patient`
- 这一步不做自动登录，保持流程简单且易验证

## 测试策略

### 后端

- 新增集成测试覆盖：
  - admin 可新增科室
  - admin 可新增 doctor/admin/patient 账号
  - 新增账号可立即登录
  - 患者可自助注册
  - 注册账号可立即登录
  - `summary` 返回真实科室与人员数据

### 前端

- 新增 `AdminOrg` 映射脚本，验证科室树、人员表、角色统计映射
- 执行 `npm run build`
- 执行 `npm run verify:bundle`