package ru.kod.client.modules.combat;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/**
 * AutoTotem: when HP drops below the configured threshold, automatically
 * moves a totem from the hotbar/inventory into the off-hand.
 */
public class AutoTotem extends Module {
    public AutoTotem() { super("AutoTotem", "Combat"); }

    @Override public void onTick() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null || mc.interactionManager == null) return;
        if (Util.isTotem(p.getOffHandStack())) return;

        double hp = p.getHealth() + p.getAbsorptionAmount();
        if (hp > Config.get().getNum("autototem_hp")) return;

        // 1) totem in hotbar -> swap with offhand
        for (int i = 0; i < 9; i++) {
            if (Util.isTotem(p.getInventory().getStack(i))) {
                mc.interactionManager.clickSlot(p.currentScreenHandler.syncId, 36 + i, 40, SlotActionType.SWAP, p);
                return;
            }
        }
        // 2) totem in main inventory -> pickup + place into offhand (slot 45)
        for (int i = 0; i < 27; i++) {
            ItemStack s = p.getInventory().getStack(i + 9);
            if (Util.isTotem(s)) {
                int syncId = p.currentScreenHandler.syncId;
                mc.interactionManager.clickSlot(syncId, 9 + i, 0, SlotActionType.PICKUP, p);
                mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, p);
                mc.interactionManager.clickSlot(syncId, 9 + i, 0, SlotActionType.PICKUP, p);
                return;
            }
        }
    }
}
