package com.phoenix.gui.ui;

public class ArraylistModule {
    private String name;
    private int color;
    private boolean useThemeColor;

    public ArraylistModule(String name) {
        this.name = name;
        this.color = ThemeManager.getTextPrimary();
        this.useThemeColor = true;
    }

    public ArraylistModule(String name, int color) {
        this.name = name;
        this.color = color;
        this.useThemeColor = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        if (useThemeColor) {
            return ThemeManager.getTextPrimary();
        }
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        this.useThemeColor = false;
    }

    public void setUseThemeColor(boolean use) {
        this.useThemeColor = use;
    }

    public boolean isUseThemeColor() {
        return useThemeColor;
    }
}