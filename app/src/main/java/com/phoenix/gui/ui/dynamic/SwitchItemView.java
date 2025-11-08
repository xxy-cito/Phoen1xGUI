package com.phoenix.gui.ui.dynamic;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.phoenix.gui.ui.ThemeManager;
import java.util.Objects;

public class SwitchItemView extends TaskItemView implements ThemeManager.OnThemeColorChangeListener {

    private int colorPrimary;
    private MaterialSwitchView switchView;
    private TextView titleText;
    private LinearLayout subtitleContainer;

    private String lastRenderedText = null;
    private String lastRenderedSubtitle = null;
    private boolean lastRenderedState = false;
    private boolean isInitialized = false;

    public SwitchItemView(@NonNull Context context, DynamicIslandManager.TaskItem task, float scale) {
        super(context, task, scale);
    }

    @Override
    protected void init(Context context) {
        colorPrimary = ThemeManager.getThemeColor();

        setLayoutParams(new LayoutParams(
            LayoutParams.MATCH_PARENT,
            (int) (dpToPx(52) * scale)
        ));
        setPadding(
            (int) (dpToPx(24) * scale), 0,
            (int) (dpToPx(24) * scale), 0
        );

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setClickable(false);
        addView(container, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        switchView = new MaterialSwitchView(context);
        switchView.setScaleX(scale * 1.1f);
        switchView.setScaleY(scale * 1.1f);
        switchView.setClickable(false);
        LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        container.addView(switchView, switchParams);

        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
            (int) (dpToPx(12) * scale), 0
        );
        container.addView(new android.view.View(context), spacerParams);

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setGravity(Gravity.CENTER_VERTICAL);
        container.addView(textContainer, new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
        ));

        titleText = new TextView(context);
        titleText.setTextColor(com.phoenix.gui.ui.ThemeManager.getTextPrimary());
        titleText.setTextSize(15 * scale);
        titleText.setTypeface(null, Typeface.BOLD);
        titleText.setSingleLine(true);
        titleText.setIncludeFontPadding(false);
        textContainer.addView(titleText, new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ));

        subtitleContainer = new LinearLayout(context);
        subtitleContainer.setOrientation(LinearLayout.HORIZONTAL);
        subtitleContainer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams subtitleContainerParams = new LinearLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        );
        subtitleContainerParams.topMargin = (int) (dpToPx(2) * scale);
        textContainer.addView(subtitleContainer, subtitleContainerParams);

        updateContent();
    }

    @Override
    protected void updateContent() {

        if (!isInitialized) {
            switchView.setChecked(task.switchState, false);
            titleText.setText(task.text);
            updateSubtitle();

            lastRenderedText = task.text;
            lastRenderedSubtitle = task.subtitle;
            lastRenderedState = task.switchState;
            isInitialized = true;

            android.util.Log.d("SwitchItemView", String.format(
                "First render: id=%s, state=%s", task.identifier, task.switchState
            ));
            return;
        }

        if (lastRenderedState != task.switchState) {
            switchView.setChecked(task.switchState, true);
            lastRenderedState = task.switchState;

            android.util.Log.d("SwitchItemView", String.format(
                "State changed: id=%s, new=%s", task.identifier, task.switchState
            ));
        }

        if (!Objects.equals(lastRenderedText, task.text)) {
            titleText.setText(task.text);
            lastRenderedText = task.text;
        }

        if (!Objects.equals(lastRenderedSubtitle, task.subtitle)) {
            updateSubtitle();
            lastRenderedSubtitle = task.subtitle;
        }
    }

    private void updateSubtitle() {
        subtitleContainer.removeAllViews();

        if (task.subtitle != null && task.subtitle.contains("|")) {
            String[] parts = task.subtitle.split("\\|", 2);

            TextView part1 = new TextView(getContext());
            part1.setText(parts[0]);
            part1.setTextColor(colorPrimary);
            part1.setTextSize(12 * scale);
            part1.setTypeface(null, Typeface.BOLD);
            part1.setSingleLine(true);
            part1.setIncludeFontPadding(false);
            subtitleContainer.addView(part1);

            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams((int) (dpToPx(4) * scale), 0);
            subtitleContainer.addView(new android.view.View(getContext()), spacerParams);

            TextView part2 = new TextView(getContext());
            part2.setText(parts[1]);
            part2.setTextColor(com.phoenix.gui.ui.ThemeManager.getTextSecondary());
            part2.setTextSize(12 * scale);
            part2.setSingleLine(true);
            part2.setIncludeFontPadding(false);
            subtitleContainer.addView(part2);

        } else if (task.subtitle != null) {
            TextView subtitle = new TextView(getContext());
            subtitle.setText(task.subtitle);
            subtitle.setTextColor(com.phoenix.gui.ui.ThemeManager.getTextSecondary());
            subtitle.setTextSize(12 * scale);
            subtitle.setSingleLine(true);
            subtitle.setIncludeFontPadding(false);
            subtitleContainer.addView(subtitle);
        }
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        this.colorPrimary = newColor;
        updateSubtitle();
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