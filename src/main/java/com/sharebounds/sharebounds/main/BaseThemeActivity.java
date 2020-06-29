package com.sharebounds.sharebounds.main;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.R;

public class BaseThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int theme = AppSettings.getInstance(getApplicationContext()).getTheme();
        setTheme(theme);

        setupActionBar();
    }

    @Override
    public void setTheme(int theme) {
        int newTheme;
        switch (theme) {
            case 1:
                newTheme = R.style.AppThemeDark;
                break;
            default:
                newTheme = R.style.AppTheme;
        }
        super.setTheme(newTheme);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
