package com.phoenix.gui.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class ArraylistItemView extends FrameLayout implements ThemeManager.OnThemeColorChangeListener {

    public enum PositionType {
        SINGLE, FIRST, MIDDLE, LAST
    }

    private TextView textView;
    private Paint backgroundPaint;
    private Path clipPath;
    private float cornerRadius;
    private int customTextColor = -1;
    private float measuredTextWidth = 0;
    private PositionType positionType = PositionType.MIDDLE;

    private Paint shadowPaint;
    private Path shadowPath;

    public ArraylistItemView(Context context) {
        super(context);
        init();
    }
    public ArraylistItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ArraylistItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        setClipChildren(false);
        setClipToPadding(false);
        
        int shadowSpace = dpToPx(8);
        setPadding(shadowSpace, shadowSpace, shadowSpace, shadowSpace);

        cornerRadius = dpToPx(3);

        textView = new TextView(getContext());
        textView.setTextColor(ThemeManager.getThemeColor());
        textView.setTextSize(10);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        textView.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), dpToPx(2));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.END;
        addView(textView, params);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(0xB2000000); 
        backgroundPaint.setStyle(Paint.Style.FILL);

        // REMOVED: sheenDrawable initialization
        
        clipPath = new Path();
        shadowPath = new Path();

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(0xFF000000);
        shadowPaint.setMaskFilter(new BlurMaskFilter(dpToPx(6), BlurMaskFilter.Blur.NORMAL));

        ThemeManager.addListener(this);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) {
            super.onDraw(canvas);
            return;
        }

        RectF backgroundBounds = new RectF(
                getPaddingLeft(), getPaddingTop(),
                width - getPaddingRight(), height - getPaddingBottom()
        );

        drawSelectiveShadow(canvas, backgroundBounds);
        drawDynamicBackground(canvas, backgroundBounds);

        super.onDraw(canvas);
    }
    
    @Override
    public void onThemeColorChanged(int newColor) {
        if (customTextColor == -1) {
            textView.setTextColor(newColor);
        }
        invalidate();
    }

    public void cleanup() {
        ThemeManager.removeListener(this);
    }
    

    // 下面由 gemini 2.5 pro 编写。够屎山。你要是想改你就去找 claude 吧，千万别用 gemini。
    public void setPositionType(PositionType type) { if (this.positionType != type) { this.positionType = type; invalidate(); } }
    private void drawSelectiveShadow(Canvas canvas, RectF bounds) { shadowPath.reset(); float spread = dpToPx(0.5f); switch (positionType) { case SINGLE: shadowPath.addRoundRect(bounds, cornerRadius, cornerRadius, Path.Direction.CW); break; case FIRST: shadowPath.moveTo(bounds.left + cornerRadius, bounds.top); shadowPath.lineTo(bounds.right - cornerRadius, bounds.top); shadowPath.quadTo(bounds.right, bounds.top, bounds.right, bounds.top + cornerRadius); shadowPath.lineTo(bounds.right, bounds.bottom + spread); shadowPath.lineTo(bounds.left, bounds.bottom + spread); shadowPath.lineTo(bounds.left, bounds.top + cornerRadius); shadowPath.quadTo(bounds.left, bounds.top, bounds.left + cornerRadius, bounds.top); break; case MIDDLE: shadowPath.moveTo(bounds.right, bounds.top - spread); shadowPath.lineTo(bounds.right, bounds.bottom + spread); shadowPath.lineTo(bounds.left, bounds.bottom + spread); shadowPath.lineTo(bounds.left, bounds.top - spread); break; case LAST: shadowPath.moveTo(bounds.left, bounds.top - spread); shadowPath.lineTo(bounds.right, bounds.top - spread); shadowPath.lineTo(bounds.right, bounds.bottom - cornerRadius); shadowPath.quadTo(bounds.right, bounds.bottom, bounds.right - cornerRadius, bounds.bottom); shadowPath.lineTo(bounds.left + cornerRadius, bounds.bottom); shadowPath.quadTo(bounds.left, bounds.bottom, bounds.left, bounds.bottom - cornerRadius); shadowPath.lineTo(bounds.left, bounds.top - spread); break; } shadowPath.close(); canvas.drawPath(shadowPath, shadowPaint); }
    private void drawDynamicBackground(Canvas canvas, RectF bounds) { clipPath.reset(); clipPath.moveTo(bounds.left + cornerRadius, bounds.top); if (positionType == PositionType.FIRST || positionType == PositionType.SINGLE) { clipPath.lineTo(bounds.right - cornerRadius, bounds.top); clipPath.quadTo(bounds.right, bounds.top, bounds.right, bounds.top + cornerRadius); } else { clipPath.lineTo(bounds.right, bounds.top); } if (positionType == PositionType.LAST || positionType == PositionType.SINGLE) { clipPath.lineTo(bounds.right, bounds.bottom - cornerRadius); clipPath.quadTo(bounds.right, bounds.bottom, bounds.right - cornerRadius, bounds.bottom); } else { clipPath.lineTo(bounds.right, bounds.bottom); } clipPath.lineTo(bounds.left + cornerRadius, bounds.bottom); clipPath.quadTo(bounds.left, bounds.bottom, bounds.left, bounds.bottom - cornerRadius); clipPath.lineTo(bounds.left, bounds.top + cornerRadius); clipPath.quadTo(bounds.left, bounds.top, bounds.left + cornerRadius, bounds.top); clipPath.close(); canvas.drawPath(clipPath, backgroundPaint); }
    public void setText(String text) { textView.setText(text); measureTextWidth(); }
    private void measureTextWidth() { TextPaint paint = textView.getPaint(); String text = textView.getText().toString(); measuredTextWidth = paint.measureText(text) + textView.getPaddingLeft() + textView.getPaddingRight(); }
    public float getMeasuredTextWidth() { return measuredTextWidth; }
    public void setTextColor(int color) { customTextColor = color; textView.setTextColor(color); }
    private int dpToPx(float dp) { return (int) (dp * getContext().getResources().getDisplayMetrics().density); }
}