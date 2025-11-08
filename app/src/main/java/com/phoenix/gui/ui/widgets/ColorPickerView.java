package com.phoenix.gui.ui.widgets;

// powered by qp

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import com.phoenix.gui.ui.ThemeManager;

public class ColorPickerView extends LinearLayout {

    private final ColorPaletteView paletteView;
    private final SeekBar hueSlider;
    private float currentHue;
    private float currentSat;
    private float currentVal;

    public ColorPickerView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setPadding(0, dpToPx(5), 0, 0);

        float[] initialHsv = ThemeManager.getThemeColorHSV();
        currentHue = initialHsv[0];
        currentSat = initialHsv[1];
        currentVal = initialHsv[2];

        paletteView = new ColorPaletteView(context);
        addView(paletteView, new LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(100)));

        hueSlider = new SeekBar(context);
        hueSlider.setMax(360);
        hueSlider.setProgress((int) currentHue);

        int[] hueColors = new int[361];
        for (int i = 0; i < hueColors.length; i++) {
            hueColors[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        GradientDrawable hueGradient = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, hueColors);
        hueSlider.setProgressDrawable(hueGradient);

        LayoutParams sliderParams = new LayoutParams(LayoutParams.MATCH_PARENT, dpToPx(10));
        sliderParams.setMargins(0, dpToPx(12), 0, 0);
        hueSlider.setLayoutParams(sliderParams);
        hueSlider.getThumb().setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));

        hueSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentHue = progress;
                    paletteView.setHue(currentHue);
                    updateThemeColor();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        addView(hueSlider);
    }

    private void updateThemeColor() {
        float[] hsv = {currentHue, currentSat, currentVal};
        int color = Color.HSVToColor(hsv);
        ThemeManager.setThemeColor(color);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private class ColorPaletteView extends View {
        private final Paint saturationPaint, valuePaint;
        private final Paint indicatorPaint;
        private Shader valueShader, saturationShader;
        private float hue;
        private float indicatorX, indicatorY;

        public ColorPaletteView(Context context) {
            super(context);
            saturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            indicatorPaint.setStyle(Paint.Style.STROKE);
            indicatorPaint.setStrokeWidth(dpToPx(2));
            indicatorPaint.setColor(Color.WHITE);
            indicatorPaint.setShadowLayer(dpToPx(2), 0, 0, Color.BLACK);
            setHue(currentHue);
        }

        public void setHue(float hue) {
            this.hue = hue;
            if (getWidth() > 0) {
                int hueColor = Color.HSVToColor(new float[]{hue, 1f, 1f});
                saturationShader = new LinearGradient(0, 0, getWidth(), 0, Color.WHITE, hueColor, Shader.TileMode.CLAMP);
                saturationPaint.setShader(saturationShader);
            }
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            valueShader = new LinearGradient(0, 0, 0, h, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);
            valuePaint.setShader(valueShader);
            setHue(hue);
            indicatorX = w * currentSat;
            indicatorY = h * (1 - currentVal);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), saturationPaint);
            canvas.drawRect(0, 0, getWidth(), getHeight(), valuePaint);
            canvas.drawCircle(indicatorX, indicatorY, dpToPx(6), indicatorPaint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    indicatorX = Math.max(0, Math.min(getWidth(), event.getX()));
                    indicatorY = Math.max(0, Math.min(getHeight(), event.getY()));
                    currentSat = indicatorX / getWidth();
                    currentVal = 1 - (indicatorY / getHeight());
                    updateThemeColor();
                    invalidate();
                    return true;
            }
            return super.onTouchEvent(event);
        }
    }
}