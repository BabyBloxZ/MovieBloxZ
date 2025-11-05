package com.kero.movieboxhooker.hooks;

import com.kero.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class SkipAdHook {

    private SkipAdHook() {}

    /**
     * يحاول عمل هوك للميثود b() في الكلاس gq.e
     * رجّع true لو نجح في هوك واحدة على الأقل.
     */
    public static boolean apply(ClassLoader cl) {
        try {
            // اسم الكلاس كما كتبته: "gq.e"
            Class<?> target = XposedHelpers.findClassIfExists("gq.e", cl);
            if (target == null) {
                Logger.warn("SkipAd: target class gq.e not found");
                return false;
            }

            int hooked = 0;
            try {
                // الدالة public final b()Z — نبدلها لترجع true مباشرة
                XposedHelpers.findAndHookMethod(target, "b",
                        XC_MethodReplacement.returnConstant(true));
                Logger.success("SkipAd: hooked gq.e.b() -> return true");
                hooked++;
            } catch (Throwable t) {
                Logger.warn("SkipAd: failed to hook gq.e.b() : " + t.getMessage());
            }

            // احتمالية احتياج هوك بديل لو الاسم مختلف (obfuscation) — نجرب نفس الاسم مع اسم بديـل شائع "b1" أو "n"
            String[] alternates = {"b1", "n", "isSkipAd", "shouldSkipAd"};
            for (String alt : alternates) {
                if (hooked > 0) break;
                try {
                    XposedHelpers.findAndHookMethod(target, alt,
                            XC_MethodReplacement.returnConstant(true));
                    Logger.success("SkipAd: hooked gq.e." + alt + "() -> return true (alternate)");
                    hooked++;
                } catch (Throwable ignored) {}
            }

            if (hooked > 0) {
                Logger.success("✓ SkipAdHook applied (" + hooked + " hooks)");
                return true;
            } else {
                Logger.warn("⚠ SkipAdHook: class found but no methods hooked");
                return false;
            }

        } catch (Throwable t) {
            Logger.error("SkipAdHook failed: " + t.getMessage());
            return false;
        }
    }
}