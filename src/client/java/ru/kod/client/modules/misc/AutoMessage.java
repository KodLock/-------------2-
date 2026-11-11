package ru.kod.client.modules.misc;

import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/** AutoMessage: sends a configured message after death / kill (GG). */
public class AutoMessage extends Module {
    private long lastAt = 0;
    public AutoMessage() { super("AutoMessage", "MISC"); }

    /** Called when the player dies (from KodClient). */
    public void onDeath() {
        var mc = Util.mc();
        if (mc.player == null) return;
        if (System.currentTimeMillis() - lastAt < (long) (Config.get().getNum("automessage_delay") * 1000)) return;
        lastAt = System.currentTimeMillis();
        String text = Config.get().getStr("autoMessage_text");
        mc.player.networkHandler.sendChatMessage(text);
    }
}

