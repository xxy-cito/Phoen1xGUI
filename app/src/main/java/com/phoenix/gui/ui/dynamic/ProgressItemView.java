package com.phoenix.gui.ui.dynamic;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.phoenix.gui.ui.ThemeManager;

public class ProgressItemView extends AnimatedTaskItemView implements ThemeManager.OnThemeColorChangeListener {

    private int colorPrimary;
    private int colorOnPrimary;
    private GradientDrawable iconBackground;

    private static final int ICON_SIZE_DP = 40;
    private static final int ICON_CORNER_RADIUS_DP = 14;
    private static final int ICON_INNER_SIZE_DP = 24;

    private ImageView iconView;
    private TextView titleText;
    private TextView subtitleText;
    private MaterialProgressBar progressBar;

    public ProgressItemView(@NonNull Context context, DynamicIslandManager.TaskItem task, float scale) {
        super(context, task, scale);
    }

    @Override
    protected void init(Context context) {
        colorPrimary = ThemeManager.getThemeColor();
        colorOnPrimary = ThemeManager.getTextOnTheme();

        setLayoutParams(new LayoutParams(
            LayoutParams.MATCH_PARENT,
            (int) (dpToPx(52 + 8 + 12) * scale)
        ));

        LinearLayout mainContainer = new LinearLayout(context);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setGravity(Gravity.CENTER_VERTICAL);
        addView(mainContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        LinearLayout topContainer = new LinearLayout(context);
        topContainer.setOrientation(LinearLayout.HORIZONTAL);
        topContainer.setGravity(Gravity.CENTER_VERTICAL);
        topContainer.setPadding(
            (int) (dpToPx(24) * scale), 0,
            (int) (dpToPx(24) * scale), 0
        );
        mainContainer.addView(topContainer, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) (dpToPx(52) * scale)
        ));

        iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int iconSize = (int) (dpToPx(ICON_SIZE_DP) * scale);

        iconBackground = new GradientDrawable();
        iconBackground.setShape(GradientDrawable.RECTANGLE);
        iconBackground.setCornerRadius(dpToPx(ICON_CORNER_RADIUS_DP) * scale);
        iconBackground.setColor(adjustAlpha(colorPrimary, 0.5f));
        iconView.setBackground(iconBackground);

        int iconPadding = (int) ((dpToPx(ICON_SIZE_DP) - dpToPx(ICON_INNER_SIZE_DP)) / 2 * scale);
        iconView.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        iconView.setColorFilter(colorOnPrimary);

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        topContainer.addView(iconView, iconParams);

        topContainer.addView(new android.view.View(context), new LinearLayout.LayoutParams(
            (int) (dpToPx(12) * scale), 0
        ));

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setGravity(Gravity.CENTER_VERTICAL);
        topContainer.addView(textContainer, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
        ));

        titleText = new TextView(context);
        titleText.setTextColor(com.phoenix.gui.ui.ThemeManager.getTextPrimary());
        titleText.setTextSize(15 * scale);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setSingleLine(true);
        titleText.setIncludeFontPadding(false);
        textContainer.addView(titleText);

        subtitleText = new TextView(context);
        subtitleText.setTextColor(com.phoenix.gui.ui.ThemeManager.getTextSecondary());
        subtitleText.setTextSize(12 * scale);
        subtitleText.setSingleLine(true);
        subtitleText.setIncludeFontPadding(false);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = (int) (dpToPx(2) * scale);
        textContainer.addView(subtitleText, subtitleParams);

        mainContainer.addView(new android.view.View(context), new LinearLayout.LayoutParams(
            0, (int) (dpToPx(6) * scale)
        ));

        progressBar = new MaterialProgressBar(context);
        progressBar.setBarHeight(8 * scale);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) (dpToPx(8) * scale)
        );
        progressParams.setMargins(
            (int) (dpToPx(24) * scale),
            0,
            (int) (dpToPx(24) * scale),
            0
        );
        mainContainer.addView(progressBar, progressParams);

        mainContainer.addView(new android.view.View(context), new LinearLayout.LayoutParams(
            0, (int) (dpToPx(6) * scale)
        ));

        updateContent();
    }

    @Override
    protected void updateContent() {

        titleText.setText(task.text);

        if (task.subtitle != null && !task.subtitle.isEmpty()) {
            subtitleText.setText(task.subtitle);
            subtitleText.setVisibility(VISIBLE);
        } else {
            subtitleText.setVisibility(GONE);
        }

        if (task.icon != null) {
            iconView.setImageDrawable(task.icon);
            iconView.setVisibility(VISIBLE);
        } else {
            iconView.setVisibility(GONE);
        }

        progressBar.setProgress(task.displayProgress);
    }

    private int adjustAlpha(int color, float alpha) {
        int alphaInt = Math.round(Color.alpha(color) * alpha);
        return Color.argb(alphaInt, Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        this.colorPrimary = newColor;
        this.colorOnPrimary = ThemeManager.getTextOnTheme();

        if (iconBackground != null) {
            iconBackground.setColor(adjustAlpha(colorPrimary, 0.5f));
        }
        if (iconView != null) {
            iconView.setColorFilter(colorOnPrimary);
        }
    }

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
}