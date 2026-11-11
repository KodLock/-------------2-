package ru.kod.client.modules.visuals;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.modules.ModuleManager;
import ru.kod.client.modules.combat.KillAura;

/**
 * ESP: draws screen-space boxes around players.
 * The KillAura current target is highlighted in accent color (gui_color).
 */
public class ESP extends Module implements HudRenderCallback {
    private static final int[][] EDGES = {
        {0,1},{1,2},{2,3},{3,0}, // bottom
        {4,5},{5,6},{6,7},{7,4}, // top
        {0,4},{1,5},{2,6},{3,7}  // verticals
    };

    public ESP() {
        super("ESP", "Visuals");
        HudRenderCallback.EVENT.register(this);
    }

    @Override public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        var mc = MinecraftClient.getInstance();
        if (!isEnabled() || mc.world == null || mc.player == null) return;
        Config c = Config.get();
        if (!c.getBool("esp_boxes")) return;
        var auraMod = ModuleManager.get().find("KillAura").orElse(null);
        var auraTarget = (auraMod instanceof KillAura aura && aura.isEnabled()) ? aura.getTarget() : null;
        for (var e : mc.world.getPlayers()) {
            if (e == mc.player || e.isDead()) continue;
            if (mc.player.distanceTo(e) > c.getNum("esp_range")) continue;
            drawBox(mc, ctx, e, e == auraTarget);
        }
    }

    private void drawBox(MinecraftClient mc, DrawContext ctx, PlayerEntity e, boolean isTarget) {
        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Vec3d pos = e.getPos();
        double x = pos.x - cam.x, y = pos.y - cam.y, z = pos.z - cam.z;
        double w = e.getWidth() * 0.6, h = e.getHeight();
        float[][] corners = project(mc, x - w, y, z - w, x + w, y + h, z + w);
        if (corners == null) return;
        int color = isTarget ? (int) Config.get().getNum("gui_color") : 0xFF55FFFF;
        for (int[] edge : EDGES) line(ctx, corners[edge[0]], corners[edge[1]], color);
        if (isTarget) { // name tag above the highlighted target
            int cx = (int) ((corners[0][0] + corners[6][0]) / 2);
            int top = (int) Math.min(Math.min(corners[4][1], corners[5][1]), Math.min(corners[6][1], corners[7][1]));
            ctx.drawTextWithShadow(mc.textRenderer, e.getName().getString(), cx - 20, top - 11, color);
        }
    }

    private float[][] project(MinecraftClient mc, double x0, double y0, double z0, double x1, double y1, double z1) {
        float yaw = mc.gameRenderer.getCamera().getYaw();
        float pitch = mc.gameRenderer.getCamera().getPitch();
        Matrix4f mvp = new Matrix4f()
            .rotateX((float) Math.toRadians(pitch))
            .rotateY((float) Math.toRadians(-yaw));
        float[][] out = new float[8][2];
        double[][] pts = {
            {x0,y0,z0},{x1,y0,z0},{x1,y0,z1},{x0,y0,z1},
            {x0,y1,z0},{x1,y1,z0},{x1,y1,z1},{x0,y1,z1}
        };
        for (int i = 0; i < 8; i++) {
            var v = new org.joml.Vector4f((float) pts[i][0], (float) pts[i][1], (float) pts[i][2], 1f);
            mvp.transform(v);
            if (v.z >= -0.1f) return null; // behind camera
            float fov = 1.0f / (float) Math.tan(Math.toRadians(mc.options.getFov().getValue() / 2.0));
            float nx = v.x * fov / -v.z;
            float ny = v.y * fov / -v.z;
            int sw = mc.getWindow().getScaledWidth(), sh = mc.getWindow().getScaledHeight();
            out[i][0] = (nx * 0.5f + 0.5f) * sw;
            out[i][1] = (0.5f - ny * 0.5f) * sh;
        }
        return out;
    }

    private void line(DrawContext ctx, float[] a, float[] b, int color) {
        int x1 = (int) a[0], y1 = (int) a[1], x2 = (int) b[0], y2 = (int) b[1];
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1, err = dx - dy;
        while (true) {
            ctx.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }
}
