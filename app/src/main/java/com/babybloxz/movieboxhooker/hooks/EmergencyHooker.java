package com.babybloxz.movieboxhooker.hooks;

import com.babybloxz.movieboxhooker.Logger;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class EmergencyHooker {

    private EmergencyHooker() {}

    public static void apply(ClassLoader cl, int limitPerClass) {
        Logger.warn("🚨 Emergency hook mode enabled — attempting generic fallback hooks");

        String[] classes = {
                "com.transsion.memberapi.MemberInfo",
                "com.transsion.member.bean.MemberBriefInfo",
                "com.transsion.memberapi.MemberCheckResult"
        };

        for (String className : classes) {
            try {
                Class<?> c = XposedHelpers.findClassIfExists(className, cl);
                if (c != null) {
                    hookBooleanMethodsWithLimit(c, limitPerClass);
                    Logger.debug("Emergency hooks applied to: " + className);
                }
            } catch (Throwable ignored) {}
        }
    }

    private static void hookBooleanMethodsWithLimit(Class<?> targetClass, int limit) {
        try {
            Method[] methods = targetClass.getDeclaredMethods();
            int hookedCount = 0;

            for (Method m : methods) {
                if (hookedCount >= limit) break;

                if (Modifier.isPublic(m.getModifiers())
                        && m.getReturnType() == boolean.class
                        && m.getParameterCount() <= 2) {

                    try {
                        XposedBridge.hookMethod(m, new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) {
                                return true;
                            }
                        });
                        hookedCount++;

                    } catch (Throwable ignored) {}
                }
            }

        } catch (Throwable ignored) {}
    }
}