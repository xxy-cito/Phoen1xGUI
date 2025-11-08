package com.phoenix.gui.ui.dynamic;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MaterialProgressBar extends View {

    private static final float DEFAULT_HEIGHT_DP = 8f;

    private float progress = 0f;

    private float sheenPosition = 0f;
    private ValueAnimator sheenAnimator;

    private Paint backgroundPaint;
    private Paint progressPaint;

    private float barHeight;
    private float cornerRadius;

    public MaterialProgressBar(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MaterialProgressBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        barHeight = DEFAULT_HEIGHT_DP * density;
        cornerRadius = barHeight / 2f;

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);

        int backgroundColor = (com.phoenix.gui.ui.ThemeManager.getThemeColor() & 0x00FFFFFF) | 0x26000000;
        backgroundPaint.setColor(backgroundColor);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.FILL);
        progressPaint.setColor(com.phoenix.gui.ui.ThemeManager.getThemeColor());

        startSheenAnimation();
    }

    private void startSheenAnimation() {
        sheenAnimator = ValueAnimator.ofFloat(0f, 2f);
        sheenAnimator.setDuration(1000);
        sheenAnimator.setInterpolator(new LinearInterpolator());
        sheenAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sheenAnimator.setRepeatMode(ValueAnimator.RESTART);

        sheenAnimator.addUpdateListener(animation -> {
            sheenPosition = (float) animation.getAnimatedValue();
            invalidate();
        });

        sheenAnimator.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (barHeight + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth() - getPaddingLeft() - getPaddingRight();
        float left = getPaddingLeft();
        float top = getPaddingTop();
        float right = left + width;
        float bottom = top + barHeight;

        RectF backgroundRect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint);

        float progressWidth = Math.max(width * progress, barHeight);
        float progressRight = left + progressWidth;

        if (progress > 0.001f) {

            canvas.save();

            RectF progressRect = new RectF(left, top, progressRight, bottom);
            canvas.clipRect(progressRect);

            canvas.drawRoundRect(progressRect, cornerRadius, cornerRadius, progressPaint);

            drawSheen(canvas, progressRect);

            canvas.restore();
        }
    }

    private void drawSheen(Canvas canvas, RectF rect) {
        float gradientWidth = rect.width() * 0.3f;
        float gradientStart = rect.width() * (sheenPosition - 1f);

        int colorSheen = (com.phoenix.gui.ui.ThemeManager.getTextPrimary() & 0x00FFFFFF) | 0x4D000000;

        LinearGradient gradient = new LinearGradient(
            rect.left + gradientStart,
            rect.top,
            rect.left + gradientStart + gradientWidth,
            rect.top,
            new int[]{0x00FFFFFF, colorSheen, 0x00FFFFFF},
            new float[]{0f, 0.5f, 1f},
            Shader.TileMode.CLAMP
        );

        Paint sheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sheenPaint.setShader(gradient);

        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, sheenPaint);
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public void setBarHeight(float heightDp) {
        float density = getResources().getDisplayMetrics().density;
        this.barHeight = heightDp * density;
        this.cornerRadius = barHeight / 2f;
        requestLayout();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (sheenAnimator != null) {
            sheenAnimator.cancel();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (sheenAnimator != null && !sheenAnimator.isRunning()) {
            sheenAnimator.start();
        }
    }
}
