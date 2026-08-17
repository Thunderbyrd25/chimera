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

## Biopedia + The Oath, Milestone 2 (taking the Oath)

- **A client-only class reference anywhere in a common item's bytecode crashes dedicated-server
  loading outright - not just when the referencing branch executes.** `TheOathItem.use()`
  originally opened `net.minecraft.client.gui.screens.ConfirmScreen` directly inside an
  `if (level.isClientSide)` guard, on the (wrong) assumption that JVM bytecode verification is
  lazy per-branch, so the server-side path would never need to resolve `Screen`/`ConfirmScreen`.
  `./gradlew runServer` crashed immediately at mod registration time -
  `RuntimeDistCleaner: Attempted to load class net/minecraft/client/gui/screens/Screen for
  invalid dist DEDICATED_SERVER`. NeoForge's `RuntimeDistCleaner` verifies a class's *entire*
  bytecode against dist restrictions the moment that class is loaded for any reason (here,
  simply constructing `TheOathItem` during item registration, which happens on both sides) -
  runtime `isClientSide` guards don't help, because the class fails to load before any branch
  ever executes.
  **Fix (the general pattern for "server-agnostic item triggers a client screen"):** the
  common-sided class must have *zero* client-only symbols in its bytecode, full stop. Bridge via
  a plain custom event (`OpenOathPromptEvent extends net.neoforged.bus.api.Event`, posted on
  `NeoForge.EVENT_BUS` - no client types in its own signature either) from the common item, with
  the actual `Minecraft`/`ConfirmScreen` code living entirely inside `ChimeraModClient`
  (`@Mod(dist = Dist.CLIENT)`) as the event's only listener - that class is never loaded on a
  dedicated server at all, by construction, so it's the one safe place for those references.
  This session's own `./gradlew runServer` habit (a genuine dedicated-server classpath, not just
  the integrated server inside `runClient`) caught this immediately - worth remembering as the
  reason that smoke-test step exists, not just gene-pool-loading sanity.
- `net.minecraft.client.gui.screens.ConfirmScreen` (vanilla) is a ready-made yes/no dialog -
  `BooleanConsumer` callback, title `Component`, message `Component`, optional custom
  yes/no button label components. No custom `Screen` subclass needed for a simple confirm
  prompt.

## Biopedia + The Oath, Milestone 3 (the Biopedia)

