package com.phoenix.gui.ui;

import android.animation.ArgbEvaluator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.phoenix.gui.module.Module;
import com.phoenix.gui.ui.widgets.SubMenuPanel;

public class ModuleItemView extends LinearLayout {

    private final Module module;
    private final MenuView parentMenu;

    private final FrameLayout mainContainer;
    private final TextView normalText;
    private final TextView boldText;
    private final SubMenuPanel subMenuPanel;

    private ValueAnimator colorAnimator;
    private ValueAnimator touchStateAnimator;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private boolean isLongPressing = false;

    private float touchStartX = 0f;
    private float touchStartY = 0f;
    private boolean hasMoved = false;
    private final int touchSlop;

    private float scaleFactor = 1.0f;

    private boolean lastKnownState = false;

    private static final long LONG_PRESS_DELAY = 500L;
        private static final float PRESS_SCALE = 0.95f; 
    private static final int PRESS_ANIMATION_DURATION = 150; 
    private static final int RELEASE_ANIMATION_DURATION = 250;
    private static final int CORNER_RADIUS_DP = 8;
    private final float cornerRadiusPx;
    private final TimeInterpolator pressInterpolator = new DecelerateInterpolator(); 
    private final TimeInterpolator releaseInterpolator = new OvershootInterpolator(1.5f); 

