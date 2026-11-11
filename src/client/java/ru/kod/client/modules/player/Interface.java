package ru.kod.client.modules.player;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.modules.ModuleManager;

/** Interface: watermark, keybinds list, server IP, potion/cooldown counters, notify — all driven by the main-menu toggles. */
public class Interface extends Module implements HudRenderCallback {
    public Interface() {
        super("Interface", "Player");
        HudRenderCallback.EVENT.register(this);
    }

    @Override public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        var mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.player == null) return;
        Config c = Config.get();
        int color = (int) c.getNum("gui_color");
        int x = 6, y = 6;
        if (c.getBool("watermark")) {
            ctx.drawTextWithShadow(mc.textRenderer, c.getStr("gui_clan") + " | " + mc.getSession().getUsername(), x, y, color);
            y += 11;
        }
        if (c.getBool("serverbinds") && mc.getNetworkHandler() != null) {
            var addr = mc.getNetworkHandler().getConnection().getAddress();
            ctx.drawTextWithShadow(mc.textRenderer, addr == null ? "singleplayer" : addr.toString(), x, y, 0xFFAAAAAA);
            y += 11;
        }
        if (c.getBool("keybinds")) {
            for (var m : ModuleManager.get().all()) {
                if (m.isEnabled()) {
                    ctx.drawTextWithShadow(mc.textRenderer, m.getName(), x, y, 0xFFCCCCCC);
                    y += 10;
                }
            }
        }
    }
}
