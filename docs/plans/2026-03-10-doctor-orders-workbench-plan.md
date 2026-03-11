# 医生工作台实施计划

## 目标

将 `DoctorOrders` 从静态演示页升级为真实“当前接诊工作台”，让医生可以独立编辑并保存文字型医嘱、处方、报告。

## 实施步骤

### 1. 先补前端红灯脚本

- 新增 `frontend/scripts/test-doctor-orders-workbench.mjs`
- 覆盖：
  - 有 `IN_PROGRESS` 记录时能选出当前工作台目标
  - 可映射出表单初始值
  - 没有进行中记录时返回空状态

### 2. 补服务层映射函数

- 修改 `frontend/src/services/doctorClinic.js`
- 新增工作台相关纯函数：
  - 选出当前工作台记录
  - 构建工作台表单

### 3. 改造 DoctorOrders 页面

- 修改 `frontend/src/views/doctor/DoctorOrders.vue`
- 接入真实记录列表与详情
- 加入当前患者摘要、三类文字编辑区、保存动作
- 无当前接诊时显示空状态并跳转接诊台

### 4. 执行验证

- `node ./scripts/test-doctor-orders-workbench.mjs`
- `npm run build`
- `npm run verify:bundle`

## 完成标准

- 医生进入 `DoctorOrders` 时可看到当前接诊患者的真实工作台
- 保存后数据能通过现有病历接口持久化
- 没有当前接诊患者时页面行为清晰
- 构建和包体校验通过
