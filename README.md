# CosmeticSkins

A small Paper/Purpur plugin: `/cosmetic` opens a GUI with one dedicated slot
per item category (helmet, chestplate, leggings, boots, sword, pickaxe, axe,
shovel by default — fully configurable, see below). Drop a "skin token" item
into its matching slot and every matching item you own (in hand, inventory,
hotbar) instantly gets that skin's `CustomModelData` applied — enchantments,
durability, name, and lore are left completely untouched. Take the token
back out and the item reverts to normal.

## Requirements

- Java 21+ (Purpur 26.3 itself runs on Java 25, but the plugin's compiled
  bytecode only needs 21)
- Paper, Purpur, or Folia **1.21.x / 26.x** — built and tested against
  Purpur **26.3** (compiled against PaperMC's `26.3-pre-2.build.0-alpha`
  API jar; also runs fine on 26.2 and 1.21.x since Purpur just forks Paper
  and this plugin only touches the plain Bukkit surface)
- Maven, to build the jar

## Build

```bash
mvn clean package
```

The finished jar will be at `target/CosmeticSkins.jar`.

## Install

1. Copy `CosmeticSkins.jar` into your server's `plugins/` folder.
2. Start (or `/reload`, though a restart is safer) the server. This generates:
   - `plugins/CosmeticSkins/config.yml`
   - `plugins/CosmeticSkins/skins.yml` (comes with two example skins already filled in)
   - `plugins/CosmeticSkins/playerdata/` (created automatically per player)

## GUI categories

`config.yml`'s `categories` section defines the layout of the `/cosmetic`
menu — one slot per category:

```yaml
categories:
  helmet:
    display-name: "&bHelmet"
    slot: 1
    icon: DIAMOND_HELMET
  pickaxe:
    display-name: "&ePickaxe"
    slot: 11
    icon: DIAMOND_PICKAXE
```

- `slot` is the raw inventory slot index (0-53). 0-8 is row 1, 9-17 is row 2,
  and so on — the menu automatically grows to however many rows your highest
  configured slot needs, up to a full double chest.
- `icon` is just the placeholder shown on an empty slot; it doesn't restrict
  anything by itself.
- Every slot only accepts skin tokens whose `category` (see below) matches
  that category's id — drop a sword skin on the pickaxe slot and it's
  rejected.
- **Add your own categories freely** (a `hoe` category, a second `sword2`
  slot, a purely decorative rename, whatever) — just pick a unique id, a
  free slot, and give matching skins that same `category` id in `skins.yml`.
  No code changes needed. Run `/cosmetic reload` afterwards.

The default layout ships 8 categories: `helmet`, `chestplate`, `leggings`,
`boots` on row 1, and `sword`, `pickaxe`, `axe`, `shovel` on row 2, with the
rest of the grid filled by decorative border panes.

## Defining a skin

Edit `skins.yml`:

```yaml
skins:
  starwar_shovel:
    display-name: "&bStarwar Shovel Skin"
    custom-model-data: 100001
    category: shovel
    allowed-materials:
      - WOODEN_SHOVEL
      - STONE_SHOVEL
      - IRON_SHOVEL
      - GOLDEN_SHOVEL
      - DIAMOND_SHOVEL
      - NETHERITE_SHOVEL
```

- `custom-model-data` is just a number — it means nothing on its own. Your
  **resource pack** is what maps that number to an actual 3D model/texture
  (see below).
- `category` must match one of the ids under `config.yml`'s `categories`
  section — it's what slot this skin's token can be dropped into. A skin
  with an unknown or missing category is skipped on load (check the log).
- `allowed-materials` limits which item types this skin can attach to (and
  which items in a player's inventory get scanned/reskinned). Group every
  tier of the same tool (wood → netherite) together if you want one skin to
  apply regardless of the tool's material tier.

Run `/cosmetic reload` (needs `cosmeticskins.admin`) to pick up changes
without restarting.

## Giving players a skin token

```
/cosmetic give <player> <skinId> [amount]
```

This is the "Skin Token" item exactly like the one in your screenshot — it's
what you'd sell in your `/ah`, shop GUI, crate, etc. Whatever plugin sells
it just needs to hand out an item created this way (or you can generate the
item once with this command and duplicate it in your shop's config).

## Player flow

1. Player runs `/cosmetic` → opens the category GUI (helmet, chestplate,
   leggings, boots, sword, pickaxe, axe, shovel by default).
2. Player drags a skin token from their inventory onto that skin's category
   slot → token is consumed, skin is equipped, and every matching item they
   own reskins immediately. Dropping it on the wrong category's slot is
   rejected with a message instead.
3. Player clicks a filled slot with an empty cursor → the token comes back
   out, and every item that had that skin reverts to normal.

Equipped skins persist across restarts (`playerdata/<uuid>.yml`) and
automatically re-apply on join, on switching held item, on picking items up,
and after closing any other inventory (crafting table, chest, anvil, etc.) —
so a freshly crafted or looted shovel picks up the skin too, not just the one
you were already holding.

## You still need a resource pack

This plugin only *tags* items with a `CustomModelData` value — it does not
generate or host a resource pack. For the shovel to actually **look** like
your 3D model in-game, you need a resource pack (built in Blockbench or by
hand) with a custom model registered for that `custom-model-data` value on
the base item (e.g. `item/diamond_shovel` with a predicate/override for
`100001`), and the pack needs to be applied to your server (via
`server.properties`' `resource-pack` field, or another plugin that hosts one
for you).

If you already run **Oraxen** or **ItemsAdder** for other custom models, the
cleanest path is to build the shovel model there and just make sure the
`custom-model-data` number in `skins.yml` matches the one Oraxen/ItemsAdder
assigned to that model — this plugin doesn't need to know about Oraxen at
all, it just sets the same tag.

## Permissions

| Permission              | Default | Description                          |
| ------------------------ | ------- | ------------------------------------- |
| `cosmeticskins.use`      | true    | Open `/cosmetic`                      |
| `cosmeticskins.admin`    | op      | `/cosmetic give`, `/cosmetic reload`  |

## Notes / next steps

This is a working base you can extend:
- Each GUI slot is now bound to a category and only accepts matching skin
  tokens (see "GUI categories" above). Two skins in *different* categories
  can still both match the same material if you deliberately configure their
  `allowed-materials` to overlap (e.g. two "sword" skins) — normal "last
  equipped wins" behavior applies there, same as before.
- No economy/shop hook is included — wire `/cosmetic give` into whatever
  shop or crate plugin you use to actually sell the tokens.
- Built and smoke-tested end-to-end on a real Purpur 26.3 server (build 2635):
  the jar loads, enables, reads `skins.yml`, and shuts down cleanly with no
  errors. Purpur 26.3 is still an experimental/alpha line — several other
  third-party plugins (FastAsyncWorldEdit, WorldGuard, MMOItems,
  packetevents) currently fail to enable on it because they use internal NMS
  hooks that haven't been updated yet. CosmeticSkins is unaffected because it
  only uses the stable public Bukkit/Paper API.
