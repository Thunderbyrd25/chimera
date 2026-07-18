# Notes

Running log of API gotchas, version quirks, and things that surprised us during development.

## Environment (2026-07-13)

- Dev laptop had only a JRE 8 on it, no JDK. Installed Temurin JDK 21 via the zip distribution
  (not the MSI installer) into `~/.jdks/`, since the install shell had no admin rights to run
  the MSI (UAC can't be approved non-interactively). `JAVA_HOME` set at user level.
- Because the old JRE 8 is on the **system** PATH and Windows composes PATH as
  system-then-user, plain `java -version` in a terminal may still resolve to JRE 8 even with
  `JAVA_HOME` correctly set to JDK 21. Not a build blocker: `gradlew.bat` reads `JAVA_HOME`
  first, and IntelliJ's project SDK is set explicitly, independent of PATH. Worth remembering
  if `java -version` ever looks wrong during troubleshooting — check `JAVA_HOME` instead.

## Scaffold (Phase 1)

- NeoForge MDK for 1.21.1 (ModDevGradle variant) pulled from
  `github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle`. Pins `neo_version=21.1.235`,
  `minecraft_version=1.21.1`, ModDevGradle plugin `2.0.141`.
- Curios API for 1.21.1 is on the **9.5.x** line; latest at scaffold time was
  `9.5.1+1.21.1`, published to `https://maven.theillusivec4.top/` under
  `top.theillusivec4.curios:curios-neoforge`. Compile against the `:api` classifier only
  (`compileOnly`); put the full jar on `localRuntime` (a config the MDK template defines,
  not the built-in `runtimeOnly`) so we don't force a hard Curios dependency on anyone
  depending on Chimera. Confirmed this pattern against Curios's own getting-started docs.
- `neoforge.mods.toml` optional dependency: `type="optional"`, `ordering="AFTER"`. Not yet
  verified against a real Curios-present/absent run — do that in Phase 6 when the actual
  accessory integration is written, and verify `ModList.get().isLoaded("curios")` is still
  the correct gating call for 1.21.1 before relying on it.
- The menu registry key is `Registries.MENU`, **not** `Registries.MENU_TYPE` (the type
  parameter is `MenuType<?>`, but the registry constant itself is just `MENU`). Caught this
  by extracting `net/minecraft/core/registries/Registries.java` from
  `build/moddev/artifacts/neoforge-21.1.235-sources.jar` after a guessed name failed to
  compile — that jar is the fastest way to check a registry/class name for this version
  when in doubt, faster than searching docs.

## Machines (Phase 4)

- `Registries.MENU`/`MenuScreens.register` are documented as deprecated in favor of the
  event-based `net.neoforged.neoforge.client.event.RegisterMenuScreensEvent` - used that
  instead of the static registration shown in a lot of older tutorials.
- `MenuType.MenuSupplier` doesn't carry extra data (like the machine's BlockPos) to the
  client by itself. Implement the factory as `net.neoforged.neoforge.network.IContainerFactory`
  instead (it extends `MenuSupplier`), which receives a `RegistryFriendlyByteBuf`; pair with
  `Player#openMenu(MenuProvider, BlockPos)` (an `IPlayerExtension` default method) on the
  server, which automatically writes the `BlockPos` for you.
- Modeled the abstract machine base (`AbstractMachineBlockEntity`) on vanilla's
  `AbstractFurnaceBlockEntity` for the overall shape (progress via `ContainerData`,
  `saveAdditional`/`loadAdditional`), but used NeoForge's `ItemStackHandler` +
  `SlotItemHandler` for item storage instead of vanilla's `Container`/`WorldlyContainer` -
  simpler to reuse across three differently-shaped machines, and gets capability-based
  automation (hoppers etc.) via `RegisterCapabilitiesEvent`/`Capabilities.ItemHandler.BLOCK`
  for free instead of needing separate `WorldlyContainer` side logic.
- `Block#codec()` is **not** abstract on the base `Block` class (defaults to
  `simpleCodec(Block::new)`) - only `AbstractFurnaceBlock` re-declares it abstract because
  *it* has multiple concrete subclasses. A single concrete block subclass (like
  `GeneSequencerBlock`) doesn't need to override it at all.
- `BlockLootSubProvider#getKnownBlocks()` defaults to **`BuiltInRegistries.BLOCK`** - every
  block in the game, not just yours. Its `generate()` wrapper validates that every enabled
  block returned by `getKnownBlocks()` has a table in `this.map`, so without overriding it,
  `runData` fails with `Missing loottable 'minecraft:blocks/stone' for 'minecraft:stone'`
  (or whatever vanilla block sorts first) even though you never touched stone. Fix: override
  `getKnownBlocks()` to return only your own mod's blocks.
- The Gene Sequencer's fuel is consumed once per completed cycle rather than burning down
  independently like a vanilla furnace (no separate lit-time countdown). Simpler to reason
  about for v0.1; revisit if a more furnace-like "pre-lit, keeps burning without fuel present"
  feel is wanted later.
