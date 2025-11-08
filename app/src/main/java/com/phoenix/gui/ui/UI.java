package com.phoenix.gui.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.phoenix.gui.R;
import com.phoenix.gui.config.ConfigManager;
import com.phoenix.gui.module.ModuleManager;
import com.phoenix.gui.module.ModuleToggleListener;
import com.phoenix.gui.module.ShortcutToggleListener;
import com.phoenix.gui.ui.dynamic.DynamicIslandWindow;

// 封装

public class UI {
    private static DynamicColorExtractor dynamicColorExtractor;
    private static boolean initialized = false;
    private static boolean isShowing = false;

    // UI
    private static FloatBallView floatBall;
    private static DynamicIslandWindow dynamicIsland;
    private static ArraylistView arraylistView;

    // 容器引用
    private static FrameLayout guiContainer;
    private static FrameLayout floatBallContainer;
    private static FrameLayout menusContainer;
    private static FrameLayout dynamicIslandContainer;
    private static FrameLayout arraylistContainer;

    // activity 引用
    private static Activity currentActivity;


    public static void init(Context ctx) {
        if (initialized) {
            return;
        }

        
        ConfigManager.init(ctx);

        
        dynamicColorExtractor = new DynamicColorExtractor(ctx);
        dynamicColorExtractor.init();

        initialized = true;
    }

    public static void show(Activity activity) {
        if (isShowing) {
            return;
        }

        try {
            currentActivity = activity;

            // 绑定容器
            bindContainers(activity);

            // 显示主容器
            if (guiContainer != null) {
                guiContainer.setVisibility(View.VISIBLE);
            }

            // 初始化ppp
            initFloatBall();
            initDynamicIsland();
            initArraylist();

            // 注册
            if (activity instanceof ModuleToggleListener) {
                ModuleManager.addToggleListener((ModuleToggleListener) activity);
            }
            if (activity instanceof ShortcutToggleListener) {
                ModuleManager.addShortcutListener((ShortcutToggleListener) activity);
            }

            isShowing = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void hide() {
        if (!isShowing) {
            return; // 回家吃饭
        }

        try {
            // 移除
            if (currentActivity instanceof ModuleToggleListener) {
                ModuleManager.removeToggleListener((ModuleToggleListener) currentActivity);
            }
            if (currentActivity instanceof ShortcutToggleListener) {
                ModuleManager.removeShortcutListener((ShortcutToggleListener) currentActivity);
            }

            // 清理
            if (arraylistView != null) {
                arraylistView.clearModules();
                arraylistView.hide();
                arraylistView = null;
            }

            // 清理
            if (dynamicIsland != null) {
                dynamicIsland.hide();
                dynamicIsland = null;
            }

            // 还是清理
            if (floatBall != null) {
                floatBall.hide();
                floatBall.destroy();
                floatBall = null;
            }

            // 清空垃圾桶
            if (floatBallContainer != null) floatBallContainer.removeAllViews();
            if (menusContainer != null) menusContainer.removeAllViews();
            if (arraylistContainer != null) arraylistContainer.removeAllViews();

            // 隐藏
            if (guiContainer != null) {
                guiContainer.setVisibility(View.GONE);
            }

            isShowing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void bindContainers(Activity activity) {
        guiContainer = activity.findViewById(R.id.gui_container);
        floatBallContainer = activity.findViewById(R.id.float_ball_container);
        menusContainer = activity.findViewById(R.id.menus_container);
        dynamicIslandContainer = activity.findViewById(R.id.dynamic_island_container);
        arraylistContainer = activity.findViewById(R.id.arraylist_container);
    }


    private static void initFloatBall() {
        try {
            if (floatBall != null) {
                floatBall.destroy();
                floatBall = null;
            }

            if (floatBallContainer != null) {
                floatBallContainer.removeAllViews();
                floatBall = new FloatBallView(currentActivity, menusContainer);
                floatBallContainer.addView(floatBall);
                floatBall.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void initDynamicIsland() {
        boolean enabled = ConfigManager.getDynamicIslandEnabled();
        if (!enabled) return;

        try {
            if (dynamicIsland != null) {
                dynamicIsland.hide();
                dynamicIsland = null;
            }

            if (dynamicIslandContainer != null) {
                dynamicIslandContainer.removeAllViews();
                dynamicIsland = new DynamicIslandWindow(currentActivity, dynamicIslandContainer);
                dynamicIsland.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void initArraylist() {
        try {
            if (arraylistView != null) {
                arraylistView.clearModules();
                arraylistView = null;
            }

            if (arraylistContainer != null) {
                arraylistContainer.removeAllViews();
                arraylistView = new ArraylistView(currentActivity);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                );
                arraylistContainer.addView(arraylistView, params);
                addDefaultArraylistModules();
                arraylistView.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void addDefaultArraylistModules() {
        if (arraylistView == null) return;

        arraylistView.clearModules();
        // arraylistView.addModule(new ArraylistModule("like this"));
        
    }
    

    public static void cleanup() {
        hide();

        if (dynamicColorExtractor != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            dynamicColorExtractor.stopListening();
            dynamicColorExtractor = null;
        }

        currentActivity = null;
        initialized = false;
    }

    public static DynamicColorExtractor getDynamicColorExtractor() {
        return dynamicColorExtractor;
    }


    public static DynamicIslandWindow getDynamicIsland() {
        return dynamicIsland;
    }


    public static ArraylistView getArraylistView() {
        return arraylistView;
    }


    public static boolean isShowing() {
        return isShowing;
    }

    private UI() {
        // 工具类
    }
}
