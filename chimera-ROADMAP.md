# Chimera — Roadmap & Design Ledger

> Open this first each modding session to reorient. It tracks three things: **where the build actually is**, **what work orders are queued and what they depend on**, and **the locked design decisions** that live across many conversations and would otherwise evaporate. Update the "Current state" section whenever a milestone commits.

_Last updated: after Byproduct Economy Milestone 1 (the item set + both acquisition points) was hands-on verified and committed._

---

## The stack (fixed)

- Minecraft **1.21.1**, **NeoForge**, **JDK 21**
- mod id `chimera`, package `com.chimera`
- **Curios API** as a soft dependency (Splice Core)
- Core architecture: datapack-driven gene pools (`SimpleJsonResourceReloadListener`), item data via **Data Components** (not NBT), one shared `AbstractMachine*` base, player state via **Data Attachments**, Curios isolated behind a compat class.

---

## Current state (the honest picture)

**Shipped & committed:**
- **v0.1** — full pipeline: scrape → sequence → analyze → extract → Splice Core (Curios) → buff. Seven phases. Established all the architecture above. Dormant hooks seeded: `requires_anima` (gene flag), `inert` (cassette component), `corruption` (player float).
- **v0.2** — star-level traits + Helix Analyzer; cow's full kit (Bovine Vigor / Grass Fed / Raging Bull); Centrifuge + Genome Splicer (split/recombine genomes); Extractor fixed to copy all traits; tiered progression (real tier check + Bioreactor → Refined Culture → Apex Scraper for T1→T2); universal fuel (Biomass) + machine upgrade slots across all six machines; GUI layout pass; procedural pixel-art placeholders.

