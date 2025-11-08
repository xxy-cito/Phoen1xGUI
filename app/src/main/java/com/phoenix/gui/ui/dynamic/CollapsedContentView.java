package com.phoenix.gui.ui.dynamic;

import android.animation.ValueAnimator; 
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.phoenix.gui.ui.ThemeManager;

public class CollapsedContentView extends LinearLayout implements ThemeManager.OnThemeColorChangeListener {

    private ImageView brandIcon;
    private TextView brandText;

    private ImageView userIcon;
    private TextView userText;

    private ImageView fpsIcon;
    private TextView fpsNumberText;
    private TextView fpsLabelText;
    private FrameLayout fpsNumberContainer;

    private DynamicIslandManager manager;
    private float scale = 0.7f;
    private int currentFps = 0;

    public CollapsedContentView(@NonNull Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        setPadding(
            (int) dpToPx(12 * scale), 0,
            (int) dpToPx(12 * scale), 0
        );

        createBrandModule(context);
        addSeparator(context);

        createUserModule(context);
        addSeparator(context);

        createFpsModule(context);
    }

    private void createBrandModule(Context context) {
        LinearLayout module = new LinearLayout(context);
        module.setOrientation(HORIZONTAL);
        module.setGravity(Gravity.CENTER_VERTICAL);

        brandIcon = new ImageView(context);
        try {
            int iconId = context.getResources().getIdentifier("icon", "drawable", context.getPackageName());
            if (iconId != 0) {
                brandIcon.setImageResource(iconId);
            } else {
                brandIcon.setImageResource(android.R.drawable.ic_menu_info_details);
            }
        } catch (Exception e) {
            brandIcon.setImageResource(android.R.drawable.ic_menu_info_details);
        }
        // 获取颜色。
        brandIcon.setColorFilter(ThemeManager.getThemeColor());
        int iconSize = (int) dpToPx(16 * scale);
        module.addView(brandIcon, new LayoutParams(iconSize, iconSize));

        module.addView(createSpacer(context, 4 * scale), new LayoutParams(
            (int) dpToPx(4 * scale), 0
        ));

        brandText = createStyledText(context, "Phoen1xGUI", ThemeManager.getThemeColor(), true); // 神必小小小opai
        module.addView(brandText);

        addView(module);
    }

    private void createUserModule(Context context) {
        LinearLayout module = new LinearLayout(context);
        module.setOrientation(HORIZONTAL);
        module.setGravity(Gravity.CENTER_VERTICAL);

        userIcon = new ImageView(context);
        userIcon.setImageResource(android.R.drawable.ic_menu_myplaces);
        userIcon.setColorFilter(com.phoenix.gui.ui.ThemeManager.getTextSecondary());
        int iconSize = (int) dpToPx(16 * scale);
        module.addView(userIcon, new LayoutParams(iconSize, iconSize));

        module.addView(createSpacer(context, 4 * scale), new LayoutParams(
            (int) dpToPx(4 * scale), 0
        ));

        userText = createStyledText(context, "Test", com.phoenix.gui.ui.ThemeManager.getTextSecondary(), false); // 用户。
        module.addView(userText);

        addView(module);
    }

    private void createFpsModule(Context context) {
        LinearLayout module = new LinearLayout(context);
        module.setOrientation(HORIZONTAL);
        module.setGravity(Gravity.CENTER_VERTICAL);

        fpsIcon = new ImageView(context);
        fpsIcon.setImageResource(android.R.drawable.ic_menu_manage);
        fpsIcon.setColorFilter(com.phoenix.gui.ui.ThemeManager.getTextSecondary());
        int iconSize = (int) dpToPx(16 * scale);
        module.addView(fpsIcon, new LayoutParams(iconSize, iconSize));

        module.addView(createSpacer(context, 4 * scale), new LayoutParams(
            (int) dpToPx(4 * scale), 0
        ));

        fpsNumberContainer = new FrameLayout(context);
        fpsNumberContainer.setClipChildren(false);
        fpsNumberContainer.setClipToPadding(false);
        module.addView(fpsNumberContainer, new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ));

