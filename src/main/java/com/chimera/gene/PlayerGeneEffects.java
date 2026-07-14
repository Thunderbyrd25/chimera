package com.chimera.gene;

import com.chimera.ChimeraAttachments;

import net.minecraft.world.entity.player.Player;

// Shared query helpers so tick/event hooks (Ruminant Gut, Hollow Bones, freeze immunity) don't
// each re-implement "does this player have a gene with behavior X active". Reads only the
// PlayerGeneData attachment - no Curios dependency here, see CLAUDE.md architecture rule #5.
public final class PlayerGeneEffects {

    private PlayerGeneEffects() {}

    public static boolean hasBehavior(Player player, String behaviorId) {
        PlayerGeneData data = player.getData(ChimeraAttachments.PLAYER_GENE_DATA.get());
        for (GeneInstance installed : data.installedGenes()) {
            Gene gene = GeneRegistry.get(installed.gene());
            if (gene == null) {
                continue;
            }
            for (GeneEffect effect : gene.effects()) {
                if (effect instanceof BehaviorGeneEffect behavior && behavior.behaviorId().equals(behaviorId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
