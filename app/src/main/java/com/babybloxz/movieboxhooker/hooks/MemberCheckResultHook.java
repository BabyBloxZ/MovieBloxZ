package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class MemberCheckResultHook {

    private MemberCheckResultHook() {}

    public static boolean apply(ClassLoader cl) {
        try {
            Class<?> c = XposedHelpers.findClassIfExists("com.transsion.memberapi.MemberCheckResult", cl);
            if (c == null) return false;

            String[] methods = {"getVipEnable", "getVipPayEnable", "isPassed"};
            int successCount = 0;

            for (String m : methods) {
                try {
                    XposedHelpers.findAndHookMethod(c, m, XC_MethodReplacement.returnConstant(true));
                    successCount++;
                } catch (Throwable ignored) {}
            }

            Logger.success("✓ MemberCheckResult hooks applied (" + successCount + "/" + methods.length + " methods)");
            return successCount > 0;

        } catch (Throwable t) {
            Logger.error("MemberCheckResult hooks failed: " + t.getMessage());
            return false;
        }
    }
}