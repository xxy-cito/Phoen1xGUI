package com.phoenix.gui.ui.dynamic;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

public abstract class AnimatedTaskItemView extends TaskItemView {

    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateContent();
            updateHandler.postDelayed(this, 16);
        }
    };

    private boolean isUpdating = false;

    public AnimatedTaskItemView(@NonNull Context context, DynamicIslandManager.TaskItem task, float scale) {
        super(context, task, scale);
    }

    protected void startUpdating() {
        if (!isUpdating) {
            isUpdating = true;
            updateHandler.post(updateRunnable);
        }
    }

    protected void stopUpdating() {
        if (isUpdating) {
            isUpdating = false;
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startUpdating();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopUpdating();
    }

    @Override
    protected void onVisibilityChanged(android.view.View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            startUpdating();
        } else {
            stopUpdating();
        }
    }
}
