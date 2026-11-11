package ru.kod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.kod.client.command.KodCommand;
import ru.kod.client.config.Config;
import ru.kod.client.gui.ClickGuiScreen;
import ru.kod.client.modules.ModuleManager;
import ru.kod.client.modules.combat.AutoSwap;
import ru.kod.client.modules.misc.AutoAccept;
import ru.kod.client.modules.misc.AutoMessage;
import ru.kod.client.modules.utilities.PotionThrower;

/**
 * KodClient — entry point for Minecraft 1.21.11 (Fabric).
 * Opens the ClickGUI with Right Shift. Config: ./config/kodclient.json
 */
public class KodClient implements ClientModInitializer {
    private static KeyBinding clickGuiKey;
    private static KeyBinding swapKey;
    private static KeyBinding potionKey;
    private static boolean rShiftHeld = false;

    @Override public void onInitializeClient() {
        Config.get();            // loads ./config/kodclient.json
        ModuleManager.get();     // registers all modules
        KodCommand.register();   // /kod toggle|set|config|modules

        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.kodclient.clickgui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.kodclient"));
        swapKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.kodclient.autoswap", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.kodclient"));
        potionKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.kodclient.potion", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.kodclient"));

        // Client tick: modules + key handling
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Прямой опрос GLFW: RShift дополнительно к KeyBinding (важно для версионки,
            // где KeyBindingHelper может не сработать из-за порядка инициализации)
            if (client.getWindow() != null && client.currentScreen == null) {
                long handle = client.getWindow().getHandle();
                if (GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS
                    || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS) {
                    if (!rShiftHeld) {
                        client.setScreen(new ClickGuiScreen());
                        rShiftHeld = true;
                    }
                } else {
                    rShiftHeld = false;
                }
            }
            while (clickGuiKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }
            while (swapKey.wasPressed()) {
                var m = ModuleManager.get().find("AutoSwap");
                if (m.isPresent() && m.get() instanceof AutoSwap s) s.swapNow();
            }
            while (potionKey.wasPressed()) {
                var m = ModuleManager.get().find("PotionThrower");
                if (m.isPresent() && m.get() instanceof PotionThrower p) p.throwPotion();
            }
            ModuleManager.get().tickAll();
        });

        // Chat listener: AutoAccept + AutoMessage hooks
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String text = message.getString();
            var m = ModuleManager.get().find("AutoAccept");
            if (m.isPresent() && m.get().isEnabled() && m.get() instanceof AutoAccept a) a.onChatMessage(text);
        });

        // Диагностика в чат при входе — видно, что чит загрузился и какая клавиша открывает GUI
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.player.sendMessage(Text.literal("[KodClient] Чит загружен. Меню: RShift или R"), false);
        });

        // (совместимость: второе сообщение при входе не нужно)

        // Save config on disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> Config.get().save());
    }
}
