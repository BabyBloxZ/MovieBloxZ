package com.kero.movieboxhooker;

import de.robv.android.xposed.XposedBridge;

public final class Logger {

    private Logger() {}

    public static void success(String msg) { XposedBridge.log("✅ " + msg); }
    public static void error(String msg)   { XposedBridge.log("❌ " + msg); }
    public static void warn(String msg)    { XposedBridge.log("⚠️ " + msg); }
    public static void info(String msg)    { XposedBridge.log("ℹ️ " + msg); }
    public static void debug(String msg)   { XposedBridge.log("🐛 " + msg); }
}