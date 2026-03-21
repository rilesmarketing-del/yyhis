# 预约持久化数据库迁移设计

## 背景

当前预约主链路已经完成数据库化：患者预约、支付、改约、取消、管理端收费汇总、医生接诊联动都直接读取 `appointment_records` 表。遗留的 [AppointmentPersistenceRepository.java](C:/Users/89466/Desktop/yy/backend/src/main/java/com/hospital/patientappointments/repository/AppointmentPersistenceRepository.java) 与 `backend/data/appointments.json` 仅保留了早期轻量持久化方案的痕迹，业务服务与现有测试已不再依赖这条 JSON 链路。

现在的目标不是“再做一套新持久化”，而是安全地下线这段遗留能力：如果历史 `appointments.json` 里还有数据，就在数据库为空时自动导入一次；导入完成后彻底以数据库作为唯一预约数据源，不再保留运行时双读。

## 目标

- 启动时在严格条件下自动导入历史 `appointments.json`
- 只在 `appointment_records` 表为空时触发导入，避免覆盖现有数据库数据
- 导入后将原 JSON 文件归档，防止重复导入
- 删除或收编 [AppointmentPersistenceRepository.java](C:/Users/89466/Desktop/yy/backend/src/main/java/com/hospital/patientappointments/repository/AppointmentPersistenceRepository.java)，不再作为业务仓储存在
- 保持 [PatientAppointmentService.java](C:/Users/89466/Desktop/yy/backend/src/main/java/com/hospital/patientappointments/service/PatientAppointmentService.java) 及现有预约链路行为不变

## 非目标

- 不引入数据库与 JSON 的长期双读
- 不支持增量合并已有数据库记录和 JSON 记录
- 不在这一轮重构预约业务规则或收费规则
- 不提供独立的管理端手工迁移页面

## 方案选择

本轮采用“启动时自动导入一次后彻底下线 JSON”的方案。

对比：
- 自动导入一次后下线 JSON：实现最小，数据来源清晰，最适合当前项目现状
- 长期双读：会重新制造双数据源复杂度，不值得
- 手动迁移工具：可控但偏重，且容易因人为遗漏导致老数据长期悬空

因此，系统最终只保留数据库为唯一预约数据源。JSON 只作为一次性历史导入来源存在。

## 启动与触发边界

新增一个启动迁移器，挂在 Spring Boot 应用启动流程中。触发条件必须同时满足：

- `backend/data/appointments.json` 文件存在
- `appointment_records` 表当前为空
- 导入开关启用，例如 `demo.migration.import-legacy-appointments=true`
- JSON 文件中存在可解析的预约记录

只要数据库中已有预约数据，就直接跳过导入，并保留原 JSON 文件不动。这样可以避免误覆盖已运行系统里的真实业务数据。

## 数据映射设计

迁移时直接将 JSON 记录映射成 [AppointmentRecord.java](C:/Users/89466/Desktop/yy/backend/src/main/java/com/hospital/patientappointments/model/AppointmentRecord.java) 实体并写入数据库。

必填字段：
- `id`
- `scheduleId`
- `patientId`
- `patientName`
- `department`
- `doctorName`
- `date`
- `timeSlot`
- `status`
- `paymentStatus`
- `fee`
- `serialNumber`
- `createdAt`

兼容字段：
- `doctorUsername`：允许为空，导入后继续依赖现有 backfill 逻辑补齐
- `paidAt`：允许为空，只保留原始值，不额外生成

为了保证历史联动不被打断，导入时必须保留原始 `id` 与 `serialNumber`，不能生成新值。

## 失败处理与归档策略

迁移过程采用单事务处理：

- 如果 JSON 解析失败
- 如果记录缺失关键字段
- 如果文件内存在重复 `id` 或重复 `serialNumber`
- 如果数据库写入违反唯一约束

则整批回滚，不允许半成功。

导入失败时：
- 应用继续启动
- 记录明确错误日志
- 原 JSON 文件保持原样
- 数据库不写入任何部分数据

导入成功时：
- 保存所有预约记录到数据库
- 将原文件归档为 `appointments.imported-<timestamp>.json`
- 之后不再读取原 JSON 文件

这样能兼顾数据安全和系统可用性：不会因为一个旧文件阻断整个服务启动，也不会出现部分导入后难以追溯的问题。

## 代码落点

建议新增一个迁移服务，例如：
- `backend/src/main/java/com/hospital/patientappointments/service/LegacyAppointmentImportService.java`

职责：
- 读取 JSON 文件
- 校验与反序列化历史预约
- 在启动阶段判断是否应导入
- 执行数据库写入
- 导入成功后归档 JSON 文件

同时处理遗留仓储：
- 删除 [AppointmentPersistenceRepository.java](C:/Users/89466/Desktop/yy/backend/src/main/java/com/hospital/patientappointments/repository/AppointmentPersistenceRepository.java)
- 或将其内部读取逻辑吸收到迁移服务中，不再以业务 Repository 形式对外存在

配置新增：
- `demo.migration.import-legacy-appointments=true`

测试环境中可以按需关闭该开关，以避免与常规测试数据准备互相干扰。

## 测试设计

### 迁移器聚焦测试

新增专门测试类，使用临时 JSON 文件和内存数据库覆盖：
- 数据库为空时自动导入并归档原文件
- 数据库非空时跳过导入
- JSON 非法时不导入、不归档
- 缺关键字段时整批回滚
- 同文件内重复 `id` 或 `serialNumber` 时整批回滚

### 启动链路验证

增加最小 Spring 集成测试，证明应用启动时迁移器会被执行，导入后的数据能被 [PatientAppointmentService.java](C:/Users/89466/Desktop/yy/backend/src/main/java/com/hospital/patientappointments/service/PatientAppointmentService.java) 正常读取。

### 业务回归

保留并复用现有 [PatientAppointmentServiceTest.java](C:/Users/89466/Desktop/yy/backend/src/test/java/com/hospital/patientappointments/service/PatientAppointmentServiceTest.java) 全链路数据库测试，确认删除 JSON 仓储后不影响预约、支付、改约、退款等能力。

## 风险与控制

- 风险：启动迁移误覆盖现有数据库数据
  控制：仅在数据库为空时触发，绝不合并或覆盖

- 风险：历史 JSON 编码或字段脏数据导致迁移失败
  控制：整批事务回滚，文件不改名，日志清晰记录失败原因

- 风险：导入成功后重复启动再次迁移
  控制：成功后文件归档改名，且数据库已非空，双重阻止重复导入

- 风险：删除 JSON 仓储后遗漏调用点
  控制：先用 `rg` 全量确认引用，再通过全量 `mvn test` 回归验证

## 结果预期

完成后，预约系统将只有一个正式持久化来源：数据库。历史 `appointments.json` 若存在，会在首次安全条件满足时自动导入并归档；之后业务、测试、运维认知都统一到数据库链路上，遗留轻持久化方案可以正式退场。