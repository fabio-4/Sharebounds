package com.sharebounds.sharebounds.keyboard;

import android.app.Dialog;
import android.content.Context;
import android.hardware.SensorManager;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.OrientationListener;
import com.sharebounds.sharebounds.R;
import com.sharebounds.sharebounds.camera.OcrCameraController;

public class CameraIME extends InputMethodService implements OcrCameraController.OcrCameraListener,
        OrientationListener.OrientationCallBack {

    private CameraIMEUIController mCameraIMEUIController;
    private OcrCameraController mOcrCameraController;
    private OrientationListener mOrientationListener;

    private boolean mDidFinish = true;

    private int mGlobeFunction;
    private boolean mFullScreenMode;
    private InputMethodManager mInputMethodManager;
    private KeyboardTextManager mKeyboardTextManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mKeyboardTextManager = new KeyboardTextManager(this);
    }

    @Override
    public View onCreateInputView() {
        if (mOcrCameraController != null && !mDidFinish) {
            mOcrCameraController.pauseCamera(true);
            mOcrCameraController.release();
        }
        if (mOcrCameraController != null) mOcrCameraController.destroy();

        ConstraintLayout keyboardLayout =
                (ConstraintLayout) getLayoutInflater().inflate(R.layout.keyboard, null);
        mCameraIMEUIController = new CameraIMEUIController(keyboardLayout);
        mOcrCameraController = new OcrCameraController(this, keyboardLayout, false);
        mFullScreenMode = AppSettings.getInstance(getApplicationContext()).getIsFullScreen();
        updateKeyboardHeight();
        return keyboardLayout;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        if (mDidFinish) {
            mCameraIMEUIController.resetFlashButton();
            mKeyboardTextManager.playSound = AppSettings.getInstance(getApplicationContext()).getKeyboardSound();
            mKeyboardTextManager.vibration = AppSettings.getInstance(getApplicationContext()).getKeyboardVibration();
            mGlobeFunction = AppSettings.getInstance(getApplicationContext()).getGlobeFunction();

            int newTheme = AppSettings.getInstance(getApplicationContext()).getTheme();
            if (mCameraIMEUIController.currentTheme != newTheme) {
                mCameraIMEUIController.setTheme(newTheme);
                mOcrCameraController.setTheme(newTheme);
            }

            if (!AppSettings.getInstance(getApplicationContext()).getTermsAccepted()) {
                if (!restarting) Toast.makeText(this, getString(R.string.acc_terms),
                        Toast.LENGTH_LONG).show();
                return;
            }

            mOrientationListener = new OrientationListener(this, SensorManager.SENSOR_DELAY_NORMAL,
                    this);
            if (mOrientationListener.canDetectOrientation()) {
                mOrientationListener.enable();
            }

            mOcrCameraController.setup();
            mDidFinish = false;
        } else {
            mOrientationListener.setup(this);
        }
        mOcrCameraController.resumeCamera(false);
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        mDidFinish = true;
        release();
    }

    private void release() {
        if (mOrientationListener != null) {
            mOrientationListener.disable();
            mOrientationListener.release();
            mOrientationListener = null;
        }
        mOcrCameraController.pauseCamera(false);
        mOcrCameraController.release();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mDidFinish) release();
    }

    @Override
    public void onRotation(int oldRotation, int newRotation) {
        if (newRotation == 180 && Utils.isOrientationUnlocked(this)) {
            mCameraIMEUIController.bitmapImageView.post(new Runnable() {
                @Override
                public void run() {
                    mOcrCameraController.pauseCamera(false);
                    mOcrCameraController.resumeCamera(false);
                    mOrientationListener.setup(CameraIME.this);
                }
            });
        }
        mOcrCameraController.onRotation(newRotation);
    }

    private void updateKeyboardHeight() {
        int recommendedHeight = 0;
        if (Build.VERSION.SDK_INT >= 21) recommendedHeight = getInputMethodWindowRecommendedHeight();
        mCameraIMEUIController.setKeyboardHeight(mFullScreenMode, recommendedHeight);
        updateFullscreenMode();
    }

    @Override
    public void onCameraEvent(boolean on) {
        mCameraIMEUIController.enableEditUI(!on);
    }

    public void onNewOcrText(String text) {
        mKeyboardTextManager.insertText(text, getCurrentInputConnection(),
                mCameraIMEUIController.bitmapImageView);
    }

    public void kbFullScreenButtonClick(View view) {
        mFullScreenMode = ((FullScreenButton) view).nextMode();
        updateKeyboardHeight();
    }

    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    public void kbGlobeButtonClick(View view) {
        if (!view.isPressed()) {
            mKeyboardTextManager.soundClick(0, view);
        } else {
            switchKeyboard();
        }
    }

    private void switchKeyboard() {
        final IBinder token = getToken();
        if (token == null) {
            mInputMethodManager.showInputMethodPicker();
            return;
        }

        switch(mGlobeFunction) {
            case 2:
                mInputMethodManager.showInputMethodPicker();
                break;
            case 1:
                if (mInputMethodManager.switchToLastInputMethod(token)) {
                    break;
                }
            default:
                if (!mInputMethodManager.switchToNextInputMethod(token, false)) {
                    if (mGlobeFunction != 1 && mInputMethodManager.switchToLastInputMethod(token)) {
                        break;
                    }
                    mInputMethodManager.showInputMethodPicker();
                }
        }
    }

    public void kbButtonClick(View view) {
        int keyCode;
        switch (view.getId()) {
            case (R.id.kb_space_button):
                keyCode = 32;
                break;
            default:
                keyCode = Keyboard.KEYCODE_DONE;
        }
        kbButtonAction(view, keyCode);
    }

    private void kbButtonAction(View view, int keyCode) {
        if (!view.isPressed()) {
            mKeyboardTextManager.soundClick(keyCode, view);
        } else {
            mKeyboardTextManager.onKey(keyCode, getCurrentInputConnection());
        }
    }

    public void kbBackspaceButtonClick(View view) {
        KeyboardRepeatButton keyboardRepeatButton = (KeyboardRepeatButton) view;
        if (keyboardRepeatButton.onClickDown) {
            mKeyboardTextManager.soundClick(Keyboard.KEYCODE_DELETE, view);
            keyboardRepeatButton.onClickDown = false;
            return;
        }

        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null && inputConnection.getSelectedText(0) == null) {
            CharSequence charSequence = inputConnection.getTextBeforeCursor(1, 0);
            if (charSequence != null && charSequence.length() == 0) return;
        }

        if (keyboardRepeatButton.repeating){
            mKeyboardTextManager.soundClick(Keyboard.KEYCODE_DELETE, view);
            mKeyboardTextManager.onKey(Keyboard.KEYCODE_DELETE, getCurrentInputConnection());
        } else {
            mKeyboardTextManager.onKey(Keyboard.KEYCODE_DELETE, getCurrentInputConnection());
        }
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return mFullScreenMode;
    }
}
