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
