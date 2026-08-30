package com.babybloxz.movieboxhooker.hooks;

import android.os.Handler;
import android.os.Looper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class Hooker implements IXposedHookLoadPackage {

    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY_MS = 500; // نص ثانية

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.community.oneroom")) return;

        XposedBridge.log("Hooker: loaded package: " + lpparam.packageName + " process:" + lpparam.processName);

        attemptHooks(lpparam.classLoader, 0, lpparam);
    }

    private void attemptHooks(final ClassLoader cl, final int attempt, final XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // تأكد إننا لاقيين على الأقل كلاس اساسي قبل تطبيق كل الhooks
            Class<?> memberInfo = XposedHelpers.findClassIfExists("com.transsion.memberapi.MemberInfo", cl);
            if (memberInfo == null) throw new ClassNotFoundException("MemberInfo not found (attempt " + attempt + ")");

            XposedBridge.log("Hooker: MemberInfo found, applying hooks");

            hookMemberInfo(cl);
            hookMemberBriefInfo(cl);
            hookDownloads(cl);
            hookMemberCheckResult(cl);
            hookUserInfo(cl);

            XposedBridge.log("Hooker: all hooks applied successfully.");

        } catch (Throwable e) {
            XposedBridge.log("Hooker: hook attempt failed: " + e.getMessage() + " (attempt " + attempt + ")");
            if (attempt < MAX_RETRIES) {
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        attemptHooks(cl, attempt + 1, lpparam), RETRY_DELAY_MS);
            } else {
                XposedBridge.log("Hooker: reached max retries, giving up.");
            }
        }
    }

    private void hookMemberInfo(ClassLoader cl) {
        XposedHelpers.findAndHookMethod("com.transsion.memberapi.MemberInfo", cl, "isActive",
                XC_MethodReplacement.returnConstant(true));
        XposedHelpers.findAndHookMethod("com.transsion.memberapi.MemberInfo", cl, "getExpiryDate",
                XC_MethodReplacement.returnConstant("2099-01-01"));
        XposedHelpers.findAndHookMethod("com.transsion.memberapi.MemberInfo", cl, "getDaysLeft",
                XC_MethodReplacement.returnConstant(9999));
        XposedHelpers.findAndHookMethod("com.transsion.memberapi.MemberInfo", cl, "getMemberType",
                XC_MethodReplacement.returnConstant(2));
        XposedHelpers.findAndHookMethod("com.transsion.memberapi.MemberInfo", cl, "getNextRenewDate",
                XC_MethodReplacement.returnConstant("2099-01-01"));
        XposedBridge.log("Hooker: MemberInfo hooks applied");
    }

    private void hookMemberBriefInfo(ClassLoader cl) {
        XposedHelpers.findAndHookMethod("com.transsion.member.bean.MemberBriefInfo", cl, "isActive",
                XC_MethodReplacement.returnConstant(true));
        XposedHelpers.findAndHookMethod("com.transsion.member.bean.MemberBriefInfo", cl, "getExpiryDate",
                XC_MethodReplacement.returnConstant("2099-01-01"));
        XposedHelpers.findAndHookMethod("com.transsion.member.bean.MemberBriefInfo", cl, "getMemberType",
                XC_MethodReplacement.returnConstant(2));
        XposedBridge.log("Hooker: MemberBriefInfo hooks applied");
    }


    private void hookDownloads(ClassLoader cl) {
        String[] classes = {
                "com.transsion.baselib.db.download.DownloadBean",
                "com.transsion.moviedetailapi.DownloadItem",
                "com.transsion.moviedetailapi.bean.DownloadResolutionItem",
                "com.transsion.shorttv.bean.DownloadItem"
        };
        for (String c : classes) {
            try {
                XposedHelpers.findAndHookMethod(c, cl, "getRequireMemberType",
                        XC_MethodReplacement.returnConstant(0));
            } catch (Throwable t) {
                XposedBridge.log("Hooker: " + c + ".getRequireMemberType hook failed -> " + t.getMessage());
            }
        }
    }

    private void hookMemberCheckResult(ClassLoader cl) {
        String[] methods = {"getVipEnable", "getVipPayEnable", "isPassed"};
        for (String m : methods) {
            try {
                XposedHelpers.findAndHookMethod("com.transsion.memberapi.MemberCheckResult", cl, m,
                        XC_MethodReplacement.returnConstant(true));
            } catch (Throwable t) {
                XposedBridge.log("Hooker: MemberCheckResult." + m + " hook failed -> " + t.getMessage());
            }
        }
    }

    private void hookUserInfo(ClassLoader cl) {
        final String clazz = "com.transsnet.loginapi.bean.UserInfo";
        try {
            XposedHelpers.findAndHookMethod(clazz, cl, "getNickname", XC_MethodReplacement.returnConstant("Hooked by babybloxz309x"));
        } catch (Throwable t) {
            XposedBridge.log("Hooker: UserInfo.getNickname hook failed -> " + t.getMessage());
        }

        try {
            final Class<?> c = XposedHelpers.findClassIfExists(clazz, cl);
            if (c != null) {
                XposedBridge.hookAllConstructors(c, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try { XposedHelpers.setObjectField(param.thisObject, "nickname", "BabyBloxZ"); }
                        catch (Throwable ignored) {}
                    }
                });
            }
        } catch (Throwable t) {
            XposedBridge.log("Hooker: UserInfo constructor fallback failed -> " + t.getMessage());
        }
    }
}