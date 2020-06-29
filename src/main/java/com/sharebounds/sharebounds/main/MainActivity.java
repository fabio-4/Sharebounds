package com.sharebounds.sharebounds.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.OrientationListener;
import com.sharebounds.sharebounds.PermissionUtils;
import com.sharebounds.sharebounds.R;
import com.sharebounds.sharebounds.camera.ImageData;
import com.sharebounds.sharebounds.camera.OcrCameraController;

public class MainActivity extends AppCompatActivity implements OrientationListener.OrientationCallBack,
        OcrCameraController.OcrCameraListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int TEXT_REQUEST = 1;
    private static final int THEME_REQUEST = 2;
    private static final int PICK_IMAGE_REQUEST = 3;

    static final String CURRENT_TEXT = "currentText";
    private static final String STATE_TEXT = "stateText";
    private static final String STATE_IMAGE_DATA = "imageData";

    private MainUIController mMainUIController;
    private OcrCameraController mOcrCameraController;
    private GalleryImageLoader mGalleryImageLoader;
    private OrientationListener mOrientationListener;

    private String mText = "";
    private void setText(String text) {
        mText = text;
        mMainUIController.enableTextButton(!text.isEmpty(), false);
    }

    private ImageData currentImageData;
    void setImage(Bitmap bitmap, byte[] bytes) {
        if (mIsLoadingImage && currentImageData != null)
            mOcrCameraController.processImage(bitmap, bytes, currentImageData);
        mIsLoadingImage = false;
        currentImageData = null;
        if (bytes != null) mGalleryImageLoader.restoreBytesTask = null;
    }
    private boolean mIsLoadingImage = false;
    private boolean mPermissionRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.pref_settings, false);
        setContentView(R.layout.activity_main);

        mOrientationListener = new OrientationListener(this, SensorManager.SENSOR_DELAY_NORMAL,
                this);
        mMainUIController = new MainUIController(this);
        mGalleryImageLoader = new GalleryImageLoader(this);
        mOcrCameraController = new OcrCameraController(this, mMainUIController.mainLayout);

        if (savedInstanceState == null) {
            mMainUIController.enableTextButton(false, false);

            AppSettings.LaunchDialog launchDialog = AppSettings.getInstance(getApplicationContext())
                    .getLaunchDialog();
            switch (launchDialog) {
                case Tutorial:
                    BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                    bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    break;
                case Rate:
                    ShareUtils.showRateDialog(this);
                    break;
            }
        }

        if (!PermissionUtils.getPermission(this, PermissionUtils.Type.Camera)) {
            mPermissionRequested = true;
            PermissionUtils.requestPermission(this, PermissionUtils.Type.Camera);
        }

        if (savedInstanceState != null) {
            String text = savedInstanceState.getString(STATE_TEXT);
            if (text != null) setText(text);

            ImageData imageData = savedInstanceState.getParcelable(STATE_IMAGE_DATA);
            if (imageData != null) {
                currentImageData = imageData;
                mMainUIController.enableFlashButton(false, false);
                mOcrCameraController.pauseCamera(true);
                reloadBitmapFromUri(imageData.uri);
            }
        }
    }

    private void reloadBitmapFromUri(final Uri uri) {
        mIsLoadingImage = true;
        if (uri == null) {
            mGalleryImageLoader.restoreImage();
        } else {
            mMainUIController.mainLayout.post(new Runnable() {
                @Override
                public void run() {
                    mGalleryImageLoader.loadSetImage(uri, mOcrCameraController.getImageViewSize());
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOrientationListener.canDetectOrientation()) {
            mOrientationListener.enable();
        }
        if (!mPermissionRequested) mOcrCameraController.resumeCamera(false);
        mOcrCameraController.setup();
    }

    @Override
    protected void onPause() {
        super.onPause();
        release(false);
    }

    private void release(boolean stop) {
        mOrientationListener.disable();
        mOcrCameraController.pauseCamera(stop);
        mOcrCameraController.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_TEXT, mText);
        ImageData currentData = mOcrCameraController.getParcelableData();
        if (currentData != null) {
            if (mOcrCameraController.getBytes() != null) {
                mGalleryImageLoader.saveImage(mOcrCameraController.getBytes());
            }
            outState.putParcelable(STATE_IMAGE_DATA, currentData);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release(true);
        mIsLoadingImage = false;
        mGalleryImageLoader.cancelRequest();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PermissionUtils.CAMERA_REQUEST:
                    mOcrCameraController.resumeCamera(true);
                    break;
                case PermissionUtils.STORAGE_REQUEST:
                    showImagePicker();
                    break;
            }
        } else {
            PermissionUtils.errorToast(this, PermissionUtils.Type.getType(requestCode));
        }
        mPermissionRequested = false;
    }

    public void onRotation(final int oldRotation, final int newRotation) {
        mMainUIController.mainLayout.post(new Runnable() {
            @Override
            public void run() {
                mMainUIController.rotateImageView(oldRotation, newRotation);
                mMainUIController.rotateButtons(oldRotation, newRotation);
                mOcrCameraController.onRotation(newRotation);
            }
        });
    }

    @Override
    public void onCameraEvent(boolean on) {
        if (on && mIsLoadingImage){
            mGalleryImageLoader.cancelRequest();
            mIsLoadingImage = false;
            currentImageData = null;
        }
        mMainUIController.enableEditUI(!on, !mText.isEmpty());
    }

    public void onNewOcrText(String text) {
        setText(mText + text);
    }

    public void cancelButtonClick(View view) {
        mText = "";
        mMainUIController.enableTextButton(false, true);
    }

    public void imagePickerIntent(View view) {
        if (PermissionUtils.getPermission(this, PermissionUtils.Type.Storage)) {
            showImagePicker();
        } else {
            PermissionUtils.requestPermission(this, PermissionUtils.Type.Storage);
        }
    }

    private void showImagePicker() {
        Intent imagePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        imagePickerIntent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(imagePickerIntent,
                        getString(R.string.image_picker_text)), PICK_IMAGE_REQUEST);
    }

    public void textIntent(View view) {
        Intent textIntent = new Intent(this, TextActivity.class);
        textIntent.putExtra(CURRENT_TEXT, mText);
        startActivityForResult(textIntent, TEXT_REQUEST);
    }

    public void settingsIntent(View view) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivityForResult(settingsIntent, THEME_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (TEXT_REQUEST):
                if (resultCode == RESULT_OK) {
                    setText(data.getStringExtra(TextActivity.INTENT_TEXT));
                }
                break;
            case (THEME_REQUEST):
                final int newTheme = AppSettings.getInstance(getApplicationContext()).getTheme();
                if (mMainUIController.currentTheme != newTheme) {
                    mMainUIController.setTheme(newTheme);
                    mOcrCameraController.setTheme(newTheme);
                }
                break;
            case (PICK_IMAGE_REQUEST):
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    mOcrCameraController.pauseCamera(true);
                    Uri imageUri = data.getData();
                    currentImageData = new ImageData(imageUri, 0, null);
                    mIsLoadingImage = true;
                    mGalleryImageLoader.loadSetImage(imageUri, mOcrCameraController.getImageViewSize());
                }
        }
    }
}
