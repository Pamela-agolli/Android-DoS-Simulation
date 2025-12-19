# Android-DoS-Simulation

### Technical Clarification
- **Vulnerability Simulation**: While CVE-2025-26429 officially targets Android 13-15, we used Android 10 as a "vulnerable baseline". Because Android 10 is end-of-life, it lacks the modern Binder transaction thresholds Google introduced in 2025.
- **Indirect Exploit Method**: As seen in `MainActivity.java`, the app does not call `AppOpsService.collectOps()` directly. Instead, it uses `startService()` and `sendBroadcast()` to flood the **Binder IPC paths** with oversized Bundles.
- **Failure Mode**: This stresses the framework's 1MB Binder transaction limit, triggering the exact resource exhaustion and framework unresponsiveness (captured in our report's Figure 4 logs) that the 2025 patch was designed to prevent.

---

### Proposed Fixes - PSEUDOCODE

#### Fix for CVE-2025-26429
**The Problem & Solution:** The original code had no validation for input data. The patched version implements strict thresholds for bundle size and entry length.

// VULNERABLE - No validation
public void collectOps(Bundle inputData) {
    for (String key : inputData.keySet()) {
        processOperation(key, inputData.get(key)); // Accepts anything
    }
}

// PATCHED - With validation
private static final int MAX_BUNDLE_SIZE = 1000;
private static final int MAX_VALUE_SIZE = 10240; // 10KB max

public void collectOps(Bundle inputData) {
    // FIX 1: Check bundle size
    if (inputData == null || inputData.size() > MAX_BUNDLE_SIZE) {
        Log.w(TAG, "Bundle size exceeds limit - rejecting");
        return;
    }
    // FIX 2: Validate each entry
    for (String key : inputData.keySet()) {
        Object value = inputData.get(key);
        if (value instanceof String) {
            if (((String) value).length() > MAX_VALUE_SIZE) {
                Log.w(TAG, "Value too large - skipping");
                continue; // Skip oversized values
            }
        }
        processOperation(key, value);
    }
}
The code above fixes the issue by 1) rejecting bundles exceeding 1000 entries, 2) rejecting individual values exceeding 10KB, 3) preventing memory allocation spikes, and 4) keeping the system responsive.

Here is the complete, single-block version of the README.md content. You can copy this entire section at once and paste it into your GitHub editor. It includes the proper code formatting so your professor can read the logic clearly.

Markdown

# Android-DoS-Simulation

### Technical Clarification
- **Vulnerability Simulation**: While CVE-2025-26429 officially targets Android 13-15, we used Android 10 as a "vulnerable baseline". Because Android 10 is end-of-life, it lacks the modern Binder transaction thresholds Google introduced in 2025.
- **Indirect Exploit Method**: As seen in `MainActivity.java`, the app does not call `AppOpsService.collectOps()` directly. Instead, it uses `startService()` and `sendBroadcast()` to flood the **Binder IPC paths** with oversized Bundles.
- **Failure Mode**: This stresses the framework's 1MB Binder transaction limit, triggering the exact resource exhaustion and framework unresponsiveness (captured in our report's Figure 4 logs) that the 2025 patch was designed to prevent.

---

### Proposed Fixes - PSEUDOCODE

#### Fix for CVE-2025-26429
**The Problem & Solution:** The original code had no validation for input data. The patched version implements strict thresholds for bundle size and entry length.
```java
// VULNERABLE - No validation
public void collectOps(Bundle inputData) {
    for (String key : inputData.keySet()) {
        processOperation(key, inputData.get(key)); // Accepts anything
    }
}

// PATCHED - With validation
private static final int MAX_BUNDLE_SIZE = 1000;
private static final int MAX_VALUE_SIZE = 10240; // 10KB max

public void collectOps(Bundle inputData) {
    // FIX 1: Check bundle size
    if (inputData == null || inputData.size() > MAX_BUNDLE_SIZE) {
        Log.w(TAG, "Bundle size exceeds limit - rejecting");
        return;
    }
    // FIX 2: Validate each entry
    for (String key : inputData.keySet()) {
        Object value = inputData.get(key);
        if (value instanceof String) {
            if (((String) value).length() > MAX_VALUE_SIZE) {
                Log.w(TAG, "Value too large - skipping");
                continue; // Skip oversized values
            }
        }
        processOperation(key, value);
    }
}
The code above fixes the issue by 1) rejecting bundles exceeding 1000 entries, 2) rejecting individual values exceeding 10KB, 3) preventing memory allocation spikes, and 4) keeping the system responsive.

Fix for CVE-2022-20425
The Problem & Solution: Lack of resource limits on ZenRules leads to synchronous processing hangs. The patched version adds quotas and timeout protection.


// VULNERABLE - No resource limits
public void addAutomaticZenRule(ZenRule rule) {
    mZenRules.add(rule); // Unbounded growth
    applyRules(); // Synchronous processing
}

// PATCHED - With resource limits
private static final int MAX_RULES_PER_APP = 10;
private static final int MAX_TOTAL_RULES = 100;

public void addAutomaticZenRule(ZenRule rule) throws IllegalStateException {
    // FIX 1: Limit total rules
    if (mZenRules.size() >= MAX_TOTAL_RULES) {
        throw new IllegalStateException("System rule limit exceeded");
    }
    // FIX 2: Limit per-app rules
    String packageName = rule.getPackageName();
    int rulesForApp = countRulesForPackage(packageName);
    if (rulesForApp >= MAX_RULES_PER_APP) {
        throw new IllegalStateException("App rule limit exceeded");
    }
    // FIX 3: Validate rule complexity
    if (rule.getConditionComponents() != null && rule.getConditionComponents().size() > 50) {
        throw new IllegalStateException("Rule too complex");
    }
    mZenRules.add(rule);
    applyRulesWithTimeout(5000); // Add timeout protection
}

private void applyRulesWithTimeout(long timeoutMs) throws TimeoutException {
    Thread ruleThread = new Thread(() -> {
        for (ZenRule rule : mZenRules) {
            rule.apply();
        }
    });
    ruleThread.start();
    try {
        ruleThread.join(timeoutMs);
        if (ruleThread.isAlive()) {
            ruleThread.interrupt();
            throw new TimeoutException("Rule processing exceeded timeout");
        }
    } catch (InterruptedException e) {
        throw new TimeoutException("Rule processing interrupted");
    }
}
The code above fixes the issue by 1) limiting total rules to 100, 2) limiting each app to 10 rules, 3) validating rule complexity, 4) implementing a timeout for infinite rules, and 5) preventing resource monopolization.
