package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;

public final class DownloadsHook {

    private DownloadsHook() {}

    public static boolean apply(ClassLoader cl) {
        try {
            String[] classes = {
                "com.transsion.baselib.db.download.DownloadBean",
                "com.transsion.baselib.db.download.VipInfo",           // NEW
                "com.transsion.moviedetailapi.DownloadItem",
                "com.transsion.moviedetailapi.bean.DownloadResolutionItem",
                "com.transsion.shorttv.bean.DownloadItem",
                "com.transsion.shorttv_pugc.bean.DownloadItem"         // NEW
            };

            int successCount = 0;

            for (String className : classes) {
                try {
                    Class<?> clazz = XposedHelpers.findClassIfExists(className, cl);
                    if (clazz != null) {
                        XposedHelpers.findAndHookMethod(clazz, "getRequireMemberType",
                                XC_MethodReplacement.returnConstant(0));
                        successCount++;
                    }
                } catch (Throwable ignored) {}
            }

            Logger.success("✓ Download hooks applied (" + successCount + "/" + classes.length + " classes)");
            return successCount > 0;

        } catch (Throwable t) {
            Logger.error("DownloadsHook failed: " + t.getMessage());
            return false;
        }
    }
}