package ru.kod.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.MathHelper;

/** Small helpers shared by modules. */
public final class Util {
    private Util() {}

    public static MinecraftClient mc() { return MinecraftClient.getInstance(); }

    public static boolean isHoldingWeapon(PlayerEntity p) {
        Item main = p.getMainHandStack().getItem();
        return main instanceof AxeItem || main instanceof TridentItem || main instanceof MaceItem
            || net.minecraft.registry.Registries.ITEM.getId(main).getPath().endsWith("_sword");
    }

    public static boolean isTotem(ItemStack s) {
        return s.getItem() == Items.TOTEM_OF_UNDYING;
    }

    public static boolean isOrb(ItemStack s) {
        // "Сфера" (end crystal / orb of protection) — treats end crystal + golden apple as orb-slot items.
        Item i = s.getItem();
        return i == Items.END_CRYSTAL || i == Items.GOLDEN_APPLE || i == Items.ENCHANTED_GOLDEN_APPLE;
    }

    /** Smoothly rotate the camera towards the target's chest. */
    public static void aimAt(Entity target, double smooth) {
        var p = mc().player;
        if (p == null) return;
        double dx = target.getX() - p.getX();
        double dy = (target.getY() + target.getHeight() * 0.75) - (p.getY() + p.getEyeHeight(p.getPose()));
        double dz = target.getZ() - p.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        p.setYaw(lerpAngle(p.getYaw(), yaw, smooth));
        p.setPitch(MathHelper.lerp((float) smooth, p.getPitch(), pitch));
    }

    private static float lerpAngle(float a, float b, double t) {
        float d = MathHelper.wrapDegrees(b - a);
        return a + d * (float) t;
    }

    /** Predicted intercept point for a thrown trident (gravity 0.03, speed ~2.5 * 20 ticks trajectory). */
    public static double[] tridentLead(PlayerEntity target, double range) {
        var p = mc().player;
        double dist = p.distanceTo(target);
        double speed = 2.5; // blocks/tick initial
        double time = Math.max(1.0, dist / speed);
        double vx = target.getVelocity().x, vy = target.getVelocity().y, vz = target.getVelocity().z;
        double tx = target.getX() + vx * time;
        double ty = target.getY() + target.getHeight() * 0.5 + vy * time - 0.5 * 0.03 * time * time;
        double tz = target.getZ() + vz * time;
        return new double[]{tx, ty, tz};
    }

    public static String fmtHp(PlayerEntity p) {
        return String.format("%.1f", p.getHealth() + p.getAbsorptionAmount());
    }

    public static String fmtDist(Entity e) {
        var p = mc().player;
        return p == null ? "?" : String.format("%.1f", p.distanceTo(e));
    }
}
