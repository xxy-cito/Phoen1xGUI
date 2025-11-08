package com.phoenix.gui.ui.dynamic;

import android.content.Context;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

public abstract class TaskItemView extends FrameLayout {

    public DynamicIslandManager.TaskItem task;
    protected float scale;

    public TaskItemView(@NonNull Context context, DynamicIslandManager.TaskItem task, float scale) {
        super(context);
        this.task = task;
        this.scale = scale;
        init(context);
    }

    protected abstract void init(Context context);

    protected abstract void updateContent();

    protected float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}
