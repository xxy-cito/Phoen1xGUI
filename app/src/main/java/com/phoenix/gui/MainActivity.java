package com.phoenix.gui;

import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import androidx.activity.ComponentActivity;
import com.phoenix.gui.config.ConfigManager;
import com.phoenix.gui.module.Module;
import com.phoenix.gui.module.ModuleManager;
import com.phoenix.gui.module.ModuleToggleListener;
import com.phoenix.gui.module.ShortcutToggleListener;
import com.phoenix.gui.ui.ArraylistModule;
import com.phoenix.gui.ui.ArraylistView;
import com.phoenix.gui.ui.DynamicColorExtractor;
import com.phoenix.gui.ui.FloatBallView;
import com.phoenix.gui.ui.ShortcutButton;
import com.phoenix.gui.ui.dynamic.DynamicIslandWindow;
import com.phoenix.gui.ui.ThemeManager;
import com.phoenix.gui.ui.UI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends ComponentActivity implements ModuleToggleListener, ShortcutToggleListener {
    
    private boolean wifiState = true;
    
    private Button btnStart, btnStop, btnTestProgress, btnTestSwitch, btnHideAll, btnSave, btnToggleDebug, btnToggleArraylist;
    private Button btnAddRandom, btnRemoveRandom, btnResetArraylist;
    private EditText etUsername;
    private SeekBar seekScale;
    private ScrollView debugPanel;
    private FrameLayout shortcutsContainer;
    
    private final Map<Module, ShortcutButton> shortcutButtons = new HashMap<>();
    private final List<String> allTestModules = new ArrayList<>();
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);
        
        UI.init(this); // 封装

        initAllTestModules();
        bindViews();
        loadConfig();
        setListeners();
        updateBtnState();

        toast("MainActivity ready");
    }

    private void initAllTestModules() {
       // 下面那个随机测试的，可以随便填点啥
    }

    private void bindViews() {
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnTestProgress = findViewById(R.id.btn_test_progress);
        btnTestSwitch = findViewById(R.id.btn_test_switch);
        btnHideAll = findViewById(R.id.btn_hide_all);
        btnSave = findViewById(R.id.btn_save);
        btnToggleDebug = findViewById(R.id.btn_toggle_debug);
        btnToggleArraylist = findViewById(R.id.btn_toggle_arraylist);
        btnAddRandom = findViewById(R.id.btn_add_random_module);
        btnRemoveRandom = findViewById(R.id.btn_remove_random_module);
        btnResetArraylist = findViewById(R.id.btn_reset_arraylist);
        
        etUsername = findViewById(R.id.et_username);
        seekScale = findViewById(R.id.seek_scale);
        debugPanel = findViewById(R.id.debug_panel);
        shortcutsContainer = findViewById(R.id.shortcuts_container);
    }

    private void loadConfig() {
        etUsername.setText(ConfigManager.getDynamicIslandUsername());
        seekScale.setProgress((int) (ConfigManager.getDynamicIslandScale() * 100));
    }

    private void saveConfig() {
        String name = etUsername.getText().toString().trim();
        float scale = seekScale.getProgress() / 100f;
        
        ConfigManager.setDynamicIslandUsername(name);
        ConfigManager.setDynamicIslandScale(scale);
        
        DynamicIslandWindow island = UI.getDynamicIsland();
        if (island != null && island.isShowing()) {
            island.updateConfig(scale, name);
        }
        
        toast("saved");
    }

    private void setListeners() {
        btnStart.setOnClickListener(v -> startGUI());
        btnStop.setOnClickListener(v -> stopGUI());
        btnSave.setOnClickListener(v -> saveConfig());
        btnToggleDebug.setOnClickListener(v -> toggleDebugPanel());
        
        if (btnToggleArraylist != null) {
            btnToggleArraylist.setOnClickListener(v -> toggleArraylist());
        }
        
        if (btnAddRandom != null) {
            btnAddRandom.setOnClickListener(v -> addRandomModule());
        }
        
        if (btnRemoveRandom != null) {
            btnRemoveRandom.setOnClickListener(v -> removeRandomModule());
        }
        
        if (btnResetArraylist != null) {
            btnResetArraylist.setOnClickListener(v -> {
                addDefaultArraylistModules();
                toast("Arraylist reset");
            });
        }
        
        btnTestProgress.setOnClickListener(v -> {
            showProgress();
            toast("test progress");
        });
        
        btnTestSwitch.setOnClickListener(v -> showSwitch());
        
        btnHideAll.setOnClickListener(v -> {
            hideAll();
            toast("hide all");
        });
    }

    private void updateBtnState() {
        boolean isRunning = UI.isShowing();
        btnStart.setEnabled(!isRunning);
        btnStop.setEnabled(isRunning);
        btnTestProgress.setEnabled(isRunning);
        btnTestSwitch.setEnabled(isRunning);
        btnHideAll.setEnabled(isRunning);
        
        boolean arraylistControlsEnabled = isRunning && UI.getArraylistView() != null;
        
        if (btnToggleArraylist != null) {
            btnToggleArraylist.setEnabled(isRunning);
        }
        
        if (btnAddRandom != null) {
            btnAddRandom.setEnabled(arraylistControlsEnabled);
        }
        
        if (btnRemoveRandom != null) {
            btnRemoveRandom.setEnabled(arraylistControlsEnabled);
        }
        
        if (btnResetArraylist != null) {
            btnResetArraylist.setEnabled(arraylistControlsEnabled);
        }
    }

    private void startGUI() {
        if (UI.isShowing()) return;
        
        try {
            UI.show(this);
            updateBtnState();
            toast("GUI started");
        } catch (Exception e) {
            e.printStackTrace();
            toast("Failed to start GUI: " + e.getMessage());
        }
    }

    private void stopGUI() {
        if (!UI.isShowing()) return;
        
        try {
            if (!shortcutButtons.isEmpty()) {
                for (ShortcutButton button : shortcutButtons.values()) {
                    try {
                        button.destroy();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                shortcutButtons.clear();
                shortcutsContainer.removeAllViews();
            }
            
            UI.hide();
            updateBtnState();
            toast("GUI stopped");
        } catch (Exception e) {
            e.printStackTrace();
            toast("Failed to stop GUI: " + e.getMessage());
        }
    }

    private void addDefaultArraylistModules() {
        ArraylistView arraylistView = UI.getArraylistView();
        if (arraylistView == null) return;

        arraylistView.clearModules(); // 是的你没看错这个才是真的能用的
        // arraylistView.addModule(new ArraylistModule("like this"));
    }

    private void addRandomModule() {
        ArraylistView arraylistView = UI.getArraylistView();
        if (arraylistView == null || allTestModules.isEmpty()) return;
        
        String moduleName = allTestModules.get(random.nextInt(allTestModules.size()));
        arraylistView.addModule(new ArraylistModule(moduleName));
        toast("Added: " + moduleName);
    }

    private void removeRandomModule() {
        ArraylistView arraylistView = UI.getArraylistView();
        if (arraylistView != null && !arraylistView.getModules().isEmpty()) {
            int indexToRemove = random.nextInt(arraylistView.getModules().size());
            String moduleName = arraylistView.getModules().get(indexToRemove).getName();
            arraylistView.removeModule(moduleName);
            toast("Removed: " + moduleName);
        } else {
            toast("Arraylist is empty");
        }
    }

    private void toggleArraylist() {
        ArraylistView arraylist = UI.getArraylistView();
        if (arraylist == null) {
            toast("请先启动 GUI");
        } else {
            if (arraylist.getVisibility() == View.VISIBLE) {
                arraylist.hide();
                toast("Arraylist 已隐藏");
            } else {
                arraylist.show();
                toast("Arraylist 已显示");
            }
        }
        updateBtnState();
    }

    private void toggleDynamicIsland() {
        DynamicIslandWindow island = UI.getDynamicIsland();
        if (island == null) {
            ConfigManager.setDynamicIslandEnabled(true);
            toast("请重启 GUI 显示灵动岛");
        } else {
            try {
                island.hide();
                ConfigManager.setDynamicIslandEnabled(false);
                toast("灵动岛已隐藏");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showProgress() {
        DynamicIslandWindow island = UI.getDynamicIsland();
        if (island == null || !island.isShowing()) return;
        
        try {
            island.addOrUpdateProgress("test_progress", "progress", "im subtitle", null, 0.65f, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSwitch() {
        DynamicIslandWindow island = UI.getDynamicIsland();
        if (island == null || !island.isShowing()) return;
        
        try {
            wifiState = !wifiState;
            island.addSwitch("test_switch", "im a function", wifiState);
            toast("test switch -> " + (wifiState ? "ON" : "OFF"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideAll() {
        DynamicIslandWindow island = UI.getDynamicIsland();
        if (island != null && island.isShowing()) {
            try {
                island.hideAllTasks();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void toggleDebugPanel() {
        if (debugPanel.getVisibility() == View.VISIBLE) {
            debugPanel.setVisibility(View.GONE);
        } else {
            debugPanel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onModuleToggled(Module module) {
        DynamicIslandWindow island = UI.getDynamicIsland();
        if (island != null && island.isShowing()) {
            try {
                island.addSwitch("module_" + module.getName(), module.getName(), module.isEnabled());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ShortcutButton button = shortcutButtons.get(module);
        if (button != null) {
            try {
                button.updateStyle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArraylistView arraylistView = UI.getArraylistView();
        if (arraylistView != null) {
            try {
                if (module.isEnabled()) {
                    arraylistView.addModule(module.getName());
                } else {
                    arraylistView.removeModule(module.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onShortcutToggled(Module module, boolean enabled) {
        if (enabled) {
            createShortcut(module);
        } else {
            removeShortcut(module);
        }
    }

    private void createShortcut(Module module) {
        if (!shortcutButtons.containsKey(module)) {
            try {
                ShortcutButton button = new ShortcutButton(this, module);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                );
                button.setLayoutParams(params);
                shortcutsContainer.addView(button);
                button.show();
                shortcutButtons.put(module, button);
            } catch (Exception e) {
                e.printStackTrace();
                toast("创建快捷按钮失败: " + e.getMessage());
            }
        }
    }

    private void removeShortcut(Module module) {
        ShortcutButton button = shortcutButtons.get(module);
        if (button != null) {
            try {
                button.hide();
                button.postDelayed(() -> {
                    shortcutsContainer.removeView(button);
                    button.destroy();
                }, 250);
            } catch (Exception e) {
                e.printStackTrace();
            }
            shortcutButtons.remove(module);
        }
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        android.util.Log.d("PHOENIX", s); // me。
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if (UI.isShowing()) {
            stopGUI();
        }
        
        UI.cleanup();
    }

    @Override
    public void onBackPressed() {
        if (UI.isShowing()) {
            stopGUI(); // 回家吃饭
        } else {
            super.onBackPressed();
        }
    }
}