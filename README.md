# RealZhiXue-Kit

项目生成两个相互独立、可同时安装的 Android 应用：

- 真智学 - Getter：只从已 root 设备上的智学网数据中读取和复制登录凭证，不获取 UserID，不包含手表通信功能。
- 真智学同步器：只接收用户输入的登录凭证并同步到手表，纯本地实现，不联网。

## 功能

- 检测 root 权限
- 检测智学网 `com.iflytek.elpmobile.smartlearning` 是否安装
- 读取登录凭证
- 一键复制登录凭证

## 构建

使用 Android Studio 打开项目，安装 Android SDK 35 后构建；或执行：

```powershell
.\gradlew.bat assembleGetterDebug assembleSyncDebug
```

APK 分别输出到 `app/build/outputs/apk/getter/debug/` 和 `app/build/outputs/apk/sync/debug/`。

为了确保您的信息安全，请勿将登录凭证发送给不受信任的人或应用。
