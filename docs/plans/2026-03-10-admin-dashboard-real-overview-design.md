# 管理端首页真实总览设计

## 背景

当前系统已经完成：

- 管理端真实排班 CRUD
- 患者端真实预约、支付、就诊记录、报告、处方联动
- 医生端真实接诊、病历、工作台与首页总览

但 [AdminDashboard.vue](/C:/Users/89466/Desktop/亿元项目/frontend/src/views/admin/AdminDashboard.vue) 仍然是静态演示页，展示的营收、药房、医保等指标没有真实数据来源。为了让三端首页都接入真实数据，需要将管理端首页改为只展示当前系统已具备真实口径的数据。

## 目标

构建管理端首页的最小真实总览：

- 展示真实的排班、预约、接诊、患者统计
- 展示真实运营概览
- 展示真实管理提醒
- 不混入营收、药房、医保等尚未做实的指标

## 方案选择

### 方案 A：新增单一聚合接口

- 新增 `GET /api/admin/dashboard/summary`
- 一次性返回统计卡、运营概览和管理提醒
- 前端首页只负责展示

优点：

- 口径统一
- 权限清晰
- 前端接入最简单

缺点：

- 需要补一个最小后端只读服务层

### 方案 B：拆成多个只读接口

优点：

- 领域拆分更细

缺点：

- 对本轮来说过重
- 增加联调面

### 方案 C：继续复用医生/患者接口

优点：

- 少写接口

缺点：

- 权限边界混乱
- 后续扩展不顺

最终采用方案 A。

## 范围

### 本轮包含

- 新增管理端首页 summary 接口
- 管理端首页展示真实统计卡
- 管理端首页展示真实运营概览
- 管理端首页展示真实管理提醒
- 保留快捷入口跳转

### 本轮不包含

- 营收与收费成功率
- 药房库存
- 医保告警
- 更多后台模块真实化

## 接口设计

### `GET /api/admin/dashboard/summary`

返回三部分：

- `stats`
  - `activeSchedules`
  - `todayAppointments`
  - `todayVisits`
  - `totalPatients`
- `overview`
  - `totalSchedules`
  - `todayActiveSchedules`
  - `todayAppointments`
  - `todayCompletedVisits`
- `alerts`
  - 余号紧张排班数
  - 当前待接诊患者数
  - 当前进行中病历数
  - 若都无异常则返回“系统运行平稳”

## 统计口径

- `activeSchedules`：启用中的全部排班数
- `todayAppointments`：预约日期等于今天的预约数
- `todayVisits`：今天日期的 `IN_PROGRESS + COMPLETED` 接诊数
- `totalPatients`：预约记录与接诊记录中患者编号去重后的数量
- `totalSchedules`：排班总数
- `todayActiveSchedules`：今天且启用中的排班数
- `todayCompletedVisits`：今天状态为 `COMPLETED` 的接诊数
- `余号紧张`：今天启用排班中 `remainingSlots <= 2` 的数量
- `待接诊患者`：医生待接诊队列数量，即 `BOOKED + PAID` 且未转入已完成接诊的预约
- `进行中病历`：`VisitRecord.status = IN_PROGRESS` 数量

## 前端展示

### 统计卡

- 启用中排班
- 今日预约
- 今日接诊
- 累计患者

### 左侧运营概览

- 排班总数
- 今日启用排班
- 今日预约数
- 今日完成接诊数

### 右侧管理提醒

- 余号紧张排班提醒
- 待接诊患者提醒
- 进行中病历提醒
- 若无异常则显示系统运行平稳

### 快捷入口

- 去排班管理
- 去医生接诊总览
- 去系统设置

## 测试策略

### 后端

先补集成测试覆盖：

- admin 可访问 summary 接口
- summary 统计口径正确
- 非 admin 访问被拒绝

### 前端

- 新增纯映射脚本，验证 summary 到页面模型的映射
- 执行 `npm run build`
- 执行 `npm run verify:bundle`
