package com.phoenix.gui.ui.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phoenix.gui.module.Module;
import com.phoenix.gui.ui.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class SubMenuPanel extends LinearLayout implements ThemeManager.OnThemeColorChangeListener {
    private final Module module;
    private boolean isOpen = false;
    private ValueAnimator heightAnimator;
    private Runnable onStateChangedListener;

    private float scaleFactor = 1.0f;

    private final List<ScalableWidget> scalableWidgets = new ArrayList<>();
    private final List<TextView> themeButtons = new ArrayList<>();
    private final int[] themeColors = {
        0xFF96CCFF, // Default
        0xFF4CAF50, // Green
        0xFFBB86FC, // Purple
        0xFFFF9800, // Orange
        0xFFE91E63  // Pink
    };

    public SubMenuPanel(Context context, Module module, float scaleFactor) {
        super(context);
        this.module = module;
        this.scaleFactor = scaleFactor;

        setOrientation(VERTICAL);
        setBackgroundColor(Color.TRANSPARENT);
        updatePadding();
        setVisibility(View.GONE);
        setAlpha(0f);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        params.setMargins(scaled(6), scaled(2), scaled(6), 0);
        setLayoutParams(params);
    }

    public SubMenuPanel(Context context, Module module) {
        this(context, module, 1.0f);
    }

    public void updateScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        updatePadding();
        LayoutParams params = (LayoutParams) getLayoutParams();
        params.setMargins(scaled(6), scaled(2), scaled(6), 0);
        setLayoutParams(params);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            updateChildScale(child);
        }
        for (ScalableWidget widget : scalableWidgets) {
            widget.updateScale(scaleFactor);
        }
        requestLayout();
    }

    private void updatePadding() {
        int padding = scaled(4);
        setPadding(padding, 0, padding, scaled(4));
    }

    private void updateChildScale(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            ViewGroup.LayoutParams lp = group.getLayoutParams();
            if (lp instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) lp;
                mlp.topMargin = scaled(2);
                mlp.bottomMargin = scaled(2);
            }
            for (int i = 0; i < group.getChildCount(); i++) {
                updateChildScale(group.getChildAt(i));
            }
        } else if (view instanceof TextView) {
            TextView tv = (TextView) view;
            float baseSize = 11f;
            if (tv.getCurrentTextColor() == ThemeManager.getTextTertiary()) {
                baseSize = 10f;
            }
            tv.setTextSize(baseSize * scaleFactor);
        }
    }

    private int scaled(int baseDp) {
        return (int) (dpToPx(baseDp) * scaleFactor);
    }

    public void addSlider(String label, int min, int max, int defaultValue, String format,
                         CustomSlider.OnValueChangedListener onValueChanged) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(VERTICAL);
        LayoutParams containerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, scaled(2), 0, scaled(2));
        container.setLayoutParams(containerParams);

        LinearLayout labelRow = new LinearLayout(getContext());
        labelRow.setOrientation(HORIZONTAL);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(11f * scaleFactor);
        labelText.setTextColor(ThemeManager.getTextSecondary());
        LayoutParams labelParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        labelText.setLayoutParams(labelParams);
        labelRow.addView(labelText);

        TextView valueText = new TextView(getContext());
        valueText.setTextSize(11f * scaleFactor);
        valueText.setTextColor(ThemeManager.getTextSecondary());
        labelRow.addView(valueText);

        container.addView(labelRow);

        CustomSlider slider = new CustomSlider(getContext(), module.getName(), label.toLowerCase(),
                min, max, defaultValue, value -> {
            if ("%.2f".equals(format)) {
                valueText.setText(String.format("%.2f", value / 100.0));
            } else {
                valueText.setText(String.valueOf(value));
            }
            onValueChanged.onValueChanged(value);
        });

        slider.updateScale(scaleFactor);
        LayoutParams sliderParams = new LayoutParams(LayoutParams.MATCH_PARENT, scaled(20));
        sliderParams.setMargins(0, scaled(2), 0, 0);
        container.addView(slider, sliderParams);
        scalableWidgets.add(new ScalableWidget(slider, labelText, valueText, sliderParams, 20));
        if ("%.2f".equals(format)) {
            valueText.setText(String.format("%.2f", defaultValue / 100.0));
        } else {
            valueText.setText(String.valueOf(defaultValue));
        }
        addView(container);
    }

    public void addShortcutSwitch(boolean defaultValue) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams containerParams = new LayoutParams(LayoutParams.MATCH_PARENT, scaled(28));
        containerParams.setMargins(0, scaled(2), 0, scaled(2));
        container.setLayoutParams(containerParams);

        TextView labelText = new TextView(getContext());
        labelText.setText("Shortcut");
        labelText.setTextSize(11f * scaleFactor);
        labelText.setTextColor(ThemeManager.getTextSecondary());
        LayoutParams labelParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        labelText.setLayoutParams(labelParams);
        container.addView(labelText);

        CustomSwitch switchView = new CustomSwitch(getContext(), module.getName(), "shortcut",
                defaultValue, enabled -> {
            module.toggleShortcut(enabled);
        });

        switchView.updateScale(scaleFactor);
        LayoutParams switchParams = new LayoutParams(scaled(32), scaled(18));
        container.addView(switchView, switchParams);
        scalableWidgets.add(new ScalableWidget(switchView, labelText, containerParams, switchParams, 28, 32, 18));
        addView(container);
    }

    public void addSwitch(String label, boolean defaultValue, CustomSwitch.OnStateChangedListener onStateChanged) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams containerParams = new LayoutParams(LayoutParams.MATCH_PARENT, scaled(28));
        containerParams.setMargins(0, scaled(2), 0, scaled(2));
        container.setLayoutParams(containerParams);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(11f * scaleFactor);
        labelText.setTextColor(ThemeManager.getTextSecondary());
        LayoutParams labelParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        labelText.setLayoutParams(labelParams);
        container.addView(labelText);

        CustomSwitch switchView = new CustomSwitch(getContext(), module.getName(), label.toLowerCase(),
                defaultValue, onStateChanged);

        switchView.updateScale(scaleFactor);
        LayoutParams switchParams = new LayoutParams(scaled(32), scaled(18));
        container.addView(switchView, switchParams);
        scalableWidgets.add(new ScalableWidget(switchView, labelText, containerParams, switchParams, 28, 32, 18));
        addView(container);
    }

    public void addMode(String label, List<String> modes, int defaultMode, ModeChips.OnModeChangedListener onModeChanged) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(VERTICAL);
        LayoutParams containerParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, scaled(2), 0, scaled(2));
        container.setLayoutParams(containerParams);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextSize(11f * scaleFactor);
        labelText.setTextColor(ThemeManager.getTextSecondary());
        LayoutParams labelParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        labelParams.setMargins(0, 0, 0, scaled(2));
        labelText.setLayoutParams(labelParams);
        container.addView(labelText);

        HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        ModeChips chips = new ModeChips(getContext(), module.getName(), label.toLowerCase(),
                modes, defaultMode, onModeChanged);

        chips.updateScale(scaleFactor);
        scrollView.addView(chips);
        container.addView(scrollView);
        scalableWidgets.add(new ScalableWidget(chips, labelText, null, null, 0));
        addView(container);
    }

    public void addText(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextSize(10f * scaleFactor);
        textView.setTextColor(ThemeManager.getTextTertiary());
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, scaled(2), 0, scaled(2));
        textView.setLayoutParams(params);
        addView(textView);
    }


    public void addCustomView(View view) {
        if (view.getLayoutParams() == null) {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0, scaled(4), 0, scaled(4));
            view.setLayoutParams(params);
        }
        addView(view);
    }

    public void addThemeButtons() {
        themeButtons.clear(); 
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(HORIZONTAL);

        String[] themeNames = {"Default", "Green", "Purple", "Orange", "Pink"};
        Runnable[] themeActions = {
            ThemeManager::applyDefaultTheme,
            ThemeManager::applyGreenTheme,
            ThemeManager::applyPurpleTheme,
            ThemeManager::applyOrangeTheme,
            ThemeManager::applyPinkTheme
        };

        for(int i = 0; i < themeNames.length; i++) {
            TextView button = new TextView(getContext());
            button.setText(themeNames[i]);
            button.setTextSize(10f * scaleFactor);
            button.setGravity(Gravity.CENTER);
            int paddingH = scaled(8);
            int paddingV = scaled(5);
            button.setPadding(paddingH, paddingV, paddingH, paddingV);

          
            button.setBackground(new GradientDrawable());

            final Runnable action = themeActions[i];
            button.setOnClickListener(v -> action.run());

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (i > 0) {
                params.leftMargin = scaled(4);
            }
            container.addView(button, params);
            themeButtons.add(button);
        }

        HorizontalScrollView scrollView = new HorizontalScrollView(getContext());
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.addView(container);
        addCustomView(scrollView);

        updateThemeButtonStyles();
    }

    private void updateThemeButtonStyles() {
        int currentThemeColor = ThemeManager.getThemeColor();
        for (int i = 0; i < themeButtons.size(); i++) {
            TextView button = themeButtons.get(i);
            GradientDrawable bg = (GradientDrawable) button.getBackground();

            boolean isSelected = currentThemeColor == themeColors[i];

            if (isSelected) {
                bg.setColor(ThemeManager.getThemeColor());
                button.setTextColor(ThemeManager.getTextOnTheme());
            } else {
                bg.setColor(ThemeManager.getBgDisabled());
                button.setTextColor(ThemeManager.getTextPrimary());
            }
            bg.setCornerRadius(scaled(6));
        }
    }

    

    public void toggle() {
        toggle(null);
    }

    public void toggle(Runnable onComplete) {
        if (isOpen) {
            collapse(onComplete);
        } else {
            expand(onComplete);
        }
    }

    private void expand(Runnable onComplete) {
        isOpen = true;
        setVisibility(View.VISIBLE);
        measure(
            MeasureSpec.makeMeasureSpec(((ViewGroup)getParent()).getWidth(), MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
        int targetHeight = getMeasuredHeight();
        if (heightAnimator != null) heightAnimator.cancel();
        getLayoutParams().height = 0;
        heightAnimator = ValueAnimator.ofInt(0, targetHeight);
        heightAnimator.setDuration(250);
        heightAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            setLayoutParams(params);
        });
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewGroup.LayoutParams params = getLayoutParams();
                params.height = LayoutParams.WRAP_CONTENT;
                setLayoutParams(params);
                if (onComplete != null) onComplete.run();
                if (onStateChangedListener != null) onStateChangedListener.run();
            }
        });
        heightAnimator.start();
        setAlpha(0f);
        animate().alpha(1f).setDuration(250).start();
    }

    public void collapse(Runnable onComplete) {
        if (!isOpen) {
            if (onComplete != null) onComplete.run();
            return;
        }
        isOpen = false;
        int currentHeight = getHeight();
        getLayoutParams().height = currentHeight;
        if (heightAnimator != null) heightAnimator.cancel();
        animate().alpha(0f).setDuration(250).start();
        heightAnimator = ValueAnimator.ofInt(currentHeight, 0);
        heightAnimator.setDuration(250);
        heightAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            setLayoutParams(params);
        });
        heightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setVisibility(View.GONE);
                if (onComplete != null) onComplete.run();
                if (onStateChangedListener != null) onStateChangedListener.run();
            }
        });
        heightAnimator.start();
    }

    public void forceCollapse() {
        if (heightAnimator != null) heightAnimator.cancel();
        isOpen = false;
        setVisibility(View.GONE);
        setAlpha(0f);
        getLayoutParams().height = 0;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOnStateChangedListener(Runnable listener) {
        onStateChangedListener = listener;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    // 3. 实现 onThemeColorChanged 方法
    @Override
    public void onThemeColorChanged(int newColor) {
        updateThemeButtonStyles();
    }
    
    // 4. 注册和注销监听器
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.removeListener(this);
    }

    private class ScalableWidget {
        View widget;
        TextView label;
        TextView valueText;
        LayoutParams sliderParams;
        LayoutParams containerParams;
        LayoutParams switchParams;
        int baseSliderHeight;
        int baseContainerHeight;
        int baseSwitchWidth;
        int baseSwitchHeight;

        ScalableWidget(View widget, TextView label, TextView valueText, LayoutParams sliderParams, int baseSliderHeight) {
            this.widget = widget;
            this.label = label;
            this.valueText = valueText;
            this.sliderParams = sliderParams;
            this.baseSliderHeight = baseSliderHeight;
        }

        ScalableWidget(View widget, TextView label, LayoutParams containerParams, LayoutParams switchParams,
                      int baseContainerHeight, int baseSwitchWidth, int baseSwitchHeight) {
            this.widget = widget;
            this.label = label;
            this.containerParams = containerParams;
            this.switchParams = switchParams;
            this.baseContainerHeight = baseContainerHeight;
            this.baseSwitchWidth = baseSwitchWidth;
            this.baseSwitchHeight = baseSwitchHeight;
        }

        void updateScale(float scale) {
            if (label != null) {
                label.setTextSize(11f * scale);
            }
            if (valueText != null) {
                valueText.setTextSize(11f * scale);
            }
            if (sliderParams != null) {
                sliderParams.height = scaled(baseSliderHeight);
                sliderParams.setMargins(0, scaled(2), 0, 0);
            }
            if (containerParams != null) {
                containerParams.height = scaled(baseContainerHeight);
                containerParams.setMargins(0, scaled(2), 0, scaled(2));
            }
            if (switchParams != null) {
                switchParams.width = scaled(baseSwitchWidth);
                switchParams.height = scaled(baseSwitchHeight);
            }
            if (widget instanceof CustomSlider) {
                ((CustomSlider) widget).updateScale(scale);
            } else if (widget instanceof CustomSwitch) {
                ((CustomSwitch) widget).updateScale(scale);
            } else if (widget instanceof ModeChips) {
                ((ModeChips) widget).updateScale(scale);
            }
        }
    }
}