package ru.kod.client.modules.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.modules.ModuleManager;
import ru.kod.client.modules.combat.KillAura;
import ru.kod.client.util.Util;

/** TargetHUD: shows name / HP / distance of the current KillAura target. */
public class TargetHUD extends Module implements HudRenderCallback {
    public TargetHUD() {
        super("TargetHUD", "Visuals");
        HudRenderCallback.EVENT.register(this);
    }

    @Override public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        var mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null) return;
        if (!Config.get().getBool("target")) return;
        KillAura aura = (KillAura) ModuleManager.get().find("KillAura").orElse(null);
        if (aura == null || !aura.isEnabled()) return;
        LivingEntity t = aura.getTarget();
        if (t == null) return;

        int color = (int) Config.get().getNum("gui_color");
        int x = 12, y = mc.getWindow().getScaledHeight() - 90;
        ctx.fill(x - 4, y - 4, x + 170, y + 34, 0xAA000000);
        ctx.fill(x - 4, y - 4, x + 170, y - 2, color);
        ctx.drawTextWithShadow(mc.textRenderer, t.getName().getString(), x, y, color);
        ctx.drawTextWithShadow(mc.textRenderer, "HP: " + Util.fmtHp((net.minecraft.entity.player.PlayerEntity) t), x, y + 10, 0xFFFFFFFF);
        ctx.drawTextWithShadow(mc.textRenderer, "Dist: " + Util.fmtDist(t), x, y + 20, 0xFFAAAAAA);
    }
}
