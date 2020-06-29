package com.sharebounds.sharebounds.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.sharebounds.sharebounds.R;

public class SettingsActivity extends BaseThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    void reloadActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