    public ModuleItemView(Context context, Module module, MenuView parentMenu, float scaleFactor) {
        super(context);
        this.module = module;
        this.parentMenu = parentMenu;
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.lastKnownState = module.isEnabled();
        this.scaleFactor = scaleFactor;
        this.cornerRadiusPx = dpToPx(CORNER_RADIUS_DP);

        setOrientation(VERTICAL);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mainContainer = createMainContainer();
        addView(mainContainer);

        normalText = createNormalText();
        boldText = createBoldText();
        mainContainer.addView(normalText);
        mainContainer.addView(boldText);

        subMenuPanel = new SubMenuPanel(context, module, scaleFactor);
        addView(subMenuPanel);

        module.configureSubMenu(subMenuPanel);

        subMenuPanel.setOnStateChangedListener(() -> {

        });

        updateVisualState(module.isEnabled(), false);

        setupTouchListeners();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_HARDWARE, null);
        }
    }

    public void syncWithModuleState(boolean currentState) {
        if (lastKnownState != currentState) {
            lastKnownState = currentState;
            updateVisualState(currentState, true);
        }
    }

    public void updateScale(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        
        LayoutParams params = (LayoutParams) mainContainer.getLayoutParams();
        params.setMargins(0, 0, 0, 0);
        mainContainer.setLayoutParams(params);

        float textSize = 11f * scaleFactor;
        int paddingV = scaled(7);
        int paddingH = scaled(12);

        normalText.setTextSize(textSize);
        normalText.setPadding(paddingH, paddingV, paddingH, paddingV);

        boldText.setTextSize(textSize);
        boldText.setPadding(paddingH, paddingV, paddingH, paddingV);

        subMenuPanel.updateScale(scaleFactor);

        requestLayout();
    }

    private int scaled(int baseDp) {
        return (int) (dpToPx(baseDp) * scaleFactor);
    }

    private FrameLayout createMainContainer() {
        FrameLayout container = new FrameLayout(getContext());
        
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        container.setLayoutParams(params);

        GradientDrawable initialBackground = new GradientDrawable();
        initialBackground.setColor(Color.TRANSPARENT);
        initialBackground.setCornerRadius(0f);
        container.setBackground(initialBackground);

        return container;
    }

    

    private TextView createNormalText() {
        TextView textView = new TextView(getContext());
        textView.setText(module.getName());
        textView.setTextSize(11f * scaleFactor);
        textView.setTextColor(ThemeManager.getTextPrimary());
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        int paddingV = scaled(7);
        int paddingH = scaled(12);
        textView.setPadding(paddingH, paddingV, paddingH, paddingV);
        textView.setTypeface(Typeface.DEFAULT);
        textView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }

    private TextView createBoldText() {
        TextView textView = new TextView(getContext());
        textView.setText(module.getName());
        textView.setTextSize(11f * scaleFactor);
        textView.setTextColor(ThemeManager.getTextOnTheme());
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        int paddingV = scaled(7);
        int paddingH = scaled(12);
        textView.setPadding(paddingH, paddingV, paddingH, paddingV);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setAlpha(0f);
        return textView;
    }


    
    private void setupTouchListeners() {
        mainContainer.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    hasMoved = false;
                    isLongPressing = false;

                    longPressRunnable = () -> {
                        if (!hasMoved) {
                            isLongPressing = true;
                            onLongPress();
                        }
                    };
                    longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DELAY);

                   
                    animateTouchState(true);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = Math.abs(event.getX() - touchStartX);
                    float deltaY = Math.abs(event.getY() - touchStartY);

                    boolean isOutOfBounds = event.getX() < 0 || event.getX() > getWidth() || event.getY() < 0 || event.getY() > getHeight();

                    if (!hasMoved && (deltaX > touchSlop || deltaY > touchSlop || isOutOfBounds)) {
                        hasMoved = true;
                        cancelLongPressDetection();
                        
                        animateTouchState(false);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    cancelLongPressDetection();
                    
                    animateTouchState(false);

                    if (!hasMoved && !isLongPressing) {
                        module.toggle();
                        lastKnownState = module.isEnabled();
                        updateVisualState(lastKnownState, true);
                    }

                    isLongPressing = false;
                    hasMoved = false;
                    return true;
            }
            return false;
        });
    }

    private void animateTouchState(boolean pressed) {
        if (touchStateAnimator != null) {
            touchStateAnimator.cancel();
        }

       
        final float startScale = mainContainer.getScaleX();
        final float endScale = pressed ? PRESS_SCALE : 1.0f;

        final GradientDrawable background = (GradientDrawable) mainContainer.getBackground();
        final float startRadius = background.getCornerRadii() != null ? background.getCornerRadii()[0] : 0f;
        final float endRadius = pressed ? cornerRadiusPx : 0f;

        
        touchStateAnimator = ValueAnimator.ofFloat(0f, 1f);
        touchStateAnimator.setDuration(pressed ? PRESS_ANIMATION_DURATION : RELEASE_ANIMATION_DURATION);
        touchStateAnimator.setInterpolator(pressed ? pressInterpolator : releaseInterpolator);

        touchStateAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction(); 
            float currentScale = startScale + (endScale - startScale) * fraction;
            float currentRadius = startRadius + (endRadius - startRadius) * fraction;

            mainContainer.setScaleX(currentScale);
            mainContainer.setScaleY(currentScale);
            background.setCornerRadius(currentRadius);
        });

        touchStateAnimator.start();
    }


    private void onLongPress() {
        performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
        subMenuPanel.toggle();
    }

    private void cancelLongPressDetection() {
        if (longPressRunnable != null) {
            longPressHandler.removeCallbacks(longPressRunnable);
        }
        longPressRunnable = null;
    }

    
    public void updateVisualState(boolean isEnabled, boolean animated) {
        if (animated) {
            animateToState(isEnabled);
        } else {
            setStateImmediately(isEnabled);
        }
    }

    private void setStateImmediately(boolean isEnabled) {
        GradientDrawable bg = (GradientDrawable) mainContainer.getBackground();
        bg.setColor(isEnabled ? ThemeManager.getThemeColor() : Color.TRANSPARENT);
        bg.setCornerRadius(0f);

        normalText.setAlpha(isEnabled ? 0f : 1f);
        boldText.setAlpha(isEnabled ? 1f : 0f);
    }

    private void animateToState(boolean isEnabled) {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }

        GradientDrawable bg = (GradientDrawable) mainContainer.getBackground();

        int fromColor = (bg.getColor() != null) ? bg.getColor().getDefaultColor() : Color.TRANSPARENT;
        int toColor = isEnabled ? ThemeManager.getThemeColor() : Color.TRANSPARENT;

        colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor);
        colorAnimator.setDuration(150);
        colorAnimator.addUpdateListener(animator -> {
            bg.setColor((int) animator.getAnimatedValue());
        });
        colorAnimator.start();

        normalText.animate().alpha(isEnabled ? 0f : 1f).setDuration(150).start();
        boldText.animate().alpha(isEnabled ? 1f : 0f).setDuration(150).start();
    }


    public void updateThemeColor() {
        if (module.isEnabled()) {
            GradientDrawable bg = (GradientDrawable) mainContainer.getBackground();
            bg.setColor(ThemeManager.getThemeColor());
        }
    }

    public void closeSubMenuIfOpen() {
        if (subMenuPanel.isOpen()) {
            subMenuPanel.forceCollapse();
        }
    }

    public void cancelAllAnimations() {
        if (colorAnimator != null) colorAnimator.cancel();
        
        if (touchStateAnimator != null) touchStateAnimator.cancel();
        mainContainer.animate().cancel(); //保留
        normalText.animate().cancel();
        boldText.animate().cancel();
        cancelLongPressDetection();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelAllAnimations();
        longPressHandler.removeCallbacksAndMessages(null);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}