package com.sharebounds.sharebounds.keyboard;

import android.content.res.Resources;
import android.support.constraint.ConstraintLayout;
import android.view.View;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.BaseThemeImageButton;
import com.sharebounds.sharebounds.R;
import com.sharebounds.sharebounds.camera.BitmapImageView;
import com.sharebounds.sharebounds.camera.CameraTextureView;
import com.sharebounds.sharebounds.camera.FlashButton;

class CameraIMEUIController {

    int currentTheme;
    private ConstraintLayout mKeyboardView;
    BitmapImageView bitmapImageView;
    private CameraTextureView mCameraTextureView;

    private FlashButton mFlashButton;
    private FullScreenButton mFullScreenButton;

    CameraIMEUIController(ConstraintLayout constraintLayout){
        mKeyboardView = constraintLayout;
        currentTheme = AppSettings.getInstance(mKeyboardView.getContext().getApplicationContext()).getTheme();
        setupViews();
    }

    private void setupViews() {
        bitmapImageView = mKeyboardView.findViewById(R.id.camera_image_view);
        mCameraTextureView = mKeyboardView.findViewById(R.id.camera_texture);
        mFlashButton = mKeyboardView.findViewById(R.id.flash_button);
        mFullScreenButton = mKeyboardView.findViewById(R.id.kb_full_screen_button);
        mKeyboardView.findViewById(R.id.kb_space_button).setOnTouchListener(new KeyClickListener());
        mKeyboardView.findViewById(R.id.kb_return_button).setOnTouchListener(new KeyClickListener());
        mKeyboardView.findViewById(R.id.kb_globe_button).setOnTouchListener(new KeyClickListener());
    }

    void setTheme(int theme) {
        currentTheme = theme;
        for (int i = 0; i < mKeyboardView.getChildCount(); i++) {
            View view = mKeyboardView.getChildAt(i);
            if (view instanceof BaseThemeImageButton) {
                ((BaseThemeImageButton) view).setTheme(theme);
            }
        }
    }

    void setKeyboardHeight(boolean fullScreenMode, int recommendedHeight) {
        int height = Resources.getSystem().getDisplayMetrics().heightPixels;
        height *= (fullScreenMode) ? (0.75) :
                (standardKeyboardHeight(recommendedHeight / height,
                        Utils.isLandscapeMode()));
        bitmapImageView.getLayoutParams().height = height;
        mCameraTextureView.getLayoutParams().height = height;
        bitmapImageView.forceLayout();
        mCameraTextureView.forceLayout();
        mKeyboardView.requestLayout();
    }

    private double standardKeyboardHeight(double recommendedHeight, boolean landscape) {
        if ((recommendedHeight > 0.25) && (recommendedHeight < 0.4)) {
            return recommendedHeight;
        }
        return landscape ? 0.5 : 0.35;
    }

    void resetFlashButton() {
        mFlashButton.setupButton();
    }

    void enableEditUI(boolean enabled) {
        mFullScreenButton.setEnabled(!enabled);
        mFlashButton.setEnabled(!enabled);
        if (enabled) {
            mFullScreenButton.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
                @Override
                public void run() {
                    mFullScreenButton.setVisibility(View.INVISIBLE);
                }
            });
            mFlashButton.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
                @Override
                public void run() {
                    mFlashButton.setVisibility(View.INVISIBLE);
                }
            });
        } else {
            mFullScreenButton.animate().alpha(1.0f).setDuration(300).withStartAction(new Runnable() {
                @Override
                public void run() {
                    mFullScreenButton.setVisibility(View.VISIBLE);
                }
            });
            mFlashButton.animate().alpha(1.0f).setDuration(300).withStartAction(new Runnable() {
                @Override
                public void run() {
                    mFlashButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
