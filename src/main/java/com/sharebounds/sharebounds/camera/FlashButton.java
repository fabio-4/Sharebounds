package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.util.AttributeSet;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.BaseThemeImageButton;
import com.sharebounds.sharebounds.R;

public class FlashButton extends BaseThemeImageButton {

    enum FlashMode {
        AUTO,
        ON,
        OFF;

        private static FlashMode[] sValues = values();
        private FlashMode next() {
            return sValues[(this.ordinal()+1) % sValues.length];
        }

        static String convertToString(int i) {
            if (i == 0) return "auto";
            else if(i == 1) return "on";
            else return "off";
        }
    }

    private FlashMode mFlashMode;

    public FlashButton(Context context, AttributeSet set) {
        super(context, set);
        setupButton();
    }

    public void setupButton() {
        int lastFlashMode = AppSettings.getInstance(getContext().getApplicationContext()).getFlashMode();
        mFlashMode = FlashMode.values()[lastFlashMode];
        setImage();
    }

    int nextMode() {
        mFlashMode = mFlashMode.next();
        AppSettings.getInstance(getContext().getApplicationContext()).setFlashMode(mFlashMode.ordinal());
        setImage();
        return mFlashMode.ordinal();
    }

    private void setImage() {
        int newImage;
        switch (mFlashMode) {
            case ON:
                newImage = R.drawable.ic_flash_on_black_24dp;
                break;
            case OFF:
                newImage = R.drawable.ic_flash_off_black_24dp;
                break;
            default:
                newImage = R.drawable.ic_flash_auto_black_24dp;
        }
        this.setImageResource(newImage);
    }
}
