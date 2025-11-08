package com.phoenix.gui.module;

import com.phoenix.gui.ui.widgets.SubMenuPanel;

public abstract class Module {
    private final String name;
    private final ModuleCategory category;
    private final String description;
    private boolean isEnabled = false;

    private boolean shortcutEnabled = false;

    public Module(String name, ModuleCategory category, String description) {
        this.name = name;
        this.category = category;
        this.description = description;
    }

    public Module(String name, ModuleCategory category) {
        this(name, category, "");
    }

    public String getName() {
        return name;
    }

    public ModuleCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public boolean isShortcutEnabled() {
        return shortcutEnabled;
    }

    public boolean supportsShortcut() {
        return true;
    }

    public void toggle() {
        if (isEnabled) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        if (!isEnabled) {
            isEnabled = true;
            onEnable();
            ModuleManager.notifyModuleToggled(this);
        }
    }

    public void disable() {
        if (isEnabled) {
            isEnabled = false;
            onDisable();
            ModuleManager.notifyModuleToggled(this);
        }
    }

    protected void onEnable() {}

    protected void onDisable() {}

    public void configureSubMenu(SubMenuPanel subMenu) {

        if (supportsShortcut()) {
            subMenu.addShortcutSwitch(shortcutEnabled);
        }
    }

    protected void onShortcutToggled(boolean enabled) {}

    public void toggleShortcut(boolean enabled) {
        if (shortcutEnabled != enabled) {
            shortcutEnabled = enabled;
            onShortcutToggled(enabled);
            ModuleManager.notifyShortcutToggled(this, enabled);
        }
    }
}
