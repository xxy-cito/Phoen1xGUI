package com.phoenix.gui.module.impl.combat;

import com.phoenix.gui.config.ConfigManager;
import com.phoenix.gui.module.Module;
import com.phoenix.gui.module.ModuleCategory;
import com.phoenix.gui.ui.widgets.SubMenuPanel;

import java.util.Arrays;

public class KillAuraModule extends Module {
    private float range = 4.0f;
    private int cps = 10;
    private int targetMode = 0;
    private boolean autoBlock = false;

    public KillAuraModule() {
        super("KillAura", ModuleCategory.COMBAT, "囊囊囊");
    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void configureSubMenu(SubMenuPanel subMenu) {

        subMenu.addSlider("Range", 300, 800, (int)(range * 100), "%.2f", value -> {
            range = value / 100.0f;

        });

        subMenu.addSlider("CPS", 1, 20, cps, "%d", value -> {
            cps = value;

        });

        subMenu.addMode("Target", Arrays.asList("Single", "Multi"), targetMode, (index, mode) -> {
            targetMode = index;

        });

        subMenu.addSwitch("Auto Block", autoBlock, enabled -> {
            autoBlock = enabled;

        });

        super.configureSubMenu(subMenu);
    }
}
