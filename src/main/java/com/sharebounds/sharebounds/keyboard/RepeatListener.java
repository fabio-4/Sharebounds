package com.sharebounds.sharebounds.keyboard;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

class RepeatListener extends KeyClickListener {

    private static final int INITIAL_TIME = 500;
    private static final int INTERVAL_TIME = 100;

    private Handler mHandler = new Handler();
    private KeyboardRepeatButton mButtonView;

    private Runnable mAction = new Runnable() {
        @Override public void run() {
            mHandler.postDelayed(this, INTERVAL_TIME);
            mButtonView.repeating = true;
            mButtonView.performClick();
        }
    };

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mButtonView = (KeyboardRepeatButton) view;
                mButtonView.onClickDown = true;
                mButtonView.repeating = false;
                setRect(view);
                mButtonView.performClick();
                mButtonView.setPressed(true);
                mHandler.removeCallbacks(mAction);
                mHandler.postDelayed(mAction, INITIAL_TIME);
                return true;
            case MotionEvent.ACTION_MOVE:
                if(!rectContains(view, motionEvent) && !mButtonView.repeating){
                    mHandler.removeCallbacks(mAction);
                    mButtonView.setPressed(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!mButtonView.repeating && rectContains(view, motionEvent)) {
                    mButtonView.performClick();
                }
            case MotionEvent.ACTION_CANCEL:
                mHandler.removeCallbacks(mAction);
                mButtonView.setPressed(false);
                mButtonView = null;
                return true;
        }
        return true;
    }
}
