package com.phoenix.gui.ui;

import android.graphics.Color;
import com.phoenix.gui.config.ConfigManager; // <-- 使用您的 ConfigManager

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    private static final String CONFIG_MODULE_NAME = "GlobalTheme";
    private static final String CONFIG_KEY_THEME_COLOR = "theme_color";
    private static final int DEFAULT_THEME_COLOR = 0xFF96CCFF;

    private static int themeColor = DEFAULT_THEME_COLOR;

    private static int bgPrimary = 0xFF292D2C;
    private static int bgSecondary = 0xFF1F2221;
    private static int bgDisabled = 0xFF333333;

    private static int textPrimary = 0xFFFFFFFF;
    private static int textSecondary = 0xFFE0E0E0;
    private static int textTertiary = 0xFFAAAAAA;
    private static int textOnTheme = 0xFF1A1A1A;

    private static int stateDisabled = 0xFF555555;

    private static int glowColor = 0x66000000;
    private static int glassBackground = 0xE6000000;

    private static int gradientStart = 0xFFa7ff8a;
    private static int gradientEnd = 0xFF82c1fb;

    private static final List<OnThemeColorChangeListener> listeners = new ArrayList<>();

    public interface OnThemeColorChangeListener {
        void onThemeColorChanged(int newColor);
    }

    private static void saveThemeColor() {
        
        ConfigManager.saveModuleConfig(CONFIG_MODULE_NAME, CONFIG_KEY_THEME_COLOR, themeColor);
    }

    public static int getThemeColor() {
        return themeColor;
    }

    public static void setThemeColor(int color) {
        if (themeColor != color) {
            themeColor = color;
            saveThemeColor(); 
            notifyListeners();
        }
    }

    public static float[] getThemeColorHSV() {
        float[] hsv = new float[3];
        Color.colorToHSV(themeColor, hsv);
        return hsv;
    }

    public static int getBgPrimary() { return bgPrimary; }
    public static void setBgPrimary(int color) { bgPrimary = color; notifyListeners(); }
    public static int getBgSecondary() { return bgSecondary; }
    public static void setBgSecondary(int color) { bgSecondary = color; notifyListeners(); }
    public static int getBgDisabled() { return bgDisabled; }
    public static void setBgDisabled(int color) { bgDisabled = color; notifyListeners(); }
    public static int getTextPrimary() { return textPrimary; }
    public static void setTextPrimary(int color) { textPrimary = color; notifyListeners(); }
    public static int getTextSecondary() { return textSecondary; }
    public static void setTextSecondary(int color) { textSecondary = color; notifyListeners(); }
    public static int getTextTertiary() { return textTertiary; }
    public static void setTextTertiary(int color) { textTertiary = color; notifyListeners(); }
    public static int getTextOnTheme() { return textOnTheme; }
    public static void setTextOnTheme(int color) { textOnTheme = color; notifyListeners(); }
    public static int getStateDisabled() { return stateDisabled; }
    public static void setStateDisabled(int color) { stateDisabled = color; notifyListeners(); }
    public static int getGlowColor() { return glowColor; }
    public static void setGlowColor(int color) { glowColor = color; notifyListeners(); }
    public static int getGlassBackground() { return glassBackground; }
    public static void setGlassBackground(int color) { glassBackground = color; notifyListeners(); }
    public static int getGradientStart() { return gradientStart; }
    public static void setGradientStart(int color) { gradientStart = color; notifyListeners(); }
    public static int getGradientEnd() { return gradientEnd; }
    public static void setGradientEnd(int color) { gradientEnd = color; notifyListeners(); }

    public static void applyDefaultTheme() {
        setThemeColor(DEFAULT_THEME_COLOR);
    }

    public static void applyGreenTheme() {
        setThemeColor(0xFF4CAF50);
    }

    public static void applyPurpleTheme() {
        setThemeColor(0xFFBB86FC);
    }

    public static void applyOrangeTheme() {
        setThemeColor(0xFFFF9800);
    }

    public static void applyPinkTheme() {
        setThemeColor(0xFFE91E63);
    }

    public static void setThemeColorRGB(int r, int g, int b) {
        setThemeColor(Color.rgb(r, g, b));
    }

    public static void setThemeColorHSV(float hue, float saturation, float value) {
        setThemeColor(Color.HSVToColor(new float[]{hue, saturation, value}));
    }

    public static void addListener(OnThemeColorChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static void removeListener(OnThemeColorChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (OnThemeColorChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onThemeColorChanged(themeColor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ThemeManager() {

    }
}