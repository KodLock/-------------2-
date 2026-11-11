package ru.kod.client;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * KodClientBootstrap — точка входа версионки (Main-Class).
 *
 * Собирает classpath из этого же jar'а (в него упакованы Minecraft,
 * Fabric Loader, Fabric API и сам чит), извлекает нативные библиотеки
 * LWJGL в .kodclient/natives и запускает Minecraft через Knot.
 *
 * Использование: java -jar kodclient-1.0.0-1.21.11.jar
 * Аргументы Minecraft пробрасываются как есть.
 */
public final class KodClientBootstrap {
    private KodClientBootstrap() {}

    public static void main(String[] args) throws Exception {
        Path jar = locateSelfJar();
        Path workDir = Path.of(".kodclient");
        Path natives = workDir.resolve("natives");
        Files.createDirectories(natives);

        // 1) Извлечь нативные библиотеки (lwjgl и т.д.) из jar'а
        extractNatives(jar, natives);

        // 2) Собрать classpath: сам jar + natives
        List<URL> cp = new ArrayList<>();
        cp.add(jar.toUri().toURL());
        for (Path p : listFiles(natives)) {
            cp.add(p.toUri().toURL());
        }

        // 3) Запустить Knot -> Minecraft с читом
        ClassLoader cl = new URLClassLoader(cp.toArray(URL[]::new), ClassLoader.getSystemClassLoader());
        Class<?> knotClient = Class.forName("net.fabricmc.loader.impl.launch.knot.KnotClient", true, cl);
        var mainMethod = knotClient.getMethod("main", String[].class);
        System.out.println("[KodClient] Версионка запущена. Чит встроен — RShift открывает меню.");
        mainMethod.invoke(null, (Object) args);
    }

    private static Path locateSelfJar() throws Exception {
        URL loc = KodClientBootstrap.class.getProtectionDomain().getCodeSource().getLocation();
        Path p = Path.of(loc.toURI());
        if (!Files.isRegularFile(p)) {
            throw new IllegalStateException("KodClient должен запускаться как jar: java -jar kodclient-*.jar");
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

    private static List<Path> listFiles(Path dir) throws IOException {
        try (var s = Files.walk(dir)) {
            return s.filter(Files::isRegularFile).toList();
        }
    }
}
