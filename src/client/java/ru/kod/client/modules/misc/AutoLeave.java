package ru.kod.client.modules.misc;

import net.minecraft.text.Text;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/** AutoLeave: runs a leave command when HP falls below the threshold. */
public class AutoLeave extends Module {
    public AutoLeave() { super("AutoLeave", "MISC"); }

    @Override public void onTick() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null) return;
        double hp = p.getHealth() + p.getAbsorptionAmount();
        if (hp > Config.get().getNum("autoleave_hp")) return;
        String cmd = Config.get().getStr("autoleave_cmd");
        p.networkHandler.sendChatMessage(cmd.startsWith("/") ? cmd.substring(1) : cmd);
        p.sendMessage(Text.literal("[KodClient] AutoLeave (" + hp + " HP)"), false);
        setEnabled(false);
    }
}