- No real art yet: block/GUI/item textures are flat-color placeholders generated via
  PowerShell `System.Drawing`, not hand-drawn. `ItemModelProvider`'s `ExistingFileHelper`
  validation (see Phase 2 notes) applies to block textures too via `cubeAll()`.
- Container screen background textures **must be authored on a 256x256 canvas** with the
  real content (176x166 for a standard single-row-of-slots dialog) drawn in the top-left
  corner. The 7-arg `GuiGraphics#blit(ResourceLocation, x, y, u, v, width, height)` overload
  hardcodes `textureWidth=256, textureHeight=256` for its UV math regardless of the actual
  file's dimensions (confirmed from source: it forwards to the 9-arg overload with `256, 256`
  baked in). Saving the PNG at the literal 176x166 target size instead makes the whole
  background (and the fixed-pixel slot coordinates from the Menu) render stretched/misaligned
  - slots and background no longer line up, even though the underlying inventory logic is
  completely unaffected (this is a pure rendering bug, not a game-logic one). Caught this from
  a screenshot showing item icons rendering offset from the drawn slot boxes.

## The loop (Phase 7)

- `RecipeBuilder#save(RecipeOutput, String id)` resolves the string via `ResourceLocation.parse(id)`,
  which defaults to the **`minecraft` namespace** when the string has no `:` in it - it does
  *not* infer the current mod's namespace from context. Used a plain `"nutrient_agar_from_slurry"`
  id for the alternate nutrient-agar recipe (needed since two recipes produce the same result
  item) and got `data/minecraft/recipe/nutrient_agar_from_slurry.json` instead of
  `data/chimera/...`. Fix: always pass the fully-qualified `"chimera:nutrient_agar_from_slurry"`.
- Splice Core's installed-trait storage was refactored from a single `INSTALLED_TRAIT`
  component to reusing the existing `TRAITS` list component (same one genomes and cassettes
  use) once Mk2/Mk3 needed more than one slot - avoided having two components doing
  effectively the same job. Known limitation, not fixed: installing the *same* trait twice in
  a Mk2/Mk3 core relies on the vanilla attribute system's own per-id dedup rather than
  anything explicit in our code; untested edge case.

## Player attachment, GUI item, Curios integration (Phase 6)

- `AttachmentType.Builder#copyOnDeath()` is the entire "handle the copy-on-death case
  explicitly" mechanism (architecture rule #4) - requires `.serialize(codec)` first, throws
  `IllegalStateException` otherwise. No manual `PlayerEvent.Clone` handling needed for the
  common case.
- Curios' recommended soft-dependency pattern is `CuriosApi.registerCurio(Item, ICurioItem)`,
  **not** `Item implements ICurioItem` directly - the latter would fail to resolve
  `ICurioItem`'s class at verification time the moment `SpliceCoreItem` is loaded, which
  happens unconditionally during item registration, before any mod-loaded check could run.
  Confirmed by extracting the actual `curios-neoforge-*-api.jar`, which ships full `.java`
  sources, not just compiled classes - much faster to check than searching docs.
- Curios slot registration is two datapack files plus a tag, not one:
  `data/chimera/curios/slots/splice_core.json` (`{"size": 1}`) defines the slot type;
  `data/chimera/curios/entities/player.json` (`{"entities": [...], "slots": [...]}`) assigns
  it to players; `data/curios/tags/item/splice_core.json` (note: **`curios` namespace, not
  `chimera`** - tags are a shared merge point keyed by the tag's own id) marks which items
  are valid for it. `SlotTypePreset`/IMC-based registration is deprecated in favor of this.
- Curios' `getAttributeModifiers()` callback means attribute-modifier traits (Bovine Vigor,
  the armor half of Thick Fleece) need **no manual add/remove/persistence code at all** -
  Curios re-derives the modifier from the equipped stack's own data every time it's needed,
  the same way vanilla item attribute modifiers work. This sidesteps the classic
  stacking/duplication failure mode by construction rather than by careful bookkeeping.
- Real bug caught by the headless server test, not compilation: called
  `ChimeraCuriosCompat.register()` (which does `ChimeraItems.SPLICE_CORE.get()`) directly in
  `ChimeraMod`'s constructor. `DeferredItem`s aren't bound until the registry-fill event runs
  *after* mod construction, so this threw `NullPointerException: Trying to access unbound
  value` at startup. Fix: defer the whole Curios-gated call to `FMLCommonSetupEvent`. The
  creative tab's own `ChimeraItems.X.get()` calls were fine because `.icon()`/`.displayItems()`
  take lazy suppliers that aren't invoked until well after registration.
