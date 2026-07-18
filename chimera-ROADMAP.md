# Chimera — Roadmap & Design Ledger

> Open this first each modding session to reorient. It tracks three things: **where the build actually is**, **what work orders are queued and what they depend on**, and **the locked design decisions** that live across many conversations and would otherwise evaporate. Update the "Current state" section whenever a milestone commits.

_Last updated: after Tier 2 Milestone 1 (drawback system) was implemented + smoke-tested, awaiting in-game verification._

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

**In flight — Tier 2 work order (`chimera-v0.2-tier2-spec`):**
- ✅ **Milestone 1 — drawback system.** Implemented, headless smoke-tested clean. **NOT yet committed** — awaiting hands-on in-game verification (tooltip colors, upside+downside both apply/scale/remove cleanly, star direction: Bovine Vigor slowness worsens at 3★, Hollow Bones frailty eases toward 0 at 3★, nothing lingers on unequip). Commit only after that check passes.
- ⬜ **Milestone 2 — Tier 2 mob kits** (horse/zombie/skeleton/spider/wolf/goat/fox/cave spider/creeper; creeper allowed to slip to its own mini-milestone). Not started.
- ⬜ **Milestone 3 — the Hunt gate** (Stress Plasma from combat-sampling → Combat Stimulant → unlocks T3 scraping). Not started.

**Note:** most recent real-world time has gone to the day job, not the mod. Design has run ahead of implementation on purpose — the orders below are *drafts pending reality*, not final. Each assumes the ones before it are in; reread an order before starting it to check its assumptions survived.

---

## Work order queue (ordered, with dependencies)

| # | Work order | Status | Depends on | File |
|---|---|---|---|---|
| 1 | Tier 2 kits + drawbacks + hunt gate | **in flight (M1 done, uncommitted)** | v0.2 | `chimera-v0.2-tier2-spec` |
| 2 | Biopedia + The Oath | queued | #1's drawback system (hedged — degrades gracefully if absent) | `chimera-biopedia-oath-spec` |
| 3 | Byproduct economy | **not yet written** | #1 (mob roster), ideally #2 | — |
| 4 | The Vat cluster (spliced mobs + DNA eggs + multiblock Gestation Vat) | not yet written | #1, #3 | — |
| 5 | The Break (Endarachnid → oath-break → Necronomicon → Scythe) | not yet written | #2, #4 | — |
| 6 | Reaper act (souls, magical weapons, soul-infused machines) | not yet written | #5 | — |
| 7 | Seraph act (atonement quest, reliquary, Evil boss) | not yet written | #6 | — |

**Next design piece to write** (whenever, no rush): **#3 the byproduct economy.** The Terraria framing promoted this from flavor to load-bearing — it's a big part of what makes the science half feel like a *complete mod worth stopping at*, which is what earns the whole back-half arc. Spec it before the Vat cluster, because the Vat consumes byproducts.

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
- **Drawbacks are design-time, two-sided traits** — resonant downsides ("the other face of the same animal"), the player nods not groans. Star-scaling per trait: `worsen` (power gamble) or `ease` (mastery).
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
