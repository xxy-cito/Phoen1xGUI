package com.phoenix.gui.ui;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

public class AnimationUtils {

    public static void expand(View view, long duration) {

        view.measure(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int targetHeight = view.getMeasuredHeight();

        view.getLayoutParams().height = 0;
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);

        ValueAnimator animator = ValueAnimator.ofInt(0, targetHeight);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);
        });
        animator.start();

        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    public static void expand(View view) {
        expand(view, 300);
    }

    public static void collapse(View view, long duration) {
        int initialHeight = view.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, 0);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = value;
            view.setLayoutParams(layoutParams);
        });
        animator.start();

        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> {
                view.setVisibility(View.GONE);
                view.getLayoutParams().height = 0;
            })
            .start();
    }

    public static void collapse(View view) {
        collapse(view, 300);
    }

    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .start();
    }

    public static void fadeIn(View view) {
        fadeIn(view, 300);
    }

    public static void fadeOut(View view, long duration) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> view.setVisibility(View.GONE))
            .start();
    }

    public static void fadeOut(View view) {
        fadeOut(view, 300);
    }

    private AnimationUtils() {

    }
}