- The Splice Core's insert/remove GUI is a **held-item** menu, not a block one: `MenuType`
  extra data is the `InteractionHand` (via `IContainerFactory` + `buf.writeEnum(hand)`/
  `player.openMenu(MenuProvider, Consumer<RegistryFriendlyByteBuf>)`) instead of a `BlockPos`.
  The transient 1-slot container is seeded from the core's `INSTALLED_TRAIT` component on
  open and written back via an `AbstractContainerMenu#broadcastChanges()` override - a single
  catch-all sync point, since `Slot#remove(int)` (used by vanilla's pickup/drag click handling)
  bypasses `Slot#setChanged()` entirely, so hooking individual Slot methods would miss cases.

## Sampling / datagen (Phase 2)

- `DataComponentType.Builder` method is `networkSynchronized` (past tense), not
  `networkSynchronizer` — easy to typo from memory.
- `DeferredRegister.createDataComponents(String modid)` is deprecated as of 1.21.1 in favor
  of `createDataComponents(ResourceKey<Registry<DataComponentType<?>>>, String)` — use the
  two-arg form with `Registries.DATA_COMPONENT_TYPE`.
- `ItemModelProvider`'s `basicItem`/`handheldItem` go through NeoForge's `ExistingFileHelper`,
  which does a **hard validation** at datagen time that the referenced texture PNG actually
  exists on disk — unlike a missing item model at runtime (Phase 1), which just logs a warning
  and renders the missing-texture checkerboard. `./gradlew runData` throws
  `IllegalArgumentException: Texture ... does not exist in any known resource pack` if the
  PNG isn't there yet. Generated flat-color 16x16 placeholders to unblock this; real art is
  still needed before ship.
- `net.neoforged.neoforge.data.event.GatherDataEvent` (not `...api.distmarker.GatherDataEvent`
  — a WebFetch summary of the docs got this package wrong). Verified against the sources jar.
  Register providers via `event.addProvider(provider)` guarded by `event.includeClient()` /
  `event.includeServer()`, or `event.createProvider(Provider::new)` for the common
  `(PackOutput)` / `(PackOutput, CompletableFuture<HolderLookup.Provider>)` constructor shapes.

## Gene registry / gene pools (Phase 3)

- Genes and gene pools are both plain `SimpleJsonResourceReloadListener`s, not formal
  `net.minecraft.core.Registry` datapack registries. A real datapack registry (via
  `DataPackRegistryEvent.NewRegistry`) was considered for genes since CLAUDE.md rule #1 says
  "genes should be a registry", but its folder path is
  `data/<pack_ns>/<registry_ns>/<registry_path>/` (from `CommonHooks.prefixNamespace`) - for
  our own registry key `chimera:genes` authored under our own pack namespace, that's the
  doubled `data/chimera/chimera/genes/*.json`. Went with the simpler reload-listener approach
  (flat `data/chimera/genes/*.json`) instead to avoid that surprise; "registry" here means
  "loaded from data, looked up by id," not the formal vanilla Registry type.
- Gene pool IDs are **not** the mob's own entity type id. `SimpleJsonResourceReloadListener`
  keeps the file's pack namespace (e.g. `chimera`, since we author under
  `data/chimera/gene_pools/...`) and only strips the reload folder prefix from the path, so
  `data/chimera/gene_pools/minecraft/cow.json` loads under the key `chimera:minecraft/cow`,
  not `minecraft:cow`. `GenePoolRegistry.get(EntityType)` reconstructs this key from the
  entity's own id so callers don't need to know the convention. Verified by extracting
  `FileToIdConverter.java` and reading `fileToId()` directly rather than assuming.
- Confirmed end-to-end via `./gradlew runServer` (headless dev server) rather than asking for
  a manual client check - `AddReloadListenerEvent`/datapack reload only fires when a
  world/server actually starts, not at the client main menu, but a full GUI client isn't
  needed to trigger it.

## Drawback system (v0.2, Milestone 1)

- A drawback is just a normal `GeneEffect` entry in a gene's existing `effects` list with
  `"drawback": true` set - not a separate top-level field. Since attribute-modifier effects are
  already derived fresh from the equipped stack every time (`ChimeraCuriosCompat`) and behavior
  effects are already read live off `PlayerGeneData` every tick (`PlayerGeneEffects`), a
  negative-amount drawback needed **zero** new application/removal code - only tooltip
  rendering (`TraitDisplay.effectDescriptionLines`) checks the flag, to color it red.
- No `drawback_scaling` field exists or is needed. Since scaling is already
  `base + perLevel * (starLevel - 1)`, the direction is just the **sign** of `per_level_amount`/
  `per_level_value` relative to the base amount: a per-level value with the *same* sign as the
  base makes the effect stronger (better upside or worse downside) at higher stars ("worsen");
  the *opposite* sign pulls it back toward zero at higher stars ("ease"). Bovine Vigor's new
  slowness drawback worsens (both negative); Hollow Bones' new frailty drawback eases (base
  -2.0, per-level +1.0, so a 3-star roll ends at -0.0 net penalty).
- `BehaviorGeneEffect` gained an optional `"description"` string for tooltips, since behaviors
  are opaque hardcoded Java (matched by `behavior_id`) with no way to generate a description
  from data alone. A literal `%s` in the text is replaced with the star-scaled value via plain
  `String#replace`, not `String.format` - avoids forcing gene authors to escape literal `%`
  characters in flavor text (e.g. "Reduces fall damage by half").
