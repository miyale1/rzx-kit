# 智学网登录凭证提取器

项目生成两个相互独立、可同时安装的 Android 应用：

- 智学网登录凭证提取器：只从已 root 设备上的智学网数据中读取和复制登录凭证，不获取 UserID，不包含手表通信功能。
- 真智学同步器：只接收用户输入的 TGT 并同步到手表，不读取智学网数据，也不申请 root 权限。

## 功能

- 检测 root 权限
- 检测智学网 `com.iflytek.elpmobile.smartlearning` 是否安装
- 检测并读取 `EDUserCenter.xml`
- 按 `get_tgt.py` 的算法解析 `Key_EDUserNew` 中的 TGT
- 一键复制 TGT

## 构建

使用 Android Studio 打开项目，安装 Android SDK 35 后构建；或执行：

```powershell
.\gradlew.bat assembleGetterDebug assembleSyncDebug
```

APK 分别输出到 `app/build/outputs/apk/getter/debug/` 和 `app/build/outputs/apk/sync/debug/`。

TGT 属于登录凭据，请勿发送给不受信任的人或应用。
