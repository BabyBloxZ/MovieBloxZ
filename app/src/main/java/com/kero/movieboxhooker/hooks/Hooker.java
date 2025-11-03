package com.kero.movieboxhooker.hooks;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XC_MethodHook;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Hooker implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final int MAX_RETRIES = 20; // Increased retries for first launch
    private static final int INITIAL_DELAY_MS = 3000; // Longer initial delay
    private static final int RETRY_DELAY_MS = 1000;
    private static final int EMERGENCY_HOOK_METHOD_LIMIT = 20; // limit emergency hooks per class

    private static boolean hooksApplied = false;
    private static long lastInstallTime = 0;

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        logInfo("🚀 Module initialized in zygote");
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.community.oneroom")) {
            return;
        }

        logSuccess("🎯 Target package loaded: " + lpparam.packageName + " | Process: " + lpparam.processName);

        // Check if app was recently installed/updated
        checkAppInstallTimeSafely(lpparam);

        // Reset hooks flag for new process
        hooksApplied = false;

        // Apply hooks with proper timing and safe fallback
        Runnable starter = () -> attemptHooks(lpparam.classLoader, 0, lpparam);
        try {
            if (Looper.getMainLooper() != null) {
                new Handler(Looper.getMainLooper()).postDelayed(starter, INITIAL_DELAY_MS);
            } else {
                // Fallback to background thread if main looper not ready
                new Thread(() -> {
                    try { Thread.sleep(INITIAL_DELAY_MS); } catch (InterruptedException ignored) {}
                    starter.run();
                }).start();
            }
        } catch (Throwable t) {
            // Last-resort fallback
            new Thread(starter).start();
        }
    }

    private void checkAppInstallTimeSafely(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Context context = AndroidAppHelper.currentApplication();
            if (context == null) {
                logWarning("Context is null at this stage, skipping install time check");
                return;
            }
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(lpparam.packageName, 0);
            long installTime = packageInfo.firstInstallTime;
            long updateTime = packageInfo.lastUpdateTime;

            long currentTime = System.currentTimeMillis();
            long timeSinceInstall = currentTime - installTime;
            long timeSinceUpdate = currentTime - updateTime;

            // If app was installed/updated in last 5 minutes
            if (timeSinceInstall < 5 * 60 * 1000 || timeSinceUpdate < 5 * 60 * 1000) {
                logWarning("📱 App recently installed/updated, using aggressive hooking strategy");
                lastInstallTime = Math.max(installTime, updateTime);
            }
        } catch (Throwable t) {
            logDebug("Could not check app install time: " + t.getMessage());
        }
    }

    private void attemptHooks(final ClassLoader cl, final int attempt, final XC_LoadPackage.LoadPackageParam lpparam) {
        // Prevent multiple hook applications in the same process
        if (hooksApplied) {
            logDebug("Hooks already applied in this process, skipping");
            return;
        }

        try {
            logInfo("🔄 Hook attempt " + (attempt + 1) + "/" + MAX_RETRIES);

            // For fresh installs/updates, use more aggressive class checking
            if (isFreshInstall()) {
                logInfo("🔍 Fresh install detected, using aggressive class discovery");
                if (!checkAllCriticalClasses(cl)) {
                    throw new ClassNotFoundException("Critical classes not ready yet");
                }
            } else {
                // Normal class checking
                if (!checkKeyClasses(cl)) {
                    throw new ClassNotFoundException("Key classes not found yet");
                }
            }

            logSuccess("📚 Classes found, applying hooks...");

            int successfulHooks = applyAllHooks(cl);
            int totalHooks = 7;

            if (successfulHooks == totalHooks) {
                logSuccess("🎉 All hooks applied successfully! " + successfulHooks + "/" + totalHooks + " hook groups active");
                hooksApplied = true;
            } else if (successfulHooks >= 4) { // At least 4/7 hooks working
                logSuccess("✅ Most hooks applied successfully! " + successfulHooks + "/" + totalHooks + " hook groups active");
                hooksApplied = true;
            } else {
                logWarning("⚠️ Partial success: " + successfulHooks + "/" + totalHooks + " hook groups active");
                throw new Exception("Insufficient hooks applied");
            }

        } catch (Throwable e) {
            logError("Hook attempt " + (attempt + 1) + " failed: " + e.getMessage());

            if (attempt < MAX_RETRIES - 1) {
                int nextDelay = calculateNextDelay(attempt);
                logInfo("⏳ Retrying in " + nextDelay + "ms...");
                try {
                    if (Looper.getMainLooper() != null) {
                        new Handler(Looper.getMainLooper()).postDelayed(() ->
                                attemptHooks(cl, attempt + 1, lpparam), nextDelay);
                    } else {
                        // fallback
                        new Thread(() -> {
                            try { Thread.sleep(nextDelay); } catch (InterruptedException ignored) {}
                            attemptHooks(cl, attempt + 1, lpparam);
                        }).start();
                    }
                } catch (Throwable t) {
                    new Thread(() -> {
                        try { Thread.sleep(nextDelay); } catch (InterruptedException ignored) {}
                        attemptHooks(cl, attempt + 1, lpparam);
                    }).start();
                }
            } else {
                logError("❌ Max retries reached, giving up.");
                // Even if max retries reached, try to apply whatever hooks we can
                applyEmergencyHooks(cl);
            }
        }
    }

    private boolean isFreshInstall() {
        return lastInstallTime > 0 && (System.currentTimeMillis() - lastInstallTime) < 10 * 60 * 1000; // 10 minutes
    }

    private boolean checkKeyClasses(ClassLoader cl) {
        Class<?> memberInfo = XposedHelpers.findClassIfExists("com.transsion.memberapi.MemberInfo", cl);
        Class<?> memberBriefInfo = XposedHelpers.findClassIfExists("com.transsion.member.bean.MemberBriefInfo", cl);
        return (memberInfo != null) || (memberBriefInfo != null);
    }

    private boolean checkAllCriticalClasses(ClassLoader cl) {
        String[] criticalClasses = {
            "com.transsion.memberapi.MemberInfo",
            "com.transsion.member.bean.MemberBriefInfo",
            "com.transsion.member.MemberProvider",
            "com.transsion.memberapi.MemberCheckResult",
            "com.transsnet.loginapi.bean.UserInfo"
        };

        int foundCount = 0;
        for (String className : criticalClasses) {
            if (XposedHelpers.findClassIfExists(className, cl) != null) {
                foundCount++;
            }
        }

        logDebug("Found " + foundCount + "/" + criticalClasses.length + " critical classes");
        return foundCount >= 3; // Require at least 3 critical classes
    }

    private int calculateNextDelay(int attempt) {
        if (isFreshInstall()) {
            // More aggressive retry for fresh installs
            return Math.min(RETRY_DELAY_MS * (attempt + 1), 5000);
        } else {
            // Normal retry strategy
            return RETRY_DELAY_MS;
        }
    }

    private int applyAllHooks(ClassLoader cl) {
        int successfulHooks = 0;

        successfulHooks += hookMemberInfo(cl) ? 1 : 0;
        successfulHooks += hookMemberBriefInfo(cl) ? 1 : 0;
        successfulHooks += hookMemberProvider(cl) ? 1 : 0;
        successfulHooks += hookHQF(cl) ? 1 : 0;
        successfulHooks += hookDownloads(cl) ? 1 : 0;
        successfulHooks += hookMemberCheckResult(cl) ? 1 : 0;
        successfulHooks += hookUserInfo(cl) ? 1 : 0;

        return successfulHooks;
    }

    private void applyEmergencyHooks(ClassLoader cl) {
        logWarning("🚨 Applying emergency hooks...");
        try {
            // Try to hook whatever we can find
            hookCommonPatterns(cl);
            hooksApplied = true;
        } catch (Throwable t) {
            logError("Emergency hooks failed: " + t.getMessage());
        }
    }

    private void hookCommonPatterns(ClassLoader cl) {
        // Emergency fallback - hook common method patterns
        String[] commonClasses = {
            "com.transsion.memberapi.MemberInfo",
            "com.transsion.member.bean.MemberBriefInfo",
            "com.transsion.memberapi.MemberCheckResult"
        };

        for (String className : commonClasses) {
            try {
                Class<?> targetClass = XposedHelpers.findClassIfExists(className, cl);
                if (targetClass != null) {
                    hookBooleanMethodsWithLimit(targetClass, EMERGENCY_HOOK_METHOD_LIMIT);
                    logDebug("Emergency hooks applied to: " + className);
                }
            } catch (Throwable t) {
                // Ignore failures in emergency mode
            }
        }
    }

    private void hookBooleanMethodsWithLimit(Class<?> targetClass, int limit) {
        try {
            Method[] methods = targetClass.getDeclaredMethods();
            int hooked = 0;
            for (Method method : methods) {
                if (hooked >= limit) break;
                if (Modifier.isPublic(method.getModifiers()) &&
                        method.getReturnType() == boolean.class &&
                        method.getParameterCount() <= 2) {

                    try {
                        XposedBridge.hookMethod(method, new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                return true;
                            }
                        });
                        hooked++;
                    } catch (Throwable t) {
                        // Ignore individual method failures
                    }
                }
            }
        } catch (Throwable t) {
            // Ignore reflection failures
        }
    }

    private boolean hookMemberInfo(ClassLoader cl) {
        try {
            Class<?> c = XposedHelpers.findClassIfExists("com.transsion.memberapi.MemberInfo", cl);
            if (c == null) return false;

            XposedHelpers.findAndHookMethod(c, "isActive", XC_MethodReplacement.returnConstant(true));
            XposedHelpers.findAndHookMethod(c, "getExpiryDate", XC_MethodReplacement.returnConstant("2099-01-01"));
            XposedHelpers.findAndHookMethod(c, "getDaysLeft", XC_MethodReplacement.returnConstant(9999));
            XposedHelpers.findAndHookMethod(c, "getMemberType", XC_MethodReplacement.returnConstant(2));
            XposedHelpers.findAndHookMethod(c, "getNextRenewDate", XC_MethodReplacement.returnConstant("2099-01-01"));
            logSuccess("✓ MemberInfo hooks applied (5 methods)");
            return true;
        } catch (Throwable t) {
            logError("MemberInfo hooks failed: " + t.getMessage());
            return false;
        }
    }

    private boolean hookMemberBriefInfo(ClassLoader cl) {
        try {
            Class<?> c = XposedHelpers.findClassIfExists("com.transsion.member.bean.MemberBriefInfo", cl);
            if (c == null) return false;

            XposedHelpers.findAndHookMethod(c, "isActive", XC_MethodReplacement.returnConstant(true));
            XposedHelpers.findAndHookMethod(c, "getExpiryDate", XC_MethodReplacement.returnConstant("2099-01-01"));
            XposedHelpers.findAndHookMethod(c, "getMemberType", XC_MethodReplacement.returnConstant(2));
            logSuccess("✓ MemberBriefInfo hooks applied (3 methods)");
            return true;
        } catch (Throwable t) {
            logError("MemberBriefInfo hooks failed: " + t.getMessage());
            return false;
        }
    }

    private boolean hookMemberProvider(ClassLoader cl) {
        try {
            Class<?> providerClass = XposedHelpers.findClassIfExists("com.transsion.member.MemberProvider", cl);
            if (providerClass == null) {
                logWarning("MemberProvider class not found");
                return false;
            }

            String[][] methodGroups = {
                {"l", "g", "g1", "M", "N1"},
                {"isMemberActive", "checkMemberStatus", "verifyMembership"},
                {"a", "b", "c", "d", "e"}
            };

            int successCount = 0;

            for (String[] methods : methodGroups) {
                for (String m : methods) {
                    try {
                        XposedHelpers.findAndHookMethod(providerClass, m, XC_MethodReplacement.returnConstant(true));
                        successCount++;
                    } catch (Throwable t) {
                        // Silent fail for individual methods
                    }
                }
            }

            if (successCount > 0) {
                logSuccess("✓ MemberProvider hooks applied (" + successCount + " methods)");
                return true;
            } else {
                logWarning("⚠ MemberProvider no methods hooked");
                return false;
            }
        } catch (Throwable t) {
            logError("MemberProvider hooks failed: " + t.getMessage());
            return false;
        }
    }

    private boolean hookHQF(ClassLoader cl) {
        try {
            Class<?> hqf = XposedHelpers.findClassIfExists("hq.f", cl);
            if (hqf == null) {
                logWarning("hq.f class not present");
                return false;
            }
            XposedHelpers.findAndHookMethod(hqf, "b", XC_MethodReplacement.returnConstant(true));
            logSuccess("✓ hq.f.b hook applied");
            return true;
        } catch (Throwable t) {
            logError("hq.f.b hook failed: " + t.getMessage());
            return false;
        }
    }

    private boolean hookDownloads(ClassLoader cl) {
        try {
            String[] classes = {
                    "com.transsion.baselib.db.download.DownloadBean",
                    "com.transsion.moviedetailapi.DownloadItem",
                    "com.transsion.moviedetailapi.bean.DownloadResolutionItem",
                    "com.transsion.shorttv.bean.DownloadItem"
            };
            int successCount = 0;
            for (String cName : classes) {
                try {
                    Class<?> c = XposedHelpers.findClassIfExists(cName, cl);
                    if (c != null) {
                        XposedHelpers.findAndHookMethod(c, "getRequireMemberType", XC_MethodReplacement.returnConstant(0));
                        successCount++;
                    }
                } catch (Throwable t) {
                    // Ignore individual failures
                }
            }
            logSuccess("✓ Download hooks applied (" + successCount + "/" + classes.length + " classes)");
            return successCount > 0;
        } catch (Throwable t) {
            logError("Download hooks failed: " + t.getMessage());
            return false;
        }
    }

    private boolean hookMemberCheckResult(ClassLoader cl) {
        try {
            Class<?> c = XposedHelpers.findClassIfExists("com.transsion.memberapi.MemberCheckResult", cl);
            if (c == null) return false;

            String[] methods = {"getVipEnable", "getVipPayEnable", "isPassed"};
            int successCount = 0;
            for (String m : methods) {
                try {
                    XposedHelpers.findAndHookMethod(c, m, XC_MethodReplacement.returnConstant(true));
                    successCount++;
                } catch (Throwable t) {
                    // Ignore individual failures
                }
            }
            logSuccess("✓ MemberCheckResult hooks applied (" + successCount + "/" + methods.length + " methods)");
            return successCount > 0;
        } catch (Throwable t) {
            logError("MemberCheckResult hooks failed: " + t.getMessage());
            return false;
        }
    }

    private boolean hookUserInfo(ClassLoader cl) {
        try {
            final String clazz = "com.transsnet.loginapi.bean.UserInfo";
            Class<?> c = XposedHelpers.findClassIfExists(clazz, cl);
            if (c == null) {
                logWarning("UserInfo class not found");
                return false;
            }

            XposedHelpers.findAndHookMethod(c, "getNickname",
                    XC_MethodReplacement.returnConstant("Hooked with Kero309x"));

            XposedBridge.hookAllConstructors(c, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        XposedHelpers.setObjectField(param.thisObject, "nickname", "Hooked by Kero309x");
                    } catch (Throwable ignored) {}
                }
            });

            logSuccess("✓ UserInfo hooks applied");
            return true;
        } catch (Throwable t) {
            logError("UserInfo hooks failed: " + t.getMessage());
            return false;
        }
    }

    // Logging methods (no timestamps as requested)
    private void logSuccess(String message) {
        XposedBridge.log("✅ " + message);
    }

    private void logError(String message) {
        XposedBridge.log("❌ " + message);
    }

    private void logWarning(String message) {
        XposedBridge.log("⚠️ " + message);
    }

    private void logInfo(String message) {
        XposedBridge.log("ℹ️ " + message);
    }

    private void logDebug(String message) {
        XposedBridge.log("🐛 " + message);
    }
}
