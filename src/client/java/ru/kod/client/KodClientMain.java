package ru.kod.client;

import net.minecraft.client.main.Main;

/**
 * KodClient — standalone client entry point.
 * "Версионка": запускает Minecraft 1.21.11 с уже встроенным читом,
 * отдельно Fabric Loader и Fabric API не нужны.
 *
 * Main-Class в MANIFEST: ru.kod.client.KodClientMain
 *
 * Пробрасывает аргументы в ванильный Main, а чит инициализируется
 * через ClientModInitializer (fabric.mod.json) при старте Knot/и игры.
 * Для запуска через чистый java -jar используйте обёртку KodClientBootstrap,
 * которая поднимает Knot с classpath из этого же jar'а.
 */
public final class KodClientMain {
    private KodClientMain() {}

    public static void main(String[] args) throws Exception {
        // Ванильный запуск Minecraft; чит подхватится через Fabric entrypoint.
        Main.main(args);
    }
}