**Shipped & committed (cont.):**
- **v0.2 Tier 2 work order (`chimera-v0.2-tier2-spec`) — done, all three milestones.**
  - **M1 — drawback system.** Tooltip colors, upside+downside apply/scale/remove cleanly, nothing lingers on unequip. (Bovine Vigor's slowness originally worsened at 3★ as a deliberate power-gamble exception - reversed to ease during the Biopedia+Oath work order once it became the *only* worsening drawback left in the mod; see below.)
  - **M2 — Tier 2 mob kits.** Horse/zombie/skeleton/spider/wolf/goat/fox/cave spider (`equine_gait`, `undying_hunger`, `steady_aim`, `arachnid_climb`, `pack_instinct`, `ramming_charge`, `silent_step`, `venom_glands`); creeper's `volatile_cells` deferred to its own mini-milestone per the spec's own allowance. Also fixed a real bug found during verification: `TissueScraperItem` relied on `Item#interactLivingEntity` alone, which doesn't reliably win against vanilla entities with nontrivial `mobInteract` overrides (horses mounting instead of being scraped) - fixed via a `PlayerInteractEvent.EntityInteract` listener.
  - **M3 — the Hunt gate.** Went through a real design revision mid-milestone: the spec's aggro/low-HP detection was too permissive (most hostiles aggro without being attacked), so Stress Plasma eligibility became a deliberate **Potion of Stress** (new MobEffect + Potion + brewing recipe, brewed from Awkward Potion + Adrenal Extract) instead. Also added two systems beyond the original spec: a one-day per-mob scrape cooldown (entity Data Attachment, proven to generalize cleanly from the existing player-attachment pattern) and tier-scaling yield (`bonusSampleChance()` now escalates Reinforced 25% → Apex 40% → Predator 55%, rolled independently for samples and Stress Plasma). Tier 3 itself is `PredatorTissueScraperItem`, crafted from Combat Stimulant, mirroring the Apex gate's exact shape - confirmed via Enderman placeholder pool (tier 3, no kit yet).

**Done — Biopedia + Oath work order (`chimera-biopedia-oath-spec`):**
- ✅ **Milestone 1 — player identity state.** New `com.chimera.oath` package: `PlayerOathData` (`hasOath`, dormant `oathBroken`, dormant `path` enum NONE/SCIENTIST/REAPER/SERAPH) and a separate `DISCOVERED_GENES` attachment (`Set<ResourceLocation>`, empty for now - nothing writes to it yet). Real contradiction found and resolved: the spec assumed player-level trait-discovery state already existed to "reuse" for the Biopedia; it didn't (identification is 100% per-item today, see `GenomeAnalyzerBlockEntity`) - folded a new attachment into this milestone rather than deferring it. Hands-on verified on both a fresh save and an existing pre-update save: both load the same sane defaults, no crash. Flagged for M2/M3: machines have no player reference during tick-based processing, so "who gets credited with discovering a gene" still needs real design when the write side gets built.
- ✅ **Milestone 2 — taking the Oath.** Self Tissue Sample (right-click air with any scraper), The Oath (Book + Self Tissue Sample, `stacksTo(1)`), vanilla `ConfirmScreen` for the accept/decline prompt, a placeholder `TheBiopediaItem` granted on accept. Boon fork resolved directly with the user (the spec and ROADMAP disagreed) - went with **diligent study**: concrete Analyzer-time auto-populate hook vs. "humane sampling," which had nothing to attach to since scraping never angered mobs to begin with. The boon's *condition* (`OathEffects.diligentStudyActive`) is built; the write-hook into `DISCOVERED_GENES` is deferred to M3 alongside the Biopedia that actually reads it. Hit and fixed a real dedicated-server crash: `TheOathItem` originally referenced `ConfirmScreen`/`Minecraft` directly inside an `isClientSide` guard, which NeoForge's `RuntimeDistCleaner` rejects at class-load time regardless of runtime branching - fixed via a plain `OpenOathPromptEvent` bridging to `ChimeraModClient` (see NOTES.md). Also fixed two bugs found during verification: the species tooltip showed "Pig" for the player sentinel (`BuiltInRegistries.ENTITY_TYPE` is a `DefaultedRegistry`, `.get()` never returns null) and reworked all scraper drops (tissue samples, Stress Plasma, self-samples) to drop on the ground instead of going straight to inventory.
- ✅ **Milestone 3 — the Biopedia.** Reused vanilla `BookViewScreen` (a `BookAccess(List<Component>)` record) rather than a custom Screen - server builds the full catalog (`BiopediaEntry` per gene, tier + reverse-indexed mob list + upside/drawback lines, `Optional.empty()` details for undiscovered genes) and sends it down via `OpenBiopediaPayload`, since `DISCOVERED_GENES` has no client sync and building the page content client-side would show stale data with nothing to ever correct it. Resolved the M1/M2-deferred discovery-write-hook question with two real hook sites: `SpliceCoreMenu.syncCassettesToCore()` (normal path - installing a cassette) and `GenomeAnalyzerMenu`'s output slot `onTake` (diligent study boon - studying without splicing). Added a placeholder inert gene (`chimera:anima_trace`, tier 1, empty effects, `requires_anima: true`, low-weight extra roll on cow) since nothing exercised the inert-notice display path before. Same `RuntimeDistCleaner`-safe bridging pattern as M2 (`OpenBiopediaScreenEvent` posted on `NeoForge.EVENT_BUS`, only `ChimeraModClient` touches `BookViewScreen`/`Minecraft`) - this time the lesson extended further: the cleaner rejects client-only symbols even inside a **lambda**, not just an `isClientSide` guard. Found and fixed a real pre-existing bug during verification, inherited by every machine Menu's `quickMoveStack` template: `slot.onTake(player, slotStack)` passed the post-move, already-drained stack (data components lost) instead of `result`, the pre-move copy - this is why the diligent-study boon silently failed on shift-click but worked on a normal drag-pickup. Also fixed a double-`%` display bug in five gene descriptions, added star-level-range display to the catalog (`describeAcrossStars`, showing 1★ and 3★ bookends when they differ) instead of a fixed 1★ reading, and reworked pagination after a user screenshot showed an underfull page followed by a clipped one - now measures the real combined candidate page with the actual font each time a paragraph is tentatively added, instead of summing independently-estimated per-paragraph line counts. Bovine Vigor's slowness direction was also reversed per explicit user correction (see M1 note above) - the mod now uses `ease`-toward-0-at-3★ as its sole star-scaling convention, no `worsen` exceptions remaining.

**In flight — Byproduct Economy work order (`chimera-byproduct-economy-spec`):**
- ✅ **Milestone 1 — the item set + both acquisition points.** Designed directly in conversation rather than pasted as a pre-written spec (a first for this project). Two byproduct tiers: *generic* (mob-agnostic - Mutagen/Chromatin Strand/Cell Culture/Nucleotide Slurry, discovered to already be half-built via `GeneSequencerBlockEntity`'s existing roll, initially mistaken for plain fuel/intermediates) and *specific* (12 new mob-unique items, one per mob with a real gene kit, added via a new optional `specific_byproduct` field on `GenePool`; Enderman deferred, no kit yet). Per explicit user correction to the first draft, both tiers are obtainable from **both** acquisition points: sequencing (`GeneSequencerBlockEntity`, now with a second byproduct slot) and scraping (`TissueScraperItem`). Shared the generic-roll table into a new `gene/ByproductRoller` so both call sites use the same logic. Passive-mob items (Marrow Extract, Adipose Reserve, Keratin Down, Lanolin Concentrate, Tendon Fiber, Horn Plate, Vestibular Gland, Adrenal Musk Gland) feed a future utility/husbandry sink (M2); hostile-mob items (Necrotic Ichor, Ossein Powder, Chitin Resin, Venom Sac) feed a future combat sink (M3) - no consuming recipes yet, this milestone only establishes the items and how you get them. Fixed a real GUI collision found during planning (a slot placed too close to the upgrade rail's column collides with it at *any* y, not just where a rail slot happens to sit, since the rail's boxes are edge-to-edge across all 3 possible upgrade slots) and, per user feedback after the first hands-on pass, repositioned both byproduct slots side by side centered under the output slot (both now drawn dynamically, the old baked-in slot outline erased from the GUI texture) and dialed byproduct chances down substantially - sequencing's rolls were guaranteed every cycle (now 50%/35% generic/specific) and scraping's reused `bonusSampleChance()` directly (Predator was 55% per roll, ~80% chance of at least one byproduct per scrape - now scaled to 40% of that rate, 22% for Predator), decoupled from the bonus-Tissue-Sample rate so that stayed untouched.

**Note:** most recent real-world time has gone to the day job, not the mod. Design has run ahead of implementation on purpose — the orders below are *drafts pending reality*, not final. Each assumes the ones before it are in; reread an order before starting it to check its assumptions survived.

---

## Work order queue (ordered, with dependencies)

| # | Work order | Status | Depends on | File |
|---|---|---|---|---|
| 1 | Tier 2 kits + drawbacks + hunt gate | **done** | v0.2 | `chimera-v0.2-tier2-spec` |
| 2 | Biopedia + The Oath | **done** | #1's drawback system (hedged — degrades gracefully if absent) | `chimera-biopedia-oath-spec` |
| 3 | Byproduct economy | **in flight (M1 done)** | #1 (mob roster), ideally #2 | `chimera-byproduct-economy-spec` |
| 4 | The Vat cluster (spliced mobs + DNA eggs + multiblock Gestation Vat) | not yet written | #1, #3 | — |
| 5 | The Break (Endarachnid → oath-break → Necronomicon → Scythe) | not yet written | #2, #4 | — |
| 6 | Reaper act (souls, magical weapons, soul-infused machines) | not yet written | #5 | — |
| 7 | Seraph act (atonement quest, reliquary, Evil boss) | not yet written | #6 | — |

**Next up:** #3's remaining milestones - M2 (passive/utility-husbandry sink) and M3 (hostile/combat sink), the actual recipes consuming the 12 items M1 established. The Terraria framing promoted this work order from flavor to load-bearing — it's a big part of what makes the science half feel like a *complete mod worth stopping at*, which is what earns the whole back-half arc. Finish it before the Vat cluster, because the Vat consumes byproducts.

---

## Locked design decisions (the arc-level calls)

These are settled and thread through everything. They don't live in any single work order — this is their home.

### The shape
- **Three acts: Scientist → Reaper → Seraph.** Innocence → fall → grace. Every act has its *own economy*, not just new content.
- **Tone progression: science → science-fiction → magic/unknown (Lovecraftian).** The horror is *epistemic* — knowledge that costs you — not just monsters. The back half should make earlier content read differently (naive-in-hindsight).
- **The science half is a complete mod.** Terraria pre-hardmode. A player can stop at the wall and feel they played a whole game. This is a *requirement*, and it's why the byproduct economy + trait breadth matter.
- **The Oath break = the Wall of Flesh.** An irreversible-forward hardmode trigger. Optional, deliberately summoned, heavily telegraphed. Post-break, the mod changes — ideally the *old* systems too, not just new additions.

### The Oath (the spine)
- **Per-player state, not world state** (multiplayer safety + it's thematically *your* soul). Player attachment carries `hasOath`, `oathBroken` (dormant), `path` enum NONE/SCIENTIST/REAPER/SERAPH (dormant past SCIENTIST).
- The Oath must have a **mechanical boon while unbroken**, so the break is a real sacrifice, not just flavor. (Implementation picks humane-sampling vs diligent-study — leaning study, because it inverts to "Necronomicon learns by reaping.")
- **Inert genes foreshadow the break.** `requires_anima` traits sequence fine but come back inert; the Biopedia shows a clinical "not encoded in the flesh" notice. By the time a player reaches the wall, they've read that barrier a dozen times and *understand* what they're reaching past. Foreshadowing is set via JSON flag, never hardcoded.

### The break itself
- **The Endarachnid** (enderman + spider, spliced in the Gestation Vat) is the hinge. Player reaches for it because they need **ender silk** and there's no clean way to get it — they *choose* to cross the line, with warnings; the game doesn't shove them. A thoughtful player should pause before injecting the ender DNA.
- Sequence: splice → Endarachnid escapes hostile → player kills it for ender silk → "harm dealt, oath broken" → the Oath returns *twisted*, instructing you to destroy it in fire → burn it → Biopedia becomes the **Necronomicon** → unlocks the **Scythe of Souls** recipe (a hoe infused with souls via a multiblock ritual).
- **Irreversibility is per-player, forward-only, with an expensive redemption path** (the Seraph act *is* that path). The commitment gate is **loud and multi-step** (twisted oath must exist, then be deliberately burned — consider a held/confirmed ritual so nobody breaks it by misclick).

### Reaper act (Act II)
- Souls are **consumable currency** — kill, spend, repeat. Scythe, magical weapons, the combat/weapon layer the mod currently lacks.
- **Machine "magical UI" is opt-in per machine via soul-infusion**, NOT an automatic world/player flip (avoids jarring UI desync in multiplayer; also a needed soul-sink; also "the lab grows veins, by your hand" is better than an instant flip).
- **Evil trait** climbs as you reap — a *fixed*-star trait in your own genome (likely the surfaced form of the dormant `corruption` float). Should be *felt* as it climbs (cosmetic dread → mechanical seduction where falling feels *good* → world reacts near max), not a silent counter. **Strongly consider: Evil rises with reaping and falls with restraint**, making it a live conscience meter you can flirt with, not just a doom timer.

### Seraph act (Act III)
- Atonement **inverts the soul economy**: souls stop being spent and become a **capacity-capped reliquary** — you shelter a *finite* number and curate *quality* (a Sovereign soul > a Dim one), so the loop is "reap to find *worthier* souls to shelter," not "hoard for infinite power." Caps power so tension survives.
- **Grace costs the fall's spoils.** The atonement quest requires *giving up* the reaper's raw consumptive power — you trade kill-power for protective power. This makes it a real choice at the end, not a free "true ending" everyone takes.
- **The Evil manifestation boss = the climax.** At max Evil, your evil manifests as a boss that **looks like you and fights using your own spliced traits** ("this is what I became"). Reuses systems already built.

### Cross-cutting craft rules
- **Drawbacks are design-time, two-sided traits** — resonant downsides ("the other face of the same animal"), the player nods not groans. Star-scaling per trait eases toward 0 at 3★ (mastery) — the mod's one prior `worsen` (power-gamble) exception, Bovine Vigor, was reversed during the Biopedia+Oath work order once it became the last trait using that direction; `ease` is now the sole convention going forward.
- **Byproduct economy runs on three axes**: tier-based / mob-specific / categorical (passive-mob vs hostile-mob, etc.). Turns every mob into a reason to keep processing even after you have its trait.
- **Multiblock the rituals, single-block the factories.** Gestation Vat = multiblock (it births life / the Endarachnid). Soul ritual = multiblock. Bioreactor = stays single-block (it's a factory you place several of).
- **Living-mob splicing stays playful/cartoonish; horror is reserved for the Endarachnid/break** so the rare horror lands hard. Contrast is the tool.

---

## Open questions (decide before the relevant act, not now)

- **Evil boss — Fork A vs Fork B.** Fork A (recommended): the boss fight is the *crisis that begins* atonement — the last violent thing the Reaper does — so the Seraph's non-violent identity stays intact. Fork B: the finale is explicitly non-violent (contain/forgive rather than slay). Not yet locked. Decide **before writing the Reaper order**, since it changes what the Reaper builds toward.
- **Evil ↔ corruption exact relationship** — is Evil just `corruption` surfaced/renamed, or is Evil the visible trait and corruption the hidden driver? Decide before surfacing the Reaper path.
- **How long is the science half** — how many tiers before the wall? The framing says "big enough to feel complete." A firm number shapes how much byproduct/trait content #3 needs.
- **Atonement quest's specific cost** — what exactly you sacrifice for grace. Deferred to the Act III order.
- **Real art** — everything is procedural placeholders. Decide if/when to commission or hand-draw.
- **Biopedia naming** — Biopedia / Field Journal / Lab Log (keep it clinical; mysticism spent later).

---

## Housekeeping reminders

- **Commit discipline:** the v0.2 session went 9+ milestones without a commit once — don't repeat that. Per-milestone commits, each a real restore point.
- **Every work order opens by making Claude Code read the real code and confirm shapes back to you** before writing — these specs are from synopses, so field/class names are *intent*, not gospel.
- **Verify NeoForge 1.21.1 APIs** against `docs.neoforged.net` or decompiled sources; never let an invented method through. Keep appending gotchas to `NOTES.md` (wall-climb, combat-state reading, book-GUI, and attachment serialization are the flagged-likely ones).
