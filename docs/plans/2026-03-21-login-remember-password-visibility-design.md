# 登录页记住密码可见性设计

## 背景

当前管理员默认登录页已经有“记住密码”逻辑，但复选框的勾选方块在界面上不够稳定可见，用户会误以为功能不存在。

## 目标

- 正常模式下明确显示“记住密码”复选框
- 患者模式继续不显示该功能
- 不改动现有账号记忆逻辑，只修正展示层可见性

## 方案

1. 将该控件改成原生 `input[type="checkbox"]`
2. 在登录页为该行增加稳定的语义类名和辅助布局类
3. 用 scoped 样式直接控制勾选框尺寸、边框和选中态颜色
4. 用前端脚本锁住这几个关键标记，防止后续再次回退成“逻辑存在但视觉不明显”

## 验证

- `node ./scripts/test-login-default-entry.mjs`
- `node ./scripts/test-common-copy.mjs`
- `node ./scripts/test-patient-mode-access.mjs`
- `npm run test:ci`
- `npm run verify:bundle`
- `npm run build`
