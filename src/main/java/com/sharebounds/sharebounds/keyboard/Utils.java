package com.sharebounds.sharebounds.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;

class Utils {

    static boolean isLandscapeMode() {
        return (Resources.getSystem().getDisplayMetrics().heightPixels <
                Resources.getSystem().getDisplayMetrics().widthPixels);
    }

    static boolean isOrientationUnlocked(Context context) {
        return (android.provider.Settings.System.getInt(context.getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
    }
}
