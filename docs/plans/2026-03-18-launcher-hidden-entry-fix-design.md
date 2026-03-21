# 启动器无窗口入口修复设计

## 背景

当前 [智慧医院系统.cmd](C:/Users/89466/Desktop/yy/智慧医院系统.cmd) 直接调用 `powershell.exe -File`，因此双击后一定会出现一个可见的 `cmd` 窗口。同时 [launch-hospital-system.ps1](C:/Users/89466/Desktop/yy/launch-hospital-system.ps1) 的健康检查把前端就绪地址固定成了 `http://127.0.0.1:5173`，但当前 Vite 实际稳定返回的是 `http://localhost:5173`。再加上 `Invoke-WebRequest` 遇到 `404` 会抛异常，导致脚本把“前端已启动但根路径暂时返回 404”的状态误判成超时，因此浏览器不会按预期自动打开。

## 目标

双击 [智慧医院系统.cmd](C:/Users/89466/Desktop/yy/智慧医院系统.cmd) 后：

- 不保留空白 `cmd` 窗口
- 能在后台启动前后端
- 当前后端已运行时不重复启动
- 前端就绪后自动打开系统首页

## 方案

### 1. 双击入口改为隐藏转发

保留 `.cmd` 作为用户入口，但将其改成自我转发模式：

- 常规双击时通过隐藏的 PowerShell 重新调用自己，并立即退出可见 `cmd`
- 隐藏实例再去执行 [launch-hospital-system.ps1](C:/Users/89466/Desktop/yy/launch-hospital-system.ps1)
- 通过环境变量标记避免递归重入

这样用户仍然双击同一个文件，但不会看到停留的黑窗口。

### 2. 前端启动与健康检查保持同一条可用链路

前端启动改为最稳定的默认命令 `npm run dev`，不再向 Vite 注入会被误当成位置参数的值；系统 URL 调整为 `http://localhost:5173`，与实际可访问地址保持一致。同时保留日志输出到：

- `frontend/launch-frontend.log`
- `frontend/launch-frontend.err.log`

### 3. 健康检查将 404 视为前端已启动

Vite 在开发态下即使已启动，也可能对探活请求返回 `404`。因此等待逻辑需要把 `2xx-4xx` 都视为“服务已起来”，避免把“前端已就绪但当前路径不存在”误判成超时。

## 测试策略

- 更新 [scripts/test-launch-hospital-system.ps1](C:/Users/89466/Desktop/yy/scripts/test-launch-hospital-system.ps1)
- 先让测试检查入口文件是否包含隐藏转发标记和 PowerShell 无窗口启动逻辑
- 再检查前端启动命令是否显式包含 `--host=127.0.0.1` 与 `--port=5173`
- 最后运行前端回归和构建，确认改动未破坏现有系统

## 非目标

- 不新增托盘程序、安装器或 Windows 服务
- 不改变用户入口文件名
- 不重做整套日志体系
