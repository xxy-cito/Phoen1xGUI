package com.phoenix.gui.module.impl.visual;

import com.phoenix.gui.module.Module;
import com.phoenix.gui.module.ModuleCategory;
import com.phoenix.gui.ui.widgets.ColorPickerView;
import com.phoenix.gui.ui.widgets.SubMenuPanel;

public class ThemeModule extends Module {

    public ThemeModule() {
        super("Theme", ModuleCategory.VISUAL, "Customize the UI colors and themes.");
    }

    // 这个模块没有简单的开关功能，所以 onEnable/onDisable 为空
    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}

    // 这个模块不支持快捷方式
    @Override
    public boolean supportsShortcut() {
        return false;
    }

    @Override
    public void configureSubMenu(SubMenuPanel subMenu) {
        // 添加一行说明文字
        subMenu.addText("Select a preset theme or create your own color below.");
        
        // 添加预设主题按钮
        subMenu.addThemeButtons();

        // 添加自定义颜色选择器
        ColorPickerView colorPicker = new ColorPickerView(subMenu.getContext());
        subMenu.addCustomView(colorPicker);
    }
}