package ru.kod.client.modules.combat;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/**
 * AutoSwap: swaps items between the off-hand and hotbar with one key press.
 * Modes: totem_orb (totem <-> orb), orb_orb (orb <-> orb), totem_totem (totem <-> totem).
 * Press the bound key (default: none, set in ClickGUI) to swap.
 */
public class AutoSwap extends Module {
    public AutoSwap() { super("AutoSwap", "Combat"); }

    /** Called when the user presses the swap key. */
    public void swapNow() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null || mc.interactionManager == null) return;
        String mode = Config.get().getStr("autoswap_mode");
        ItemStack off = p.getOffHandStack();

        int want = findInHotbar(mode, off);
        if (want == -1) return; // nothing to swap with

        // offhand slot index in player inventory = 40 (hotbar 0..8 -> 36..44, offhand = 45)
        int offhandSlot = 45;
        int hotbarSlot = 36 + want;
        mc.interactionManager.clickSlot(p.currentScreenHandler.syncId, hotbarSlot, 40, SlotActionType.SWAP, p);
    }

    private int findInHotbar(String mode, ItemStack off) {
        var p = Util.mc().player;
        boolean offIsTotem = Util.isTotem(off);
        boolean offIsOrb = Util.isOrb(off);
        for (int i = 0; i < 9; i++) {
            ItemStack s = p.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            boolean ok = switch (mode) {
                case "orb_orb" -> offIsOrb && Util.isOrb(s);
                case "totem_totem" -> offIsTotem && Util.isTotem(s);
                default -> // totem_orb: swap whatever is in offhand with the opposite type
                    (offIsTotem && Util.isOrb(s)) || (offIsOrb && Util.isTotem(s))
                    || (!offIsTotem && !offIsOrb && (Util.isTotem(s) || Util.isOrb(s)));
            };
            if (ok) return i;
        }
        return -1;
    }
}
