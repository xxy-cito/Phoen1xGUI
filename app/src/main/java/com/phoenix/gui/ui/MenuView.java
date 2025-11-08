package com.phoenix.gui.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.ViewGroup;
import com.phoenix.gui.module.Module;
import com.phoenix.gui.module.ModuleCategory;
import com.phoenix.gui.module.ModuleManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuView extends LinearLayout implements ThemeManager.OnThemeColorChangeListener {

    private static final String PREF_NAME = "menu_view_settings";
    private static final String KEY_SCALE_FACTOR = "scale_factor";

    private final Context context;
    private final ModuleCategory category;
    private final MenuPosition position;
    private final SharedPreferences preferences;

    private boolean isExpanded = false;

    private final LinearLayout titleBar;
    private final ScrollView scrollView;
    private final LinearLayout contentLayout;

    private final Map<Module, ModuleItemView> moduleItems = new HashMap<>();
    private ValueAnimator expandAnimator;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private float touchStartX = 0f;
    private float touchStartY = 0f;
    private boolean isDragging = false;
    private final int touchSlop;

    private float scaleFactor = 1.0f;

    private final float baseScaleFactor;

    private static final long ANIMATION_DURATION = 250L;
    private static final float CORNER_RADIUS_DP = 8f;

    private static int globalMaxWidth = 0;

    private final Runnable statusChecker = new Runnable() {
        @Override
        public void run() {
            if (getVisibility() == View.VISIBLE && isExpanded) {
                refreshAllModuleStates();
                handler.postDelayed(this, 500);
            }
        }
    };

    public enum MenuPosition {
        POSITION_1, POSITION_2, POSITION_3, POSITION_4, POSITION_5
    }

    public MenuView(Context context, ModuleCategory category, MenuPosition position) {
        super(context);
        this.context = context;
        this.category = category;
        this.position = position;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        this.baseScaleFactor = calculateBaseScaleFactor() * 0.75f;

        this.scaleFactor = 1.0f;

        setOrientation(VERTICAL);

        List<Module> modules = ModuleManager.getModulesByCategory(category);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ThemeManager.getBgPrimary());
        bg.setCornerRadius(scaled(CORNER_RADIUS_DP));
        setBackground(bg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setClipToOutline(true);
            setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            setElevation(scaled(12));
        } else {
            setClipChildren(true);
            setClipToPadding(true);
        }

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        setLayoutParams(layoutParams);

        int titleHeight = scaled(36);
        int spacing = scaled(12);
        setTranslationY(position.ordinal() * (titleHeight + spacing));

        titleBar = createTitleBar();
        addView(titleBar);

        scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setVisibility(View.GONE);
        scrollView.setClipChildren(false);
        scrollView.setClipToPadding(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            scrollView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        contentLayout = new LinearLayout(context);
        contentLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        contentLayout.setOrientation(VERTICAL);
        contentLayout.setPadding(0, 0, 0, 0);
        contentLayout.setClipChildren(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            contentLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        for (Module module : modules) {
            ModuleItemView item = new ModuleItemView(context, module, this, baseScaleFactor * scaleFactor);
            moduleItems.put(module, item);
            contentLayout.addView(item);
        }

        scrollView.addView(contentLayout);
        addView(scrollView);

        setupTouchListeners();

        ThemeManager.addListener(this);
    }

    private void refreshAllModuleStates() {
        for (Map.Entry<Module, ModuleItemView> entry : moduleItems.entrySet()) {
            Module module = entry.getKey();
            ModuleItemView item = entry.getValue();
            item.syncWithModuleState(module.isEnabled());
        }
    }

    private float calculateBaseScaleFactor() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        float dpi = metrics.densityDpi;

        if (dpi <= 240) {
            return 0.7f;
        } else if (dpi <= 320) {
            return 0.85f;
        } else if (dpi <= 480) {
            return 1.0f;
        } else if (dpi <= 640) {
            return 1.15f;
        } else {
            return 1.3f;
        }
    }

    private int scaled(int baseDp) {
        return (int) (dpToPx(baseDp) * baseScaleFactor * scaleFactor);
    }

    private float scaled(float baseDp) {
        return dpToPx((int) baseDp) * baseScaleFactor * scaleFactor;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float factor) {
        factor = Math.max(0.5f, Math.min(2.0f, factor));
        if (Math.abs(this.scaleFactor - factor) < 0.01f) {
            return;
        }

        this.scaleFactor = factor;

        preferences.edit()
            .putFloat(KEY_SCALE_FACTOR, factor)
            .apply();

        rebuildUI();
    }

    public void increaseScale() {
        setScaleFactor(scaleFactor * 1.1f);
    }

    public void decreaseScale() {
        setScaleFactor(scaleFactor * 0.9f);
    }

    public void resetScale() {
        setScaleFactor(1.0f);
    }

    private void rebuildUI() {
        boolean wasExpanded = isExpanded;

        if (isExpanded) {
            collapseContent();
        }

        GradientDrawable bg = (GradientDrawable) getBackground();
        bg.setCornerRadius(scaled(CORNER_RADIUS_DP));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setElevation(scaled(12));
        }

        updateTitleBar();

        for (ModuleItemView item : moduleItems.values()) {
            item.updateScale(baseScaleFactor * scaleFactor);
        }

        globalMaxWidth = 0;
        measureInitialSize();

        if (wasExpanded) {
            handler.postDelayed(this::expandContent, 100);
        }
    }

    private void updateTitleBar() {
        int height = scaled(36);
        titleBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height));
        titleBar.setPadding(scaled(14), 0, scaled(14), 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ThemeManager.getBgSecondary());
        bg.setCornerRadius(scaled(CORNER_RADIUS_DP));
        titleBar.setBackground(bg);

        TextView textView = (TextView) titleBar.getChildAt(0);
        textView.setTextSize(11.5f * baseScaleFactor * scaleFactor);
    }

    private LinearLayout createTitleBar() {
        LinearLayout bar = new LinearLayout(context);
        int height = scaled(36);
        bar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, height));
        bar.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        bar.setPadding(scaled(14), 0, scaled(14), 0);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(ThemeManager.getBgSecondary());
        bg.setCornerRadius(scaled(CORNER_RADIUS_DP));
        bar.setBackground(bg);

        TextView textView = new TextView(context);
        textView.setText(category.getDisplayName());
        textView.setTextSize(11.5f * baseScaleFactor * scaleFactor);
        textView.setTextColor(ThemeManager.getTextPrimary());
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        bar.addView(textView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            bar.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }

        return bar;
    }

    private void setupTouchListeners() {
        final float[] lastRaw = new float[2];

        titleBar.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    lastRaw[0] = event.getRawX();
                    lastRaw[1] = event.getRawY();
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaX = Math.abs(event.getX() - touchStartX);
                    float deltaY = Math.abs(event.getY() - touchStartY);

                    if (!isDragging && (deltaX > touchSlop || deltaY > touchSlop)) {
                        isDragging = true;
                    }

                    if (isDragging) {
                        float nowRawX = event.getRawX();
                        float nowRawY = event.getRawY();
                        float moveDeltaX = nowRawX - lastRaw[0];
                        float moveDeltaY = nowRawY - lastRaw[1];
                        MenuView.this.setTranslationX(MenuView.this.getTranslationX() + moveDeltaX);
                        MenuView.this.setTranslationY(MenuView.this.getTranslationY() + moveDeltaY);
                        lastRaw[0] = nowRawX;
                        lastRaw[1] = nowRawY;
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isDragging) {
                        toggleExpand();
                    }
                    isDragging = false;
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    isDragging = false;
                    return true;
            }
            return false;
        });
    }

    private void toggleExpand() {
        if (expandAnimator != null) expandAnimator.cancel();

        if (isExpanded) {
            collapseContent();
        } else {
            expandContent();
        }
    }

    private void expandContent() {
        isExpanded = true;
        scrollView.setVisibility(View.VISIBLE);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics screenSize = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(screenSize);

        contentLayout.measure(
            MeasureSpec.makeMeasureSpec(globalMaxWidth, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
        int maxHeight = (int) (screenSize.heightPixels * 0.5);
        int targetHeight = Math.min(contentLayout.getMeasuredHeight(), maxHeight);

        scrollView.setAlpha(0f);
        LayoutParams lp = (LayoutParams) scrollView.getLayoutParams();
        lp.height = 0;
        scrollView.setLayoutParams(lp);

        expandAnimator = ValueAnimator.ofInt(0, targetHeight);
        expandAnimator.setDuration(ANIMATION_DURATION);
        expandAnimator.setInterpolator(new DecelerateInterpolator());
        expandAnimator.addUpdateListener(animator -> {
            int value = (int) animator.getAnimatedValue();
            LayoutParams params = (LayoutParams) scrollView.getLayoutParams();
            params.height = value;
            scrollView.setLayoutParams(params);
            scrollView.setAlpha((float) value / targetHeight);
        });
        expandAnimator.start();

        handler.post(statusChecker);
    }

    private void collapseContent() {
        isExpanded = false;
        int startHeight = scrollView.getHeight();

        closeAllSubMenus();

        handler.removeCallbacks(statusChecker);

        expandAnimator = ValueAnimator.ofInt(startHeight, 0);
        expandAnimator.setDuration(ANIMATION_DURATION);
        expandAnimator.setInterpolator(new DecelerateInterpolator());
        expandAnimator.addUpdateListener(animator -> {
            int value = (int) animator.getAnimatedValue();
            LayoutParams params = (LayoutParams) scrollView.getLayoutParams();
            params.height = value;
            scrollView.setLayoutParams(params);
            if (startHeight > 0) {
                scrollView.setAlpha((float) value / startHeight);
            }
        });
        expandAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                scrollView.setVisibility(View.GONE);
            }
        });
        expandAnimator.start();
    }

    public void show() {
        setVisibility(View.VISIBLE);
        measureInitialSize();

        setAlpha(0f);
        setScaleX(0.8f);
        setScaleY(0.8f);
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(ANIMATION_DURATION)
            .setInterpolator(new DecelerateInterpolator())
            .start();

        if (isExpanded) {
            handler.post(statusChecker);
        }
    }

    private void measureInitialSize() {
        int maxWidth = globalMaxWidth;

        titleBar.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
        maxWidth = Math.max(maxWidth, titleBar.getMeasuredWidth());

        for (ModuleItemView item : moduleItems.values()) {
            item.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            );
            maxWidth = Math.max(maxWidth, item.getMeasuredWidth());
        }

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
DisplayMetrics screenSize = new DisplayMetrics();
windowManager.getDefaultDisplay().getRealMetrics(screenSize);
int smallerDimension = Math.min(screenSize.widthPixels, screenSize.heightPixels);
int minWidth = (int) (smallerDimension * 0.3);
int maxAllowedWidth = (int) (smallerDimension * 0.5);
maxWidth = Math.max(minWidth, Math.min(maxWidth, maxAllowedWidth)); // 你说屎吧是屎 但是好像还有可读性
        maxWidth = Math.max(minWidth, Math.min(maxWidth, maxAllowedWidth));

        if (maxWidth > globalMaxWidth) {
            globalMaxWidth = maxWidth;
        }

        ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            params.width = globalMaxWidth;
            setLayoutParams(params);
        }
    }

    public void hide() {
        handler.removeCallbacks(statusChecker);

        if (expandAnimator != null) expandAnimator.cancel();
        animate().cancel();

        for (ModuleItemView item : moduleItems.values()) {
            item.cancelAllAnimations();
        }

        animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(ANIMATION_DURATION)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> setVisibility(View.GONE))
            .start();
    }

    public void closeAllSubMenus() {
        for (ModuleItemView item : moduleItems.values()) {
            item.closeSubMenuIfOpen();
        }
    }

    public String getScaleInfo() {
        return String.format("基础缩放: %.2f, 用户缩放: %.2f, 总缩放: %.2f", //debug
            baseScaleFactor, scaleFactor, baseScaleFactor * scaleFactor);
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        for (ModuleItemView item : moduleItems.values()) {
            item.updateThemeColor();
        }
    }

    public void destroy() {
        ThemeManager.removeListener(this);
        if (expandAnimator != null) expandAnimator.cancel();
        handler.removeCallbacksAndMessages(null);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}