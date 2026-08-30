package com.babybloxz.movieboxhooker;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import com.babybloxz.movieboxhooker.hooks.Hooker;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        //استدعي الهوك بتاعك
        new Hooker().handleLoadPackage(lpparam);
    }
}