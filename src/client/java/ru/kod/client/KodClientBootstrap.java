package ru.kod.client;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * KodClientBootstrap — точка входа версионки (Main-Class).
 *
 * Запускает Minecraft через Knot (Fabric Loader) в ТОМ ЖЕ JVM и classloader'е,
 * что и сам бутстрап: jar'а достаточно на classpath (он уже там, т.к. мы запущены
 * через java -jar). Отдельный URLClassLoader НЕ создаём — он ломал загрузку
 * fabric.mod.json и entrypoint'ов, из-за чего чит не инициализировался и
 * RShift не работал.
 *
 * Использование: java -jar kodclient-*-versionka.jar
 * Аргументы Minecraft пробрасываются как есть.
 */
public final class KodClientBootstrap {
    private KodClientBootstrap() {}

    public static void main(String[] args) throws Exception {
        Path jar = locateSelfJar();
        Path workDir = Path.of(".kodclient");
        Path natives = workDir.resolve("natives");
        Files.createDirectories(natives);

        // 1) Извлечь нативные библиотеки (LWJGL и т.д.) рядом с jar'ом
        extractNatives(jar, natives);

        // 2) Прописать natives в java.library.path, чтобы LWJGL нашёл .dll/.so
        String oldLibPath = System.getProperty("java.library.path", "");
        System.setProperty("java.library.path", natives.toAbsolutePath() + File.pathSeparator + oldLibPath);

        // 3) Запустить Knot в ЭТОМ classloader'е — Fabric увидит kodclient как мод
        System.out.println("[KodClient] Версионка запущена. Чит встроен — RShift открывает меню.");
        try {
            Class<?> knotClient = Class.forName("net.fabricmc.loader.impl.launch.knot.KnotClient");
            var mainMethod = knotClient.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        } catch (ClassNotFoundException e) {
            System.err.println("[KodClient] Ошибка: Fabric Loader не найден в jar'е. Пересоберите fatClient.");
            throw e;
        }
    }

    private static Path locateSelfJar() throws Exception {
        URL loc = KodClientBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        Path p = Path.of(loc.toURI());
        if (!Files.isRegularFile(p)) {
            throw new IllegalStateException("KodClient должен запускаться как jar: java -jar kodclient-*-versionka.jar");
        }
        return p;
    }

    private static void extractNatives(Path jar, Path natives) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(jar))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                String name = e.getName();
                if (!name.startsWith("natives/") || name.endsWith("/")) continue;
                Path out = natives.resolve(name.substring("natives/".length())).normalize();
                if (!out.startsWith(natives)) continue;
                Files.createDirectories(out.getParent());
                Files.copy(zin, out, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
