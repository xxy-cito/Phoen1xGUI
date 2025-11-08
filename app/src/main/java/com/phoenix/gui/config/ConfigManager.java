package com.phoenix.gui.config;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {

    private static final String PREF_NAME = "phoenix_gui_config";
    private static SharedPreferences sharedPreferences;

    private static final String KEY_DYNAMIC_ISLAND_ENABLED = "dynamic_island_enabled";
    private static final String KEY_DYNAMIC_ISLAND_SCALE = "dynamic_island_scale";
    private static final String KEY_DYNAMIC_ISLAND_USERNAME = "dynamic_island_username";
    
    // 初始化
    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    private static String getKey(String moduleName, String configKey) {
        return moduleName + "_" + configKey;
    }
        private static SharedPreferences getPrefs() {
        if (sharedPreferences == null) {
            throw new IllegalStateException("ConfigManager not initialized. Call init() first.");
        }
        return sharedPreferences;
    }
    public static float getFloat(String moduleName, String key, float defaultValue) {
        try {
            return getPrefs().getFloat(moduleName + "_" + key, defaultValue);
        } catch (ClassCastException e) {

            try {
                return (float) getPrefs().getInt(moduleName + "_" + key, (int) defaultValue);
            } catch (Exception e2) {
                return defaultValue;
            }
        }
    }

    public static void saveInt(String moduleName, String configKey, int value) {
        if (sharedPreferences == null) return;
        sharedPreferences.edit().putInt(getKey(moduleName, configKey), value).apply();
    }

    public static int getInt(String moduleName, String configKey, int defaultValue) {
        if (sharedPreferences == null) return defaultValue;
        return sharedPreferences.getInt(getKey(moduleName, configKey), defaultValue);
    }
    public static void saveBoolean(String moduleName, String configKey, boolean value) {
        if (sharedPreferences == null) return;
        sharedPreferences.edit().putBoolean(getKey(moduleName, configKey), value).apply();
    }

    public static boolean getBoolean(String moduleName, String configKey, boolean defaultValue) {
        if (sharedPreferences == null) return defaultValue;
        return sharedPreferences.getBoolean(getKey(moduleName, configKey), defaultValue);
    }

    public static void saveModuleConfig(String moduleName, String configKey, int value) {
        saveInt(moduleName, configKey, value);
    }

    public static void saveModuleConfig(String moduleName, String configKey, boolean value) {
        saveBoolean(moduleName, configKey, value);
    }

    public static boolean getDynamicIslandEnabled() {
        return getPrefs().getBoolean(KEY_DYNAMIC_ISLAND_ENABLED, true);
    }

    public static void setDynamicIslandEnabled(boolean enabled) {
        getPrefs().edit()
            .putBoolean(KEY_DYNAMIC_ISLAND_ENABLED, enabled)
            .apply();
    }

    public static float getDynamicIslandScale() {
        return getPrefs().getFloat(KEY_DYNAMIC_ISLAND_SCALE, 0.7f);
    }

    public static void setDynamicIslandScale(float scale) {
        getPrefs().edit()
            .putFloat(KEY_DYNAMIC_ISLAND_SCALE, scale)
            .apply();
    }

    public static String getDynamicIslandUsername() {
        return getPrefs().getString(KEY_DYNAMIC_ISLAND_USERNAME, "User");
    }

    public static void setDynamicIslandUsername(String username) {
        getPrefs().edit()
            .putString(KEY_DYNAMIC_ISLAND_USERNAME, username)
            .apply();
    }

    public static void clearAll() {
        getPrefs().edit().clear().apply();
    }

    private ConfigManager() {

    }
}
