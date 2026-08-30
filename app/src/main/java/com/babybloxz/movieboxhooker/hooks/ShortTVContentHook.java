package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class ShortTVContentHook {

    private ShortTVContentHook() {}

    public static boolean apply(ClassLoader cl) {
        int ok = 0;

        // ShortTVItem.getNeedPaid() → 0
        try {
            Class<?> c = XposedHelpers.findClassIfExists(
                    "com.transsion.shorttv.bean.ShortTVItem", cl);
            if (c != null) {
                XposedHelpers.findAndHookMethod(c, "getNeedPaid",
                        XC_MethodReplacement.returnConstant(0));
                Logger.success("✓ ShortTVItem.getNeedPaid hooked");
                ok++;
            }
        } catch (Throwable t) {
            Logger.error("ShortTVItem hook failed: " + t.getMessage());
        }

        // Subject.getNeedPaid() → 0
        try {
            Class<?> c = XposedHelpers.findClassIfExists(
                    "com.transsion.shorttv.bean.Subject", cl);
            if (c != null) {
                XposedHelpers.findAndHookMethod(c, "getNeedPaid",
                        XC_MethodReplacement.returnConstant(0));
                Logger.success("✓ Subject.getNeedPaid hooked");
                ok++;
            }
        } catch (Throwable t) {
            Logger.error("Subject hook failed: " + t.getMessage());
        }

        Logger.success("✓ ShortTVContentHook done (" + ok + "/2)");
        return ok > 0;
    }
}