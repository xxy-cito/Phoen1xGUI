package com.phoenix.gui.ui.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phoenix.gui.config.ConfigManager;
import com.phoenix.gui.ui.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class ModeChips extends LinearLayout implements ThemeManager.OnThemeColorChangeListener {
    private final String moduleName;
    private final String configKey;
    private final List<String> modes;
    private final OnModeChangedListener onModeChanged;

    private int selectedIndex;
    private final List<TextView> chips = new ArrayList<>();
    private final List<GradientDrawable> chipBackgrounds = new ArrayList<>();
    private float scaleFactor = 1.0f;

    public interface OnModeChangedListener {
        void onModeChanged(int index, String mode);
    }

    public ModeChips(Context context, String moduleName, String configKey,
                    List<String> modes, int initialMode, OnModeChangedListener listener) {
        super(context);
        this.moduleName = moduleName;
        this.configKey = configKey;
        this.modes = modes;
        this.onModeChanged = listener;
        this.selectedIndex = initialMode;

        setOrientation(HORIZONTAL);

        for (int i = 0; i < modes.size(); i++) {
            TextView chip = createChip(modes.get(i), i);
            chips.add(chip);
            addView(chip);

            if (i < modes.size() - 1) {
                addView(createSpacer());
            }
        }

        updateSelection(initialMode, false);
        ThemeManager.addListener(this);
    }

    public void updateScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;

        for (TextView chip : chips) {
            chip.setTextSize(11f * scaleFactor);
            int paddingH = scaled(12);
            int paddingV = scaled(6);
            chip.setPadding(paddingH, paddingV, paddingH, paddingV);
        }

        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) instanceof LinearLayout) {
                LinearLayout spacer = (LinearLayout) getChildAt(i);
                if (spacer.getChildCount() == 0) {
                    spacer.setLayoutParams(new LayoutParams(scaled(4), LayoutParams.MATCH_PARENT));
                }
            }
        }

        updateSelection(selectedIndex, false);

        requestLayout();
    }

    private int scaled(int baseDp) {
        return (int) (dpToPx(baseDp) * scaleFactor);
    }

    private TextView createChip(String text, final int index) {
        TextView chip = new TextView(getContext());
        chip.setText(text);
        chip.setTextSize(11f * scaleFactor);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(scaled(12), scaled(6), scaled(12), scaled(6));

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        chip.setLayoutParams(params);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(scaled(8));
        chipBackgrounds.add(bg);
        chip.setBackground(bg);

        chip.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        v.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(100)
                            .start();
                        return true;

                    case MotionEvent.ACTION_UP:

                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();

                        if (event.getX() >= 0 && event.getX() <= v.getWidth() &&
                            event.getY() >= 0 && event.getY() <= v.getHeight()) {

                            if (selectedIndex != index) {
                                updateSelection(index, true);
                            }
                        }
                        return true;

                    case MotionEvent.ACTION_CANCEL:

                        v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                        return true;
                }
                return false;
            }
        });

        return chip;
    }

    private LinearLayout createSpacer() {
        LinearLayout spacer = new LinearLayout(getContext());
        spacer.setLayoutParams(new LayoutParams(scaled(4), LayoutParams.MATCH_PARENT));
        return spacer;
    }

    private void updateSelection(int newIndex, boolean animate) {
        int oldIndex = selectedIndex;
        selectedIndex = newIndex;

        for (int i = 0; i < chips.size(); i++) {
            applyChipStyle(i, i == newIndex);
        }

        if (animate && newIndex >= 0 && newIndex < chips.size()) {
            TextView newChip = chips.get(newIndex);
            newChip.setScaleX(0.9f);
            newChip.setScaleY(0.9f);
            newChip.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();

            onModeChanged.onModeChanged(newIndex, modes.get(newIndex));
            ConfigManager.saveModuleConfig(moduleName, configKey, newIndex);
        }
    }

    private void applyChipStyle(int index, boolean selected) {
        if (index < 0 || index >= chips.size()) return;

        TextView chip = chips.get(index);
        GradientDrawable bg = chipBackgrounds.get(index);

        if (selected) {
            bg.setColor(ThemeManager.getThemeColor());
            bg.setCornerRadius(scaled(8));
            chip.setTextColor(ThemeManager.getTextOnTheme());
            chip.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            bg.setColor(ThemeManager.getBgDisabled());
            bg.setCornerRadius(scaled(8));
            chip.setTextColor(ThemeManager.getTextSecondary());
            chip.setTypeface(Typeface.DEFAULT);
        }
    }

    public String getSelectedMode() {
        return modes.get(selectedIndex);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        updateSelection(selectedIndex, false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.removeListener(this);

        for (TextView chip : chips) {
            chip.animate().cancel();
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
