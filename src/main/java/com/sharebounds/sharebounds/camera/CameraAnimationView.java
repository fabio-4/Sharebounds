package com.sharebounds.sharebounds.camera;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.sharebounds.sharebounds.R;

class CameraAnimationView extends View {

    private Animation mShutterAnim;

    public CameraAnimationView(Context context, AttributeSet set) {
        super(context, set);
        setBackgroundColor(Color.WHITE);
        setVisibility(View.INVISIBLE);
        mShutterAnim = AnimationUtils.loadAnimation(context.getApplicationContext(), R.anim.shutter);
        mShutterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                CameraAnimationView.this.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                CameraAnimationView.this.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });
    }

    void shutter() {
        this.startAnimation(mShutterAnim);
    }
}
