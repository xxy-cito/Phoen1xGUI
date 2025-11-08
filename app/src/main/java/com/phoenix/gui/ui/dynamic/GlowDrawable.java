package com.phoenix.gui.ui.dynamic;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

public class GlowDrawable extends Drawable {

    private final Paint paint;
    private float cornerRadius = 0;
    private float blurRadius = 0;
    private float spreadRadius = 0;

    public GlowDrawable() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0x66000000);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        invalidateSelf();
    }

    public void setBlurRadius(float radius) {
        this.blurRadius = radius;
        if (radius > 0) {
            paint.setMaskFilter(new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL));
        } else {
            paint.setMaskFilter(null);
        }
        invalidateSelf();
    }

    public void setSpreadRadius(float radius) {
        this.spreadRadius = radius;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        RectF rect = new RectF(
            bounds.left - spreadRadius,
            bounds.top - spreadRadius,
            bounds.right + spreadRadius,
            bounds.bottom + spreadRadius
        );
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
