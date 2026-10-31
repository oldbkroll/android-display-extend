# Repository guidance

## Scope and layout
- Extend owns application selection/fullscreen launching, display configuration, touchpad/input/navigation and built-in panel control; HyperDesktopUnlock supplies HyperOS independent-display unlock only. Do not introduce private coupling between APKs.
- `app/src/main/java/io/github/jqssun/displayextend/`: Java Android UI/fragments, adapters, `job/` actions and `shizuku/` privileged service/compatibility layer. `app/src/main/aidl/`: user-service IPC. `app/src/main/res/`: native XML resources. `hidden-api-stub/` contains compile-only framework declarations, not replacement platform implementations.
- Baseline: upstream `jqssun/android-display-extend`, commit `4598db3f710a7d7081c4386c5353d909f573ed10`, version 0.0.27 (27). GPL-3.0; retain source and attribution when distributing APKs.

## Build and validation
- Use committed wrapper: `./gradlew --no-daemon :app:assembleDebug :app:lintDebug :app:testDebugUnitTest` (Windows `gradlew.bat`). AGP 9.4.1, Gradle 9.8.1; upstream CI uses JDK 21. App compile/target SDK 37 (SDK package `platforms;android-37.0`), min SDK 26; stub compile SDK 34; Java source level 11.
- Do not arbitrarily upgrade/downgrade Gradle, SDK or dependencies. Set SDK in ignored `local.properties` or environment. Release signing is optional upstream; unsigned release APKs are not installable. Clearly label debug-signed artifacts and never commit signing secrets/local SDK paths.
- Check native resources/locale parity and formatting, merged manifest and `git diff --check`. Existing sample tests are not proof of firmware compatibility. Physical hotplug, rotation, input routing, resolution and phone-panel restoration require actual authorized device tests.

## Privileged behavior and safety
- Shizuku permission is granted by the user; document whether an operation needs shell or root and any accessibility/overlay dependency. Do not auto-grant permissions, enable accessibility, root the device or expand exported IPC.
- Review hidden APIs, broadcasts, input injection/routing, panel power, media projection and shell calls for clear purpose and teardown. Verify uncertain behavior from owning source before editing.
- No unnecessary permissions, network remote control, download-and-execute, user-data deletion, security-credential clearing, TEE/Gatekeeper/KeyMint/Keystore or system-partition changes. Do not force HyperOS freeform features known to crash SystemUI.
- Never install, uninstall, reboot, flash or modify device/system settings without explicit user permission. Preserve display reset and phone-screen recovery. Do not claim Xiaomi compatibility from compilation alone.
- Keep localization independent of business logic; prefer small fixes with clear rollback and no broad refactoring.

## Resources and Git
- English remains in `values/`; Simplified Chinese in `values-zh-rCN/`. Translate visible UI, permission/error/notification/accessibility text; externalize hardcoded UI using context resources. Preserve format arguments, package names, API/protocol identifiers, app labels from other packages and numeric display data.
- Work on `codex/zh-cn-localization`, not `main`. `origin` is `oldbkroll/android-display-extend`; `upstream` is read-only. No upstream pushes, force-pushes, public Releases or upstream PRs without explicit permission.
- Separate initialization/audit and localization/security changes into reviewable commits. Document validation limits and restore instructions. Do not commit generated build outputs or credentials.
- Initialized manually because interactive `/init` is unavailable in this tool session. No pre-existing AGENTS.md was present.
