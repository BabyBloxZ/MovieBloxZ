package com.kero.movieboxhooker;

import android.os.Handler;
import android.os.Looper;
import de.robv.android.xposed.XposedHelpers;

public final class HookUtils {

    private HookUtils() {}

    public static Class<?> findClassIfExists(String name, ClassLoader cl) {
        try { return XposedHelpers.findClassIfExists(name, cl); }
        catch (Throwable ignored) { return null; }
    }

    public static boolean isMainLooperReady() {
        return Looper.getMainLooper() != null;
    }

    public static void postDelayed(Runnable r, int delayMs) {
        if (isMainLooperReady()) {
            new Handler(Looper.getMainLooper()).postDelayed(r, delayMs);
        } else {
            new Thread(() -> {
                try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
                r.run();
            }).start();
        }
    }

    public static void safeSleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}