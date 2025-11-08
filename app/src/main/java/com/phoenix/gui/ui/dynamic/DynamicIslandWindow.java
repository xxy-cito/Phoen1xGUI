package com.phoenix.gui.ui.dynamic;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;

public class DynamicIslandWindow {

    private final Context context;
    private final FrameLayout container;
    private DynamicIslandView dynamicIslandView;
    private DynamicIslandManager manager;
    private boolean isShowing = false;

    public DynamicIslandWindow(@NonNull Context context, FrameLayout container) {
        this.context = context.getApplicationContext();
        this.container = container;
    }

    public void show() {
        if (isShowing) return;

        android.content.SharedPreferences prefs = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);
        float scale = prefs.getFloat("dynamicIslandScale", 0.7f);
        String username = prefs.getString("dynamicIslandUsername", "User");
        manager = new DynamicIslandManager(scale, username);

        dynamicIslandView = new DynamicIslandView(context);
        dynamicIslandView.setManager(manager);
        dynamicIslandView.setAlpha(0.80f);

        if (container != null) {
            container.removeAllViews();
            container.addView(dynamicIslandView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ));
            container.setVisibility(View.VISIBLE);
            isShowing = true;
        }
    }

    public void hide() {
        if (!isShowing) return;

        try {
            if (container != null) {
                container.removeAllViews();
                container.setVisibility(View.GONE);
            }

            if (dynamicIslandView != null) {
                dynamicIslandView = null;
            }

            if (manager != null) {
                manager.destroy();
                manager = null;
            }

            isShowing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateConfig(float scale, String username) {
        if (manager != null) {
            manager.updateConfig(scale, username);
        }
    }

    public void addSwitch(String identifier, String text, boolean state) {
        if (manager != null) {
            manager.addSwitch(identifier, text, state);
        }
    }

    public void addOrUpdateProgress(String identifier, String text, String subtitle,
                                   android.graphics.drawable.Drawable icon,
                                   Float progress, Long duration) {
        if (manager != null) {
            manager.addOrUpdateProgress(identifier, text, subtitle, icon, progress, duration);
        }
    }

    public void removeTask(String identifier) {
        if (manager != null) {
            manager.removeTask(identifier);
        }
    }

    public void hideAllTasks() {
        if (manager != null) {
            manager.hide();
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    public DynamicIslandManager getManager() {
        return manager;
    }
}
