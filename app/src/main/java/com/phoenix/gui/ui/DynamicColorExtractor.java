package com.phoenix.gui.ui;

import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class DynamicColorExtractor {

    private static final String TAG = "DynamicColorExtractor";
    private final Context context;
    private WallpaperManager wallpaperManager;
    private WallpaperManager.OnColorsChangedListener colorsChangedListener;

    public DynamicColorExtractor(Context context) {
        this.context = context.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wallpaperManager = WallpaperManager.getInstance(context);
        }
    }

    public void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            extractAndApplyColors();
            startListening();
        } else {
            Log.w(TAG, "nah i cant use dc");
            ThemeManager.applyDefaultTheme();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private void extractAndApplyColors() {
        try {
            WallpaperColors wallpaperColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);

            if (wallpaperColors != null) {

                Color primaryColor = wallpaperColors.getPrimaryColor();

                if (primaryColor != null) {
                    int color = primaryColor.toArgb();

                    int adjustedColor = adjustColorForUI(color);

                    Log.d(TAG, String.format("color: #%08X -> #%08X", color, adjustedColor));

                    ThemeManager.setThemeColor(adjustedColor);

                    Color secondaryColor = wallpaperColors.getSecondaryColor();
                    if (secondaryColor != null) {
                        int secColor = secondaryColor.toArgb();

                        ThemeManager.setGradientStart(adjustedColor);
                        ThemeManager.setGradientEnd(adjustColorForUI(secColor));
                    }

                    Color tertiaryColor = wallpaperColors.getTertiaryColor();
                    if (tertiaryColor != null) {
                        Log.d(TAG, "Tertiary color: #" + Integer.toHexString(tertiaryColor.toArgb()));
                    }
                } else {
                    
                    ThemeManager.applyDefaultTheme();
                }
            } else {
                
                ThemeManager.applyDefaultTheme();
            }
        } catch (Exception e) {
            
            ThemeManager.applyDefaultTheme();
        }
    }

    private int adjustColorForUI(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        if (hsv[1] < 0.5f) {
            hsv[1] = Math.min(hsv[1] * 1.5f, 0.7f);
        }

        if (hsv[2] < 0.6f) {
            hsv[2] = 0.7f;
        } else if (hsv[2] > 0.9f) {
            hsv[2] = 0.85f;
        }

        return Color.HSVToColor(hsv);
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    private void startListening() {
        if (colorsChangedListener == null) {
            colorsChangedListener = new WallpaperManager.OnColorsChangedListener() {
                @Override
                public void onColorsChanged(WallpaperColors colors, int which) {
                    if ((which & WallpaperManager.FLAG_SYSTEM) != 0) {
                        
                        extractAndApplyColors();
                    }
                }
            };

            wallpaperManager.addOnColorsChangedListener(colorsChangedListener, null);
            
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O_MR1)
    public void stopListening() {
        if (colorsChangedListener != null && wallpaperManager != null) {
            wallpaperManager.removeOnColorsChangedListener(colorsChangedListener);
            colorsChangedListener = null;
            
        }
    }

    public void refresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            extractAndApplyColors();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public String getColorSchemeInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            try {
                WallpaperColors colors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
                if (colors != null) {
                    StringBuilder info = new StringBuilder();
                    info.append("主题色方案: ");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        int colorHints = colors.getColorHints();
                        if ((colorHints & WallpaperColors.HINT_SUPPORTS_DARK_TEXT) != 0) {
                            info.append("亮色背景 ");
                        }
                        if ((colorHints & WallpaperColors.HINT_SUPPORTS_DARK_THEME) != 0) {
                            info.append("深色主题 ");
                        }
                    }

                    info.append("\n当前主题色: #").append(Integer.toHexString(ThemeManager.getThemeColor()));

                    return info.toString();
                }
            } catch (Exception e) {
                Log.e(TAG, "failed", e);
            }
        }
        return "动态取色不可用";
    }
}
