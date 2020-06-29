package com.sharebounds.sharebounds.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.R;

public class PrivTouActivity extends AppCompatActivity {

    private Button mButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.MainTheme);
        super.onCreate(savedInstanceState);

        if (AppSettings.getInstance(getApplicationContext()).getTermsAccepted()) {
            toMainActivity();
        }

        setContentView(R.layout.activity_priv_tou);

        mButton = findViewById(R.id.buttonTouPriv);
        CheckBox mCheckBox = findViewById(R.id.checkBoxTouPriv);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mButton.setEnabled(isChecked);
            }
        });
    }

    public void buttonClicked(View view) {
        AppSettings.getInstance(getApplicationContext()).setTermsAccepted(true);
        toMainActivity();
    }

    private void toMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
