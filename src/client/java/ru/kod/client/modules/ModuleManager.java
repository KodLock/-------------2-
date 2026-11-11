package ru.kod.client.modules;

import ru.kod.client.modules.combat.*;
import ru.kod.client.modules.misc.*;
import ru.kod.client.modules.movement.*;
import ru.kod.client.modules.player.*;
import ru.kod.client.modules.utilities.*;
import ru.kod.client.modules.visuals.*;
import java.util.*;

/** Registry of all modules, grouped by category (Combat / Movement / Visuals / Player / Misc / Utilities). */
public final class ModuleManager {
    private static final ModuleManager INSTANCE = new ModuleManager();
    public static ModuleManager get() { return INSTANCE; }

    private final List<Module> modules = new ArrayList<>();

    private ModuleManager() {
        // Combat
        modules.add(new KillAura());
        modules.add(new AutoSwap());
        modules.add(new AutoTotem());
        modules.add(new AimAssist());
        // Movement
        modules.add(new AutoSprint());
        // Visuals
        modules.add(new ESP());
        modules.add(new TargetHUD());
        modules.add(new FullBright());
        modules.add(new NoHurtCam());
        modules.add(new Nametags());
        // Player
        modules.add(new Interface());
        // Utilities (Server & Utilities)
        modules.add(new ServerHelper());
        modules.add(new PotionThrower());
        modules.add(new UseTracker());
        modules.add(new ItemScroller());
        modules.add(new NoPush());
        modules.add(new ClickAction());
        // MISC
        modules.add(new AutoRegister());
        modules.add(new AuctionHelper());
        modules.add(new AutoLeave());
        modules.add(new AutoTool());
        modules.add(new AutoEat());
        modules.add(new AutoAccept());
        modules.add(new ChestStealer());
        modules.add(new InfoReplace());
        modules.add(new StreamerMod());
        modules.add(new NameProtect());
        modules.add(new DiscordRPC());
        modules.add(new DuelHelper());
        modules.add(new AutoMessage());
        modules.add(new BassBoost());
    }

    public List<Module> all() { return Collections.unmodifiableList(modules); }

    public List<Module> byCategory(String category) {
        return modules.stream().filter(m -> m.getCategory().equals(category)).toList();
    }

    public List<String> categories() {
        return modules.stream().map(Module::getCategory).distinct().toList();
    }

    public Optional<Module> find(String name) {
        return modules.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst();
    }

    public void tickAll() { modules.stream().filter(Module::isEnabled).forEach(Module::onTick); }
    public void renderAll() { modules.stream().filter(Module::isEnabled).forEach(Module::onRender); }
}
