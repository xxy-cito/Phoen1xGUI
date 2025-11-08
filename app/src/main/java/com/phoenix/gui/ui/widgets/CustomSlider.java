package com.phoenix.gui.ui.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

import com.phoenix.gui.config.ConfigManager;
import com.phoenix.gui.ui.ThemeManager;

public class CustomSlider extends View implements ThemeManager.OnThemeColorChangeListener {
    private final String moduleName;
    private final String configKey;
    private final int min;
    private final int max;
    private final OnValueChangedListener onValueChanged;

    private float progress = 0f;
    private float scaleFactor = 1.0f;

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public interface OnValueChangedListener {
        void onValueChanged(int value);
    }

    public CustomSlider(Context context, String moduleName, String configKey,
                       int min, int max, int initialValue, OnValueChangedListener listener) {
        super(context);
        this.moduleName = moduleName;
        this.configKey = configKey;
        this.min = min;
        this.max = max;
        this.onValueChanged = listener;

        updatePaints();

        thumbFillPaint.setColor(ThemeManager.getTextPrimary());

        setValue(initialValue, false);

        ThemeManager.addListener(this);
    }

    public void updateScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        updatePaints();
        invalidate();
    }

    private void updatePaints() {
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(scaledDp(3f));
        thumbStrokePaint.setStyle(Paint.Style.STROKE);
        thumbStrokePaint.setStrokeWidth(scaledDp(2f));
    }

    private float scaledDp(float baseDp) {
        return dpToPx(baseDp) * scaleFactor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int themeColor = ThemeManager.getThemeColor();
        thumbStrokePaint.setColor(themeColor);

        float trackY = getHeight() / 2f;
        float thumbRadius = getHeight() / 2f - scaledDp(3f);
        float thumbX = thumbRadius + (getWidth() - 2 * thumbRadius) * progress;

        trackPaint.setColor(ThemeManager.getStateDisabled());
        canvas.drawLine(thumbX, trackY, getWidth() - thumbRadius, trackY, trackPaint);

        trackPaint.setColor(themeColor);
        canvas.drawLine(thumbRadius, trackY, thumbX, trackY, trackPaint);

        canvas.drawCircle(thumbX, trackY, thumbRadius, thumbFillPaint);
        canvas.drawCircle(thumbX, trackY, thumbRadius, thumbStrokePaint);
    }

    public void setValue(int value, boolean notify) {
        progress = (float)(value - min) / (max - min);
        if (notify) {
            onValueChanged.onValueChanged(value);

            ConfigManager.saveModuleConfig(moduleName, configKey, value);
        }
        invalidate();
    }

    public int getValue() {
        return min + (int)(progress * (max - min));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float newProgress = event.getX() / getWidth();
                progress = Math.max(0f, Math.min(1f, newProgress));
                int currentValue = getValue();
                onValueChanged.onValueChanged(currentValue);
                ConfigManager.saveModuleConfig(moduleName, configKey, currentValue);
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.removeListener(this);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
