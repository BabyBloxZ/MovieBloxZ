package com.kero.movieboxhooker.hooks;

import com.kero.movieboxhooker.Logger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class SkipAdHook {

    private SkipAdHook() {}

    /**
     * Hook عالمي لـ MMKV.getBoolean("kv_is_skip_ad", ...)
     * حتى لو الكلاس أو الميثود اللي بيناديها اتغيروا بالكامل.
     */
    public static boolean apply(ClassLoader cl) {
        try {
            Class<?> mmkvClass = XposedHelpers.findClassIfExists(
                    "com.tencent.mmkv.MMKV",
                    cl
            );

            if (mmkvClass == null) {
                Logger.warn("SkipAd: MMKV class not found");
                return false;
            }

            // Hook MMKV.getBoolean(String key, boolean defaultValue)
            XposedHelpers.findAndHookMethod(
                    mmkvClass,
                    "getBoolean",
                    String.class,
                    boolean.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String key = (String) param.args[0];

                            if (key != null && key.equalsIgnoreCase("kv_is_skip_ad")) {
                                Logger.success("SkipAd: Forced TRUE for key → " + key);
                                param.setResult(true);
                            }
                        }
                    }
            );

            Logger.success("✓ SkipAdHook applied via MMKV.getBoolean()");
            return true;

        } catch (Throwable t) {
            Logger.error("SkipAdHook failed: " + t.getMessage());
            return false;
        }
    }
}