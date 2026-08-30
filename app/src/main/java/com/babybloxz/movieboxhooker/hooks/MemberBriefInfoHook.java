package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class MemberBriefInfoHook {

    private MemberBriefInfoHook() {}

    public static boolean apply(ClassLoader cl) {
        try {
            Class<?> c = XposedHelpers.findClassIfExists("com.transsion.member.bean.MemberBriefInfo", cl);
            if (c == null) return false;

            XposedHelpers.findAndHookMethod(c, "isActive", XC_MethodReplacement.returnConstant(true));
            XposedHelpers.findAndHookMethod(c, "getExpiryDate", XC_MethodReplacement.returnConstant("2099-01-01"));
            XposedHelpers.findAndHookMethod(c, "getMemberType", XC_MethodReplacement.returnConstant(2));

            Logger.success("✓ MemberBriefInfo hooks applied (3 methods)");
            return true;

        } catch (Throwable t) {
            Logger.error("MemberBriefInfo hooks failed: " + t.getMessage());
            return false;
        }
    }
}