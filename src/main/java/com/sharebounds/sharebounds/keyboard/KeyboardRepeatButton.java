package com.sharebounds.sharebounds.keyboard;

import android.content.Context;
import android.util.AttributeSet;

import com.sharebounds.sharebounds.BaseThemeImageButton;

class KeyboardRepeatButton extends BaseThemeImageButton {

    boolean onClickDown;
    boolean repeating;

    public KeyboardRepeatButton(Context context, AttributeSet set) {
        super(context, set);
        this.setOnTouchListener(new RepeatListener());
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
