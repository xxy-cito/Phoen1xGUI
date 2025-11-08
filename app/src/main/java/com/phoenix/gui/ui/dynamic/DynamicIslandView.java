package com.phoenix.gui.ui.dynamic;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.phoenix.gui.ui.ThemeManager;

import java.util.ArrayList;
import java.util.List;

public class DynamicIslandView extends FrameLayout implements DynamicIslandManager.StateChangeListener, ThemeManager.OnThemeColorChangeListener {

    private static final float COLLAPSED_HEIGHT_DP = 36f;
    private static final float EXPANDED_CORNER_RADIUS_DP = 28f;
    private static final float ITEM_HEIGHT_DP = 52f;
    private static final float VIEW_PADDING_DP = 12f;
    private static final float HORIZONTAL_ITEM_PADDING_DP = 24f;
    private static final float ICON_SIZE_DP = 40f;
    private static final float ICON_SPACING_DP = 12f;
    private static final float GLOW_BLUR_RADIUS_DP = 12f;
    private static final float GLOW_SPREAD_RADIUS_DP = 4f;
    private static final float SHADOW_SAFETY_MARGIN_DP = 8f;
    private static final int SIZE_ANIMATION_DURATION_MS = 350;
    private static final int CONTENT_FADE_OUT_MS = 100;
    private static final int CONTENT_FADE_IN_MS = 200;

    private DynamicIslandManager manager;

    private Paint backgroundPaint;
    private Paint glowPaint;
    private RectF backgroundRect = new RectF();
    private RectF glowRect = new RectF();

    private float sheenPosition = -1.0f;
    private ValueAnimator sheenAnimator;

    private CollapsedContentView collapsedContent;
    private FrameLayout expandedContainer;

    private float currentHeight;
    private float currentWidth;
    private float currentCornerRadius;
    private ValueAnimator sizeAnimator;
    private boolean wasExpanded = false;

    private TextPaint textPaint;

    private int frameCount = 0;
    private long lastFpsTime = System.nanoTime();

