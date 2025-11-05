package com.kero.movieboxhooker.hooks;

import com.kero.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class HQFHook {

    private HQFHook() {}

    public static boolean apply(ClassLoader cl) {
        try {
            Class<?> hqf = XposedHelpers.findClassIfExists("hq.f", cl);
            if (hqf == null) {
                Logger.warn("hq.f class not found");
                return false;
            }

            XposedHelpers.findAndHookMethod(hqf, "b", XC_MethodReplacement.returnConstant(true));

            Logger.success("✓ hq.f.b hook applied");
            return true;

        } catch (Throwable t) {
            Logger.error("HQFHook failed: " + t.getMessage());
            return false;
        }
    }
}