# CLAUDE.md

Guidance for future Claude Code sessions working on this repo.

## What this is

`CosmeticSkins` — a Paper/Purpur Minecraft plugin. `/cosmetic` opens a GUI
where dropping a "skin token" into a slot tags every matching item a player
owns with a `CustomModelData` value (stats/enchants/durability untouched).
See `README.md` for the full player-facing feature description.

## Build

No system-wide Maven is installed on this machine. Use the local copy that
already exists under another project:

```bash
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
export PATH="/c/Users/ACER/Desktop/Project/GUIShop/apache-maven-3.9.9/bin:$JAVA_HOME/bin:$PATH"
cd "C:\Users\ACER\Desktop\Project\CosmeticSkins"
mvn clean package
```

- **Must build with JDK 25** (`JAVA_HOME` above). The `paper-api` snapshot for
  Purpur 26.3 ships class files compiled with Java 25 (class file version
  69); compiling/resolving it under JDK 21 fails with
  `class file has wrong version 69.0, should be 65.0`.
- `maven.compiler.target` in `pom.xml` is intentionally kept at **21**, not
  25 — the plugin's own bytecode only needs to run on the JVM, not match the
  API jar's compile version, and staying at 21 keeps it loadable on slightly
  older Paper/Purpur builds too.
- The finished jar is `target/CosmeticSkins-${version}.jar` (e.g.
  `target/CosmeticSkins-1.2.2.jar`) — `finalName` in `pom.xml` embeds the
  version so each build produces a distinct filename, making it easy to keep
  old jars around for rollback instead of silently overwriting a generic
  `CosmeticSkins.jar`.
- `pom.xml`'s `<resources>` block has `filtering=true` so `${project.version}`
  in `plugin.yml` gets substituted at build time. If you ever see
  `CosmeticSkins v${project.version}` in a server's startup log, the filtering
  config got lost/reverted — check `pom.xml`'s `<build><resources>` block.

## Paper API version pinning

`pom.xml` depends on `io.papermc.paper:paper-api:26.3-pre-2.build.0-alpha`.
Purpur 26.3 has no *stable* published `paper-api` yet (as of 2026-09-17,
PaperMC's maven-metadata.xml still lists the whole 26.3 line as `-alpha`
builds); `26.3-pre-2.build.0-alpha` was the `<latest>`/`<release>` tag at
that time and is the closest thing to "current" for the 26.3 series. When
Purpur/Paper eventually stamp a `-stable` 26.3 build, bump this dependency
to that version instead of the alpha snapshot.

To check what's currently available:
```bash
curl -s https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml
```

The plugin only touches the plain, long-stable Bukkit surface (ItemStack,
ItemMeta, Inventory events, PersistentDataContainer, etc.) — nothing
Purpur-specific — so it isn't expected to need real code changes as the
26.3 API stabilizes, just a pom.xml version bump + rebuild + retest.

## Always test on the real SMP test server

Per standing instruction, **always** test this plugin against the real
Survival SMP test server, not a throwaway instance:

```
C:\Users\ACER\Desktop\Project\Survival SMP Purpur 26.2 test\Survival SMP Purpur 26.2 test
```

(Folder name says "26.2" but the server was upgraded to **Purpur 26.3**
build 2635 on 2026-09-17 as part of adding 26.3 support here — see
`HANDOFF.md`. The old 26.2 jar is kept as `purpur-26.2.jar.bak` in that same
folder in case a revert is ever needed: `mv purpur-26.2.jar.bak purpur.jar`.)

This server already has ~30 other plugins installed (CMI, LuckPerms,
WorldGuard, MMOItems, FastAsyncWorldEdit, etc.) — some of those currently
fail to enable on the 26.3 alpha line because they use internal NMS hooks
that haven't been updated for it yet. That's expected and unrelated to
CosmeticSkins; only watch the `[CosmeticSkins]` log lines.

To deploy a freshly built jar for testing, remove any older `CosmeticSkins-*.jar`
from the server's `plugins` folder first — Bukkit will try to load every jar it
finds, and two jars registering the same plugin name causes a duplicate-plugin
enable failure — then copy in the new versioned jar:
```bash
rm -f "C:\Users\ACER\Desktop\Project\Survival SMP Purpur 26.2 test\Survival SMP Purpur 26.2 test\plugins\CosmeticSkins-"*.jar
cp "C:\Users\ACER\Desktop\Project\CosmeticSkins\target\CosmeticSkins-${version}.jar" \
   "C:\Users\ACER\Desktop\Project\Survival SMP Purpur 26.2 test\Survival SMP Purpur 26.2 test\plugins\"
```

To smoke-test a startup/shutdown from the CLI without a real player (checks
the plugin loads/enables/reads config with no exceptions), run from the
server folder with JDK 25, piping a delayed `stop` in over stdin so the
process shuts down on its own — **do not use a named pipe/`mkfifo`** for
this on Windows; it breaks the console's stdin reader
(`Failed to read console input: IOException: The handle is invalid`) and
the server dies mid-boot. A plain anonymous shell pipe works fine:

```bash
( sleep 90; echo stop ) | "/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot/bin/java.exe" \
  -Xms2G -Xmx2G --add-modules=jdk.incubator.vector -XX:+UseG1GC \
  -jar purpur.jar --nogui > server_test_console.log 2>&1
```

Then check the log for `[CosmeticSkins] Enabling`, `Loaded N cosmetic
skin(s)`, and `CosmeticSkins enabled.` with nothing between those lines
looking like a stack trace. A full in-game playtest (dragging a token onto
a GUI slot, confirming the item reskins) still needs a real client connect
and hasn't been automated — do it manually when possible.

## Release / publish

- GitHub: https://github.com/xGRAFEW/CosmeticSkins (public repo under the
  `xGRAFEW` account — `gh` is already authenticated as that account on this
  machine).
- Releases are tagged `vX.Y.Z` matching `pom.xml`'s `<version>`, with
  `target/CosmeticSkins.jar` attached as the release asset.
- Bump `<version>` in `pom.xml` before cutting a new release (it flows into
  both the jar's `plugin.yml` and the git tag).