    public DynamicIslandView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public DynamicIslandView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setClipChildren(false);
        setClipToPadding(false);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        initPaints();
        initSheenAnimation();
        initContentViews(context);
        startFpsMonitor();
    }

    private void initPaints() {
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(ThemeManager.getGlowColor());
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setMaskFilter(new BlurMaskFilter(dpToPx(GLOW_BLUR_RADIUS_DP), BlurMaskFilter.Blur.NORMAL));

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(ThemeManager.getGlassBackground());
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    private void initSheenAnimation() {
        sheenAnimator = ValueAnimator.ofFloat(-1.0f, 2.0f);
        sheenAnimator.setDuration(2500);
        sheenAnimator.setStartDelay(500);
        sheenAnimator.setInterpolator(new android.view.animation.LinearInterpolator());
        sheenAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sheenAnimator.setRepeatMode(ValueAnimator.RESTART);
        sheenAnimator.addUpdateListener(animation -> {
            sheenPosition = (float) animation.getAnimatedValue();
            invalidate();
        });
        sheenAnimator.start();
    }

    private void initContentViews(Context context) {
        collapsedContent = new CollapsedContentView(context);
        collapsedContent.setVisibility(VISIBLE);
        LayoutParams collapsedParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        collapsedParams.gravity = Gravity.CENTER;
        addView(collapsedContent, collapsedParams);

        expandedContainer = new FrameLayout(context);
        expandedContainer.setVisibility(GONE);
        expandedContainer.setAlpha(0f);
        expandedContainer.setBackgroundColor(Color.TRANSPARENT);
        LayoutParams expandedParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        expandedParams.gravity = Gravity.CENTER;
        addView(expandedContainer, expandedParams);
    }

    private void startFpsMonitor() {
        Choreographer.getInstance().postFrameCallback(new Choreographer.FrameCallback() {
            @Override
            public void doFrame(long frameTimeNanos) {
                frameCount++;
                long elapsedNanos = frameTimeNanos - lastFpsTime;
                if (elapsedNanos >= 1_000_000_000L) {
                    if (manager != null) {
                        manager.setCurrentFps(frameCount);
                        if (collapsedContent != null) {
                            collapsedContent.updateFps(frameCount);
                        }
                    }
                    frameCount = 0;
                    lastFpsTime = frameTimeNanos;
                }
                Choreographer.getInstance().postFrameCallback(this);
            }
        });
    }

    public void setManager(DynamicIslandManager manager) {
        if (this.manager != null) {
            this.manager.removeListener(this);
        }
        this.manager = manager;
        this.manager.addListener(this);

        if (collapsedContent != null) {
            collapsedContent.setManager(manager);
        }

        updateContent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float scale = manager != null ? manager.getScale() : 0.7f;
        int glowPadding = (int) (dpToPx(GLOW_BLUR_RADIUS_DP + GLOW_SPREAD_RADIUS_DP) * scale);
        int safetyMargin = (int) (dpToPx(SHADOW_SAFETY_MARGIN_DP) * scale);
        int totalPadding = glowPadding + safetyMargin;

        int contentWidth = (int) currentWidth;
        int contentHeight = (int) currentHeight;

        if (currentWidth == 0 || currentHeight == 0) {
            boolean isExpanded = manager != null && manager.isExpanded();
            if (isExpanded) {
                contentHeight = calculateExpandedHeight(scale);
                contentWidth = calculateExpandedWidth(scale);
            } else {
                contentHeight = (int) (dpToPx(COLLAPSED_HEIGHT_DP) * scale);
                contentWidth = calculateCollapsedWidth(scale);
            }
            currentHeight = contentHeight;
            currentWidth = contentWidth;
            currentCornerRadius = isExpanded ? dpToPx(EXPANDED_CORNER_RADIUS_DP) * scale : contentHeight / 2f;
        }

        int contentWidthSpec = MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.AT_MOST);
        int contentHeightSpec = MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY);

        measureChild(collapsedContent, contentWidthSpec, contentHeightSpec);
        measureChild(expandedContainer, MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), contentHeightSpec);

        setMeasuredDimension(
                contentWidth + totalPadding * 2,
                contentHeight + totalPadding * 2
        );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float scale = manager != null ? manager.getScale() : 0.7f;
        int glowPadding = (int) (dpToPx(GLOW_BLUR_RADIUS_DP + GLOW_SPREAD_RADIUS_DP) * scale);
        int safetyMargin = (int) (dpToPx(SHADOW_SAFETY_MARGIN_DP) * scale);
        int totalPadding = glowPadding + safetyMargin;

        int contentLeft = totalPadding;
        int contentTop = totalPadding;
        int contentRight = getWidth() - totalPadding;
        int contentBottom = getHeight() - totalPadding;

        int contentWidth = contentRight - contentLeft;
        int collapsedWidth = collapsedContent.getMeasuredWidth();
        int collapsedLeft = contentLeft + (contentWidth - collapsedWidth) / 2;
        collapsedContent.layout(collapsedLeft, contentTop, collapsedLeft + collapsedWidth, contentBottom);

        expandedContainer.layout(contentLeft, contentTop, contentRight, contentBottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float scale = manager != null ? manager.getScale() : 0.7f;
        int glowPadding = (int) (dpToPx(GLOW_BLUR_RADIUS_DP + GLOW_SPREAD_RADIUS_DP) * scale);
        int safetyMargin = (int) (dpToPx(SHADOW_SAFETY_MARGIN_DP) * scale);
        int totalPadding = glowPadding + safetyMargin;

        float contentLeft = totalPadding;
        float contentTop = totalPadding;
        float contentRight = getWidth() - totalPadding;
        float contentBottom = getHeight() - totalPadding;

        backgroundRect.set(contentLeft, contentTop, contentRight, contentBottom);

        float spreadPx = dpToPx(GLOW_SPREAD_RADIUS_DP) * scale;
        glowRect.set(
                contentLeft - spreadPx,
                contentTop - spreadPx,
                contentRight + spreadPx,
                contentBottom + spreadPx
        );

        canvas.drawRoundRect(glowRect, currentCornerRadius, currentCornerRadius, glowPaint);
        canvas.drawRoundRect(backgroundRect, currentCornerRadius, currentCornerRadius, backgroundPaint);
        drawSheen(canvas, backgroundRect, currentCornerRadius);
    }

    private void drawSheen(Canvas canvas, RectF rect, float cornerRadius) {
        float gradientWidth = rect.width() * 0.5f;
        float gradientStart = rect.width() * sheenPosition;

        LinearGradient gradient = new LinearGradient(
                gradientStart - gradientWidth, rect.top,
                gradientStart, rect.bottom,
                new int[]{0x00FFFFFF, 0x14FFFFFF, 0x00FFFFFF},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP
        );

        Paint sheenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sheenPaint.setShader(gradient);

        canvas.save();
        canvas.clipRect(rect);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, sheenPaint);
        canvas.restore();
    }

    @Override
    public void onTasksChanged() {
        updateContent();
        post(() -> animateSizeToFitContent());
    }

    @Override
    public void onExpandedStateChanged(boolean isExpanded) {
        if (wasExpanded != isExpanded) {
            wasExpanded = isExpanded;
            updateContent();
            animateToState(isExpanded);
        }
    }

    @Override
    public void onConfigChanged(float scale, String persistentText) {
        if (collapsedContent != null) {
            collapsedContent.updateConfig(scale, persistentText);
        }
        requestLayout();
    }

    private void updateContent() {
        if (manager == null) return;
        boolean isExpanded = manager.isExpanded();

        if (isExpanded) {
            LinearLayout taskContainer = null;
            if (expandedContainer.getChildCount() > 0 && expandedContainer.getChildAt(0) instanceof LinearLayout) {
                taskContainer = (LinearLayout) expandedContainer.getChildAt(0);
            }

            if (taskContainer == null) {
                rebuildExpandedContent();
            } else {
                if (!areTasksMatching(taskContainer)) {
                    rebuildExpandedContent();
                } else {
                    updateExistingContent(taskContainer);
                }
            }
        }

        if (collapsedContent != null) {
            collapsedContent.updateContent();
        }
    }

    private boolean areTasksMatching(LinearLayout taskContainer) {
        List<DynamicIslandManager.TaskItem> tasks = manager.getVisibleTasks();

        if (taskContainer.getChildCount() != tasks.size()) {
            android.util.Log.d("DynamicIsland", String.format(
                "Tasks not matching: count mismatch (view=%d, task=%d)",
                taskContainer.getChildCount(), tasks.size()
            ));
            return false;
        }

        for (int i = 0; i < tasks.size(); i++) {
            View child = taskContainer.getChildAt(i);
            if (!(child instanceof TaskItemView)) {
                android.util.Log.d("DynamicIsland", "Tasks not matching: child is not TaskItemView");
                return false;
            }

            TaskItemView view = (TaskItemView) child;
            DynamicIslandManager.TaskItem task = tasks.get(i);

            if (!view.task.identifier.equals(task.identifier)) {
                android.util.Log.d("DynamicIsland", String.format(
                    "Tasks not matching: identifier mismatch at %d (view=%s, task=%s)",
                    i, view.task.identifier, task.identifier
                ));
                return false;
            }

            if (view.task.type != task.type) {
                android.util.Log.d("DynamicIsland", String.format(
                    "Tasks not matching: type mismatch at %d (view=%s, task=%s)",
                    i, view.task.type, task.type
                ));
                return false;
            }
        }

        android.util.Log.d("DynamicIsland", "Tasks matching, using updateExistingContent");
        return true;
    }

    private void updateExistingContent(LinearLayout taskContainer) {
        List<DynamicIslandManager.TaskItem> tasks = manager.getVisibleTasks();
        android.util.Log.d("DynamicIsland", String.format("updateExistingContent: %d tasks", tasks.size()));

        for (int i = 0; i < tasks.size(); i++) {
            if (i >= taskContainer.getChildCount()) break;

            View child = taskContainer.getChildAt(i);
            if (!(child instanceof TaskItemView)) continue;

            TaskItemView view = (TaskItemView) child;
            DynamicIslandManager.TaskItem newTask = tasks.get(i);

            view.task = newTask;

            view.updateContent();
        }
    }

    private void rebuildExpandedContent() {
        android.util.Log.d("DynamicIsland", "rebuildExpandedContent called");
        expandedContainer.removeAllViews();
        LinearLayout taskContainer = new LinearLayout(getContext());
        taskContainer.setOrientation(LinearLayout.VERTICAL);
        taskContainer.setPadding(
                0, (int) (dpToPx(VIEW_PADDING_DP) * manager.getScale()),
                0, (int) (dpToPx(VIEW_PADDING_DP) * manager.getScale())
        );
        for (DynamicIslandManager.TaskItem task : manager.getVisibleTasks()) {
            TaskItemView itemView = createTaskItemView(task);
            taskContainer.addView(itemView);
        }
        expandedContainer.addView(taskContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    private TaskItemView createTaskItemView(DynamicIslandManager.TaskItem task) {
        switch (task.type) {
            case SWITCH:
                return new SwitchItemView(getContext(), task, manager.getScale());
            case PROGRESS:
                return new ProgressItemView(getContext(), task, manager.getScale());
            default:
                throw new IllegalArgumentException("Unknown task type: " + task.type);
        }
    }

    private void animateSizeToFitContent() {
        if (manager == null) return;
        boolean isExpanded = manager.isExpanded();
        float scale = manager.getScale();

        float targetHeight = isExpanded ? calculateExpandedHeight(scale) : dpToPx(COLLAPSED_HEIGHT_DP) * scale;
        float targetWidth = isExpanded ? calculateExpandedWidth(scale) : calculateCollapsedWidth(scale);
        float targetCorner = isExpanded ? dpToPx(EXPANDED_CORNER_RADIUS_DP) * scale : targetHeight / 2f;

        if (Math.abs(targetHeight - currentHeight) > 1f || Math.abs(targetWidth - currentWidth) > 1f) {
            animateSizeWithSpring(targetHeight, targetWidth, targetCorner);
        }
    }

    private void animateToState(boolean expanded) {
        if (sizeAnimator != null) sizeAnimator.cancel();
        float scale = manager != null ? manager.getScale() : 0.7f;
        float targetHeight = expanded ? calculateExpandedHeight(scale) : dpToPx(COLLAPSED_HEIGHT_DP) * scale;
        float targetWidth = expanded ? calculateExpandedWidth(scale) : calculateCollapsedWidth(scale);
        float targetCorner = expanded ? dpToPx(EXPANDED_CORNER_RADIUS_DP) * scale : targetHeight / 2f;

        if (expanded) {
            collapsedContent.animate().alpha(0f).scaleX(0.9f).scaleY(0.9f).setDuration(CONTENT_FADE_OUT_MS)
                    .setInterpolator(new FastOutSlowInInterpolator()).withEndAction(() -> collapsedContent.setVisibility(GONE)).start();
            expandedContainer.setVisibility(VISIBLE);
            expandedContainer.setAlpha(0f);
            expandedContainer.setScaleX(0.9f);
            expandedContainer.setScaleY(0.9f);
            expandedContainer.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(CONTENT_FADE_IN_MS)
                    .setInterpolator(new FastOutSlowInInterpolator()).start();
        } else {
            expandedContainer.animate().alpha(0f).scaleX(0.9f).scaleY(0.9f).setDuration(CONTENT_FADE_OUT_MS)
                    .setInterpolator(new FastOutSlowInInterpolator()).withEndAction(() -> expandedContainer.setVisibility(GONE)).start();
            collapsedContent.setVisibility(VISIBLE);
            collapsedContent.setAlpha(0f);
            collapsedContent.setScaleX(0.9f);
            collapsedContent.setScaleY(0.9f);
            collapsedContent.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(CONTENT_FADE_IN_MS)
                    .setInterpolator(new FastOutSlowInInterpolator()).start();
        }
        animateSizeWithSpring(targetHeight, targetWidth, targetCorner);
    }

    private void animateSizeWithSpring(float targetHeight, float targetWidth, float targetCorner) {
        final float startHeight = currentHeight;
        final float startWidth = currentWidth;
        final float startCorner = currentCornerRadius;

        if (sizeAnimator != null) sizeAnimator.cancel();

        sizeAnimator = ValueAnimator.ofFloat(0f, 1f);
        sizeAnimator.setDuration(SIZE_ANIMATION_DURATION_MS);
        sizeAnimator.setInterpolator(new OvershootInterpolator(0.8f));

        sizeAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            currentHeight = startHeight + (targetHeight - startHeight) * fraction;
            currentWidth = startWidth + (targetWidth - startWidth) * fraction;
            currentCornerRadius = startCorner + (targetCorner - startCorner) * fraction;
            requestLayout();
        });

        sizeAnimator.start();
    }

    private int calculateExpandedHeight(float scale) {
        if (manager == null) return (int) (dpToPx(COLLAPSED_HEIGHT_DP) * scale);
        int totalHeight = 0;
        for (DynamicIslandManager.TaskItem task : manager.getVisibleTasks()) {
            switch (task.type) {
                case SWITCH:
                    totalHeight += dpToPx(ITEM_HEIGHT_DP) * scale;
                    break;
                case PROGRESS:
                    totalHeight += dpToPx(ITEM_HEIGHT_DP + 8 + 12) * scale;
                    break;
            }
        }
        return totalHeight + (int) (dpToPx(VIEW_PADDING_DP * 2) * scale);
    }

    private int calculateExpandedWidth(float scale) {
        if (manager == null) return (int) (dpToPx(280) * scale);

        int minWidth = (int) (dpToPx(280) * scale);
        int maxWidth = minWidth;

        for (DynamicIslandManager.TaskItem task : manager.getVisibleTasks()) {
            int requiredWidth = calculateTaskWidth(task, scale);
            maxWidth = Math.max(maxWidth, requiredWidth);
        }

        int absoluteMaxWidth = (int) (dpToPx(450) * scale);
        return Math.min(maxWidth, absoluteMaxWidth);
    }

    private int calculateCollapsedWidth(float scale) {
        if (collapsedContent == null) return (int) (dpToPx(280) * scale);

        int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int heightSpec = MeasureSpec.makeMeasureSpec((int)(dpToPx(COLLAPSED_HEIGHT_DP) * scale), MeasureSpec.EXACTLY);
        collapsedContent.measure(widthSpec, heightSpec);

        int contentWidth = collapsedContent.getMeasuredWidth();
        int padding = (int) (dpToPx(40) * scale);
        return contentWidth + padding;
    }

    private int calculateTaskWidth(DynamicIslandManager.TaskItem task, float scale) {
        float sidePadding = dpToPx(HORIZONTAL_ITEM_PADDING_DP * 2) * scale;
        float iconWidth = 0;
        float spacing = 0;

        if (task.icon != null || task.type == DynamicIslandManager.TaskItem.Type.SWITCH) {
            if (task.type == DynamicIslandManager.TaskItem.Type.SWITCH) iconWidth = dpToPx(52) * scale;
            else iconWidth = dpToPx(ICON_SIZE_DP) * scale;
            spacing = dpToPx(ICON_SPACING_DP) * scale;
        }

        textPaint.setTextSize(spToPx(15 * scale));
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        float titleWidth = textPaint.measureText(task.text);

        float subtitleWidth = 0;
        if (task.subtitle != null) {
            textPaint.setTextSize(spToPx(12 * scale));
            textPaint.setTypeface(Typeface.DEFAULT);
            subtitleWidth = textPaint.measureText(task.subtitle.replace("|", " "));
        }

        float textWidth = Math.max(titleWidth, subtitleWidth);
        float extraPadding = dpToPx(32) * scale;

        return (int) (sidePadding + iconWidth + spacing + textWidth + extraPadding);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private int spToPx(float sp) {
        return (int) (sp * getResources().getDisplayMetrics().scaledDensity);
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ThemeManager.addListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (sheenAnimator != null) sheenAnimator.cancel();
        if (sizeAnimator != null) sizeAnimator.cancel();
        // 停止监听应该是
        ThemeManager.removeListener(this);
    }
}