package ru.kod.client.modules.misc;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.util.Hand;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/** AutoEat: eats food from the hotbar when hunger drops below the threshold. */
public class AutoEat extends Module {
    public AutoEat() { super("AutoEat", "MISC"); }

    @Override public void onTick() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null || mc.interactionManager == null) return;
        if (p.getHungerManager().getFoodLevel() > Config.get().getNum("autoeat_hunger")) return;
        if (p.isUsingItem()) return;
        for (int i = 0; i < 9; i++) {
            var stack = p.getInventory().getStack(i);
            if (stack.get(DataComponentTypes.FOOD) != null) {
                int prev = p.getInventory().selectedSlot;
                p.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(p, Hand.MAIN_HAND);
                p.getInventory().selectedSlot = prev;
                return;
            }
        }
    }
}

