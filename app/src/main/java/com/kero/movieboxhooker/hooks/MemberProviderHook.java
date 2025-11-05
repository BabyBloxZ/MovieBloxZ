package com.kero.movieboxhooker.hooks;

import com.kero.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class MemberProviderHook {

    private MemberProviderHook() {}

    public static boolean apply(ClassLoader cl) {
        try {
            Class<?> providerClass = XposedHelpers.findClassIfExists(
                    "com.transsion.member.MemberProvider", cl);

            if (providerClass == null) {
                Logger.warn("MemberProvider class not found");
                return false;
            }

            int success = 0;

            success += hook(providerClass, "b1"); // kv_is_enable_member
            success += hook(providerClass, "h");  // kv_is_pay_enable_member
            success += hook(providerClass, "n");  // kv_is_skip_ad

            if (success > 0) {
                Logger.success("✓ MemberProvider hooks applied (" + success + " methods)");
                return true;
            } else {
                Logger.warn("⚠ MemberProvider found but no methods hooked");
                return false;
            }

        } catch (Throwable t) {
            Logger.error("MemberProvider hooks failed: " + t.getMessage());
            return false;
        }
    }

    private static int hook(Class<?> cls, String method) {
        try {
            XposedHelpers.findAndHookMethod(cls, method, XC_MethodReplacement.returnConstant(true));
            Logger.success("Hooked " + method + "() → return true");
            return 1;
        } catch (Throwable e) {
            return 0;
        }
    }
}