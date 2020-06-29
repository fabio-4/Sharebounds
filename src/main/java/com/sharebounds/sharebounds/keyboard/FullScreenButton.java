package com.sharebounds.sharebounds.keyboard;

import android.content.Context;
import android.util.AttributeSet;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.BaseThemeImageButton;
import com.sharebounds.sharebounds.R;

class FullScreenButton extends BaseThemeImageButton {

    private boolean mIsFullScreen;

    public FullScreenButton(Context context, AttributeSet set) {
        super(context, set);
        mIsFullScreen = AppSettings.getInstance(context.getApplicationContext()).getIsFullScreen();
        setImage();
    }

    boolean nextMode() {
        mIsFullScreen = !mIsFullScreen;
        AppSettings.getInstance(getContext().getApplicationContext()).setIsFullScreen(mIsFullScreen);
        setImage();
        return mIsFullScreen;
    }

    private void setImage() {
        this.setImageResource(mIsFullScreen ?
                R.drawable.ic_fullscreen_exit_black_24dp : R.drawable.ic_fullscreen_black_24dp);
    }
}
