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
- Default convention going forward (post-user-feedback on the T2 batch): drawbacks should
  **ease toward 0 at 3-star** ("mastery") unless a trait has a specific narrative reason to be
  a power-gamble ("worsen", like Bovine Vigor - more bull, less human, the bigger you get the
  clumsier you get). "Ease to exactly 0 at 3-star" is the default shape: pick `base_value`/
  `per_level_value` (or the attribute equivalent) so `base + per_level * 2 == 0`.

## Tier 2 mob kits (v0.2, Milestone 2)

- **Wall climbing** (`arachnid_climb`): `LivingEntity#onClimbable()` isn't overridable for
  `Player` without Mixins, so this is reimplemented directly in the tick handler - if
  `player.horizontalCollision` is true and the player is airborne, set a small upward
  `deltaMovement.y` and call `resetFallDistance()`. No block-type check, unlike vanilla ladders -
  works against any wall, matching "spider climbs anything" flavor.
- **Sunlight weakness** (`undying_hunger_sunburn`): `Mob#isSunBurnTick()` is protected and
  `Mob`-only, but every piece of its logic is public on `Entity`/`Level`
  (`isInWaterRainOrBubble()`, `isInPowderSnow`/`wasInPowderSnow`, `getLightLevelDependentMagicValue()`,
  `Level#canSeeSky()`) - manually replicated in `GeneEffectHandlers.tryUndyingHungerSunburn`.
  `Entity#getLightLevelDependentMagicValue()` is `@Deprecated` but is exactly what vanilla's own
  check calls - there's no non-deprecated equivalent, the warning is expected and harmless.
- **Damage scaling** (bow bonus, magic vulnerability, pack bonus): `LivingIncomingDamageEvent`
  (pre-finalization, `setAmount()`) keyed on `DamageSource#is(DamageTypes.ARROW)`/
  `DamageTypes.MAGIC` - verified `PoisonMobEffect` itself calls `hurt(damageSources().magic(), 1.0F)`,
  so "poison vulnerability" is honestly "vulnerability to magic-type damage" (vanilla has no
  poison-specific damage type). `DamageSource#getEntity()` returns the *causing* entity (the
  shooter, for arrows) not the direct entity (the arrow itself) - `getDirectEntity()` is the arrow.
- **Post-hit mechanics** (lifesteal, poison-on-hit): `LivingDamageEvent.Post` (post-finalization,
  `getNewDamage()`) rather than the pre-finalization event, so they trigger off the real dealt
  damage, not the pre-armor amount.
- **Horse scraping gotcha (the big one):** `TissueScraperItem` originally relied purely on
  `Item#interactLivingEntity`, which works fine for most mobs (via `Player.interactOn`'s own
  fallback call when `Entity#interact()` returns PASS). It structurally *should* also work for
  horses - `AbstractHorse#mobInteract()` tries the held item first and only falls through to
  `doPlayerRide()` if the item didn't consume the action, confirmed by reading the decompiled
  source directly. In practice it didn't: right-clicking a horse with the (correct-tier) Apex
  Scraper never invoked our item at all and mounted the horse instead. Root cause (per NeoForge's
  own interaction docs): NeoForge patches the whole `Entity#interact()` + `Item#interactLivingEntity()`
  sequence behind a single cancelable `PlayerInteractEvent.EntityInteract`, and explicitly
  recommends hooking that event directly for reliable entity interception rather than depending
  on a given vanilla entity's own `mobInteract` override cooperating. **Fix:** added
  `TissueScraperEventHandler`, a `PlayerInteractEvent.EntityInteract` listener that tries the
  scraper itself and cancels the event outright on success, running before any vanilla
  per-entity logic (mounting, GUIs, etc.) gets a chance to fire. This is the correct general
  pattern for any future "reliably intercept right-click regardless of the target's own special
  behavior" need - don't rely on `Item#interactLivingEntity` alone for entities with nontrivial
  `mobInteract` overrides. Diagnosed by adding a temporary log line in the item's own method
  (proved it was never being called for horses despite 20+ seconds of confirmed right-clicks,
  while the exact same click worked instantly on a skeleton) before finding the real mechanism -
  don't skip straight to guessing when static tracing and empirical behavior disagree.

## The Hunt gate (v0.2, Milestone 3)

- The tier gate's "static per-tool-subclass `maxTier()` override" pattern (base=1, Reinforced=1,
  Apex=2) turned out to generalize cleanly to a "combat-unlocked" tier without any new mutable
  state. `ApexTissueScraperItem`'s real recipe is a plain standalone shapeless craft (2x Refined
  Culture + Diamond + Iron Ingot - it doesn't consume the Reinforced Scraper as an ingredient),
  so Tier 3 is just another scraper subclass (`PredatorTissueScraperItem`, `maxTier() = 3`)
  crafted the same standalone way from Combat Stimulant. No ItemStack-level Data Component, no
  threading `ItemStack` through `maxTier()` - worth remembering before reaching for mutable
  per-item state when a static class-hierarchy tier already exists and a work order says
  "mirror the existing pattern."
