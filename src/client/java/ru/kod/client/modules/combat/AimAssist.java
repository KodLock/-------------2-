package ru.kod.client.modules.combat;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.TridentItem;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/**
 * AimAssist: while holding a trident, smoothly aims along the predicted
 * ballistic trajectory at the nearest player (with velocity lead).
 */
public class AimAssist extends Module {
    private LivingEntity target;

    public AimAssist() { super("AimAssist", "Combat"); }

    @Override public void onTick() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null || mc.world == null) { target = null; return; }
        Config c = Config.get();
        if (c.getBool("assist_only_trident") && !(p.getMainHandStack().getItem() instanceof TridentItem)) {
            target = null; return;
        }
        double range = c.getNum("assist_range");
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (var e : mc.world.getPlayers()) {
            if (e == p || e.isDead()) continue;
            double d = p.distanceTo(e);
            if (d <= range && d < bestDist) { best = e; bestDist = d; }
        }
        target = best;
        if (target == null) return;

        // Aim at predicted position (projectile lead)
        double[] lead = Util.tridentLead((PlayerEntity) target, range);
        double dx = lead[0] - p.getX();
        double dy = lead[1] - (p.getY() + p.getEyeHeight(p.getPose()));
        double dz = lead[2] - p.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        double smooth = c.getNum("assist_smooth");
        p.setYaw(p.getYaw() + net.minecraft.util.math.MathHelper.wrapDegrees(yaw - p.getYaw()) * (float) smooth);
        p.setPitch(net.minecraft.util.math.MathHelper.lerp((float) smooth, p.getPitch(), pitch));
    }

    public LivingEntity getTarget() { return target; }
}
