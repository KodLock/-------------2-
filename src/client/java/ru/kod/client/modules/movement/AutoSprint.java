package ru.kod.client.modules.movement;

import net.minecraft.entity.effect.StatusEffects;
import ru.kod.client.config.Config;
import ru.kod.client.modules.Module;
import ru.kod.client.util.Util;

/**
 * AutoSprint: keeps sprinting.
 * - keep_sprint: doesn't stop sprint on hit
 * - ignore_blindness: sprint even while blinded
 * - stop_on_hunger: pauses below 7 hunger (vanilla sprint limit)
 */
public class AutoSprint extends Module {
    public AutoSprint() { super("AutoSprint", "Movement"); }

    @Override public void onTick() {
        var mc = Util.mc();
        var p = mc.player;
        if (p == null || mc.options == null) return;
        Config c = Config.get();

        boolean blinded = p.hasStatusEffect(StatusEffects.BLINDNESS);
        if (blinded && !c.getBool("sprint_ignore_blindness")) {
            p.setSprinting(false);
            return;
        }
        if (c.getBool("sprint_stop_on_hunger") && p.getHungerManager().getFoodLevel() <= 6) {
            p.setSprinting(false);
            return;
        }
        boolean moving = mc.options.forwardKey.isPressed()
            || mc.options.backKey.isPressed() || mc.options.leftKey.isPressed()
            || mc.options.rightKey.isPressed();
        if (moving) p.setSprinting(true);
    }
}
