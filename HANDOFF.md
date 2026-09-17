# HANDOFF

Running log of what's been done and what's left, so a new session can pick
up without re-deriving context. Newest entry on top.

---

## 2026-09-17 — Add Purpur 26.3 support, first real build+test, first release

**Done:**
- Bumped `pom.xml`'s `paper-api` dependency from `1.21.4-R0.1-SNAPSHOT` to
  `26.3-pre-2.build.0-alpha` (the current `<latest>` in PaperMC's metadata —
  no stable 26.3 build exists yet). Compiling against it requires JDK 25
  (its class files are version 69); `maven.compiler.target` stays at 21.
- Fixed a real bug found along the way: `pom.xml` had no resource filtering,
  so `plugin.yml`'s `${project.version}` was never substituted — the plugin
  reported itself as `CosmeticSkins v${project.version}` in every server
  log. Added `<resources><resource><filtering>true</filtering></resource></resources>`.
- Bumped project version `1.0.0` → `1.1.0` for this compatibility update.
- Found a local Maven install to build with (no system `mvn`):
  `C:\Users\ACER\Desktop\Project\GUIShop\apache-maven-3.9.9`.
- Updated `README.md`'s requirements/notes sections to reflect 26.3 support
  and the real test results (see below).
- **Upgraded the SMP test server** at
  `C:\Users\ACER\Desktop\Project\Survival SMP Purpur 26.2 test\Survival SMP Purpur 26.2 test`
  from Purpur 26.2 (build 2632) to **Purpur 26.3 (build 2635)**. Old jar
  backed up as `purpur-26.2.jar.bak` in that same folder — revert with
  `mv purpur-26.2.jar.bak purpur.jar` if 26.3 ever needs to be rolled back
  for the rest of the SMP.
- Deployed the built jar to that server's `plugins/CosmeticSkins.jar` and
  ran two real boot tests (see `CLAUDE.md` for the exact command — anonymous
  pipe feeding a delayed `stop`, **not** `mkfifo`, which broke the console
  reader on the first attempt and killed the server almost immediately).
  Second run confirmed clean behavior:
  ```
  [CosmeticSkins] Loading server plugin CosmeticSkins v1.1.0
  [CosmeticSkins] Enabling CosmeticSkins v1.1.0
  [CosmeticSkins] Loaded 2 cosmetic skin(s).
  [CosmeticSkins] CosmeticSkins enabled.
  ...
  [CosmeticSkins] Disabling CosmeticSkins v1.1.0
  ```
  No exceptions attributable to CosmeticSkins. (Several *other* plugins on
  that server — FastAsyncWorldEdit, WorldGuard, MMOItems, packetevents,
  zMenu — currently fail to enable on 26.3 due to their own internal NMS
  hooks; unrelated to this plugin and not something to fix here.)
- Initialized git in this directory, created the public GitHub repo
  `xGRAFEW/CosmeticSkins`, pushed the initial commit, and cut release
  `v1.1.0` with `CosmeticSkins.jar` attached.

**Not done / next steps:**
- No real in-game playtest yet (a human player dragging a skin token onto
  the GUI slot and confirming the reskin + revert). The console boot test
  only proves the plugin loads/enables without crashing — it doesn't
  exercise the GUI/inventory logic at all. Do this before relying on it in
  production.
- Purpur 26.3's `paper-api` is still alpha/experimental — no code changes
  were needed this round since the plugin only uses long-stable Bukkit API,
  but re-check `CLAUDE.md`'s "Paper API version pinning" section once a
  `-stable` 26.3 build exists, and bump the pom dependency then.
- The SMP test server's other broken plugins (FAWE, WorldGuard, MMOItems,
  packetevents, zMenu) are still broken on 26.3 — not this plugin's problem,
  but worth knowing if anyone asks why the *server* still shows errors.
