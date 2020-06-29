package com.sharebounds.sharebounds.keyboard;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;

class KeyClickListener implements View.OnTouchListener {

    private Rect mRect;

    void setRect(View view) {
        mRect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    boolean rectContains(View view, MotionEvent motionEvent) {
        return mRect.contains(view.getLeft() + (int) motionEvent.getX(),
                view.getTop() + (int) motionEvent.getY());
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setRect(view);
                view.performClick();
                view.setPressed(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                if(!rectContains(view, motionEvent)){
                    view.setPressed(false);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (rectContains(view, motionEvent)) {
                    view.performClick();
                }
            case MotionEvent.ACTION_CANCEL:
                view.setPressed(false);
                return true;
        }
        return false;
    }
}
