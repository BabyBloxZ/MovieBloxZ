package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

public final class MemberProviderHook {

    private MemberProviderHook() {}

    /**
     * Hook عالمي: بدل ما نهوّك دوال غامضة ومعمولة Obfuscation
     * نهوّك مباشرة MMKV.getBoolean(key, defaultValue)
     * ونجبر القيم الثلاثة: TRUE
     */
    public static boolean apply(ClassLoader cl) {
        try {
            Class<?> mmkvClass = XposedHelpers.findClassIfExists(
                    "com.tencent.mmkv.MMKV",
                    cl
            );

            if (mmkvClass == null) {
                Logger.warn("MemberProviderHook: MMKV class not found");
                return false;
            }

            XposedHelpers.findAndHookMethod(
                    mmkvClass,
                    "getBoolean",
                    String.class,
                    boolean.class,
                    new XC_MethodHook() {

                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String key = (String) param.args[0];
                            if (key == null) return;

                            switch (key) {

                                case "kv_is_enable_member":
                                case "kv_is_pay_enable_member":
                                case "kv_is_skip_ad":
                                    Logger.success("Force TRUE → " + key);
                                    param.setResult(true);
                                    break;
                            }
                        }
                    }
            );

            Logger.success("✓ MemberProviderHook applied via MMKV.getBoolean()");
            return true;

        } catch (Throwable t) {
            Logger.error("MemberProviderHook failed: " + t.getMessage());
            return false;
        }
    }
}