package com.sharebounds.sharebounds;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

public class BaseThemeImageButton extends AppCompatImageButton {

    public BaseThemeImageButton(Context context, AttributeSet set) {
        super(context, set);
        setupButton(context);
    }

    private void setupButton(Context context) {
        int theme = AppSettings.getInstance(context.getApplicationContext()).getTheme();
        setTheme(theme);
    }

    public void setTheme(int theme) {
        int tintColor;
        int background;
        switch (theme) {
            case 1:
                tintColor = 255;
                background = R.drawable.round_button_dark;
                break;
            default:
                tintColor = 255;
                background = R.drawable.round_button;
        }
        this.setBackgroundResource(background);
        this.setColorFilter(Color.argb(tintColor, tintColor, tintColor, tintColor));
        this.invalidate();
    }
}
