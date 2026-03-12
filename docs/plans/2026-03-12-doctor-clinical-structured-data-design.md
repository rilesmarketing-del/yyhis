# 医生工作台临床数据结构化设计

## 背景

当前医生端的接诊主数据都挂在 `VisitRecord` 上，已经形成了比较稳定的接诊主链路：医生接诊台进入记录，病历页和工作台页都能编辑接诊信息，患者端和管理端也会消费这些接诊结果。

问题是临床数据结构仍然偏文本：

- 医嘱保存在 `doctorOrderNote`
- 处方保存在 `prescriptionNote`
- 报告保存在 `reportNote`

这三块目前基本是整段文本，导致几个明显问题：

- 医生端 `DoctorRecords` 和 `DoctorOrders` 在重复维护同一批文本字段
- 患者端处方和报告只能把文本硬映射成展示数据
- 管理端药房页只能读取文字处方摘要，难以稳定复用
- 后续如果要继续扩展药房、随访或报告能力，文本字段会成为阻力

本轮目标是先把“医嘱 / 处方 / 报告”结构化成条目列表，保留对旧文本链路的兼容，同时不在这一轮把完整病历全部重构。

## 目标

- 将 `医嘱 / 处方 / 报告` 结构化成条目列表
- 医生端 `DoctorRecords` 与 `DoctorOrders` 共用同一份结构化接诊数据
- 患者端处方与报告优先消费结构化条目
- 管理端药房页优先消费结构化处方条目
- 保留旧文本字段作为兼容摘要，不打断历史数据和现有联动

## 非目标

- 本轮不重构 `主诉 / 诊断 / 治疗计划`
- 不引入独立的处方、医嘱、报告数据库子表
- 不接药品目录、库存扣减或收费联动
- 不做复杂模板、拖拽排序、打印排版
- 不删除旧的 `doctorOrderNote / prescriptionNote / reportNote`

## 方案选择

本轮采用“`VisitRecord` 内嵌结构化列表字段”的方案：

- 在 `VisitRecord` 上新增 3 个 JSON 字符串字段
- `VisitRecordRequest / Response` 新增结构化列表字段
- 保存时双写：结构化 JSON + 文本摘要
- 读取时优先返回结构化列表，没有结构化数据时从旧文本退化成最小条目列表

相比拆独立子表，这版改动面更可控，也最适合当前项目已经围绕 `VisitRecord` 形成的主链路。相比只做前端表单而不改后端，这版能真正让患者端和管理端得到可复用的结构化数据。

## 数据结构设计

### 医嘱条目 `doctorOrders`

每条字段：

- `id`
- `category`
- `content`
- `priority`

建议枚举：

- `category`: `LIFESTYLE`、`MEDICATION`、`FOLLOW_UP`、`CHECK`
- `priority`: `NORMAL`、`IMPORTANT`

用途：把生活方式建议、复诊要求、检查建议等拆成独立条目，便于医生快速编辑和患者逐条查看。

### 处方条目 `prescriptions`

每条字段：

- `id`
- `drugName`
- `dosage`
- `frequency`
- `days`
- `remark`

用途：将原本整段文字处方拆成真正的药品项，为患者端和管理端药房页提供稳定结构。

### 报告条目 `reports`

每条字段：

- `id`
- `itemName`
- `resultSummary`
- `resultFlag`
- `advice`

建议枚举：

- `resultFlag`: `NORMAL`、`ATTENTION`、`FOLLOW_UP`

用途：把报告内容拆成独立检查结论项，便于患者端展示结果和建议。

## 后端设计

### 实体与持久化

在 `VisitRecord` 新增：

- `doctorOrdersJson`
- `prescriptionsJson`
- `reportsJson`

字段类型先采用长字符串列保存 JSON 数组，不引入新表。

### DTO

在 `VisitRecordRequest` 新增：

- `doctorOrders`
- `prescriptions`
- `reports`

在 `VisitRecordResponse` 新增相同 3 个列表字段，用于前端直接消费结构化数据。

必要时新增条目 DTO：

- `DoctorOrderItemDto`
- `PrescriptionItemDto`
- `ReportItemDto`

### 服务层兼容策略

