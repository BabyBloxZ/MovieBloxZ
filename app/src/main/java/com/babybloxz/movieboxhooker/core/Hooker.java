package com.babybloxz.movieboxhooker.core;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;

import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import com.babybloxz.movieboxhooker.*;
import com.babybloxz.movieboxhooker.hooks.*;

public class Hooker implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static volatile boolean hooksApplied = false;
    private static volatile long lastInstallTime = 0;

    @Override
    public void initZygote(StartupParam startupParam) {
        Logger.info("🚀 Module initialized in zygote");
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        Set<String> targets = Constants.getTargetPackages();
        if (!targets.contains(lpparam.packageName)) return;

        Logger.success("🎯 Target package loaded: " + lpparam.packageName + " | Process: " + lpparam.processName);

        checkAppInstallTime(lpparam);

        hooksApplied = false;

        Runnable starter = () -> attemptHooks(lpparam.classLoader, 0, lpparam);
        try {
            if (Looper.getMainLooper() != null) {
                HookUtils.postDelayed(starter, Constants.INITIAL_DELAY_MS);
            } else {
                new Thread(() -> {
                    HookUtils.safeSleep(Constants.INITIAL_DELAY_MS);
                    starter.run();
                }).start();
            }
        } catch (Throwable t) {
            new Thread(starter).start();
        }
    }

    private void checkAppInstallTime(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Context context = AndroidAppHelper.currentApplication();
            if (context == null) {
                Logger.warn("Context null, skipping install time check");
                return;
            }

            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(lpparam.packageName, 0);

            long install = pi.firstInstallTime;
            long update  = pi.lastUpdateTime;
            long newest  = Math.max(install, update);

            if (System.currentTimeMillis() - newest < 5 * 60 * 1000) {
                Logger.warn("📱 App recently installed/updated - enabling aggressive hook mode");
                lastInstallTime = newest;
            }
        } catch (Throwable t) {
            Logger.debug("Install time check failed: " + t.getMessage());
        }
    }

    private boolean isFreshInstall() {
        return lastInstallTime > 0 &&
                (System.currentTimeMillis() - lastInstallTime) < Constants.FRESH_INSTALL_WINDOW_MS;
    }

    private int calculateNextDelay(int attempt) {
        if (isFreshInstall()) {
            return Math.min(Constants.RETRY_DELAY_MS * (attempt + 1), 5000);
        }
        return Constants.RETRY_DELAY_MS;
    }

    private boolean checkKeyClasses(ClassLoader cl) {
        return HookUtils.findClassIfExists("com.transsion.memberapi.MemberInfo", cl) != null ||
               HookUtils.findClassIfExists("com.transsion.member.bean.MemberBriefInfo", cl) != null;
    }

    private boolean checkAllCriticalClasses(ClassLoader cl) {
        String[] classes = {
                "com.transsion.memberapi.MemberInfo",
                "com.transsion.member.bean.MemberBriefInfo",
                "com.transsion.member.MemberProvider",
                "com.transsion.memberapi.MemberCheckResult",
                "com.transsnet.loginapi.bean.UserInfo"
        };

        int found = 0;
        for (String c : classes) {
            if (HookUtils.findClassIfExists(c, cl) != null) found++;
        }

        Logger.debug("Found " + found + "/" + classes.length + " critical classes");
        return found >= 3;
    }

    private void attemptHooks(final ClassLoader cl, final int attempt, final XC_LoadPackage.LoadPackageParam lpparam) {
        if (hooksApplied) {
            Logger.debug("Hooks already applied, skipping");
            return;
        }

        try {
            Logger.info("🔄 Hook attempt " + (attempt + 1) + "/" + Constants.MAX_RETRIES);

            if (isFreshInstall()) {
                if (!checkAllCriticalClasses(cl)) throw new ClassNotFoundException("Not all classes loaded yet");
            } else {
                if (!checkKeyClasses(cl)) throw new ClassNotFoundException("Key classes missing");
            }

            Logger.success("📚 Classes loaded, applying hooks...");

            // Ganti bagian ini di attemptHooks():

int ok = 0;
int total = 12; // updated total

ok += MemberInfoHook.apply(cl)           ? 1 : 0;
ok += MemberBriefInfoHook.apply(cl)      ? 1 : 0;
ok += MemberProviderHook.apply(cl)       ? 1 : 0;
ok += MemberCheckResultHook.apply(cl)    ? 1 : 0;
ok += MemberResolutionBeanHook.apply(cl) ? 1 : 0;  // NEW
ok += DownloadsHook.apply(cl)            ? 1 : 0;
ok += SkipAdHook.apply(cl)               ? 1 : 0;
ok += PremiumAccessHook.apply(cl)        ? 1 : 0;  // NEW
ok += ShortTVContentHook.apply(cl)       ? 1 : 0;  // NEW
ok += UserInfoHook.apply(cl)             ? 1 : 0;

if (ok == total) {
    Logger.success("🎉 ALL hooks active: " + ok + "/" + total);
    hooksApplied = true;
} else if (ok >= 7) {  // threshold ~70%
    Logger.success("✅ Partial success: " + ok + "/" + total);
    hooksApplied = true;
} else {
    throw new Exception("Not enough hooks applied: " + ok + "/" + total);
}

        } catch (Throwable e) {
            Logger.error("Hook attempt " + (attempt + 1) + " failed: " + e.getMessage());
            if (attempt < Constants.MAX_RETRIES - 1) {
                int delay = calculateNextDelay(attempt);
                Logger.info("⏳ Retrying in " + delay + "ms...");
                HookUtils.postDelayed(() -> attemptHooks(cl, attempt + 1, lpparam), delay);
            } else {
                Logger.error("❌ Max retries reached - Emergency mode active");
                EmergencyHooker.apply(cl, Constants.EMERGENCY_HOOK_METHOD_LIMIT);
                hooksApplied = true;
            }
        }
    }
}