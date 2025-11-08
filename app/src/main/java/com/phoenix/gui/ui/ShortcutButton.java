package com.phoenix.gui.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phoenix.gui.module.Module;

public class ShortcutButton extends LinearLayout implements ThemeManager.OnThemeColorChangeListener {

    private static final String PREF_NAME = "shortcut_button_positions";
    private static final String KEY_PREFIX_X = "button_x_";
    private static final String KEY_PREFIX_Y = "button_y_";

    private final Module module;
    private final SharedPreferences preferences;

    private final TextView textView;
    private final GradientDrawable background;

    private int currentBackgroundColor;
    private int fixedTextWidth = -1;

    public ShortcutButton(Context context, Module module) {
        super(context);
        this.module = module;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);

        background = new GradientDrawable();
        currentBackgroundColor = module.isEnabled()
            ? ThemeManager.getThemeColor()
            : ThemeManager.getBgDisabled();
        background.setColor(currentBackgroundColor);
        background.setCornerRadius(dpToPx(20));
        setBackground(background);

        int paddingH = dpToPx(12);
        int paddingV = dpToPx(8);
        setPadding(paddingH, paddingV, paddingH, paddingV);

        textView = new TextView(context);
        textView.setText(module.getName());
        textView.setTextSize(11f);
        int textColor = module.isEnabled()
            ? ThemeManager.getTextOnTheme()
            : ThemeManager.getTextPrimary();
        textView.setTextColor(textColor);
        textView.setTypeface(module.isEnabled()
            ? Typeface.DEFAULT_BOLD
            : Typeface.DEFAULT);
        textView.setGravity(Gravity.CENTER);

        fixedTextWidth = calculateMaxTextWidth();
        LayoutParams textParams = new LayoutParams(fixedTextWidth, LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(textParams);

        addView(textView);

        
        loadPosition();

        setupTouchListeners();

        ThemeManager.addListener(this);

        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    private int calculateMaxTextWidth() {
        Paint paint = new Paint();
        paint.setTextSize(textView.getTextSize());

        paint.setTypeface(Typeface.DEFAULT);
        float normalWidth = paint.measureText(module.getName());

        paint.setTypeface(Typeface.DEFAULT_BOLD);
        float boldWidth = paint.measureText(module.getName());

        return (int) Math.ceil(Math.max(normalWidth, boldWidth));
    }

    private void loadPosition() {
        String key = getPositionKey();

        boolean hasSavedPosition = preferences.contains(KEY_PREFIX_X + key)
                                  && preferences.contains(KEY_PREFIX_Y + key);

        if (hasSavedPosition) {
            float x = preferences.getInt(KEY_PREFIX_X + key, 0);
            float y = preferences.getInt(KEY_PREFIX_Y + key, 0);
            setTranslationX(x);
            setTranslationY(y);
        } else {
            generateRandomPosition();
        }
    }

    private void generateRandomPosition() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);

        int estimatedWidth = dpToPx(150);
        int estimatedHeight = dpToPx(50);

        int margin = dpToPx(30);

        int minX = margin - metrics.widthPixels / 2 + estimatedWidth / 2;
        int maxX = metrics.widthPixels / 2 - estimatedWidth / 2 - margin;
        int minY = margin - metrics.heightPixels / 2 + estimatedHeight / 2;
        int maxY = metrics.heightPixels / 2 - estimatedHeight / 2 - margin;

        if (maxX > minX) {
            setTranslationX(minX + (int) (Math.random() * (maxX - minX)));
        }

        if (maxY > minY) {
            setTranslationY(minY + (int) (Math.random() * (maxY - minY)));
        }
    }

    private void savePosition() {
        String key = getPositionKey();
        preferences.edit()
            .putInt(KEY_PREFIX_X + key, (int) getTranslationX())
            .putInt(KEY_PREFIX_Y + key, (int) getTranslationY())
            .apply();
    }

    private String getPositionKey() {
        return module.getName().replaceAll("[^a-zA-Z0-9]", "_");
    }

    private void setupTouchListeners() {
        final float[] downPos = new float[2];
        final boolean[] hasMoved = {false};
        final long[] downTime = {0};

        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downPos[0] = event.getX();
                    downPos[1] = event.getY();
                    hasMoved[0] = false;
                    downTime[0] = System.currentTimeMillis();

                    animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .start();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    float moveY = event.getY();
                    float deltaX = moveX - downPos[0];
                    float deltaY = moveY - downPos[1];

                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        hasMoved[0] = true;
                        setTranslationX(getTranslationX() + deltaX);
                        setTranslationY(getTranslationY() + deltaY);
                    }
                    return true;

                case MotionEvent.ACTION_UP:

                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();

                    long clickDuration = System.currentTimeMillis() - downTime[0];

                    if (!hasMoved[0] && clickDuration < 300) {
                        module.toggle();
                        updateStyle();
                    } else if (hasMoved[0]) {
                        savePosition();
                    }

                    hasMoved[0] = false;
                    return true;

                case MotionEvent.ACTION_CANCEL:

                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();
                    hasMoved[0] = false;
                    return true;
            }
            return false;
        });
    }

    public void updateStyle() {
        boolean isEnabled = module.isEnabled();
        int targetColor = isEnabled ? ThemeManager.getThemeColor() : ThemeManager.getBgDisabled();
        int textColor = isEnabled ? ThemeManager.getTextOnTheme() : ThemeManager.getTextPrimary();
        Typeface targetTypeface = isEnabled ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT;

        ValueAnimator colorAnimator = ValueAnimator.ofArgb(currentBackgroundColor, targetColor);
        colorAnimator.setDuration(150);
        colorAnimator.addUpdateListener(animator -> {
            int color = (int) animator.getAnimatedValue();
            background.setColor(color);
            currentBackgroundColor = color;
        });
        colorAnimator.start();

        textView.animate()
            .alpha(0.5f)
            .setDuration(75)
            .withEndAction(() -> {
                textView.setTextColor(textColor);
                textView.setTypeface(targetTypeface);
                textView.animate()
                    .alpha(1f)
                    .setDuration(75)
                    .start();
            })
            .start();
    }

    public void show() {
        setVisibility(VISIBLE);
        setAlpha(0f);
        setScaleX(0.5f);
        setScaleY(0.5f);
        
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start();
    }

    public void hide() {
        animate()
            .alpha(0f)
            .scaleX(0.5f)
            .scaleY(0.5f)
            .setDuration(200)
            .withEndAction(() -> setVisibility(GONE))
            .start();
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        if (module.isEnabled()) {
            ValueAnimator colorAnimator = ValueAnimator.ofArgb(currentBackgroundColor, newColor);
            colorAnimator.setDuration(150);
            colorAnimator.addUpdateListener(animator -> {
                int color = (int) animator.getAnimatedValue();
                background.setColor(color);
                currentBackgroundColor = color;
            });
            colorAnimator.start();
        }
    }

    public void destroy() {
        clearAnimation();
        if (animate() != null) {
            animate().cancel();
        }
        if (textView != null && textView.animate() != null) {
            textView.animate().cancel();
        }
        
        ThemeManager.removeListener(this);
        
        savePosition();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