- **Book-GUI choice (logged per the spec's own instruction): reused vanilla
  `net.minecraft.client.gui.screens.inventory.BookViewScreen`** rather than writing a custom
  `Screen`. It takes a `BookAccess(List<Component>)` record - a bare data holder, not tied to
  actual written-book NBT - so a fully dynamic catalog just means building one `Component` per
  page and handing them to `new BookViewScreen(new BookViewScreen.BookAccess(pages))`. Comes
  with page-turn buttons/sounds and the classic book texture for free. No reason to hand-roll
  pagination for a read-only catalog.
- **The `RuntimeDistCleaner` lesson from Milestone 2 generalizes past `if`-branches to lambdas
  too.** `ChimeraPayloads.register()` is a *common* method (called on both sides via
  `modEventBus.addListener(ChimeraPayloads::register)`), so its `playToClient` handler lambda
  for `OpenBiopediaPayload` must not reference `Minecraft`/`BookViewScreen` either - Java lambdas
  compile to synthetic methods *inside* the enclosing class, so a client-only reference there is
  exactly as dangerous as one in a plain method body. Same fix, one hop further: the handler
  posts a plain `OpenBiopediaScreenEvent` (no client types in its signature) on
  `NeoForge.EVENT_BUS`; only `ChimeraModClient` listens to it and actually opens the screen.
- **Resolved the discovery-write-hook question flagged in M1 and deferred again in M2.**
  `AbstractMachineBlockEntity` still has no player reference during tick-based processing, so
  the two write-hook sites are both in *Menus* instead, where a real `Player` always exists:
  - Normal path: `SpliceCoreMenu.syncCassettesToCore()` (already "a single catch-all
    interception point" per its own comment) - every installed cassette's traits get marked
    discovered on every `broadcastChanges()`. Idempotent (`DISCOVERED_GENES` is a `Set`), so no
    "is this actually new" diffing needed.
  - Diligent study boon: `Slot#onTake(Player, ItemStack)` overridden on `GenomeAnalyzerMenu`'s
    output slot (mirrors how `SpliceCoreMenu`'s cassette slots already override `mayPlace`) -
    discovers a trait the moment it's taken from the Analyzer, without needing to also splice it
    in. `Slot#onTake` is public, non-final, and already called by every `quickMoveStack` in this
    codebase at the end of a successful transfer - the right general hook for "a player took X
    out of this specific slot," not just a `GenomeAnalyzerMenu`-specific trick.
- **`BuiltInRegistries.ENTITY_TYPE` being a `DefaultedRegistry` (see Milestone 2's "Pig" bug)
  applies just as much when reverse-indexing gene pools for the catalog's mob list** -
  `containsKey()` before `get()` again, same fix, different call site (`TheBiopediaItem`,
  `ChimeraModClient`'s page builder).
- `Slot#onTake` and the whole discovery/catalog design assume a `Player` is always reachable at
  the actual moment of "using" a trait (splicing or studying) - deliberately never tried to
  solve this from inside a `BlockEntity#tick()`, where there is no such guarantee.
- **Found during hands-on verification: `quickMoveStack`'s own `slot.onTake(player, slotStack)`
  call (present in every machine `Menu` in this codebase, inherited from the same template) was
  passing the wrong `ItemStack`.** By the time that line runs, `slotStack` has already been
  drained to empty by the preceding `moveItemStackTo(slotStack, ...)` call - data components
  included, not just count. `result` (the `.copy()` taken *before* the move) is the one that
  still has the real data. This went unnoticed for every existing `quickMoveStack` in the mod
  because nothing previously overrode `onTake` in a way that read the taken stack's data - only
  the diligent-study hook (this milestone) exposed it: normal single-item clicks worked (vanilla's
  own `doClick` doesn't have this bug), shift-click silently never discovered anything. Fixed at
  the one call site that matters (`GenomeAnalyzerMenu`), but worth checking `slot.onTake(player,
  slotStack)` specifically (not `result`) if any *other* menu ever grows an `onTake` override
  that needs the taken item's actual data.
- **Star-scaling direction correction:** Bovine Vigor's slowness drawback was the one
  deliberate "worsen" exception from Milestone 1 of the tier-2 work order (a power-gamble
  framing, confirmed with the user at the time). Reversed per explicit new instruction: drawbacks
  should ease at higher star by default, full stop - Bovine Vigor was the only remaining "worsen"
  trait in the mod, so this removes the exception rather than adding another one.
- **Pagination needed real font measurement, and even that needed a second pass.** An initial
  attempt summed each paragraph's `Font#split(text, width).size()` separately against a
  13-line/114px budget (matching `BookViewScreen`'s own protected `TEXT_WIDTH`/`TEXT_HEIGHT`
  constants) - in practice one page came back visibly underfull while the very next page still
  overflowed and got clipped. Fixed by (a) measuring the *actual combined candidate page* each
  time a paragraph is tentatively added, rather than summing independent estimates, and (b)
  backing off to a deliberately conservative 100px/10-line budget instead of trying to match
  `BookViewScreen`'s real numbers exactly. Also split the long inert-notice paragraph into
  individual sentences rather than one atomic block, so pagination has room to pack partial
  content instead of being forced to move the whole notice to a fresh page.

## Byproduct Economy, Milestone 1 (the item set + both acquisition points)

- **Two byproduct tiers, discovered mid-design rather than built from scratch.** Initial research
  mistook `GeneSequencerBlockEntity`'s existing `rollByproduct()` (a weighted roll among Mutagen/
  Chromatin Strand/Cell Culture/Nucleotide Slurry, biased by `yieldBias()`) for plain fuel/
  intermediate stock. It's actually the *generic* (mob-agnostic) byproduct axis, half-built
  already. *Specific* (mob-unique) items were the actual net-new piece this milestone - 12 items,
  one per mob with a real gene kit, added via a new optional `specific_byproduct` field on
  `GenePool` (mirrors the existing `tier` field's codec pattern exactly).
- **Both tiers now come from both acquisition points, per explicit user correction to the first
  draft** (which only wired scraping). Sequencing (`GeneSequencerBlockEntity`) is the *reliable*
  source - both its byproduct slots roll every cycle, same guarantee the generic slot already
  had. Scraping (`TissueScraperItem`) is the *opportunistic* source - both rolls gated by
  `bonusSampleChance()`, the same tier-escalating chance already used for bonus Tissue Samples,
  which means the base scraper (0% bonus chance) yields no byproducts from scraping at all, only
  Reinforced/Apex/Predator do. This required zero changes to the three tier subclasses - they
  only override `bonusSampleChance()`/`maxTier()`, and this reuses `bonusSampleChance()` as-is.
- **Extracted `rollByproduct()`'s weighted table into a shared `gene/ByproductRoller`** so both
  `GeneSequencerBlockEntity` and `TissueScraperItem` call the same logic instead of duplicating
  it - the sequencer passes its real `yieldBias()`, the scraper (a hand tool, no upgrade concept)
  passes `0`.
- **New GUI slot placement had a real collision to avoid.** The upgrade rail column sits at
  `x=152`, with up to 3 slots stacked at `y=8/26/44` (`MAX_UPGRADE_SLOTS=3`), covering `y=7-61`
  continuously - any new slot within ~18px of `x=152` collides with the rail at *any* y in that
  band, not just where a rail slot happens to be, since the boxes are edge-to-edge. The original
  placement (`140,47`, next to the existing byproduct slot) collided.
- **Final layout, per your feedback after the first hands-on pass: both byproduct slots side by
  side, centered as a pair under the output slot** (`116,17`, visual center `x=124`) rather than
  stacked - `x=107`/`x=125` at `y=47`. Centering the pair meant the generic slot could no longer
  stay at its original baked-in-texture position (`116,47`), so both slots are now drawn
  dynamically (`MachineScreenUtil.drawSlotBox`, same treatment the fuel slot already got) instead
  of relying on the background PNG - the old baked-in box was erased from
  `gene_sequencer.png` (painted over with the background's own flat grey) to match. Worth
  remembering for any future machine GUI edit: `GuiGraphics#fill`'s bottom-right bound turned out
  to be inclusive in practice, not exclusive - erasing a slot's fill region at its "expected"
  `x-1..x+17` bounds left a 1px border remnant until the erase rectangle was widened by one on
  the right/bottom edge.
- **The 12 new item textures reuse `cell_culture.png`'s exact silhouette/shading mask** (a simple
  2-tone organic blob, extracted pixel-by-pixel via a PowerShell+GDI+ script), remapped to a
  different light/dark color pair per item. Placeholder tier, consistent with the rest of the
  mod's procedural art - shape variety wasn't worth the effort at this stage.
- **Byproduct rates dialed down per user feedback after the first hands-on pass** - sequencing's
  rolls were guaranteed every cycle and scraping reused `bonusSampleChance()` directly (55% for
  Predator on both generic and specific, ~80% chance of at least one per scrape), which read as
  "byproducts basically every time." Sequencing (`GeneSequencerBlockEntity`) now gates both rolls
  behind their own flat chances - `GENERIC_BYPRODUCT_CHANCE = 0.5F`, `SPECIFIC_BYPRODUCT_CHANCE =
  0.35F` - independent of `yieldBias()`, which still only affects *which* generic item you get,
  not whether you get one. Scraping (`TissueScraperItem`) keeps scaling off `bonusSampleChance()`
  (so the base scraper still yields nothing) but through a new `BYPRODUCT_CHANCE_MULTIPLIER =
  0.4F` applied on top, rather than reusing that rate directly - decoupled instead of just
  lowering `bonusSampleChance()` itself, so the bonus-Tissue-Sample rate stays untouched. Predator
  now sits at 22% per roll (55% x 0.4) instead of 55%.

## Byproduct Economy, Milestone 2a (material synthesis - the Synthesizer)

- **A crafted-potion draft for the passive-byproduct sink was rejected as too generic** - per
  user feedback, replaced with "synthesizing resources" (their own framing): the 5 domesticated-
  mob byproducts (Marrow Extract/Adipose Reserve/Keratin Down/Lanolin Concentrate/Tendon Fiber)
  turn into real vanilla materials instead, "the science route to farming." Goat/Fox/Wolf's
  byproducts are deferred to a separate Milestone 2b as Curios trinkets/tools - a genuinely
  different mechanic (equippable passives vs. a machine), not worth cramming into one milestone.
- **Chance-based synthesis needed a real machine, not a crafting recipe** - the user's own
  example (synthesizing a cow sometimes gives Leather, sometimes Beef) can't be expressed as a
  plain vanilla recipe (fixed output only). New machine, the **Synthesizer** - seventh machine,
  built on the same `AbstractMachineBlockEntity` base as the other six, fueled by Biomass (no new
  fuel type - confirmed directly with the user rather than assumed, since a bespoke fuel would
  have undone the "one universal fuel" rule from the v0.2 work order).
- **Two-stage nugget-style balancing, per explicit user request** - the Synthesizer doesn't
  output the real material directly; it rolls a "Scrap" item (`leather_scrap`/`beef_scrap`/
  `pork_scrap`/`chicken_scrap`/`mutton_scrap`), and a *separate* ordinary crafting recipe
  assembles 4 Scraps into the real thing. Already-granular vanilla items (Feather, String) skip
  the Scrap tier entirely and come straight out of the machine - gating those further would just
  be busywork, not balance.
- **`GenePool` extended a second time** (see Milestone 1's `specificByproduct`) with
  `synthesisOutputs` (a new nested `SynthesisOutput(item, weight)` record) and
  `rollSynthesisOutput()`, a cumulative-weight pick mirroring the existing `rollStarLevel` logic
  almost exactly - reusing an established pattern rather than inventing a new one. Defaults to an
  empty list (`optionalFieldOf("synthesis_outputs", List.of())`), so the 8 mobs without one
  (goat/fox/wolf, all hostiles, enderman) need no JSON change at all.
- **The Synthesizer resolves "which mob does this byproduct belong to" by reverse-indexing
  `GenePoolRegistry.getAll()`**, comparing each pool's `specificByproduct()` against the input
  stack's registry id - the same reverse-lookup shape `TheBiopediaItem` already uses for "which
  mobs carry gene X" (Biopedia Milestone 3). No new registry needed.
- **Known, accepted edge case**: `canProcess()` pre-checks room using only the *first* of a
  pool's synthesis candidates as a representative probe (the real output isn't known until the
  roll in `process()`). For the three 2-candidate pools (cow/chicken/sheep), if the output slot
  already holds a full/mismatched stack of the *other* candidate, the pre-check can pass but the
  roll then silently fails to insert - input and fuel are still spent that cycle. Not engineered
  around; matches this codebase's own existing stance on bonus-roll misses
  (`GeneSequencerBlockEntity`'s byproduct comment says the same thing).
- **Confirmed Gene Extractor's own "No fuel." comment is stale documentation, not current
  behavior** - every one of the six existing machines requires Biomass via the shared
  `AbstractMachineBlockEntity.hasFuel()`/`tick()` gate (confirmed by reading its Menu, which does
  wire a real fuel slot bound to `getFuelInventory()`), and the v0.2 changelog already says so
  explicitly ("universal fuel across all six machines"). The comment just never got updated when
  Biomass became universal - harmless, but worth remembering if that file is touched again.
- **Recipe category note**: used `RecipeCategory.FOOD` for the Beef/Porkchop/Chicken/Mutton
  assembly recipes and `RecipeCategory.MISC` for Leather - no prior example of `FOOD` in this
  mod's own recipe provider, but it's a standard vanilla category, used the same way vanilla's
  own food recipes do.
- **Found during hands-on verification: shift-clicking Biomass (or a byproduct) from the player
  inventory did nothing.** `SynthesizerMenu.quickMoveStack` was written from the fueled-menu
  template but the item-type routing branches (the `slotStack.is(...)` checks every other machine
  menu uses to route a shift-clicked item into its correct machine slot, not just shuffle it
  within the player's own inventory/hotbar) were dropped in the process. Fixed by adding both
  back: Biomass routes to the fuel slot, and any item `SynthesizerBlockEntity.findSynthesisPool()`
  recognizes routes to `SLOT_INPUT` - reusing that reverse-lookup instead of duplicating an item
  list, so a future byproduct only needs a `synthesis_outputs` JSON entry to also shift-click
  correctly, no Menu change required. Worth a mental checklist item for any *future* new machine
  menu: don't drop the per-item routing branches when adapting an existing menu as a template.

## Byproduct Economy, Milestone 2b (trinkets & tools - goat/fox/wolf)

- **Reworked mid-design, per user feedback: an initial "wild-wolf lure" tool idea was called too
  niche.** Replaced with Adrenaline Draught, a bonemeal-style tool usable on *any* vanilla animal
  (`AgeableMob#setAge(0)` to instantly grow a baby, `Animal#setInLove(player)` to instantly ready
  an adult for breeding) - genuinely broad rather than tied to one mob, closer to what "byproduct
  economy" should feel like at this tier.
- **Verified the real Curios and vanilla jars directly (decompiled from the Gradle cache) before
  designing the trinkets**, rather than assuming API shapes. This paid off: vanilla 1.21.1
  already has `Attributes.FALL_DAMAGE_MULTIPLIER` (default 1.0) and `Attributes.ATTACK_KNOCKBACK`
  alongside the already-known `Attributes.KNOCKBACK_RESISTANCE`. That meant the Vestibular
  Charm's fall-immunity effect could be a **plain attribute modifier** (`ADD_VALUE, -1.0`,
  canceling the default to 0) instead of the originally-planned `LivingFallEvent` listener
  querying Curios equipped-state - simpler, and consistent with the mod's own existing preference
  (`GeneEffectHandlers.java`'s own comment: attribute-modifier Curios effects need nothing beyond
  `getAttributeModifiers`). Confirmed `ICuriosHelper.findFirstCurio(LivingEntity, Item)` as the
  fallback API shape too, in case a future trinket ever needs a live equipped-check instead of a
  static attribute.
- **Two new `ICurioItem`s in `ChimeraCuriosCompat` are meaningfully simpler than `SpliceCoreCurio`**
  - fixed, non-stack-dependent modifiers, so no `onEquip`/`onUnequip` bookkeeping at all, just
  `getAttributeModifiers` returning constants.
- **New Curios slot per trinket** (not a shared slot) - `data/chimera/curios/slots/*.json` (size
  1 each) + an entry in `data/chimera/curios/entities/player.json`'s `"slots"` array + a
  `data/curios/tags/item/*.json` tag, mirroring Splice Core's own 3-file pattern exactly. Kept
  separate (not combined into one shared "charm" slot) so wearing one never competes with the
  other, and so a player can't stack two of the same charm for a doubled effect.
- **Trinket textures reuse `splice_core.png`'s own medallion mask** (dark ring + glowing core),
  recoloring only the core per charm - visually signals "this is a Curios accessory," consistent
  with the mod's other equippable. Adrenaline Draught reuses `mutagen.png`'s vial mask instead,
  recolored to a bright yellow-green liquid (distinct from Mutagen's own toxic green).
- **Found during hands-on verification: Adrenaline Draught didn't actually behave like
  "reusable bone meal" - it was a single-use consumable (`stack.shrink(1)`), consumed same as
  the item it was modeled after conceptually, but the user's original ask was for the item
  itself to be reusable, not just its *effect* to be bonemeal-like.** Fixed by switching it to a
  durability tool exactly like `TissueScraperItem` - `Item.Properties().durability(32)` +
  `stack.hurtAndBreak(1, player, slot)` instead of `shrink`/a plain `Properties()`. Worth
  remembering: "bonemeal-style effect" and "reusable like a tool" are two separate design axes -
  this mod already had a durability-tool precedent (the scrapers) that should have been the
  template from the start rather than a consumable.
- **Second correction on the same item: "bonemeal-style" meant real bonemeal's actual effect
  (instant crop/sapling growth on blocks), not just an animal-growth analogy.** Added a
  `useOn(UseOnContext)` override that reuses vanilla's own `BoneMealItem.growCrop(ItemStack,
  Level, BlockPos)` directly rather than reimplementing `BonemealableBlock`'s
  `isValidBonemealTarget`/`isBonemealSuccess`/`performBonemeal` orchestration - the animal-growth
  `interactLivingEntity` path from the first pass stays too, as this item's own extension beyond
  what real bonemeal does. `growCrop` is `@Deprecated` in vanilla (common for many Mojang-mapped
  helpers, not a real warning to act on) - left a comment explaining why it's still the right
  call, so a future pass doesn't "fix" it into a reimplementation of the same logic.
- **Adrenaline Draught's recipe now also requires Ossein Powder** (skeleton's hostile byproduct,
  Milestone 3's own material), per explicit user direction that a sink item doesn't need to stay
  confined to its own milestone's mob set - bone powder in a growth tool is arguably more
  literally "bone meal" than the wolf-only original anyway.

## Byproduct Economy, Milestone 3a (the Necrotic Venom Blade)

- **Poison has no vanilla "who caused this" tracking at all - confirmed by decompiling, not
  assumed.** NeoForge's poison-tick `DamageSource` (`NeoForgeMod.POISON_DAMAGE`) is built with
  `new DamageSource(type)` - no entity argument - and `MobEffectInstance` itself carries no
  cause/source field. `GeneEffectHandlers.onLivingDamagePost` already proves the event shape
  (zombie's `undying_hunger_lifesteal` heals the attacker from real post-armor damage in the same
  handler that applies cave spider's `venom_glands` Poison), but that pattern reads
  `source.getEntity()`, which is always `null` for a poison tick specifically - a lifesteal-from-
  poison-tick mechanic genuinely can't be built that way. Fixed with a new entity Data Attachment,
  `ChimeraAttachments.POISON_BLADE_ATTACKER` (`UUID`, default `Util.NIL_UUID`), mirroring
  `LAST_SCRAPED_TIME`'s exact shape - set on the target when `NecroticVenomBladeItem.hurtEnemy`
  poisons it, read back in the new `combat/NecroticVenomHandler`'s own `onLivingDamagePost` (a
  separate handler class, not folded into `GeneEffectHandlers` - this is real combat gear, not a
  spliced gene effect, a genuinely different category worth its own home).
- **New `combat` package** - first non-gene, non-machine gameplay system in the mod. Registered
  in `ChimeraMod.java` the same way `GeneEffectHandlers` already is
  (`NeoForge.EVENT_BUS.register(new NecroticVenomHandler())`), no new pattern needed.
- **Blade texture reuses `tissue_scraper.png`'s own diagonal tool mask** (blade + wooden handle),
  recolored to a venom-tinted steel tone - same reuse convention as every prior milestone's
  textures.
- **Confirmed emergent quirk, kept intentionally per explicit user call**: `POISON_BLADE_ATTACKER`
  never expires or clears on its own - once a mob has been hit by the blade, *any* later poison
  on it (a splash potion, a different player's blade) still heals whoever's UUID is currently
  tagged, even after the blade's own 80-tick Poison would have worn off. Flagged during hands-on
  verification and deliberately left as-is ("could be cool to have") rather than adding an expiry
  tick to the attachment - reads as a fun perk of the weapon (mark a target, benefit from
  whatever poisons it afterward) rather than a bug worth closing.

## Byproduct Economy, Milestone 3b (the Web Slinger - the mod's first custom entity)

- **You explicitly chose the bigger build over the safer one.** The original recommendation was
  an instant raycast grapple (no new entity, far less risk) - you picked a real thrown hook with
  travel time and a multi-tick reel instead, so this is genuinely the mod's first custom
  `Entity`/`EntityType`, first custom renderer registration, and first system living outside the
  gene/machine/Curios architecture entirely.
- **Nothing here was guessed - every API was decompiled and read directly** (both the renamed
  bytecode jar and, for real method *bodies* rather than just signatures, a `decompile_*` output
  jar also present in the Gradle cache under `neoformruntime/intermediate_results`, which turned
  out to hold genuine readable `.java` sources, not just javap signatures - worth remembering
  next time a body-level check is needed, not just a signature check).
- **The key insight that shaped the whole design**: `Entity#push(x, y, z)` (confirmed by reading
  `Entity.java` directly) does `setDeltaMovement(getDeltaMovement().add(...))` *and* sets
  `hasImpulse = true` - and `ServerEntity.sendChanges()` broadcasts a impulse-triggered motion
  packet to every tracking client, **including the affected entity's own controlling
  connection**. This is the same mechanism vanilla knockback already uses to shove a player from
  server code. It means the multi-tick "reel" (pulling the owner toward a stuck point, or a
  hooked entity toward the owner) needed **zero custom networking** - just calling `.push(...)`
  from the hook's own `tick()` once per tick while stuck.
- **`EntityType.Builder.build(String)` takes the real registry name, not `null`** - this differs
  from `BlockEntityType.Builder.build(Type<?>)`'s nullable-DFU-type parameter, which looks
  superficially similar but isn't the same method shape at all. Caught this by reading the real
  source rather than assuming the block-entity pattern would transfer directly.
- **`WebHookEntity extends ThrowableItemProjectile`** specifically (not the more general
  `ThrowableProjectile`) purely to get `ThrownItemRenderer` for free via already implementing
  `ItemSupplier` - the hook's `getDefaultItem()`/`getItem()` just returns the Web Slinger tool's
  own icon, reusing an existing texture rather than authoring a dedicated "hook" visual for a
  first pass.
- **Overriding `tick()` to branch on a `stuck` state (block pos or hooked entity set) was the
  cleanest way to separate "still flying" from "now reeling"** - `ThrowableProjectile.tick()`
  itself already runs collision detection *and* continues to apply gravity/movement in the same
  call even after a hit is detected, so `onHitBlock`/`onHitEntity` additionally zero the
  velocity immediately to prevent a one-tick overshoot past the impact point, and the very next
  tick skips calling `super.tick()` at all once stuck, switching entirely to the custom reel
  logic instead.
- **`ChimeraEntityTypes.java` is a new top-level registry class**, mirroring
  `ChimeraAttachments`/`ChimeraMenus`'s own `DeferredRegister.create(...)` shape rather than the
  `.createBlocks()`/`.createItems()` convenience helpers - no entity-specific convenience helper
  exists in NeoForge's `DeferredRegister`.
- **Found during hands-on verification, via diagnostic logging: pulling a mob worked, pulling
  the *player* did nothing at all** - the log showed the owner's velocity growing every single
  tick exactly as expected, but `owner.position()` never changed across 50+ ticks. Root cause,
  confirmed by reading `ServerEntity.sendChanges()` directly rather than re-guessing: `push()`
  (via `hasImpulse`) only makes that method `broadcast` the motion packet to *observing* clients
  - for a mob, that's irrelevant since the server directly drives its position anyway, so it
  visibly moves regardless. For the entity's own *controlling* player, the client only receives
  the updated velocity if `Entity#hurtMarked` is separately set, which triggers a distinct
  `broadcastAndSend` call at the very end of that same method - the earlier research summary
  ("push() broadcasts to every tracking client including the entity's own controlling
  connection") turned out to be an incomplete read of that method, missing this exact
  broadcast-vs-broadcastAndSend distinction. Fixed by setting `hurtMarked = true` right after
  every `push()` call in `WebHookEntity`, on both the owner and the hooked-entity branch (only
  strictly required for the player case, but harmless either way, so applied uniformly rather
  than special-casing by entity type).
- **Second bug in the same feature, found right after the first was fixed: the hook itself kept
  visibly falling after impact.** Root cause: `onHitBlock`/`onHitEntity` only set the "stuck"
  fields when `!level().isClientSide` - but `tick()` runs independently on *both* the server and
  the client's own local copy of the entity. The client-side copy never learned it was stuck, so
  it kept calling `super.tick()` (full flight/gravity simulation) forever, re-detecting the same
  block collision every tick without ever freezing - a client-side physics loop invisible to the
  server, which really was frozen correctly the whole time. Fixed by letting *both* sides set the
  stuck fields and zero velocity in `onHitBlock`/`onHitEntity` (purely a local, cosmetic
  freeze on the client - harmless to compute twice), while gating the actual pull side effects
  (`push`/`hurtMarked`/`discard`) inside `reelTick()` itself to server-only, since only the
  server is authoritative for moving other entities. General lesson for any future entity work
  in this mod: "stuck"/state-machine flags on a networked entity need to make sense when set
  independently on each side, not just once from the server's perspective.
- **String visual + left-click retract, added after the core mechanic was confirmed working.**
  No shared "line between two points" helper exists in vanilla - both the fishing line
  (`FishingHookRenderer`) and the leash line (`EntityRenderer#renderLeash`, gated behind real
  `Leashable` semantics) hand-roll their own vertex-buffer code. Ported `FishingHookRenderer`'s
  approach directly (confirmed by reading its full source, not guessed) into a new
  `entity/WebHookRenderer.java`, which delegates the actual icon rendering to a held
  `ThrownItemRenderer` instance (keeps the exact prior look) and additionally draws a
  quadratic-sag string from the firing player's hand to the hook via the same `stringVertex`
  math, `RenderType.lineStrip()`.
- **Left-click retract needed the same client-input-to-server-payload shape `GRASS_FED_KEY`
  already uses**, not a new pattern - confirmed by reading that whole chain first
  (`GrassFedUsePayload`, its `ChimeraPayloads` registration, the keybind consumption in
  `ChimeraModClient.onClientTick`, `GeneEffectHandlers.handleGrassFedUse`). Considered
  `PlayerInteractEvent.LeftClickEmpty` first, but its own javadoc says it's client-only and
  fires only for true empty-air clicks - insufficient, since punching a block/mob while holding
  the Web Slinger should also retract. Used `InputEvent.InteractionKeyMappingTriggered` instead
  (`isAttack()`, fires for every left-click regardless of target), sending a new
  `RetractWebSlingerPayload` (identical empty-record shape to `GrassFedUsePayload`). The
  server-side handler lives on `WebSlingerItem` itself as a static method reading a small
  per-player `Map<UUID, WebHookEntity>` populated in `use()` - mirrors the per-player cooldown-map
  shape `GeneEffectHandlers` already uses for Raging Bull/Ramming Charge, avoids needing a
  world-scan to find "this player's active hook."
- **One-hook-at-a-time + a max range (started at 15 blocks, bumped to 25 after hands-on testing
  felt too short), per user feedback** - right-clicking with an already-active hook now retracts
  it instead of firing another (mirrors the fishing rod's own right-click-again-to-reel-in
  convention, reusing `retractActiveHook` rather than duplicating that logic), fixing the ability
  to spam out an unbounded number of hooks at once. The range cap lives in `WebHookEntity.tick()`
  itself (discards once `position().distanceTo(spawnPos) > MAX_RANGE` while still in flight) -
  deliberately server-only,
  since `spawnPos` is only ever known on the side that actually threw the hook (the
  `(LivingEntity, Level)` constructor call in `WebSlingerItem`; the client's own copy of the
  entity is built through the plain `(EntityType, Level)` factory constructor used for network
  spawning, with no `spawnPos` at all) and `discard()` already syncs entity removal to every
  client automatically, so there was nothing useful for the client side to compute here itself -
  the same "don't assume both sides have the same state" lesson as the earlier stuck-flag bug.

## The Vat Cluster (work order #4), Milestone 1a: the splice pipeline

- **No multiblock framework exists anywhere in NeoForge or vanilla** - confirmed by searching the
  decompiled sources directly for `multiblock`/`MultiblockPattern` (zero hits); `StructureTemplate`/
  `.nbt` is for world-gen structures, not a live "is this player-built thing still intact" check.
  `BeaconBlockEntity` is the closest vanilla precedent, throttling its own pyramid-base scan to
  once every 80 ticks - but every existing machine in this mod already re-evaluates `canProcess()`
  every tick unthrottled, so `GestationVatBlockEntity.isStructureFormed()` (a small fixed 16-offset
  glass-ring check, not an expanding scan) just lives inside `canProcess()` and runs every tick
  too, consistent with this codebase's own pattern rather than importing Beacon's optimization for
  a problem this mod doesn't have at this scale.
- Structure requirement is a 2-tall hollow glass ring: 8 vanilla Glass blocks at the controller's
  own Y level (the 3x3 ring around it, center is the controller itself) plus 8 more at Y+1 (same
  ring shape, center left open so the tank is visibly hollow from above) - 16 fixed relative
  offsets total, reusing vanilla Glass rather than adding a new "frame" block/texture.
- `GestationVatBlockEntity extends AbstractMachineBlockEntity` directly, same as all six prior
  machines - a multiblock controller needed no new registration shape, just a 7th
  `DeferredBlock`/`DeferredHolder<BlockEntityType<?>>` entry.
- `SpliceRecipeRegistry` mirrors `GeneRegistry`'s exact shape (not `GenePoolRegistry`'s) - keyed
  by the JSON file's own id rather than reconstructed from either parent species, since a splice
  recipe isn't "owned" by one side. `SpliceRecipe.matches()` checks both `(A,B)` and `(B,A)` so
  recipe authors don't need to worry about parent order.
- `DNA_EGG_RESULT` (the tagged hybrid's `EntityType` id) mirrors `SPECIES`'s own shape exactly -
  plain `ResourceLocation` data component, blank = absent, rather than an `Optional`-typed codec.
  Consistent with how `SequencedGenomeItem`/`TissueSampleItem` already distinguish "not yet
  identified" from "identified" without a separate item registration per state.
- Test recipe (`splice_recipes/test_chicken_cow.json`, cow + chicken -> `minecraft:chicken`) is a
  deliberate throwaway, mirroring the `anima_trace` placeholder-gene precedent - exercises the
  full pipeline (structure check, species matching, egg tagging) without pre-building any real
  hybrid content. The real curated recipe (the Endarachnid) is Milestone 2's job.
- This milestone deliberately stops at "you're holding a filled egg" - `DnaEggItem` is a plain
  `Item` for now (tooltip only), not yet a `BlockItem`. Placement, the hatch-tick `BlockEntity`,
  and the blank-egg-placement-refusal all belong to Milestone 1b.
