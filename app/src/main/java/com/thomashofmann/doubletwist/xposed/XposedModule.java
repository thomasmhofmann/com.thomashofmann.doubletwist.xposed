package com.thomashofmann.doubletwist.xposed;


import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * An Xposed module to fix doubleTwist music player to work with Musixmatch Floating Lyrics and also with Music Announcer or Songie.
 * For details see https://plus.google.com/+ThomasHofmannDE/posts/56uE85H9aim
 */
public class XposedModule implements IXposedHookLoadPackage {

    public static final String TAG = "xposedDTW";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("com.doubleTwist.androidPlayer".equals(lpparam.packageName)) {
            XposedBridge.log("Loaded " + lpparam.packageName);
            try {
                /**
                 * Remove the {@link ComponentName f} from the intents so that it is implicit and can be seen by all apps.
                 */
                XposedHelpers.findAndHookMethod(ContextWrapper.class,"sendBroadcast", Intent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.i(TAG, "sendBroadcast called in AudioPlayerService");
                        Intent intent = (Intent) param.args[0];
                        String action = intent.getAction();
                        if("com.android.music.playbackcomplete".equals(action) || "com.android.music.playstatechanged".equals(action) || "com.android.music.metachanged".equals(action)) {
                            Log.i(TAG, "Removing component name from Intent: " + intent);
                            intent.setComponent(null);
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Cannot hook methods.", e);
            }
        }

        if ("com.musixmatch.android.lyrify".equals(lpparam.packageName)) {
            XposedBridge.log("Loaded " + lpparam.packageName);
            try {
                /**
                 * I don't know why but Musixmatch changes the playing state information to false always. The fix is to prevent this.
                 */
                XposedHelpers.findAndHookMethod("com.musixmatch.android.scrobbler.notifications.MXMNotificationListenerService", lpparam.classLoader, "ˋ", "o.anb", new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Log.i(TAG, "Replacing method so that playing (state) is not changed for doubleTwist player.");
                        return null;
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Cannot hook methods.", e);
            }
        }

    }
}
