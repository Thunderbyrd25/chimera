# Chimera

A biopunk genetics mod for NeoForge. Harvest DNA from mobs, sequence it, isolate traits,
equip them via a Curios accessory for permanent-feeling player buffs.

## Target stack (non-negotiable — do not upgrade/switch without asking)

- Minecraft **1.21.1**
- **NeoForge** (`neo_version=21.1.235`, ModDevGradle plugin)
- **JDK 21**
- **Curios API** `9.5.x` line, as a **soft dependency**
- Mod id: `chimera`. Package: `com.chimera`

## Architecture rules

1. **Data-driven gene pools.** Mob → trait mapping lives in JSON datapack files
   (`data/chimera/gene_pools/<namespace>/<mob>.json`), never in Java. Genes are a registry
   loaded from data. Adding a mob = add a JSON file, touch zero Java.
2. **Data Components, not NBT.** `sequenced_genome`, `tissue_sample`, `gene_cassette` all use
   custom registered `DataComponentType`s (Minecraft 1.20.5+ system). Do not reach for
   `CompoundTag`/`ItemStack#getTag()` — that's pre-1.20.5 and won't compile here.
3. **Everything registers through `DeferredRegister`** — items, blocks, block entities, menus,
   creative tabs, data component types, attachment types. See `ChimeraMod.java`.
4. **Player data via NeoForge Data Attachments**, `persistent = true`. Must survive death
   (explicit copy-on-death handling), dimension change, and relog — required test, not optional.
5. **Curios is a soft dependency.** Gate all Curios integration behind a mod-loaded check
   (`ModList.get().isLoaded("curios")` — verify this API when Phase 6 implements it). The mod
   must load and run fine with Curios absent.
6. **Future-proofing hooks — add now, leave unused, do not build systems around them:**
   - `requiresAnima` boolean on gene JSON schema (v0.1: always `false`)
   - `inert` boolean on gene cassette's data component (v0.1: always `false`)
   - `corruption` float on the player data attachment, persisted, unread
7. **Datagen** for recipes, item models, block states, loot tables, lang entries — not
   hand-written JSON, where NeoForge datagen supports it.
8. **One abstract machine base class** (BlockEntity: item handler, tick progress, menu sync,
   save/load). Gene Sequencer / Genome Analyzer / Gene Extractor are configuration on top of it,
   not three near-copies.

## Explicit non-goals for v0.1

No souls, Anima, rituals, altars, corruption mechanics, mythical creatures, gestation vats,
chimera synthesis, DNA splicing/recombination, mob tiers beyond tier 1, hostile-mob sampling,
permanent gene integration, RF/FE energy, or a lore/guide book. If a milestone seems to need
one of these, stop and ask before building it.

## Working agreement

- **Verify APIs, never invent them.** Check `docs.neoforged.net` (1.21.1 docs), the decompiled
  NeoForge/Minecraft sources in the Gradle dependency cache, or the actual source — not memory.
  Training data is full of pre-1.20.5 NBT patterns and pre-NeoForge Forge patterns; assume any
  familiar-looking API call is suspect until checked.
- **Small increments, always compiling.** Build + launch after every milestone.
- **Commit after every green build.**
- Read this file and `NOTES.md` at the start of every session.

## Project layout

- `src/main/java/com/chimera/` — mod source
- `src/main/resources/assets/chimera/` — client assets (lang, models, textures)
- `src/main/resources/data/chimera/` — datapack data (gene pools, recipes, loot tables)
- `src/main/templates/META-INF/neoforge.mods.toml` — mod metadata template (Gradle-expanded)

## Running

`./gradlew runClient` launches a dev-mode Minecraft client directly — no separate Minecraft
install or launcher needed. `./gradlew runData` runs datagen.
