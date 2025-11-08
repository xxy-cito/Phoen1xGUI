package com.phoenix.gui.ui.widgets;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.phoenix.gui.config.ConfigManager;
import com.phoenix.gui.ui.ThemeManager;

public class CustomSwitch extends View implements ThemeManager.OnThemeColorChangeListener {
    private final String moduleName;
    private final String configKey;
    private final OnStateChangedListener onStateChanged;

    private boolean isChecked = false;
    private float thumbX = 0f;
    private float scaleFactor = 1.0f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF trackRect = new RectF();
    private ValueAnimator thumbAnimator;

    public interface OnStateChangedListener {
        void onStateChanged(boolean enabled);
    }

    public CustomSwitch(Context context, String moduleName, String configKey,
                       boolean initialState, OnStateChangedListener listener) {
        super(context);
        this.moduleName = moduleName;
        this.configKey = configKey;
        this.onStateChanged = listener;

        thumbPaint.setColor(ThemeManager.getTextPrimary());
        setChecked(initialState, false);

        setOnClickListener(v -> setChecked(!isChecked, true));

        ThemeManager.addListener(this);
    }

    public void updateScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;

        setChecked(isChecked, false);
        invalidate();
    }

    private float scaledDp(float baseDp) {
        return dpToPx(baseDp) * scaleFactor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int themeColor = ThemeManager.getThemeColor();

        trackRect.set(0f, 0f, getWidth(), getHeight());
        trackPaint.setColor(isChecked ? themeColor : ThemeManager.getStateDisabled());
        canvas.drawRoundRect(trackRect, getHeight() / 2f, getHeight() / 2f, trackPaint);

        float thumbRadius = getHeight() / 2f - scaledDp(2f);
        canvas.drawCircle(thumbX, getHeight() / 2f, thumbRadius, thumbPaint);
    }

    public void setChecked(boolean checked, boolean animate) {
        if (isChecked == checked && thumbAnimator != null && thumbAnimator.isRunning()) return;

        isChecked = checked;
        float thumbRadius = getHeight() / 2f - scaledDp(2f);
        float targetX = isChecked ? getWidth() - thumbRadius - scaledDp(2f) : thumbRadius + scaledDp(2f);

        if (animate) {
            if (thumbAnimator != null) thumbAnimator.cancel();
            thumbAnimator = ValueAnimator.ofFloat(thumbX, targetX);
            thumbAnimator.setDuration(200);
            thumbAnimator.setInterpolator(new DecelerateInterpolator());
            thumbAnimator.addUpdateListener(animation -> {
                thumbX = (float) animation.getAnimatedValue();
                invalidate();
            });
            thumbAnimator.start();
        } else {
            thumbX = targetX;
            invalidate();
        }

        onStateChanged.onStateChanged(isChecked);
        ConfigManager.saveModuleConfig(moduleName, configKey, isChecked);
    }

    public boolean isChecked() {
        return isChecked;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setChecked(isChecked, false);
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.removeListener(this);
        if (thumbAnimator != null) thumbAnimator.cancel();
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
