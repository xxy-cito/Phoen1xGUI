package com.phoenix.gui.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.phoenix.gui.config.ConfigManager;
import com.phoenix.gui.module.ModuleCategory;

import java.util.ArrayList;
import java.util.List;

public class FloatBallView extends LinearLayout {

    private final List<MenuView> menuViews = new ArrayList<>();
    private boolean menusVisible = false;

    private final LinearLayout outerBall;
    private final LinearLayout innerBall;

    private final FrameLayout menusContainer;

    public FloatBallView(Context context, FrameLayout menusContainer) {
        super(context);
        this.menusContainer = menusContainer;

        ConfigManager.init(context);

        setLayoutParams(new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ));

        outerBall = new LinearLayout(context);
        int size = (int) (getScreenHeight(context) * 0.05);
        outerBall.setLayoutParams(new LayoutParams(size, size));

        GradientDrawable gradient = new GradientDrawable();
        gradient.setColors(new int[]{ThemeManager.getGradientStart(), ThemeManager.getGradientEnd()});
        gradient.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradient.setShape(GradientDrawable.OVAL);
        outerBall.setBackground(gradient);
        outerBall.setGravity(Gravity.CENTER);

        innerBall = new LinearLayout(context);
        int innerSize = (int) (getScreenHeight(context) * 0.03);
        innerBall.setLayoutParams(new LayoutParams(innerSize, innerSize));

        GradientDrawable innerDrawable = new GradientDrawable();
        innerDrawable.setColor(ThemeManager.getTextPrimary());
        innerDrawable.setShape(GradientDrawable.OVAL);
        innerBall.setBackground(innerDrawable);

        outerBall.addView(innerBall);

        LinearLayout controlView = new LinearLayout(context);
        controlView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        controlView.addView(outerBall);
        addView(controlView);

        setupTouchListeners();

        createMenus();
    }

    private void createMenus() {
        menuViews.add(new MenuView(getContext(), ModuleCategory.PLAYER, MenuView.MenuPosition.POSITION_1));
        menuViews.add(new MenuView(getContext(), ModuleCategory.WORLD, MenuView.MenuPosition.POSITION_2));
        menuViews.add(new MenuView(getContext(), ModuleCategory.MOVEMENT, MenuView.MenuPosition.POSITION_3));
        menuViews.add(new MenuView(getContext(), ModuleCategory.COMBAT, MenuView.MenuPosition.POSITION_4));
        menuViews.add(new MenuView(getContext(), ModuleCategory.VISUAL, MenuView.MenuPosition.POSITION_5));
    }

    private void setupTouchListeners() {
        final float[] downPos = new float[2];
        final boolean[] hasMoved = {false};
        final long[] downTime = {0};

        setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downPos[0] = event.getX();
                    downPos[1] = event.getY();
                    hasMoved[0] = false;
                    downTime[0] = System.currentTimeMillis();

                    animate()
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(100)
                        .start();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getX();
                    float moveY = event.getY();
                    float deltaX = moveX - downPos[0];
                    float deltaY = moveY - downPos[1];

                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        hasMoved[0] = true;
                        setTranslationX(getTranslationX() + deltaX);
                        setTranslationY(getTranslationY() + deltaY);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start();

                    long clickDuration = System.currentTimeMillis() - downTime[0];
                    if (!hasMoved[0] && clickDuration < 300) {
                        toggleMenus();
                    }
                    return true;
            }
            return false;
        });
    }

    private void toggleMenus() {
        menusVisible = !menusVisible;

        if (menusVisible) {
            showMenus();
        } else {
            hideMenus();
        }
    }

    private void showMenus() {
        if (menusContainer == null) return;

        menusContainer.removeAllViews();
        menusContainer.setVisibility(View.VISIBLE);

        for (MenuView menu : menuViews) {
            if (menu.getParent() != null) {
                ((FrameLayout) menu.getParent()).removeView(menu);
            }

            menusContainer.addView(menu);
            menu.show();
        }
    }

    private void hideMenus() {
        if (menusContainer == null) return;

        for (MenuView menu : menuViews) {
            menu.hide();
        }

        postDelayed(() -> {
            try {
                for (MenuView menu : menuViews) {
                    if (menu.getParent() != null) {
                        ((FrameLayout) menu.getParent()).removeView(menu);
                    }
                }
                menusContainer.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 300);
    }

    public void show() {
        setVisibility(View.VISIBLE);
        setAlpha(0f);
        setScaleX(0.8f);
        setScaleY(0.8f);
        animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(250)
            .start();
    }

    public void hide() {
        if (menusVisible) {
            hideMenus();
            menusVisible = false;
        }

        animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(250)
            .withEndAction(() -> setVisibility(View.GONE))
            .start();
    }

    public void destroy() {
        try {
            for (MenuView menu : menuViews) {
                if (menu.getParent() != null) {
                    ((FrameLayout) menu.getParent()).removeView(menu);
                }
                menu.destroy();
            }
            menuViews.clear();

            if (menusContainer != null) {
                menusContainer.removeAllViews();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        return metrics.heightPixels;
    }
}