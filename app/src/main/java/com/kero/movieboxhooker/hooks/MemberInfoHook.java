package com.kero.movieboxhooker.hooks;

import com.kero.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class MemberInfoHook {

    private MemberInfoHook() {}

    public static boolean apply(ClassLoader cl) {
        try {
            Class<?> c = XposedHelpers.findClassIfExists("com.transsion.memberapi.MemberInfo", cl);
            if (c == null) return false;

            XposedHelpers.findAndHookMethod(c, "isActive", XC_MethodReplacement.returnConstant(true));
            XposedHelpers.findAndHookMethod(c, "getExpiryDate", XC_MethodReplacement.returnConstant("2099-01-01"));
            XposedHelpers.findAndHookMethod(c, "getDaysLeft", XC_MethodReplacement.returnConstant(9999));
            XposedHelpers.findAndHookMethod(c, "getMemberType", XC_MethodReplacement.returnConstant(2));
            XposedHelpers.findAndHookMethod(c, "getNextRenewDate", XC_MethodReplacement.returnConstant("2099-01-01"));

            Logger.success("✓ MemberInfo hooks applied (5 methods)");
            return true;

        } catch (Throwable t) {
            Logger.error("MemberInfo hooks failed: " + t.getMessage());
            return false;
        }
    }
}