`DoctorClinicService` 采用双写兼容：

- 保存结构化列表时，将其序列化到 JSON 字段
- 同时自动生成文本摘要，回写到旧字段：
  - `doctorOrderNote`
  - `prescriptionNote`
  - `reportNote`

读取时：

- 如果 JSON 字段存在且可解析，优先返回结构化列表
- 如果 JSON 字段为空，则尝试根据旧文本字段退化成最小单条列表

这样可以保证：

- 新数据从当前版本开始具备结构化能力
- 老数据不会因为没有 JSON 字段而在新界面里不可读

## 医生端页面设计

### `DoctorRecords.vue`

定位：完整病历编辑器。

保留：

- 左侧接诊记录列表
- 右侧完整病历编辑区
- `主诉 / 诊断 / 治疗计划` 文本输入

改造：

- `医嘱 / 处方 / 报告` 改为条目编辑器，而不是单个大文本框
- 每块先支持 `新增一条` 与 `删除一条`
- 不做拖拽，不做模板库

### `DoctorOrders.vue`

定位：当前接诊快捷工作台。

改造：

- 不再维护独立的三段文本草稿
- 改为编辑当前 `IN_PROGRESS` 接诊记录上的结构化 `doctorOrders / prescriptions / reports`
- 页面仍强调“快速录入当前接诊患者的关键临床数据”
- 完整整理需求继续引导到 `DoctorRecords`

### 页面协同

`DoctorOrders` 和 `DoctorRecords` 编辑的是同一条 `VisitRecord`：

- `DoctorOrders` 是快捷入口
- `DoctorRecords` 是完整入口

两者都通过同一组结构化字段保存，避免“双轨编辑不同步”。

## 患者端与管理端设计

### 患者处方页

`patientPrescriptions.js` 与 `PatientPrescriptions.vue` 优先消费结构化 `prescriptions`：

- 每条药品项独立展示
- 保留当前按接诊记录组织的展示方式即可
- 旧文本记录通过退化条目继续可见

### 患者报告页

`patientReports.js` 与 `PatientReports.vue` 优先消费结构化 `reports`：

- 每条报告项生成更稳定的报告展示项
- 建议、结果标记可以直接来源于结构化字段
- 如果没有结构化报告，仍保留当前预约映射兜底逻辑

### 管理端药房页

`adminPharmacy.js` 优先读取结构化 `prescriptions`：

- 可以直接展示药品名、用法用量等摘要
- 保留当前“这是处方工作总览，不代表库存实物”的产品边界

## 错误边界

- JSON 解析失败时不应阻断整条接诊记录返回，应退回旧文本摘要或空数组
- 结构化条目为空时，旧文本摘要也应生成为空，不伪造内容
- 已完成接诊记录仍不可编辑，这条规则继续沿用
- 新旧字段并存期间，前端优先结构化、后备文本，避免空白页或断链路

## 测试设计

### 后端

扩展医生接诊相关集成测试，覆盖：

- 保存结构化医嘱 / 处方 / 报告条目
- 读取时能返回结构化列表
- 文本摘要被正确回写
- 历史纯文本记录能退化为最小条目列表
- 完成态记录仍不可编辑

### 前端

新增或扩展脚本覆盖：

- `doctorClinic.js` 的结构化草稿构建与映射
- `DoctorRecords.vue` / `DoctorOrders.vue` 的条目编辑入口
- `patientPrescriptions.js` 的结构化处方映射
- `patientReports.js` 的结构化报告映射
- `adminPharmacy.js` 的结构化处方摘要映射

### 全量验证

- `backend`: `mvn test`
- `frontend`: 医生工作台结构化相关脚本
- `frontend`: `npm run build`
- `frontend`: `npm run verify:bundle`

## 风险与控制

- 结构化字段与旧文本字段共存期间，最容易出问题的是数据不一致；通过服务层统一生成文本摘要降低风险
- `DoctorRecords` 与 `DoctorOrders` 同时编辑同一记录，最容易出现展示分叉；通过共用同一接口和同一数据模型控制
- 患者端与管理端已经消费旧文本，本轮必须保留退化与兜底逻辑，不能直接切断历史数据链路