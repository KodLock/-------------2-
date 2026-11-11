package ru.kod.client.modules.utilities;

import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/** PotionThrower: throws a splash potion from the hotbar when the key is pressed. */
public class PotionThrower extends Module {
    public PotionThrower() { super("PotionThrower", "Utilities"); }

    /** Called via keybind in KodClient. */
    public void throwPotion() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null || mc.interactionManager == null) return;
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).getItem() == Items.SPLASH_POTION
                || p.getInventory().getStack(i).getItem() == Items.LINGERING_POTION) {
                int prev = p.getInventory().getSelectedSlot();
                p.getInventory().setSelectedSlot(i);
                mc.interactionManager.interactItem(p, Hand.MAIN_HAND);
                p.getInventory().setSelectedSlot(prev);
                return;
            }
        }
    }
}
