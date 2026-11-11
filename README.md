# KodClient — Fabric client for Minecraft 1.21.11

A Fabric mod (client-side) with Combat / Movement / Visuals / Player / Utilities / MISC modules,
a ClickGUI on **Right Shift**, a JSON config and a `/kod` client command.

## Requirements

- Minecraft **1.21.11**, Fabric Loader **0.18.4+**, Fabric API **0.141.3+1.21.11**
- Java **21**

## Install

1. Install Fabric Loader for 1.21.11 (fabricmc.net).
2. Put `kodclient-1.0.0-1.21.11.jar` into `.minecraft/mods/`.
3. Launch the game, press **RShift** to open the ClickGUI.

## Controls

| Action | Key |
|---|---|
| ClickGUI | Right Shift |
| AutoSwap (swap off-hand item) | bind in Options → Controls (default: unbound) |
| PotionThrower | bind in Options → Controls (default: unbound) |
| KillAura CPS − / + | `-` / `=` inside ClickGUI |

## Config

Saved automatically to `./config/kodclient.json` on every setting change and on disconnect.
Edit it manually or via ClickGUI / `/kod set <key> <value>`.

```json
{
  "booleans": { "KillAura": true, "AutoSprint": true, "watermark": true, "...": "..." },
  "numbers":  { "ka_cps": 6.0, "ka_range": 3.0, "gui_color": 16753985, "...": "..." },
  "strings":  { "autoswap_mode": "totem_orb", "gui_clan": "KodClient", "...": "..." }
}
```

## Commands (`/kod`)

- `/kod toggle <ModuleName>` — toggle a module
- `/kod set <key> <value>` — set any config value
- `/kod config export|save|reload`
- `/kod modules` — list all modules and their state

## Modules

### Combat
- **KillAura** — auto-aims + attacks nearest player. Includes spoof-time bypass:
  randomized CPS (`ka_min_cps`..`ka_max_cps`), ±35 ms jitter and occasional human-like pauses.
  The current target is highlighted by ESP + TargetHUD.
- **AutoSwap** — swaps off-hand with hotbar by key press. Modes:
  `totem_orb` (totem ⟷ orb), `orb_orb` (orb ⟷ orb), `totem_totem` (totem ⟷ totem).
- **AutoTotem** — moves a totem into the off-hand when HP < `autototem_hp`.
- **AimAssist** — while holding a trident, aims along the predicted ballistic
  trajectory (velocity lead) at the nearest player.

### Movement
- **AutoSprint** — keep sprint, sprint while blinded (`sprint_ignore_blindness`),
  optional stop on low hunger.

### Visuals
- **ESP** — through-wall boxes; KillAura target in accent color with name tag.
- **TargetHUD** — name / HP / distance of the KillAura target.
- **FullBright**, **NoHurtCam**, **Nametags**.

### Player
- **Interface** — watermark, keybinds list, server address (driven by the
  `watermark/keybinds/serverbinds/potions/cooldowns/staffs/target/inventory/notify` toggles).

### Utilities (Server & Utilities)
ServerHelper, PotionThrower, UseTracker, ItemScroller, NoPush (mixin), ClickAction —
all enabled by default.

### MISC
AutoRegister, AuctionHelper, AutoLeave, AutoTool, AutoEat, AutoAccept,
ChestStealer, InfoReplace, StreamerMod, NameProtect, DiscordRPC, DuelHelper,
AutoMessage, BassBoost — defaults are in `Config.java` exactly as requested.

## Build

```bash
./gradlew build        # or: gradle build (Gradle 8.14+, JDK 21)
```

Output: `build/libs/kodclient-1.0.0-1.21.11.jar`

> Note: features like DiscordRPC / BassBoost are stubs (toggle + config entry);
> they require additional libraries to be fully functional.
