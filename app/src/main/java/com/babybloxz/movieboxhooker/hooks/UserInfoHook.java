package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class UserInfoHook {

    private UserInfoHook() {}

    public static boolean apply(ClassLoader cl) {
        try {
            final String targetClass = "com.transsnet.loginapi.bean.UserInfo";
            Class<?> c = XposedHelpers.findClassIfExists(targetClass, cl);
            if (c == null) {
                Logger.warn("UserInfo class not found");
                return false;
            }

            // Force nickname return
            XposedHelpers.findAndHookMethod(
                    c,
                    "getNickname",
                    XC_MethodReplacement.returnConstant("BabyBloxZ")
            );

            // Force set field after object creation
            XposedBridge.hookAllConstructors(c, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        XposedHelpers.setObjectField(param.thisObject, "nickname", "BabyBloxZ");
                    } catch (Throwable ignored) {}
                }
            });

            Logger.success("✓ UserInfo hooks applied");
            return true;

        } catch (Throwable t) {
            Logger.error("UserInfo hook failed: " + t.getMessage());
            return false;
        }
    }
}