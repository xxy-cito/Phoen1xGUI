package com.phoenix.gui.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArraylistView extends LinearLayout {
    private List<ArraylistModule> modules;
    private Map<String, ArraylistItemView> itemViews;

    public ArraylistView(Context context) {
        super(context);
        init();
    }
    public ArraylistView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ArraylistView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setGravity(Gravity.RIGHT | Gravity.TOP);
        modules = new ArrayList<>();
        itemViews = new HashMap<>();

        setClipChildren(false);
        setClipToPadding(false);

        int paddingTop = dpToPx(16);
        setPadding(0, paddingTop, 0, 0);

        setupLayoutAnimations();
    }
    
    private void setupLayoutAnimations() {
        LayoutTransition transition = new LayoutTransition();

        transition.setDuration(300);
        
        transition.setAnimator(LayoutTransition.APPEARING, null);
        transition.setAnimator(LayoutTransition.DISAPPEARING, null);
        
        this.setLayoutTransition(transition);
    }
    
    private void rebuildViews() {
        List<View> viewsToShow = new ArrayList<>();
        int moduleCount = modules.size();

        for (int i = 0; i < moduleCount; i++) {
            ArraylistModule module = modules.get(i);
            ArraylistItemView itemView = itemViews.get(module.getName());
            
            if (itemView != null) {
                viewsToShow.add(itemView);

                if (moduleCount == 1) {
                    itemView.setPositionType(ArraylistItemView.PositionType.SINGLE);
                } else if (i == 0) {
                    itemView.setPositionType(ArraylistItemView.PositionType.FIRST);
                } else if (i == moduleCount - 1) {
                    itemView.setPositionType(ArraylistItemView.PositionType.LAST);
                } else {
                    itemView.setPositionType(ArraylistItemView.PositionType.MIDDLE);
                }

                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) itemView.getLayoutParams();
                if (params == null) {
                    params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                }
                params.gravity = Gravity.RIGHT;

                int shadowSpaceInPx = dpToPx(8);
                if (i > 0) {
                    params.topMargin = -(shadowSpaceInPx * 2); 
                } else {
                    params.topMargin = 0;
                }
                itemView.setLayoutParams(params);
            }
        }
        
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View child = getChildAt(i);
            if (!viewsToShow.contains(child)) {
                removeViewAt(i);
            }
        }
        
        for (int i = 0; i < viewsToShow.size(); i++) {
            View view = viewsToShow.get(i);
            if (indexOfChild(view) != i) {
                if (view.getParent() != null) {
                    removeView(view);
                }
                addView(view, i);
            }
        }
    }
    
    public void addModule(String moduleName) { addModule(new ArraylistModule(moduleName)); }
    public void addModule(String moduleName, int color) { addModule(new ArraylistModule(moduleName, color)); }
    public void addModule(ArraylistModule module) { 
        if (itemViews.containsKey(module.getName())) { return; } 
        modules.add(module); 
        ArraylistItemView itemView = new ArraylistItemView(getContext()); 
        itemView.setText(module.getName()); 
        if (!module.isUseThemeColor()) { itemView.setTextColor(module.getColor()); } 
        
        itemViews.put(module.getName(), itemView);
        
        itemView.setAlpha(0f);
        itemView.setTranslationX(dpToPx(20)); 
        
        sortModules();
        rebuildViews();
        
        animateItemIn(itemView);
    }
    
    public void removeModule(String moduleName) { 
        ArraylistModule toRemove = null; 
        for (ArraylistModule module : modules) { if (module.getName().equals(moduleName)) { toRemove = module; break; } } 
        if (toRemove != null) { 
            modules.remove(toRemove);
            ArraylistItemView itemView = itemViews.get(moduleName); 
            if (itemView != null) { 
                animateItemOut(itemView, () -> { 
                    itemViews.remove(moduleName); 
                    itemView.cleanup(); 
                    sortModules(); 
                    rebuildViews();
                }); 
            } 
        } // 下面也是 gemini 写的，由于我懒得动。所以建议不要动。
    }
    private void sortModules() { Collections.sort(modules, (m1, m2) -> { ArraylistItemView v1 = itemViews.get(m1.getName()); ArraylistItemView v2 = itemViews.get(m2.getName()); if (v1 == null || v2 == null) { return Integer.compare(m2.getName().length(), m1.getName().length()); } float width1 = v1.getMeasuredTextWidth(); float width2 = v2.getMeasuredTextWidth(); int widthCompare = Float.compare(width2, width1); if (widthCompare != 0) { return widthCompare; } return m1.getName().compareTo(m2.getName()); }); }
    private void animateItemIn(ArraylistItemView itemView) { ValueAnimator alphaAnimator = ValueAnimator.ofFloat(0f, 1f); alphaAnimator.setDuration(300); alphaAnimator.setInterpolator(new DecelerateInterpolator()); alphaAnimator.addUpdateListener(animation -> { float value = (float) animation.getAnimatedValue(); itemView.setAlpha(value); }); ValueAnimator translationAnimator = ValueAnimator.ofFloat(dpToPx(20), 0f); translationAnimator.setDuration(300); translationAnimator.setInterpolator(new DecelerateInterpolator()); translationAnimator.addUpdateListener(animation -> { float value = (float) animation.getAnimatedValue(); itemView.setTranslationX(value); }); alphaAnimator.start(); translationAnimator.start(); }
    private void animateItemOut(ArraylistItemView itemView, Runnable onComplete) { ValueAnimator alphaAnimator = ValueAnimator.ofFloat(itemView.getAlpha(), 0f); alphaAnimator.setDuration(250); alphaAnimator.setInterpolator(new DecelerateInterpolator()); alphaAnimator.addUpdateListener(animation -> { float value = (float) animation.getAnimatedValue(); itemView.setAlpha(value); }); ValueAnimator translationAnimator = ValueAnimator.ofFloat(itemView.getTranslationX(), dpToPx(20)); translationAnimator.setDuration(250); translationAnimator.setInterpolator(new DecelerateInterpolator()); translationAnimator.addUpdateListener(animation -> { float value = (float) animation.getAnimatedValue(); itemView.setTranslationX(value); }); translationAnimator.addListener(new AnimatorListenerAdapter() { @Override public void onAnimationEnd(Animator animation) { if (onComplete != null) { onComplete.run(); } } }); alphaAnimator.start(); translationAnimator.start(); }
    public void clearModules() { for (ArraylistItemView itemView : itemViews.values()) { itemView.cleanup(); } modules.clear(); itemViews.clear(); removeAllViews(); }
    public void show() { setVisibility(VISIBLE); }
    public void hide() { setVisibility(GONE); }
    private int dpToPx(float dp) { return (int) (dp * getContext().getResources().getDisplayMetrics().density); }
    public List<ArraylistModule> getModules() { return this.modules; }
}