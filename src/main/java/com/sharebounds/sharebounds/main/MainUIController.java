package com.sharebounds.sharebounds.main;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.BaseThemeImageButton;
import com.sharebounds.sharebounds.R;
import com.sharebounds.sharebounds.camera.BitmapImageView;

class MainUIController {

    int currentTheme;
    ConstraintLayout mainLayout;
    private BitmapImageView mBitmapImageView;

    private BaseThemeImageButton[] mButtons;
    private BaseThemeImageButton mFlashButton, mTextButton, mCancelButton;

    MainUIController(AppCompatActivity activity) {
        currentTheme = AppSettings.getInstance(activity.getApplicationContext()).getTheme();
        setupViews(activity);
    }

    private void setupViews(AppCompatActivity activity) {
        mBitmapImageView = activity.findViewById(R.id.camera_image_view);
        mainLayout = activity.findViewById(R.id.main_layout);
        mTextButton = activity.findViewById(R.id.text_button);
        mCancelButton = activity.findViewById(R.id.cancel_button);
        mFlashButton = activity.findViewById(R.id.flash_button);

        mButtons = new BaseThemeImageButton[]{
                mTextButton, mCancelButton, mFlashButton,
                activity.findViewById(R.id.capture_button),
                activity.findViewById(R.id.photos_button),
                activity.findViewById(R.id.settings_button)
        };
    }

    void setTheme(int theme) {
        currentTheme = theme;
        for (BaseThemeImageButton button: mButtons) {
            button.setTheme(theme);
        }
    }

    void rotateButtons(int oldRotation, int newRotation) {
        for (ImageButton button: mButtons) {
            button.setRotation(oldRotation);
            button.animate().rotation(newRotation).setDuration(300);
        }
    }

    void enableTextButton(boolean enabled, boolean animate) {
        mTextButton.setEnabled(enabled);
        mCancelButton.setEnabled(enabled);
        if (animate) {
            mTextButton.animate().alpha(enabled ? 1.0f : 0.5f).setDuration(300);
            mCancelButton.animate().alpha(enabled ? 1.0f : 0.5f).setDuration(300);
        } else {
            mTextButton.setAlpha(enabled ? 1.0f : 0.5f);
            mCancelButton.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }

    void enableFlashButton(boolean enabled, boolean animate) {
        if (enabled == mFlashButton.isEnabled()) return;
        mFlashButton.setEnabled(enabled);
        if (animate) {
            if (enabled) {
                mFlashButton.animate().alpha(1.0f).setDuration(300).withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        mFlashButton.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                mFlashButton.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mFlashButton.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
        else {
            mFlashButton.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
            mFlashButton.setAlpha(enabled ? 1.0f : 0.0f);
        }
    }

    void enableEditUI(boolean enabled, boolean textNotEmpty) {
        enableTextButton(textNotEmpty, true);
        enableFlashButton(!enabled, true);
    }

    void rotateImageView(int oldRotation, int newRotation) {
        mBitmapImageView.setRotation(oldRotation);
        mBitmapImageView.animate().rotation(newRotation).setDuration(300);

        int w = mainLayout.getWidth();
        int h = mainLayout.getHeight();
        ViewGroup.LayoutParams layoutParams = mBitmapImageView.getLayoutParams();
        if (newRotation % 180 == 0) {
            layoutParams.height = h;
            layoutParams.width = w;
            mBitmapImageView.setTranslationX(0);
            mBitmapImageView.setTranslationY(0);
        } else {
            layoutParams.height = w;
            layoutParams.width = h;
            mBitmapImageView.setTranslationX((w - h) / 2);
            mBitmapImageView.setTranslationY((h - w) / 2);
        }
        mBitmapImageView.setLayoutParams(layoutParams);
        mBitmapImageView.requestLayout();
    }
}
