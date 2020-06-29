package com.sharebounds.sharebounds.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.sharebounds.sharebounds.AppSettings;
import com.sharebounds.sharebounds.R;

public class BitmapImageView extends AppCompatImageView {

    AnimatorSet animatorSet;
    private RectF mAnimationRect;
    private float mAnimationRectRadius;
    private Paint mPaint = new Paint();
    private int animationFrames = 60;

    void animate(RectF rect) {
        mAnimationRect = rect;
        setStroke(Math.min(80, rect.height()));
        animatorSet.start();
    }

    void endAnimation() {
        if (animatorSet != null && animatorSet.isRunning())
            animatorSet.end();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mAnimationRect != null) {
            canvas.drawRoundRect(mAnimationRect, mAnimationRectRadius, mAnimationRectRadius, mPaint);
        }
    }

    public BitmapImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setScaleType(ScaleType.MATRIX);
        int theme = AppSettings.getInstance(context.getApplicationContext()).getTheme();
        setTheme(theme);
        setupAnimation();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void setStroke(float width) {
        mAnimationRectRadius = width / 8;
        mPaint.setStrokeWidth(mAnimationRectRadius);
    }

    void setTheme(int theme) {
        int color;
        switch (theme) {
            case 1:
                color = R.color.colorDarkPrimary;
                break;
            default:
                color = R.color.colorPrimary;
        }
        mPaint.setColor(getResources().getColor(color));
    }

    private void setupAnimation() {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        ValueAnimator appearAnimator = ValueAnimator.ofInt(0, animationFrames);
        appearAnimator.setDuration(150).setInterpolator(new DecelerateInterpolator());
        appearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                mPaint.setAlpha(255 * value / animationFrames);
                invalidate();
            }
        });

        ValueAnimator disappearAnimator = ValueAnimator.ofInt(0, animationFrames);
        disappearAnimator.setDuration(150).setInterpolator(new AccelerateInterpolator());
        disappearAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                mPaint.setAlpha(255 * (animationFrames - (value / animationFrames)));
                invalidate();
            }
        });

        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(appearAnimator, disappearAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationRect = null;
                invalidate();
            }
        });
    }

    Matrix setImageViewBitmap(Bitmap rotImageBitmap, Pair<Integer, Integer> sizeValues) {
        Matrix fitMatrix = new Matrix();
        RectF imageRectF = new RectF(0, 0, rotImageBitmap.getWidth(), rotImageBitmap.getHeight());
        RectF viewRectF = new RectF(0, 0, sizeValues.first, sizeValues.second);
        fitMatrix.setRectToRect(imageRectF, viewRectF, Matrix.ScaleToFit.CENTER);
        this.setImageMatrix(fitMatrix);

        this.setImageBitmap(null);
        this.setImageBitmap(rotImageBitmap);
        invalidate();
        if (this.getAlpha() == 0.0f) {
            this.animate().alpha(1.0f).setDuration(300).withStartAction(new Runnable() {
                @Override
                public void run() {
                    BitmapImageView.this.setVisibility(View.VISIBLE);
                }
            });
        }
        return fitMatrix;
    }

    void reset() {
        this.animate().alpha(0.0f).setDuration(300).withEndAction(new Runnable() {
            @Override
            public void run() {
                BitmapImageView.this.setVisibility(View.INVISIBLE);
            }
        });
    }
}
