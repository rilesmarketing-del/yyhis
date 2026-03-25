# 启动器强制回登录页设计

## 背景

当前启动器打开的是系统根地址，前端又会在检测到本地仍有登录态时，从 [router/index.js](C:/Users/89466/Desktop/yy/frontend/src/router/index.js) 自动跳回角色首页。因此通过快捷方式重新打开系统时，浏览器常常直接落在上次关闭前的业务页面，而不是用户预期的登录界面。

## 目标

- 通过快捷方式打开系统时，总是回到登录页
- 不清除 [loginMemory.js](C:/Users/89466/Desktop/yy/frontend/src/services/loginMemory.js) 里保存的后台账号密码记忆
- 不改变手动访问 `/login` 时的现有行为

## 方案

### 1. 启动器改为打开带标记的登录地址

让 [launch-hospital-system.ps1](C:/Users/89466/Desktop/yy/launch-hospital-system.ps1) 在打开浏览器时使用一个专用地址，例如：

- `http://localhost:5173/login?launcher=fresh`

这样前端就能区分“普通手动访问登录页”和“由启动器发起的新一轮登录入口”。

### 2. 登录路由识别启动器标记并清除当前登录态

在 [router/index.js](C:/Users/89466/Desktop/yy/frontend/src/router/index.js) 中，对 `/login` 增加一层启动器入口判断：

- 如果 `query.launcher === "fresh"`，先执行 `clearAuthSession()`
- 然后允许进入登录页，不再按旧 token 自动跳回业务首页

因为“记住密码”独立存放在 [loginMemory.js](C:/Users/89466/Desktop/yy/frontend/src/services/loginMemory.js) 中，这样做只会清当前登录会话，不会影响后台预填账号。

## 非目标

- 不自动关闭患者模式
- 不改变手动刷新业务页时的登录态策略
- 不修改后台账号“记住密码”的存储方式
