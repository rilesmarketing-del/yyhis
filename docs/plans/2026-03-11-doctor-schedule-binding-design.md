# 医生排班稳定绑定与医生排班页真实化设计

## 目标

解决医生账号与排班、预约之间仍依赖姓名文本匹配的问题，并将医生端“我的排班”页面从静态演示页升级为真实只读页面。

## 当前问题

- `DoctorClinicService` 仍通过医生账号 `displayName` 推导 `doctorName`，再和预约记录里的医生姓名文本匹配。
- 管理端排班页当前仍是手工录入医生姓名、职称、科室，无法保证和新建医生账号稳定关联。
- 医生端 [DoctorSchedule.vue](/C:/Users/89466/Desktop/yy/frontend/src/views/doctor/DoctorSchedule.vue) 仍为静态页面。

这些问题会导致：新建医生账号虽然能立即登录，但若管理端排班姓名和账号显示名不一致，医生就可能看不到自己的真实排班和待接诊队列。

## 设计方案

### 1. 用 `doctorUsername` 做稳定主键绑定

- 在 `DoctorSchedule` 中新增 `doctorUsername`
- 在 `AppointmentRecord` 中新增 `doctorUsername`
- `doctorName / title / department` 继续保留，作为展示快照

这样可以保证：

- 管理端排班和医生账号稳定绑定
- 患者预约创建后，预约记录保留排班归属的医生账号
- 医生待接诊、开始接诊、我的排班都通过 `doctorUsername` 查真实数据

### 2. 管理端排班改为选择医生账号

管理端新建和编辑排班时：

- 不再手工输入医生姓名、职称、科室
- 改为先从真实医生账号列表中选择医生
- 选择后自动带出显示名、职称、科室

后端创建和更新排班时，会根据 `doctorUsername` 回填并固化：

- `doctorName`
- `title`
- `department`

这样管理端仍然能看到清晰的展示字段，但保存逻辑已不依赖手工文本。

### 3. 医生端“我的排班”改为真实只读

医生端新增只读接口：

- `GET /api/doctor/schedule/mine`

返回当前登录医生自己的排班列表，按日期和时段升序展示。医生端页面仅做：

- 真实排班表格
- 刷新按钮

本轮不做：

- 医生请假
- 医生调班
- 医生自己上下架排班

排班仍完全由管理端控制。

### 4. 兼容已有数据

为了避免现有 H2 中的旧排班和旧预约在切字段后失联，本轮增加轻量兼容回填：

- 启动时扫描历史排班和预约
- 如果 `doctorUsername` 为空，则尝试按现有医生账号 `displayName` 唯一匹配回填

这只是兼容措施，不再作为长期主逻辑。后续所有新排班、新预约都必须写入真实 `doctorUsername`。

## 数据流

1. 管理员在排班页选择医生账号并创建排班
2. 后端根据该医生账号写入 `doctorUsername + doctorName + title + department`
3. 患者预约时，预约记录同时保存 `doctorUsername`
4. 医生端：
   - 我的排班：按 `doctorUsername` 查询排班
   - 待接诊：按 `doctorUsername` 查询已支付且归属当前医生的预约
   - 开始接诊：校验预约归属 `doctorUsername`

## 接口调整

### 管理端

- 现有：
  - `GET /api/admin/schedules`
  - `POST /api/admin/schedules`
  - `PUT /api/admin/schedules/{id}`
  - `POST /api/admin/schedules/{id}/disable`
- 新增：
  - `GET /api/admin/schedules/doctors`

### 医生端

- 新增：
  - `GET /api/doctor/schedule/mine`

## 测试策略

后端优先补测试：

- admin 创建排班必须基于真实医生账号
- 排班保存后包含 `doctorUsername`
- 患者预约后记录包含 `doctorUsername`
- 医生待接诊只按 `doctorUsername` 命中
- 医生“我的排班”只返回自己的排班

前端验证：

- 管理端排班页医生选择映射正确
- 医生排班页正确展示真实排班数据
- `npm run build`
- `npm run verify:bundle`

## 本轮边界

- 不做医生请假或调班申请
- 不做管理端批量排班
- 不改患者端排班列表展示结构
- 不做更复杂的医生排班审批流程
