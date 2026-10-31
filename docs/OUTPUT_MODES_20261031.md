# Extend 外屏输出规格探测 — 2026-10-31

环境：小米17 Pro Max / popsicle，HyperOS OS4.0.5.0.XPBCNXM / Android17；Unlock 1.12.8-extend-cn.7，Extend 0.0.27-zh-cn.2；F27G51Q Pro，独立模式，总开关开启、两项桌面能力强制默认关闭。用户操作Extend，助手抓日志并读取Android/SurfaceFlinger状态。未改源码、安装、重启或调整安全组件。

## 实际请求与结果

| 请求 | Extend及系统偏好 | Android活动模式 | SurfaceFlinger/HWC活动模式 | 结果 |
| --- | --- | --- | --- | --- |
| 基线 | 未指定有效用户模式 | 2560×1440@60，mode12 | 2560×1440@60，sf0/hwc0 | 已有基线 |
| 2560×1440@180，mode13 | 用户起初点错该行，日志记录三次写入，偏好180 | 仍1440p60 | 仍1440p60 | 未切换 |
| 1920×1080@60，mode22 | 用户随后点正确行，偏好与Director默认模式更新22 | 仍1440p60 | 仍1440p60 | 未切换 |
| 2560×1440@50，mode18 | 用户点正确行，偏好更新50 | 仍1440p60 | 仍1440p60 | 未切换 |

用户均未观察到相应画面切换/重新握手。未获得显示器OSD或链路分析仪的独立时序测量；本表以Android及SF/HWC报告的活动模式为证据。系统模式编号会随重连变化，不能永久硬编码。

## 能确认与不能确认的结论

当前Extend的标准setUserPreferredDisplayMode路径能够把宽、高、刷新率请求送入系统；这不等于驱动物理链路切换。此次三种其他规格均未实际应用，不能将按钮显示应用成功当作适配通过，也不能据此认定显示器、线缆或手机硬件不支持这些模式。

Android报告25个支持模式（包含部分系统合成的比例修正模式），有1440p50/60/100/120/144/165/180，1080p60/120/165和其他分辨率。未遍历所有模式，未单独测试新的1440p120，也没有更换显示器/线缆/接口。此前DPI、缩放及逻辑尺寸成功，仅证明渲染配置变化，不证明链路时序变化。

1080p60偏好进入DisplayModeDirector，默认模式与历史desired specs出现mode22，但LocalDisplayDevice实际specs仍base12、SF外屏策略仍defaultMode0；说明请求与实际应用之间存在落差。系统处于WITHIN_GROUPS/allowGroupSwitching=false，且有MIUI渲染60上限投票，尚未通过控制变量证明哪项阻止了应用。50Hz同分辨率也失败，因此不能仅用“高于60Hz限制”或“跨分辨率分组”解释所有现象。

厂商日志Invalid displayId2 only0and1supported经本机miui-services.jar检查，来自setSceneMaxRefreshRate(int,float)辅助接口的0/1限定；不是物理模式设置API直接返回的拒绝，不能仅靠此日志宣布根因。

## 结束状态

已用设备帮助中核实的cmd display clear-user-preferred-display-mode 2清除测试偏好；读回null，实际仍2560×1440@60。未修改DPI/缩放、刷新率全局策略、手机显示参数或APK。日志抓取结束，临时无线调试关闭结果见wireless-cleanup.json，配对凭据保留。其他原跳过测试保持SKIPPED。

完整模式清单、原始日志和固件分析仅在本地device-validation/20261031/output-modes保存；仓库仅保存本概括记录，不上传设备日志、应用列表、网络地址或固件副本。

## Release迁移期间的新观察（17:22左右）

在暂时关闭Unlock总开关、恢复镜像（master=0/mirror=1）、清除用户模式偏好的状态，安装后某次Android活动模式读数为2560×1440@120（mode16）。没有同期SurfaceFlinger/HWC或OSD读数；17:23再次读取时Android与SF/HWC均回到2560×1440@60。因此这是镜像状态下的瞬时读数线索，不是Extend可靠切换120Hz的验证通过，也不能据此前独立模式测试认定硬件始终只能60Hz。后续若继续，需同步观察模式变化、前台负载和镜像/独立状态。
