package ru.kod.client.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;
import java.util.Random;

/**
 * KillAura: automatically aims at and attacks the nearest valid target.
 * "Обход под спуфи тайм" — randomized CPS between min/max, random tick jitter
 * and occasional missed clicks, so attacks no longer look perfectly periodic.
 * Current target gets a highlighted box (rendered by ESP / TargetHUD).
 */
public class KillAura extends Module {
    private static final Random RNG = new Random();
    private LivingEntity target;
    private long nextAttackAt = 0;

    public KillAura() { super("KillAura", "Combat"); }

    @Override public void onTick() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null || mc.world == null) { target = null; return; }
        Config c = Config.get();

        if (!c.getBool("ka_weapons_only") || Util.isHoldingWeapon(p)) {
            target = findTarget();
        } else {
            target = null;
        }
        if (target == null) return;

        if (c.getBool("ka_aim")) {
            Util.aimAt(target, c.getNum("ka_aim_smooth"));
        }

        long now = System.currentTimeMillis();
        if (now < nextAttackAt) return;

        // Spoof-time: random interval from CPS range + jitter
        double minCps = c.getNum("ka_min_cps"), maxCps = c.getNum("ka_max_cps");
        if (!c.getBool("ka_randomize")) { minCps = maxCps = c.getNum("ka_cps"); }
        double cps = minCps + RNG.nextDouble() * Math.max(0, maxCps - minCps);
        long interval = Math.max(16, (long) (1000.0 / Math.max(0.5, cps)));
        if (c.getBool("ka_spoof_time")) {
            interval += RNG.nextLong(-35, 36); // ±35ms jitter
            if (RNG.nextDouble() < 0.04) interval += 120; // occasional "human" pause
        }
        nextAttackAt = now + Math.max(16, interval);

        mc.interactionManager.attackEntity(p, target);
        p.swingHand(p.getMainHandStack().isEmpty() ? net.minecraft.util.Hand.OFF_HAND : net.minecraft.util.Hand.MAIN_HAND);
    }

    private LivingEntity findTarget() {
        var mc = Util.mc();
        var p = mc.player;
        Config c = Config.get();
        double range = c.getNum("ka_range");
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le)) continue;
            if (le == p || le.isDead() || le.getHealth() <= 0) continue;
            if (c.getBool("ka_only_players") && !(le instanceof PlayerEntity)) continue;
            double d = p.distanceTo(le);
            if (d <= range && d < bestDist) { best = le; bestDist = d; }
        }
        return best;
    }

    public LivingEntity getTarget() { return target; }
}