        fpsNumberText = createStyledText(context, "0", com.phoenix.gui.ui.ThemeManager.getTextSecondary(), false);
        fpsNumberContainer.addView(fpsNumberText, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        fpsLabelText = createStyledText(context, " FPS", com.phoenix.gui.ui.ThemeManager.getTextSecondary(), false);
        module.addView(fpsLabelText);

        addView(module);
    }

    private TextView createStyledText(Context context, String text, int color, boolean bold) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(color);
        textView.setTextSize(13 * scale);
        textView.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
        textView.setSingleLine(true);
        textView.setIncludeFontPadding(false);
        return textView;
    }

    private TextView createSeparator(Context context) {
        TextView separator = new TextView(context);
        separator.setText(" • ");

        int separatorColor = (com.phoenix.gui.ui.ThemeManager.getTextSecondary() & 0x00FFFFFF) | 0x66000000;
        separator.setTextColor(separatorColor);
        separator.setTextSize(13 * scale);
        separator.setIncludeFontPadding(false);
        LayoutParams params = new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        );
        params.setMargins(
            (int) dpToPx(4 * scale), 0,
            (int) dpToPx(4 * scale), 0
        );
        return separator;
    }

    private void addSeparator(Context context) {
        addView(createSeparator(context));
    }

    private android.view.View createSpacer(Context context, float widthDp) {
        return new android.view.View(context);
    }

    public void setManager(DynamicIslandManager manager) {
        this.manager = manager;
        updateContent();
    }

    public void updateContent() {
        if (manager == null) return;

        userText.setText(manager.getPersistentText());
    }

    public void updateFps(int fps) {
        if (currentFps == fps) return;

        final String newFpsText = String.valueOf(fps);
        currentFps = fps;

        fpsNumberText.animate()
            .translationY(-dpToPx(20 * scale))
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(150)
            .setInterpolator(new androidx.interpolator.view.animation.FastOutSlowInInterpolator())
            .withEndAction(() -> {
                final int startWidth = fpsNumberContainer.getWidth();

                Paint textPaint = fpsNumberText.getPaint();
                final int endWidth = (int) Math.ceil(textPaint.measureText(newFpsText));

                fpsNumberText.setText(newFpsText);

                fpsNumberText.setTranslationY(dpToPx(20 * scale));
                fpsNumberText.setAlpha(0f);
                fpsNumberText.setScaleX(1.2f);
                fpsNumberText.setScaleY(1.2f);

                fpsNumberText.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(400)
                    .setInterpolator(new OvershootInterpolator(2.5f))
                    .start();
                
                if (startWidth != endWidth) {
                    ValueAnimator widthAnimator = ValueAnimator.ofInt(startWidth, endWidth);
                    widthAnimator.setDuration(400);
                    widthAnimator.setInterpolator(new OvershootInterpolator(2.5f));
                    widthAnimator.addUpdateListener(animation -> {
                        int animatedWidth = (Integer) animation.getAnimatedValue();
                        ViewGroup.LayoutParams params = fpsNumberContainer.getLayoutParams();
                        params.width = animatedWidth;
                        fpsNumberContainer.setLayoutParams(params);
                    });
                    widthAnimator.start();
                }
            })
            .start();
    }

    public void updateConfig(float scale, String text) {
        this.scale = scale;
        userText.setText(text);

        float textSize = 13 * scale;
        brandText.setTextSize(textSize);
        userText.setTextSize(textSize);
        fpsNumberText.setTextSize(textSize);
        fpsLabelText.setTextSize(textSize);

        int iconSize = (int) dpToPx(16 * scale);
        updateIconSize(brandIcon, iconSize);
        updateIconSize(userIcon, iconSize);
        updateIconSize(fpsIcon, iconSize);

        setPadding(
            (int) dpToPx(12 * scale), 0,
            (int) dpToPx(12 * scale), 0
        );

        requestLayout();
    }

    private void updateIconSize(ImageView icon, int size) {
        if (icon != null) {
            LayoutParams params = (LayoutParams) icon.getLayoutParams();
            params.width = size;
            params.height = size;
            icon.setLayoutParams(params);
        }
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        if (brandIcon != null) {
            brandIcon.setColorFilter(newColor);
        }
        if (brandText != null) {
            brandText.setTextColor(newColor);
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.addListener(this);
        onThemeColorChanged(ThemeManager.getThemeColor());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ThemeManager.removeListener(this);
    }
}