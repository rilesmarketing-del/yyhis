# 启动快捷方式无闪烁入口设计

## 背景

当前 [智慧医院系统.lnk](C:/Users/89466/Desktop/yy/智慧医院系统.lnk) 指向的是 [智慧医院系统.cmd](C:/Users/89466/Desktop/yy/智慧医院系统.cmd)。虽然 `.cmd` 内部已经会二次转交给隐藏的 PowerShell，但 Explorer 首次启动 `.cmd` 时仍会短暂拉起控制台窗口，所以用户双击快捷方式时会看到 `cmd` 闪一下。

## 目标

- 双击 [智慧医院系统.lnk](C:/Users/89466/Desktop/yy/智慧医院系统.lnk) 时不再出现可见的 `cmd` 闪屏
- 保留现有 [智慧医院系统.cmd](C:/Users/89466/Desktop/yy/智慧医院系统.cmd) 作为底层备用入口
- 保持现有自定义图标与工作目录不变
- 不改变 [launch-hospital-system.ps1](C:/Users/89466/Desktop/yy/launch-hospital-system.ps1) 的主启动逻辑

## 方案

### 1. 新增无控制台入口

新增一个基于 Windows Script Host 的入口，例如 [智慧医院系统.vbs](C:/Users/89466/Desktop/yy/智慧医院系统.vbs)。它只负责：

- 解析项目根目录
- 以隐藏窗口方式调用 `powershell.exe`
- 执行 [launch-hospital-system.ps1](C:/Users/89466/Desktop/yy/launch-hospital-system.ps1)

由于 `.vbs` 通过 `wscript.exe` 运行，不会像 `.cmd` 一样先弹出一个控制台窗口。

### 2. 快捷方式改为指向无控制台入口

更新 [scripts/create-launcher-shortcut.ps1](C:/Users/89466/Desktop/yy/scripts/create-launcher-shortcut.ps1)：

- 快捷方式目标从 `.cmd` 切换为 `.vbs`
- 仍保留当前图标文件 [hospital-launcher.ico](C:/Users/89466/Desktop/yy/assets/icons/hospital-launcher.ico)
- 工作目录仍保持项目根目录

这样用户日常只需要点 `.lnk`，就能获得真正无闪屏的体验；`.cmd` 仍可作为故障排查时的备用入口。

### 3. 回归测试覆盖入口切换

更新 [test-launcher-shortcut.ps1](C:/Users/89466/Desktop/yy/scripts/test-launcher-shortcut.ps1) 覆盖：

- 项目根目录存在 `.vbs` 启动入口
- 快捷方式创建函数支持以 `.vbs` 为目标
- 生成后的 `.lnk` 目标指向 `.vbs` 而不是 `.cmd`
- 快捷方式图标与工作目录保持正确

## 非目标

- 不移除 [智慧医院系统.cmd](C:/Users/89466/Desktop/yy/智慧医院系统.cmd)
- 不重写 [launch-hospital-system.ps1](C:/Users/89466/Desktop/yy/launch-hospital-system.ps1) 的业务启动流程
- 不引入 `.exe` 打包或安装器
