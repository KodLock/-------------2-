package ru.kod.client.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;
import ru.kod.client.config.Config;
import ru.kod.client.modules.ModuleManager;

/** Client command: /kod <toggle|set|config|modules> — no server round-trip needed. */
public final class KodCommand {
    private KodCommand() {}

    public static void register() {
        ClientCommandManager.getActiveDispatcher().register(
            ClientCommandManager.literal("kod")
                .then(ClientCommandManager.literal("toggle")
                    .then(ClientCommandManager.argument("module", StringArgumentType.word())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "module");
                            var m = ModuleManager.get().find(name);
                            if (m.isPresent()) {
                                m.get().toggle();
                                ctx.getSource().sendFeedback(Text.literal(name + " -> " + m.get().isEnabled()));
                            } else {
                                ctx.getSource().sendError(Text.literal("Module not found: " + name));
                            }
                            return 1;
                        })))
                .then(ClientCommandManager.literal("set")
                    .then(ClientCommandManager.argument("key", StringArgumentType.word())
                        .then(ClientCommandManager.argument("value", StringArgumentType.string())
                            .executes(ctx -> {
                                String key = StringArgumentType.getString(ctx, "key");
                                String val = StringArgumentType.getString(ctx, "value");
                                Config c = Config.get();
                                if (c.bools().containsKey(key)) c.setBool(key, Boolean.parseBoolean(val));
                                else if (c.nums().containsKey(key)) c.setNum(key, Double.parseDouble(val));
                                else c.setStr(key, val);
                                ctx.getSource().sendFeedback(Text.literal(key + " = " + val));
                                return 1;
                            }))))
                .then(ClientCommandManager.literal("config")
                    .then(ClientCommandManager.literal("export")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(Text.literal(Config.get().exportJson()));
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("save")
                        .executes(ctx -> {
                            Config.get().save();
                            ctx.getSource().sendFeedback(Text.literal("Config saved"));
                            return 1;
                        }))
                    .then(ClientCommandManager.literal("reload")
                        .executes(ctx -> {
                            Config.get().load();
                            ctx.getSource().sendFeedback(Text.literal("Config reloaded"));
                            return 1;
                        })))
                .then(ClientCommandManager.literal("modules")
                    .executes(ctx -> {
                        StringBuilder sb = new StringBuilder();
                        for (var m : ModuleManager.get().all()) {
                            sb.append(m.getCategory()).append('/').append(m.getName())
                              .append('=').append(m.isEnabled()).append(' ');
                        }
                        ctx.getSource().sendFeedback(Text.literal(sb.toString()));
                        return 1;
                    }))
        );
    }
}
