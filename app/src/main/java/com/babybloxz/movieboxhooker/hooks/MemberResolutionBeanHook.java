package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class MemberResolutionBeanHook {

    private MemberResolutionBeanHook() {}

    public static boolean apply(ClassLoader cl) {
        try {
            Class<?> c = XposedHelpers.findClassIfExists(
                    "com.transsion.baselib.db.member.MemberResolutionBean", cl);
            if (c == null) {
                Logger.warn("MemberResolutionBean: class not found");
                return false;
            }

            XposedHelpers.findAndHookMethod(c, "isUnlock",
                    XC_MethodReplacement.returnConstant(true));

            Logger.success("✓ MemberResolutionBeanHook applied");
            return true;

        } catch (Throwable t) {
            Logger.error("MemberResolutionBeanHook failed: " + t.getMessage());
            return false;
        }
    }
}