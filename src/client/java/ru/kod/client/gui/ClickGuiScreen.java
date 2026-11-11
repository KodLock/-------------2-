package ru.kod.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.modules.ModuleManager;

import java.util.*;

/**
 * ClickGUI — opens with Right Shift.
 * Columns per category; click a module to toggle it; scroll to change the
 * accent color and tweak selected numeric settings inline.
 */
public class ClickGuiScreen extends Screen {
    private static final int COL_W = 150, ROW_H = 16, HEADER_H = 20;
    private final List<String> categories;
    private final Map<String, Integer> colX = new LinkedHashMap<>();
    private int selectedModule = -1; // flat index into all()
    private int colorHue = 0; // 0..360 for accent color

    public ClickGuiScreen() {
        super(Text.literal("KodClient ClickGUI"));
        this.categories = ModuleManager.get().categories();
        int x = 10;
        for (String cat : categories) { colX.put(cat, x); x += COL_W + 8; }
        colorHue = (int) (((int) Config.get().getNum("gui_color") >> 16 & 0xFF) * 360.0 / 255.0);
    }

    @Override public boolean shouldPause() { return false; }

    @Override public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int accent = (int) Config.get().getNum("gui_color");
        ctx.fill(0, 0, width, height, 0x88000000);
        ctx.drawTextWithShadow(textRenderer, "KodClient — RShift: меню | ЛКМ: вкл/выкл | Колесо: цвет", 10, height - 14, accent);

        for (String cat : categories) {
            int x = colX.get(cat);
            int y = 30;
            ctx.fill(x, y, x + COL_W, y + HEADER_H, accent);
            ctx.drawTextWithShadow(textRenderer, cat, x + 6, y + 6, 0xFFFFFFFF);
            y += HEADER_H;
            for (Module m : ModuleManager.get().byCategory(cat)) {
                boolean on = m.isEnabled();
                int bg = on ? 0xFF222222 : 0xFF111111;
                ctx.fill(x, y, x + COL_W, y + ROW_H, bg);
                ctx.fill(x, y, x + 3, y + ROW_H, on ? accent : 0xFF333333);
                ctx.drawTextWithShadow(textRenderer, m.getName(), x + 8, y + 4, on ? 0xFFFFFFFF : 0xFF888888);
                if (mouseX >= x && mouseX <= x + COL_W && mouseY >= y && mouseY <= y + ROW_H) {
                    ctx.fill(x, y, x + COL_W, y + ROW_H, 0x33FFFFFF);
                }
                y += ROW_H;
            }
            // settings panel for selected module in this category
            y += 6;
            ctx.drawTextWithShadow(textRenderer, "settings:", x + 4, y, 0xFFAAAAAA);
            y += 12;
            ctx.drawTextWithShadow(textRenderer, "gui_color hue: " + colorHue + "°", x + 4, y, 0xFFFFFFFF);
            y += 12;
            ctx.drawTextWithShadow(textRenderer, "ka_cps: " + Config.get().getNum("ka_cps"), x + 4, y, 0xFFFFFFFF);
            y += 12;
            ctx.drawTextWithShadow(textRenderer, "ka_range: " + Config.get().getNum("ka_range"), x + 4, y, 0xFFFFFFFF);
        }
    }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        int button = click.button();
        for (String cat : categories) {
            int x = colX.get(cat);
            int y = 30 + HEADER_H;
            for (Module m : ModuleManager.get().byCategory(cat)) {
                if (mouseX >= x && mouseX <= x + COL_W && mouseY >= y && mouseY <= y + ROW_H) {
                    if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) m.toggle();
                    else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) selectedModule = ModuleManager.get().all().indexOf(m);
                    return true;
                }
                y += ROW_H;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double horiz, double vert) {
        colorHue = (colorHue + (int) (vert * 10) + 360) % 360;
        int rgb = java.awt.Color.HSBtoRGB(colorHue / 360f, 0.6f, 1.0f);
        Config.get().setNum("gui_color", 0xFF000000 | (rgb & 0xFFFFFF));
        return true;
    }

    @Override public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        // +/- adjusts KillAura CPS
        if (keyCode == GLFW.GLFW_KEY_EQUAL) {
            Config.get().setNum("ka_cps", Math.min(20, Config.get().getNum("ka_cps") + 0.5));
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_MINUS) {
            Config.get().setNum("ka_cps", Math.max(1, Config.get().getNum("ka_cps") - 0.5));
            return true;
        }
        return super.keyPressed(input);
    }
}
