package ru.kod.client.config;

import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** JSON config: ./config/kodclient.json. Settings editable via ClickGUI, saved on change. */
public final class Config {
    private static final Path PATH = Path.of("config", "kodclient.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance;

    public static Config get() {
        if (instance == null) instance = new Config();
        return instance;
    }

    private final Map<String, Boolean> bools = new LinkedHashMap<>();
    private final Map<String, Double> numbers = new LinkedHashMap<>();
    private final Map<String, String> strings = new LinkedHashMap<>();

    public Config() {
        // Main menu toggles
        bool("watermark", true);  bool("keybinds", true);  bool("serverbinds", true);
        bool("potions", true);    bool("cooldowns", true); bool("staffs", true);
        bool("target", true);     bool("inventory", false); bool("notify", true);
        // GUI
        num("gui_color", 0xFFAA55); num("gui_scale", 1.0); str("gui_clan", "KodClient");
        // Server & Utilities
        bool("ServerHelper", true);  bool("PotionThrower", true);
        bool("UseTracker", true);    bool("ItemScroller", true);
        bool("NoPush", true);        bool("ClickAction", true);
        // MISC
        bool("AutoRegister", true);  bool("AuctionHelper", true);
        bool("AutoLeave", false);    bool("AutoTool", false);
        bool("AutoEat", false);      bool("AutoAccept", true);
        bool("ChestStealer", false); bool("InfoReplace", false);
        bool("StreamerMod", false);  bool("NameProtect", false);
        bool("DiscordRPC", false);   bool("DuelHelper", false);
        bool("AutoMessage", false);  bool("BassBoost", false);
        // Combat: KillAura
        bool("KillAura", false); num("ka_range", 3.0); num("ka_cps", 6.0);
        num("ka_min_cps", 4.0); num("ka_max_cps", 8.0); bool("ka_randomize", true);
        bool("ka_spoof_time", true); bool("ka_highlight", true);
        bool("ka_only_players", true); bool("ka_aim", true); num("ka_aim_smooth", 0.35);
        bool("ka_weapons_only", true);
        // Combat: AutoSwap / AutoTotem
        bool("AutoSwap", false); str("autoswap_mode", "totem_orb"); // totem_orb | orb_orb | totem_totem
        bool("AutoTotem", false); num("autototem_hp", 6.0);
        // Combat: AimAssist (trident)
        bool("AimAssist", false); num("assist_range", 32.0); num("assist_smooth", 0.2);
        bool("assist_only_trident", true);
        // Movement
        bool("AutoSprint", true); bool("sprint_keep", true); bool("sprint_ignore_blindness", true);
        bool("sprint_stop_on_hunger", true);
        // Visuals
        bool("ESP", false); bool("esp_players", true); num("esp_range", 64.0);
        bool("esp_boxes", true); bool("esp_tracers", false); bool("esp_chams", false);
        bool("TargetHUD", true); bool("FullBright", false); bool("NoHurtCam", false);
        bool("Nametags", false); bool("ChinHUD", true);
        // Player
        bool("Interface", true); bool("AutoRespawn", false); bool("FastPlace", false);
        // Misc settings
        num("cheststealer_delay_ms", 50.0); bool("cheststealer_loot", true);
        num("autoeat_hunger", 14.0); str("autoeat_food", "any");
        num("autoaccept_mode", 1.0); str("autoaccept_text", "/tpaccept");
        str("autoMessage_text", "gg"); num("automessage_delay", 3.0);
        num("autoleave_hp", 4.0); str("autoleave_cmd", "/hub");
        load();
    }

    public boolean getBool(String k) { return bools.getOrDefault(k, false); }
    public double getNum(String k)  { return numbers.getOrDefault(k, 0.0); }
    public String getStr(String k) { return strings.getOrDefault(k, ""); }
    public void setBool(String k, boolean v) { bools.put(k, v); save(); }
    public void setNum(String k, double v)   { numbers.put(k, v); save(); }
    public void setStr(String k, String v)   { strings.put(k, v); save(); }
    private void bool(String k, boolean v) { bools.put(k, v); }
    private void num(String k, double v)   { numbers.put(k, v); }
    private void str(String k, String v)   { strings.put(k, v); }

    public Map<String, Boolean> bools() { return bools; }
    public Map<String, Double> nums()   { return numbers; }
    public Map<String, String> strs()   { return strings; }

    public synchronized void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, exportJson());
        } catch (IOException ignored) {}
    }

    public synchronized void load() {
        try {
            if (!Files.exists(PATH)) { save(); return; }
            JsonObject root = JsonParser.parseString(Files.readString(PATH)).getAsJsonObject();
            read(root.getAsJsonObject("booleans"), (k, v) -> bools.put(k, v.getAsBoolean()));
            read(root.getAsJsonObject("numbers"),  (k, v) -> numbers.put(k, v.getAsDouble()));
            read(root.getAsJsonObject("strings"),  (k, v) -> strings.put(k, v.getAsString()));
        } catch (Exception ignored) {}
    }

    private static void read(JsonElement el, java.util.function.BiConsumer<String, JsonElement> fn) {
        if (el == null || !el.isJsonObject()) return;
        el.getAsJsonObject().entrySet().forEach(e -> fn.accept(e.getKey(), e.getValue()));
    }

    public String exportJson() {
        JsonObject root = new JsonObject();
        JsonObject b = new JsonObject(), n = new JsonObject(), s = new JsonObject();
        bools.forEach(b::addProperty); numbers.forEach(n::addProperty); strings.forEach(s::addProperty);
        root.add("booleans", b); root.add("numbers", n); root.add("strings", s);
        return GSON.toJson(root);
    }
}
