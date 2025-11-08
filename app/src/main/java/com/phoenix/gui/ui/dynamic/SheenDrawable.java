package com.phoenix.gui.ui.dynamic;

import android.animation.ValueAnimator;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;
import androidx.annotation.NonNull;

public class SheenDrawable extends Drawable {

    private final Paint paint;
    private float cornerRadius = 0;
    private float sheenPosition = -1.0f;
    private ValueAnimator animator;

    public SheenDrawable() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        startAnimation();
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidateSelf();
    }

    private void startAnimation() {
        animator = ValueAnimator.ofFloat(-1.0f, 2.0f);
        animator.setDuration(2500);
        animator.setStartDelay(500);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);

        animator.addUpdateListener(animation -> {
            sheenPosition = (float) animation.getAnimatedValue();
            invalidateSelf();
        });

        animator.start();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        float gradientWidth = bounds.width() * 0.5f;
        float gradientStart = bounds.width() * sheenPosition;

        LinearGradient gradient = new LinearGradient(
            gradientStart - gradientWidth, 0,
            gradientStart, bounds.height(),
            new int[]{0x00FFFFFF, 0x14FFFFFF, 0x00FFFFFF},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );

        paint.setShader(gradient);

        RectF rect = new RectF(bounds);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
