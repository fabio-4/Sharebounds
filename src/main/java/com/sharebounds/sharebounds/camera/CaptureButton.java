package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.util.AttributeSet;

import com.sharebounds.sharebounds.BaseThemeImageButton;
import com.sharebounds.sharebounds.R;

public class CaptureButton extends BaseThemeImageButton {

    public CaptureButton(Context context, AttributeSet set) {
        super(context, set);
        setImage(true);
    }

    void nextMode(boolean cameraOn) {
        setImage(cameraOn);
        setSoundEffectsEnabled(!cameraOn);
    }

    @Override
    public void setTheme(int theme) {
        int tintColor;
        int background;
        switch (theme) {
            case 1:
                tintColor = getResources().getColor(R.color.colorDarkPrimary);
                background = R.drawable.capture_button_dark;
                break;
            default:
                tintColor = getResources().getColor(R.color.colorPrimary);
                background = R.drawable.capture_button;
        }
        this.setBackgroundResource(background);
        this.setColorFilter(tintColor);
        this.invalidate();
    }

    private void setImage(boolean cameraOn) {
        this.setImageAlpha(cameraOn ? 0 : 255);
    }
}
