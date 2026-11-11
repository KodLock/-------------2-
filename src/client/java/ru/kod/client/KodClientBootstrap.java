package ru.kod.client;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
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

        // 2b) Извлечь nested jars (Fabric Loader, Fabric API) в mods/ —
        //     Knot ищет моды в --gameDir/mods, а не в fat jar'е.
        //     ВАЖНО: извлекаем ТОЛЬКО fabric-* и kodclient, НЕ ASM/guava/gson —
        //     иначе Knot сочтёт их "non-fabric mods" и упадёт на ClassTweaker.
        Path modsDir = workDir.resolve("mods");
        Files.createDirectories(modsDir);
        extractNestedJars(jar, modsDir);

        // 3) Диагностика: что видит Knot
        System.out.println("[KodClient] Версионка запущена. Чит встроен — RShift открывает меню.");
        System.out.println("[KodClient] java.class.path = " + System.getProperty("java.class.path"));
        System.out.println("[KodClient] fabric-loader:inputGameJar = " + System.getProperty("fabric-loader:inputGameJar"));

        // 4) Fabric Loader и Fabric API лежат в META-INF/jars/ внутри fat jar'а
        //    (nested jar format). Knot сам распаковывает их при старте,
        //    если запустить его из system classloader'а — что мы и делаем.
        //    Отдельно извлекаем только game.jar (Minecraft) для inputGameJar.
        //    ВАЖНО: передаём --version 1.21.11, чтобы Knot знал, какую версию
        //    игры искать (без этого ClassTweaker падает: "Namespace (intermediary)
        //    does not match current runtime namespace (official)").
        Path gameJar = workDir.resolve("game.jar");
        extractGameJar(jar, gameJar);

        List<String> knotArgs = new ArrayList<>(List.of(args));
        if (knotArgs.stream().noneMatch(a -> a.equals("--gameDir"))) {
            knotArgs.add("--gameDir");
            knotArgs.add(workDir.toAbsolutePath().toString());
        }
        if (knotArgs.stream().noneMatch(a -> a.equals("--version"))) {
            knotArgs.add("--version");
            knotArgs.add("1.21.11");
        }
        System.setProperty("fabric-loader:inputGameJar", gameJar.toAbsolutePath().toString());
        String cp = System.getProperty("java.class.path", "");
        System.setProperty("java.class.path", cp + File.pathSeparator + gameJar.toAbsolutePath());
        if (knotArgs.stream().noneMatch(a -> a.equals("fabric-loader:inputGameJar"))) {
            knotArgs.add("fabric-loader:inputGameJar");
            knotArgs.add(gameJar.toAbsolutePath().toString());
        }
        try {
            Class<?> knotClient = Class.forName("net.fabricmc.loader.impl.launch.knot.KnotClient");
            var mainMethod = knotClient.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) knotArgs.toArray(String[]::new));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            if (cause instanceof Exception ex) throw ex;
            if (cause instanceof Error err) throw err;
            throw new RuntimeException(cause);
        } catch (ClassNotFoundException e) {
            System.err.println("[KodClient] Ошибка: Fabric Loader не найден в jar'е. Пересоберите fatClient.");
            throw e;
        }
    }

    /** Извлекает все nested jars (META-INF/jars/*.jar) в mods/ — Knot ищет моды там. */
    private static void extractNestedJars(Path fatJar, Path modsDir) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(fatJar))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                String name = e.getName();
                if (!name.startsWith("META-INF/jars/") || !name.endsWith(".jar")) continue;
                String fileName = name.substring("META-INF/jars/".length());
                // Только Fabric-моды и наш чит. ASM/guava/gson/mapping-io и т.д. —
                // это библиотеки, Knot сочтёт их "non-fabric mods" и упадёт.
                if (!fileName.startsWith("fabric") && !fileName.startsWith("kodclient")
                    && !fileName.equals("fabric-loader-0.18.4.jar")) continue;
                Path out = modsDir.resolve(fileName);
                if (Files.exists(out) && Files.size(out) > 0) continue;
                Files.copy(zin, out, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        System.out.println("[KodClient] Моды извлечены в " + modsDir);
    }

    /** Извлекает один nested jar (META-INF/jars/<prefix>*.jar) из fat jar'а. */
    private static void extractNestedJar(Path fatJar, String prefix, Path out) throws IOException {
        if (Files.exists(out) && Files.size(out) > 100_000) return;
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(fatJar))) {
            ZipEntry e;
            while ((e = zin.getNextEntry()) != null) {
                if (e.getName().startsWith(prefix) && e.getName().endsWith(".jar")) {
                    Files.copy(zin, out, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }
        throw new IOException("Не найден nested jar с префиксом " + prefix + " в " + fatJar);
    }

    /** Извлекает Minecraft-классы из fat jar'а в отдельный game.jar для Knot. */
    private static void extractGameJar(Path fatJar, Path gameJar) throws IOException {
        if (Files.exists(gameJar) && Files.size(gameJar) > 1_000_000) {
            System.out.println("[KodClient] game.jar уже есть (" + Files.size(gameJar) + " байт), пропускаю.");
            return;
        }
        System.out.println("[KodClient] Извлекаю Minecraft в game.jar...");
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(fatJar));
             ZipOutputStream zout = new ZipOutputStream(Files.newOutputStream(gameJar))) {
            ZipEntry e;
            int count = 0;
            while ((e = zin.getNextEntry()) != null) {
                String name = e.getName();
                // В game.jar кладём только Minecraft (net/minecraft/**, com/mojang/**,
                // version.json, assets/, data/) — без Fabric и без чита.
                boolean isMc = name.startsWith("net/minecraft/")
                    || name.startsWith("com/mojang/")
                    || name.equals("version.json")
                    || name.startsWith("assets/")
                    || name.startsWith("data/");
                if (!isMc) continue;
                zout.putNextEntry(new ZipEntry(name));
                zout.write(zin.readAllBytes());
                zout.closeEntry();
                count++;
            }
            System.out.println("[KodClient] game.jar собран: " + count + " записей, " + Files.size(gameJar) + " байт.");
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
                // LWJGL нативки: META-INF/<platform>/... (linux/windows/macos) — извлекаем
                // в natives/<platform>/... и прописываем в java.library.path
                String rel = null;
                if (name.startsWith("META-INF/linux/") || name.startsWith("META-INF/windows/")
                    || name.startsWith("META-INF/macos/")) {
                    rel = name.substring("META-INF/".length());
                } else if (name.startsWith("natives/")) {
                    rel = name.substring("natives/".length());
                }
                if (rel == null || rel.isEmpty() || name.endsWith("/")) continue;
                Path out = natives.resolve(rel).normalize();
                if (!out.startsWith(natives)) continue;
                Files.createDirectories(out.getParent());
                Files.copy(zin, out, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
