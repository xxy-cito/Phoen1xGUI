package com.phoenix.gui.module;

import com.phoenix.gui.module.impl.combat.*;
import com.phoenix.gui.module.impl.movement.*;
import com.phoenix.gui.module.impl.player.*;
import com.phoenix.gui.module.impl.visual.*;
import com.phoenix.gui.module.impl.world.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private static final List<Module> modules = new ArrayList<>();
    private static final List<ModuleToggleListener> listeners = new ArrayList<>();
    private static final List<ShortcutToggleListener> shortcutListeners = new ArrayList<>();

    static {
        registerModules();
    }

    private static void registerModules() { //注册

        register(new HitBoxModule());
        register(new InfiniteAuraModule());
        register(new KillAuraModule());
        register(new FightBotModule());
        register(new RideModule());
        register(new LockBackModule());

        register(new AirJumpModule());
        register(new SpeedModule());
        register(new FastStopModule());
        register(new ScaffoldModule());
        register(new JekBackModule());
        register(new FlyModule());

        register(new AntiBotModule());
        register(new BlinkModule());
        register(new NameTagModule());
        register(new PhasesModule());
        register(new TeleportModule());

        register(new ThemeModule());
        register(new FovModule());
        register(new DisableModule());
        register(new GodModeModule());
        register(new OnPosModule());
        register(new InfiniteYModule());
        

        register(new FuckerModule());
        register(new RemoteStopModule());
        register(new GameModeModule());
        register(new AutoClickModule());
        register(new AimAssetsModule());
    }

    private static void register(Module module) {
        modules.add(module);
    }

    public static List<Module> getAllModules() {
        return new ArrayList<>(modules);
    }

    public static List<Module> getModulesByCategory(ModuleCategory category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) {
                result.add(module);
            }
        }
        return result;
    }

    public static Module getModuleByName(String name) {
        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    public static void addToggleListener(ModuleToggleListener listener) {
        listeners.add(listener);
    }

    public static void removeToggleListener(ModuleToggleListener listener) {
        listeners.remove(listener);
    }

    public static void addShortcutListener(ShortcutToggleListener listener) {
        shortcutListeners.add(listener);
    }

    public static void removeShortcutListener(ShortcutToggleListener listener) {
        shortcutListeners.remove(listener);
    }

    public static void notifyModuleToggled(Module module) {
        for (ModuleToggleListener listener : listeners) {
            listener.onModuleToggled(module);
        }
    }

    public static void notifyShortcutToggled(Module module, boolean enabled) {
        for (ShortcutToggleListener listener : shortcutListeners) {
            listener.onShortcutToggled(module, enabled);
        }
    }

    public static void disableAll() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                module.disable();
            }
        }
    }

    private ModuleManager() {

    }
}
