package com.phoenix.gui.ui.dynamic;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DynamicIslandManager {

    public static final long VALUE_PROGRESS_TIMEOUT_MS = 1000L;
    public static final long SWITCH_DISPLAY_DURATION_MS = 500L;
    public static final long TIME_PROGRESS_GRACE_PERIOD_MS = 1000L;

    public static class TaskItem {
        public enum Type { SWITCH, PROGRESS }

        public final Type type;
        public final String identifier;
        public String text;
        public String subtitle;
        public boolean switchState;
        public Drawable icon;
        public boolean isTimeBased;
        public long lastUpdateTime;
        public boolean isAwaitingData;
        public boolean removing;
        public long duration;
        public float displayProgress;
        public float targetProgress;
        public boolean isVisuallyHidden;

        private Runnable hideRunnable;
        private Runnable progressRunnable;

        public TaskItem(Type type, String identifier, String text, @Nullable String subtitle) {
            this.type = type;
            this.identifier = identifier;
            this.text = text;
            this.subtitle = subtitle;
            this.switchState = false;
            this.icon = null;
            this.isTimeBased = false;
            this.lastUpdateTime = System.currentTimeMillis();
            this.isAwaitingData = false;
            this.removing = false;
            this.duration = 0;
            this.displayProgress = 1.0f;
            this.targetProgress = 1.0f;
            this.isVisuallyHidden = false;
        }

        public void cancelJobs(Handler handler) {
            if (hideRunnable != null) {
                handler.removeCallbacks(hideRunnable);
                hideRunnable = null;
            }
            if (progressRunnable != null) {
                handler.removeCallbacks(progressRunnable);
                progressRunnable = null;
            }
        }
    }

    public interface StateChangeListener {
        void onTasksChanged();
        void onExpandedStateChanged(boolean isExpanded);
        void onConfigChanged(float scale, String persistentText);
    }

    private float scale = 0.7f;
    private String persistentText = "User";
    private final List<TaskItem> tasks = new CopyOnWriteArrayList<>();
    private final List<StateChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int currentFps = 0;

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTasks();
            handler.postDelayed(this, 250);
        }
    };

    public DynamicIslandManager(float initialScale, String initialText) {
        this.scale = Math.max(0.5f, Math.min(2.0f, initialScale));
        this.persistentText = initialText;
        handler.post(updateRunnable);
    }

    public float getScale() { return scale; }
    public String getPersistentText() { return persistentText; }
    public List<TaskItem> getTasks() { return new ArrayList<>(tasks); }
    public int getCurrentFps() { return currentFps; }
    public void setCurrentFps(int fps) { this.currentFps = fps; }

    public boolean isExpanded() {
        for (TaskItem task : tasks) {
            if (!task.removing && !task.isVisuallyHidden) return true;
        }
        return false;
    }

    public List<TaskItem> getVisibleTasks() {
        List<TaskItem> activeSwitchTasks = new ArrayList<>();
        List<TaskItem> otherTasks = new ArrayList<>();

        for (TaskItem task : tasks) {
            if (task.removing || task.isVisuallyHidden) continue;

            if (task.type == TaskItem.Type.SWITCH) {
                activeSwitchTasks.add(task);
            } else {
                otherTasks.add(task);
            }
        }

        if (!activeSwitchTasks.isEmpty()) {
            return activeSwitchTasks;
        } else {
            return otherTasks;
        }
    }

    public void updateConfig(float newScale, String newText) {
        this.scale = Math.max(0.5f, Math.min(2.0f, newScale));
        this.persistentText = newText;
        notifyConfigChanged();
    }

    public void addSwitch(String identifier, String text, boolean state) {
        android.util.Log.d("DynamicIsland", String.format("addSwitch: id=%s, text=%s, state=%s", identifier, text, state));

        String mainTitle = "功能开关";
        String subTitle = text + "|已被" + (state ? "开启" : "关闭");

        int taskIndex = findTaskIndex(identifier);
        TaskItem task;

        if (taskIndex != -1) {
            task = tasks.get(taskIndex);
            task.cancelJobs(handler);
            task.text = mainTitle;
            task.subtitle = subTitle;
            task.switchState = state;
            task.lastUpdateTime = System.currentTimeMillis();
            task.duration = SWITCH_DISPLAY_DURATION_MS;
            task.removing = false;
            task.isTimeBased = true;

            startTimeBasedAnimation(task);
            android.util.Log.d("DynamicIsland", "Updated existing switch task");
        } else {
            task = new TaskItem(TaskItem.Type.SWITCH, identifier, mainTitle, subTitle);
            task.switchState = state;
            task.duration = SWITCH_DISPLAY_DURATION_MS;
            task.isTimeBased = true;

            startTimeBasedAnimation(task);
            tasks.add(0, task);
            android.util.Log.d("DynamicIsland", "Created new switch task");
        }

        notifyTasksChanged();
    }

    public void addOrUpdateProgress(String identifier, String text,
                                   @Nullable String subtitle, @Nullable Drawable icon,
                                   @Nullable Float progress, @Nullable Long duration) {
        int taskIndex = findTaskIndex(identifier);
        if (taskIndex != -1) {
            updateProgressInternal(tasks.get(taskIndex), text, subtitle, progress, duration);
        } else {
            addProgressInternal(identifier, text, subtitle, icon, progress, duration);
        }
        notifyTasksChanged();
    }

    public void removeTask(String identifier) {
        int taskIndex = findTaskIndex(identifier);
        if (taskIndex != -1) {
            TaskItem task = tasks.get(taskIndex);
            if (task.removing) return;

            task.removing = true;
            task.cancelJobs(handler);
            notifyTasksChanged();

            handler.postDelayed(() -> {
                tasks.remove(task);
                notifyTasksChanged();
            }, 500);
        }
    }

    public void hide() {
        for (TaskItem task : tasks) {
            task.cancelJobs(handler);
        }
        tasks.clear();
        notifyTasksChanged();
    }

    public void destroy() {
        handler.removeCallbacksAndMessages(null);
        hide();
    }

    public void addListener(StateChangeListener listener) { listeners.add(listener); }
    public void removeListener(StateChangeListener listener) { listeners.remove(listener); }

    private void notifyTasksChanged() {
        boolean expanded = isExpanded();
        for (StateChangeListener listener : listeners) {
            listener.onTasksChanged();
            listener.onExpandedStateChanged(expanded);
        }
    }

    private void notifyConfigChanged() {
        for (StateChangeListener listener : listeners) {
            handler.post(() -> listener.onConfigChanged(scale, persistentText));
        }
    }

    private int findTaskIndex(String identifier) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).identifier.equals(identifier)) return i;
        }
        return -1;
    }

    private void updateTasks() {
        if (tasks.isEmpty()) return;
        long currentTime = System.currentTimeMillis();
        boolean hasChanged = false;

        Iterator<TaskItem> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            TaskItem task = iterator.next();
            if (task.removing) continue;

            boolean shouldBeMarkedForRemoval = false;
            if (task.isTimeBased) {
                if (task.displayProgress <= 0.01f && !task.isAwaitingData) {
                    task.isAwaitingData = true;
                    task.lastUpdateTime = currentTime;
                }
                if (task.isAwaitingData && (currentTime - task.lastUpdateTime > TIME_PROGRESS_GRACE_PERIOD_MS)) {
                    shouldBeMarkedForRemoval = true;
                }
            }
            else if (task.type == TaskItem.Type.PROGRESS) {
                if (currentTime - task.lastUpdateTime > VALUE_PROGRESS_TIMEOUT_MS) {
                    shouldBeMarkedForRemoval = true;
                }
            }

            if (shouldBeMarkedForRemoval) {
                task.removing = true;
                hasChanged = true;
                handler.postDelayed(() -> {
                    tasks.remove(task);
                    notifyTasksChanged();
                }, 500);
            }
        }
        if (hasChanged) {
            notifyTasksChanged();
        }
    }

    private void addProgressInternal(String identifier, String text, String subtitle,
                                    Drawable icon, Float progressValue, Long duration) {
        TaskItem newTask = new TaskItem(TaskItem.Type.PROGRESS, identifier, text, subtitle);
        newTask.icon = icon != null ? icon.mutate() : null;
        if (progressValue != null) {
            newTask.isTimeBased = false;
            newTask.displayProgress = 0f;
            animateProgressTo(newTask, progressValue, 400);
        } else {
            newTask.isTimeBased = true;
            newTask.duration = duration != null ? duration : 5000L;
            startTimeBasedAnimation(newTask);
        }
        tasks.add(0, newTask);
    }

    private void updateProgressInternal(TaskItem task, String text, String subtitle,
                                       Float progressValue, Long duration) {
        task.text = text;
        task.subtitle = subtitle;
        task.lastUpdateTime = System.currentTimeMillis();
        task.isAwaitingData = false;
        task.removing = false;
        task.cancelJobs(handler);

        if (progressValue != null) {
            task.isTimeBased = false;
            animateProgressTo(task, progressValue, 400);
        } else {
            task.isTimeBased = true;
            task.duration = duration != null ? duration : 5000L;
            startTimeBasedAnimation(task);
        }
    }

    private void animateProgressTo(TaskItem task, float targetProgress, long durationMs) {
        task.cancelJobs(handler);
        task.targetProgress = Math.max(0f, Math.min(1f, targetProgress));
        final float startProgress = task.displayProgress;
        final float delta = task.targetProgress - startProgress;

        if (Math.abs(delta) < 0.001f) return;

        final long startTime = System.currentTimeMillis();
        final boolean isSwitch = task.type == TaskItem.Type.SWITCH;

        task.progressRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float fraction = Math.min(1f, elapsed / (float) durationMs);
                float easedFraction = (float) (1 - Math.pow(1 - fraction, 3));
                task.displayProgress = startProgress + delta * easedFraction;

                if (!isSwitch) {
                    notifyTasksChanged();
                }

                if (fraction < 1f) {
                    handler.postDelayed(this, 16);
                } else {
                    task.displayProgress = task.targetProgress;
                    task.progressRunnable = null;

                    notifyTasksChanged();
                }
            }
        };
        handler.post(task.progressRunnable);
    }

    private void startTimeBasedAnimation(TaskItem task) {
        task.cancelJobs(handler);

        if (task.displayProgress < 1.0f) {
            animateProgressTo(task, 1.0f, 500);
            handler.postDelayed(() -> animateProgressTo(task, 0f, task.duration), 500);
        } else {
            animateProgressTo(task, 0f, task.duration);
        }
    }
}
