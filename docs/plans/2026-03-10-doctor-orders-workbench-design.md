# 医生工作台设计

## 背景

当前医生端已经具备真实接诊能力：

- 接诊台可从已支付预约中开始接诊
- 病历页可编辑主诉、诊断、处理建议、医嘱、处方、报告
- 患者端已能读取已完成接诊后的真实就诊记录、真实报告和真实处方

但 [DoctorOrders.vue](/C:/Users/89466/Desktop/yy/frontend/src/views/doctor/DoctorOrders.vue) 仍然是静态演示页，和真实 `VisitRecord` 没有关系，导致医生“文字工作台”缺少独立入口。

## 目标

将 `DoctorOrders` 改造成“当前接诊工作台”：

- 页面只服务当前 `IN_PROGRESS` 的一条接诊记录
- 医生可独立编辑 `doctorOrderNote`、`prescriptionNote`、`reportNote`
- 保存后复用现有接诊记录接口落库
- 如无当前接诊患者，则引导医生先去接诊台开始接诊

## 方案选择

### 方案 A：当前接诊工作台

- 只读取当前 `IN_PROGRESS` 的接诊记录
- 聚焦三个文字区：医嘱、处方、报告
- 保存时直接调用现有 `saveDoctorRecord`

优点：

- 改动最小
- 与病历页职责清晰互补
- 演示链路最顺

缺点：

- 暂不包含历史筛选与汇总

### 方案 B：当前接诊加最近历史摘要

优点：

- 信息更丰富

缺点：

- 页面职责变重
- 本轮范围超出最小工作台

### 方案 C：独立历史医嘱中心

优点：

- 更像正式后台

缺点：

- 与病历页职责重叠
- 不能最快形成闭环

最终采用方案 A。

## 范围

### 本轮包含

- `DoctorOrders` 改为真实数据页
- 只围绕当前 `IN_PROGRESS` 记录编辑三类文本
- 保存后回读最新详情
- 提供跳转到完整病历页和接诊台的入口

### 本轮不包含

- 历史医嘱列表
- 打印、下载、模板库
- 新增后端接口
- 改动接诊状态机

## 数据来源

页面数据完全复用现有接口：

- `GET /api/doctor/records`
- `GET /api/doctor/records/{visitId}`
- `PUT /api/doctor/records/{visitId}`

前端流程：

1. 拉取医生记录列表
2. 通过 `pickCurrentVisit` 选出当前 `IN_PROGRESS` 记录
3. 再取该记录详情作为工作台表单初始值
4. 保存后重新读取详情，保持页面状态与后端一致

## 页面设计

### 有当前接诊记录时

- 顶部展示患者、科室、就诊时间、状态
- 中部三张编辑卡片：
  - 医嘱
  - 处方
  - 报告
- 底部操作：
  - 保存当前工作台
  - 前往完整病历

### 无当前接诊记录时

- 展示空状态
- 提供按钮跳转到接诊台开始接诊

## 职责边界

- `DoctorClinic`：开始接诊、查看待接诊队列
- `DoctorOrders`：快速编辑文字型医嘱/处方/报告
- `DoctorRecords`：完整病历编辑与结束接诊

## 测试策略

前端先补一个最小脚本，覆盖：

- 能从医生记录中选出当前接诊记录
- 能将记录详情映射为工作台表单初始值
- 没有 `IN_PROGRESS` 记录时返回空工作台状态

然后执行：

- `node ./scripts/test-doctor-orders-workbench.mjs`
- `npm run build`
- `npm run verify:bundle`
