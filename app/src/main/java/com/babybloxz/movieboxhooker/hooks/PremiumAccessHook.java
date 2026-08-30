package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class PremiumAccessHook {

    private PremiumAccessHook() {}

    public static boolean apply(ClassLoader cl) {
        int ok = 0;

        // PremiumV2CheckAccessDto.getHasAccess() → true
        try {
            Class<?> c = XposedHelpers.findClassIfExists(
                    "com.transsion.memberapi.PremiumV2CheckAccessDto", cl);
            if (c != null) {
                XposedHelpers.findAndHookMethod(c, "getHasAccess",
                        XC_MethodReplacement.returnConstant(true));
                Logger.success("✓ PremiumV2CheckAccessDto.getHasAccess hooked");
                ok++;
            }
        } catch (Throwable t) {
            Logger.error("PremiumV2CheckAccessDto hook failed: " + t.getMessage());
        }

        // PointInfo.getPoint() → MAX_VALUE
        try {
            Class<?> c = XposedHelpers.findClassIfExists(
                    "com.transsion.memberapi.PointInfo", cl);
            if (c != null) {
                XposedHelpers.findAndHookMethod(c, "getPoint",
                        XC_MethodReplacement.returnConstant(Integer.MAX_VALUE));
                Logger.success("✓ PointInfo.getPoint hooked");
                ok++;
            }
        } catch (Throwable t) {
            Logger.error("PointInfo hook failed: " + t.getMessage());
        }

        // rewardscenterapi.User.getPoint() → MAX_VALUE
        try {
            Class<?> c = XposedHelpers.findClassIfExists(
                    "com.transsion.rewardscenterapi.User", cl);
            if (c != null) {
                XposedHelpers.findAndHookMethod(c, "getPoint",
                        XC_MethodReplacement.returnConstant(Integer.MAX_VALUE));
                Logger.success("✓ rewardscenterapi.User.getPoint hooked");
                ok++;
            }
        } catch (Throwable t) {
            Logger.error("RewardsCenter User hook failed: " + t.getMessage());
        }

        Logger.success("✓ PremiumAccessHook done (" + ok + "/3)");
        return ok > 0;
    }
}