- `Mob#getTarget()` (public) is the live "is this mob currently aggro'd on X" check.
  `LivingEntity#getLastHurtByMob()`/`getLastHurtByMobTimestamp()` (public) track the last
  attacker, and - useful, easy to miss - **vanilla already ages this out itself**:
  `LivingEntity`'s own tick logic nulls `lastHurtByMob` once
  `tickCount - lastHurtByMobTimestamp > 100`. A "was this recently hurt by the player" check
  doesn't need its own timestamp/window bookkeeping - `getLastHurtByMob() == player` alone is
  already scoped to the last ~5 seconds. **Superseded** - the first pass used this aggro/low-HP
  check for Stress Plasma eligibility, but it was too permissive (most hostiles aggro without
  ever being attacked) and got replaced by the Potion of Stress marker below before commit. The
  API facts above are still true, just not what the shipped code uses.
- Bioreactor's recipe logic (`canProcess()`/`process()`) is hardcoded to one fixed 4-slot
  recipe, not a generic recipe-lookup system - there's no clean way to add a second recipe to it
  without restructuring. Since Apex Scraper already proves a "refined" item doesn't need a
  machine step at all, Combat Stimulant is a plain crafting-table recipe too - sidesteps the
  work order's "no new machine if possible" guardrail rather than negotiating around Bioreactor's
  fixed shape.
- **Custom MobEffect/Potion/brewing recipe (all new territory for this mod):**
  - `MobEffect`'s constructor (`MobEffect(MobEffectCategory, int color)`) is `protected` - it's
    meant to be subclassed, even for a pure marker effect with no attribute modifiers or tick
    behavior. `new MobEffect(category, color) {}` (empty anonymous subclass) is the minimal fix;
    a plain `new MobEffect(...)` doesn't compile.
  - `DeferredHolder<R, T extends R> implements Holder<R>` - pass the `DeferredHolder` itself
    (e.g. `ChimeraMobEffects.STRESSED`, no `.get()`) anywhere a `Holder<T>` is expected
    (`MobEffectInstance`'s constructor, `LivingEntity#hasEffect(Holder<MobEffect>)`). Calling
    `.get()` first gives the raw unwrapped object, which is the wrong type for those APIs.
  - Brewing recipes are **not** static `PotionBrewing.addMix(...)` calls anymore - register a
    `RegisterBrewingRecipesEvent` listener on `NeoForge.EVENT_BUS` and call
    `event.getBuilder().addMix(Holder<Potion> input, Item ingredient, Holder<Potion> output)`.
    Splash/Lingering variants need no separate registration - vanilla's own
    `PotionBrewing.bootstrap` already registers generic container-upgrade recipes
    (Potion+Gunpowder->Splash, Splash+Dragon's Breath->Lingering) that apply to every potion
    type automatically, ours included.
  - A custom potion's item display name is **not** a `chimera:`-namespaced key. `Potion.getName()`
    builds the key from the *container item's* own description id (`item.minecraft.potion`,
    `.splash_potion`, `.lingering_potion`) plus `.effect.<potion_registry_path>` - so a potion
    registered as `chimera:stress` needs lang entries at `item.minecraft.potion.effect.stress`
    (and the splash/lingering variants), not `item.chimera.stress`.
  - NeoForge Data Attachments are entity-agnostic, not player-specific, despite every existing
    use in this codebase being on `Player` - `IAttachmentHolder` is mixed into the base `Entity`
    class. The exact same `entity.getData(...)`/`.setData(...)` calls already used for
    `PlayerGeneData` work unchanged on any `LivingEntity` (used here for a per-mob scrape
    cooldown attached to the scraped mob itself, not the player).

## Biopedia + The Oath, Milestone 1 (player identity state)

- Identification (`GenomeAnalyzerBlockEntity.process()`) is entirely per-item today - it flips
  an `IDENTIFIED` data component and rolls `TRAITS` onto the item stack being processed, never
  touching the player. There is no player-scoped "discovered genes" state anywhere in the
  codebase before this milestone, despite the Biopedia spec assuming one exists to "reuse."
  Flagged to the user; folded a new `DISCOVERED_GENES` attachment into this milestone rather
  than deferring it, so the Biopedia milestone doesn't need a second attachment migration.
- **Nothing writes to `DISCOVERED_GENES` yet** - this milestone is schema only. The real design
  question for whichever milestone wires up the write side: `AbstractMachineBlockEntity` ticks
  with no player reference at all (machines process automatically regardless of whether anyone
  has the GUI open), so "the Analyzer finished" doesn't naturally answer "who gets credited with
  discovering this gene." Installing a trait into a Splice Core (a genuinely player-driven
  action with a real `Player` reference already in hand) is the leading candidate for the actual
  credit-attribution moment - not machine-tick completion.
- `AttachmentType.builder(...)` is overloaded on `Supplier<T>` vs. `Function<IAttachmentHolder,
  T>`, and a bare method reference for an immutable-empty-collection default (e.g. `Set::of`)
  is ambiguous between them - the compiler can't tell which shape you mean. Use an explicit
  lambda (`() -> Set.<T>of()`) to force the `Supplier` overload instead of a method reference.
