package com.sharebounds.sharebounds.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.sharebounds.sharebounds.R;

public class TextActivity extends BaseThemeActivity {

    static final String INTENT_TEXT = "textActivityText";

    private TextInputEditText mTextField;
    private MenuItem mShareButton;
    private boolean mDidScroll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        mTextField = findViewById(R.id.textField);

        if (savedInstanceState == null) {
            TextInputLayout textInputLayout = findViewById(R.id.textLayout);
            textInputLayout.setHintAnimationEnabled(false);
            String text = getIntent().getStringExtra(MainActivity.CURRENT_TEXT);
            mTextField.setText(text);
            mTextField.clearFocus();
            textInputLayout.setHintAnimationEnabled(true);
        }

        setupListeners();
    }

    private void setupListeners() {
        mTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                shareButtonStatus(charSequence);
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        findViewById(R.id.textScroll).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scrollTouchHandleKeyboard(motionEvent);
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && !mDidScroll) view.performClick();
                return false;
            }
        });
    }

    private void shareButtonStatus(CharSequence charSequence) {
        boolean empty = TextUtils.isEmpty(charSequence.toString().trim());
        if (mShareButton != null && (mShareButton.isEnabled() == empty)) {
            enableShareButton(!empty);
        }
    }

    private void enableShareButton(boolean bool) {
        mShareButton.setEnabled(bool);
        mShareButton.getIcon().setAlpha(bool ? 255 : 130);
    }

    private void scrollTouchHandleKeyboard(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_SCROLL:
                mDidScroll = true;
                break;
            case MotionEvent.ACTION_UP:
                if (!mDidScroll) {
                    hideKeyboard();
                } else {
                    mDidScroll = false;
                }
        }
    }

    private void hideKeyboard() {
        InputMethodManager input = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (input != null) {
            input.hideSoftInputFromWindow(mTextField.getWindowToken(), 0);
            mTextField.clearFocus();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_share, menu);
        mShareButton = menu.findItem(R.id.menu_item_share);
        enableShareButton(!TextUtils.isEmpty(mTextField.getText().toString().trim()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                ShareUtils.startShareIntent(this, mTextField.getText().toString().trim());
                mTextField.clearFocus();
                return true;
            case android.R.id.home:
                saveResult();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(INTENT_TEXT, mTextField.getText().toString());
        setResult(RESULT_OK, resultIntent);
    }

    @Override
    public void onBackPressed() {
        saveResult();
        super.onBackPressed();
    }
}
