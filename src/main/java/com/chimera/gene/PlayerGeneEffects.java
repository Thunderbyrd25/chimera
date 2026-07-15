package com.chimera.gene;

import java.util.Optional;

import com.chimera.ChimeraAttachments;

import net.minecraft.world.entity.player.Player;

// Shared query helpers so tick/event hooks (Ruminant Gut, Hollow Bones, freeze immunity, Grass
// Fed, Raging Bull) don't each re-implement "does this player have a gene with behavior X
// active". Reads only the PlayerGeneData attachment - no Curios dependency here, see CLAUDE.md
// architecture rule #5.
public final class PlayerGeneEffects {

    private PlayerGeneEffects() {}

    public static boolean hasBehavior(Player player, String behaviorId) {
        return findBehavior(player, behaviorId).isPresent();
    }

    // Same gene installed twice at different star levels (Mk2/Mk3) resolves to the highest
    // level, matching the attribute-effect dedup rule in ChimeraCuriosCompat.
    public static Optional<BehaviorMatch> findBehavior(Player player, String behaviorId) {
        PlayerGeneData data = player.getData(ChimeraAttachments.PLAYER_GENE_DATA.get());
        BehaviorMatch best = null;
        for (GeneInstance installed : data.installedGenes()) {
            Gene gene = GeneRegistry.get(installed.gene());
            if (gene == null) {
                continue;
            }
            for (GeneEffect effect : gene.effects()) {
                if (effect instanceof BehaviorGeneEffect behavior && behavior.behaviorId().equals(behaviorId)) {
                    if (best == null || installed.starLevel() > best.instance().starLevel()) {
                        best = new BehaviorMatch(installed, behavior);
                    }
                }
            }
        }
        return Optional.ofNullable(best);
    }

    public record BehaviorMatch(GeneInstance instance, BehaviorGeneEffect effect) {
        public double scaledValue() {
            return effect.scaledValue(instance.starLevel());
        }
    }
}
