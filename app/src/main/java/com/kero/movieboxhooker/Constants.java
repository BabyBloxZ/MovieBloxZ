package com.kero.movieboxhooker;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Constants {

    private Constants() {}

    // Retry / timing settings
    public static final int MAX_RETRIES = 20;
    public static final int INITIAL_DELAY_MS = 3000;
    public static final int RETRY_DELAY_MS = 1000;
    public static final int EMERGENCY_HOOK_METHOD_LIMIT = 20;
    public static final long FRESH_INSTALL_WINDOW_MS = 10L * 60L * 1000L; // 10 minutes

    // Multiple supported package names
    private static final Set<String> TARGET_PACKAGES;

    static {
        HashSet<String> t = new HashSet<>();
        t.add("com.community.oneroom");
        t.add("com.community.mbox.in");
        TARGET_PACKAGES = Collections.unmodifiableSet(t);
    }

    public static Set<String> getTargetPackages() {
        return TARGET_PACKAGES;
    }
}