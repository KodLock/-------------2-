package ru.kod.client.modules.misc;

import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/** ChestStealer: loots items from an open container with a configurable delay. */
public class ChestStealer extends Module {
    private long lastAt = 0;
    public ChestStealer() { super("ChestStealer", "MISC"); }

    @Override public void onTick() {
        var mc = Util.mc();
        var p = mc.player;
        long now = System.currentTimeMillis();
        if (now - lastAt < (long) Config.get().getNum("cheststealer_delay_ms")) return;
        lastAt = now;
        if (p == null || mc.interactionManager == null) return;
        if (!(p.currentScreenHandler instanceof GenericContainerScreenHandler h)) return;
        int rows = h.getRows();
        for (int i = 0; i < rows * 9; i++) {
            var stack = h.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                mc.interactionManager.clickSlot(h.syncId, i, 0, SlotActionType.QUICK_MOVE, p);
                return;
            }
        }
    }
}

