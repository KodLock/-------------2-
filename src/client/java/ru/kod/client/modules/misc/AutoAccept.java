package ru.kod.client.modules.misc;

import net.minecraft.text.Text;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/** AutoAccept: auto-replies to /tpa-type requests detected in chat. */
public class AutoAccept extends Module {
    private long lastAt = 0;
    public AutoAccept() { super("AutoAccept", "MISC"); }

    /** Called from KodClient chat listener. */
    public void onChatMessage(String msg) {
        var mc = Util.mc();
        if (mc.player == null) return;
        String lower = msg.toLowerCase();
        if (lower.contains("tpa") || lower.contains("teleport request") || lower.contains("запрос на телепортацию")) {
            if (System.currentTimeMillis() - lastAt < 5000) return;
            lastAt = System.currentTimeMillis();
            String cmd = Config.get().getStr("autoaccept_text");
            mc.player.networkHandler.sendChatMessage(cmd.startsWith("/") ? cmd.substring(1) : cmd);
            mc.player.sendMessage(Text.literal("[KodClient] AutoAccept: " + cmd), false);
        }
    }
}

