# Android-DoS-Simulation

### Technical Clarification
- **Vulnerability Simulation**: While CVE-2025-26429 officially targets Android 13-15, we used Android 10 as a "vulnerable baseline." Because Android 10 is end-of-life, it lacks the modern Binder transaction thresholds Google introduced in 2025.
- **Indirect Exploit Method**: As seen in `MainActivity.java`, the app does not call `AppOpsService.collectOps()` directly. Instead, it uses `startService()` and `sendBroadcast()` to flood the **Binder IPC paths** with oversized Bundles.
- **Failure Mode**: This stresses the framework's 1MB Binder transaction limit, triggering the exact resource exhaustion and framework unresponsiveness (captured in our report's Figure 4 logs) that the 2025 patch was designed to prevent.
