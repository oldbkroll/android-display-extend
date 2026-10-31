# 架构审计与实施计划（2026-10-31，实施前）

基线 `4598db3f710a7d7081c4386c5353d909f573ed10`，标签/版本 v0.0.27 / 27。Fork `oldbkroll/android-display-extend`，开发分支 `codex/zh-cn-localization`。本报告在本地化代码之前记录，AGENTS.md 已手动初始化。

## 架构与功能归属

- 原生 Java、Android XML、Material/AppCompat/Navigation；没有 Compose/Flutter。MainActivity 管理导航、USB/外部 deep link、Shizuku binder 生命周期与任务恢复，OverviewFragment 枚举 DisplayManager 的物理/虚拟屏，LauncherActivity/AppListAdapter 枚举可启动包和外屏任务。
- ServiceUtils.launchPackage → 普通 ActivityOptions.setLaunchDisplayId；有 Shizuku 时用包装的 IActivityManager 调用 startActivityAsUserWithFeature / startActivity，ActivityOptionsHidden.setLaunchWindowingMode(1) 请求全屏。全屏请求能否被 HyperOS 接受依赖固件，不能把源码支持写成实机成功。
- DisplayDetailFragment / job.ChangeResolution、ChangeDPI、ChangeRotation、ApplyDisplayMode 修改单屏 IWindowManager / IDisplayManager 配置。分辨率和 DPI 有确认/超时回退；刷新模式来自 supportedModes。ResetDisplayConfig 清除 size、density、preferred mode 与旋转覆盖。隐藏 API stub 仅编译，不打包替换系统实现。
- TouchpadActivity 支持鼠标/多点手势、滚动、指针锁定、内屏旋转、外屏 Back/Home；经 Shizuku IInputManager 注入到指定 display。TouchpadAccessibilityService 读取窗口/节点、显示光标和触控覆盖层，用于弹窗/系统表面上的输入和导航。FloatingButtonService 支持 overlay 或 accessibility fallback。
- InputRouting 先尝试 descriptor → display uniqueId，再用 dumpsys input 解析 port，回退到物理端口关联。InputDeviceMonitor 支持热插拔自动绑定；默认启用自动绑定。需要在断开外屏后验证键鼠是否自动返回内屏，源码目前没有集中撤销全部关联的流程。
- PureBlackActivity 默认显示黑画面；可选真实关内屏，调用 Shizuku UserService.setScreenPower：API 35+ IDisplayManager.requestDisplayPower（bool/int 兼容），老版 SurfaceControl。Root 模式下可用 getevent 监听音量键退出；shell 模式不能假定可读 getevent。onDestroy 尝试恢复屏幕。
- DisplayMonitor 监听连接/删除/变化，支持连接自动黑屏、自动开上次应用、托管虚拟屏；ManagedVirtualDisplayActivity、MediaProjectionService、CreateVirtualDisplay / PresentManagedVirtualDisplay 管理可选虚拟显示和投影授权。这不是解锁 HyperOS 强制镜像的替代层。
- SettingsFragment 有全局 USB audio、内容帧率匹配、充电保持亮屏，以及 force_desktop / enable_freeform / force_resizable 等系统开关。这些是显式 UI 行为。**HyperOS 协同使用时不启用 freeform/强制多窗，不与 Unlock 的设置保护对抗。**

## 高权限与隐私审查

- Shizuku 经用户授权（AcquireShizuku）提供 shell/root 身份；ServiceUtils 用 ShizukuBinderWrapper 调用 WM/DM/Input/AM/权限/音频服务。没有自行运行 su 的流程。
- 上游在设置页尝试授予本包 WRITE_SECURE_SETTINGS；无障碍启动路径还会在用户已接受披露后，授予该权限并修改本包的 enabled_accessibility_services 条目。这是实际存在的高权限行为，不能描述为“仅普通权限”。保持既有授权边界，本地化须清楚表述。
- UserService AIDL 只有退出、logcat -d、dumpsys input、screenPower、音量监听接口；内部 `_exec` 不是接受任意远程 shell 的公开 AIDL。未发现互联网服务端、网络遥控或下载执行；manifest 无 INTERNET。
- 导出入口：MainActivity 的导航/USB intent；受系统权限保护的 ShizukuProvider；导出的 PureBlack ExitReceiver 只退出黑屏，不执行任意参数命令。FileProvider 不导出，仅共享明确授权 URI；file_paths 允许整个应用缓存目录，日志导出使用缓存内文件。
- 日志导出会读取 **系统 logcat -d**，可能含第三方应用和设备敏感信息。只由用户分享，不自动上传。应用列表、输入 descriptor、任务/窗口信息用于本机显示管理。调试日志保留技术原文/API 标识，页面和操作文案本地化。
- Reset All 清理本包偏好和显示服务、恢复部分全局设置与外屏覆盖；不是删除用户目录。未发现刷写系统分区、破坏文件、清除系统安全凭据或操作 TEE/Gatekeeper/KeyMint/Keystore 的实现。此结论限于所审源码，不是固件/依赖安全认证。

## 需要处理的协同缺口

1. 外屏删除路径只更新 floating button 和 lastSingleAppDisplay，没有主动关闭 PureBlackActivity；如果真实内屏电源已关，拔线恢复依赖额外生命周期事件。应做小范围恢复修复：最后一个外屏删除时关闭黑屏 Activity，使现有 onDestroy 恢复路径运行。
2. ExitAll 直接 killProcess，可能绕过 PureBlackActivity.onDestroy 的内屏恢复。退出前显式调用既有 user service 的恢复/停止监听接口，单独提交，保留原有行为并记录实机限制。
3. 输入设备关联和 IME policy 撤销仍需实机证据，不猜测加入可能误解绑其他软件的全局清理逻辑。

## 实施计划与验收

- 增加完整 `values-zh-rCN`；默认英文不变。外置 MediaProjection 通知/渠道中的英文硬编码，检查格式占位、导航 `{display_id}` / `{device_id}` 模板、无障碍标签和布局高度。数字、度数、包名、API/flag 和系统来源标签不翻译。
- 本地化独立提交并构建；恢复修复另一个提交。保持 AGP 9.4.1 / Gradle 9.8.1 / compile SDK 37，不通过升级或降级掩盖问题。记录实际使用 JDK 和 debug 签名。
- Unlock 只创建独立外屏必要能力、解除镜像策略和保留手动镜像入口；本项目独占应用启动、显示参数、触控/导航/内屏控制。无需连接 Unlock 私有 API。
- 本机执行 assembleDebug、lintDebug、testDebugUnitTest、资源一致性和 manifest 检查。上游有测试依赖但无测试源码，初次测试任务为 NO-SOURCE；随后新增 ScreenRecoveryTest 的 4 个恢复顺序/失败隔离测试。它们不验证 Android Binder、实际面板电源和固件行为。
- 实机矩阵：DP 与 HDMI、原始/改分辨率和 DPI、刷新/旋转、首次/再次连接、外屏独立全屏、手机同时操作、触控板和实体输入、音量恢复、拔线恢复、退出恢复、Shizuku 服务断开、原生镜像恢复。均标记待验证，未经用户许可不安装/重启/修改设备。
