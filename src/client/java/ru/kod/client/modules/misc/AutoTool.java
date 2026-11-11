package ru.kod.client.modules.misc;

import net.minecraft.util.math.BlockPos;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/** AutoTool: switches to the best hotbar tool for the block you are looking at. */
public class AutoTool extends Module {
    public AutoTool() { super("AutoTool", "MISC"); }

    @Override public void onTick() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null || mc.crosshairTarget == null) return;
        if (!(mc.crosshairTarget instanceof net.minecraft.util.hit.BlockHitResult hit)) return;
        BlockPos pos = hit.getBlockPos();
        var state = mc.world.getBlockState(pos);
        if (state.isAir()) return;
        float best = -1;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            var stack = p.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > best) { best = speed; bestSlot = i; }
        }
        if (bestSlot >= 0) p.getInventory().setSelectedSlot(bestSlot);
    }
}

