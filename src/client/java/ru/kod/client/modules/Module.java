package ru.kod.client.modules;

import ru.kod.client.config.Config;

/** Base class for every toggleable module. Keybinds & settings live in Config. */
public abstract class Module {
    protected final String configKey;
    protected final String name;
    protected final String category;
    private boolean enabled;

    protected Module(String name, String category) {
        this(name, category, name);
    }

    protected Module(String name, String category, String configKey) {
        this.name = name;
        this.category = category;
        this.configKey = configKey;
        this.enabled = Config.get().getBool(configKey);
    }

    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public String getCategory() { return category; }

    public void setEnabled(boolean v) {
        if (this.enabled == v) return;
        this.enabled = v;
        Config.get().setBool(configKey, v);
        if (v) onEnable(); else onDisable();
    }

    public void toggle() { setEnabled(!enabled); }

    protected void onEnable() {}
    protected void onDisable() {}

    /** Called every client tick. */
    public void onTick() {}

    /** Called every render frame (visuals / HUD). */
    public void onRender() {}
}
