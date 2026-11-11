package ru.kod.client.modules.visuals;

import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/** FullBright: max gamma while enabled, restores on disable. */
public class FullBright extends Module {
    private double prevGamma;
    public FullBright() { super("FullBright", "Visuals"); }

    @Override protected void onEnable() {
        var mc = Util.mc();
        if (mc.options == null) return;
        prevGamma = mc.options.getGamma().getValue();
        mc.options.getGamma().setValue(16.0);
    }

    @Override protected void onDisable() {
        var mc = Util.mc();
        if (mc.options == null) return;
        mc.options.getGamma().setValue(prevGamma);
    }
}
