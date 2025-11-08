package com.phoenix.gui.ui.dynamic;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.phoenix.gui.ui.ThemeManager;

public class MaterialSwitchView extends View implements ThemeManager.OnThemeColorChangeListener {

    private static final float TRACK_WIDTH_DP = 52f;
    private static final float TRACK_HEIGHT_DP = 32f;
    private static final float THUMB_SIZE_DP = 24f;
    private static final float THUMB_PADDING_DP = 4f;

    private boolean isChecked = false;
    private float thumbPosition = 0f;
    private float trackColorFraction = 0f;

    private ValueAnimator thumbAnimator;
    private ValueAnimator colorAnimator;

    private Paint trackPaint;
    private Paint thumbPaint;

    private float trackWidth;
    private float trackHeight;
    private float thumbSize;
    private float thumbPadding;
    private float trackRadius;
    private float thumbRadius;

    public MaterialSwitchView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MaterialSwitchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        trackWidth = TRACK_WIDTH_DP * density;
        trackHeight = TRACK_HEIGHT_DP * density;
        thumbSize = THUMB_SIZE_DP * density;
        thumbPadding = THUMB_PADDING_DP * density;
        trackRadius = trackHeight / 2f;
        thumbRadius = thumbSize / 2f;

        trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trackPaint.setStyle(Paint.Style.FILL);

        thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setColor(com.phoenix.gui.ui.ThemeManager.getTextPrimary());
        thumbPaint.setShadowLayer(
            4f * density,
            0f,
            2f * density,
            0x40000000
        );

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        setClickable(false);
        setFocusable(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) (trackWidth + getPaddingLeft() + getPaddingRight());
        int height = (int) (trackHeight + getPaddingTop() + getPaddingBottom());
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float trackLeft = centerX - trackWidth / 2f;
        float trackTop = centerY - trackHeight / 2f;
        float trackRight = centerX + trackWidth / 2f;
        float trackBottom = centerY + trackHeight / 2f;

        int colorTrackChecked = ThemeManager.getThemeColor();
        int colorTrackUnchecked = (com.phoenix.gui.ui.ThemeManager.getTextPrimary() & 0x00FFFFFF) | 0x4D000000;
        int trackColor = interpolateColor(colorTrackUnchecked, colorTrackChecked, trackColorFraction);
        trackPaint.setColor(trackColor);

        RectF trackRect = new RectF(trackLeft, trackTop, trackRight, trackBottom);
        canvas.drawRoundRect(trackRect, trackRadius, trackRadius, trackPaint);

        float thumbTravelDistance = trackWidth - thumbSize - 2 * thumbPadding;
        float thumbCenterX = trackLeft + thumbPadding + thumbRadius + thumbTravelDistance * thumbPosition;
        float thumbCenterY = centerY;

        canvas.drawCircle(thumbCenterX, thumbCenterY, thumbRadius, thumbPaint);
    }

    public void setChecked(boolean checked, boolean animate) {
        if (this.isChecked == checked) return;

        this.isChecked = checked;

        if (animate) {
            animateToPosition(checked ? 1f : 0f);
            animateTrackColor(checked ? 1f : 0f);
        } else {
            thumbPosition = checked ? 1f : 0f;
            trackColorFraction = checked ? 1f : 0f;
            invalidate();
        }
    }

    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }

    public boolean isChecked() {
        return isChecked;
    }

    private void animateToPosition(float targetPosition) {
        if (thumbAnimator != null) {
            thumbAnimator.cancel();
        }

        thumbAnimator = ValueAnimator.ofFloat(thumbPosition, targetPosition);
        thumbAnimator.setDuration(200);
        thumbAnimator.setInterpolator(new OvershootInterpolator(1.2f));
        thumbAnimator.addUpdateListener(animation -> {
            thumbPosition = (float) animation.getAnimatedValue();
            invalidate();
        });
        thumbAnimator.start();
    }

    private void animateTrackColor(float targetFraction) {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }

        colorAnimator = ValueAnimator.ofFloat(trackColorFraction, targetFraction);
        colorAnimator.setDuration(150);
        colorAnimator.addUpdateListener(animation -> {
            trackColorFraction = (float) animation.getAnimatedValue();
            invalidate();
        });
        colorAnimator.start();
    }

    private int interpolateColor(int colorA, int colorB, float fraction) {
        int alphaA = Color.alpha(colorA);
        int redA = Color.red(colorA);
        int greenA = Color.green(colorA);
        int blueA = Color.blue(colorA);

        int alphaB = Color.alpha(colorB);
        int redB = Color.red(colorB);
        int greenB = Color.green(colorB);
        int blueB = Color.blue(colorB);

        int alpha = (int) (alphaA + (alphaB - alphaA) * fraction);
        int red = (int) (redA + (redB - redA) * fraction);
        int green = (int) (greenA + (greenB - greenA) * fraction);
        int blue = (int) (blueA + (blueB - blueA) * fraction);

        return Color.argb(alpha, red, green, blue);
    }

    @Override
    public void onThemeColorChanged(int newColor) {
        
        if (isChecked) {
            invalidate();
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
        if (thumbAnimator != null) {
            thumbAnimator.cancel();
        }
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
        ThemeManager.removeListener(this);
    }
}