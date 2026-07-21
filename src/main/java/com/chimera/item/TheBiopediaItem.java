package com.chimera.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.chimera.ChimeraAttachments;
import com.chimera.gene.Gene;
import com.chimera.gene.GeneEffect;
import com.chimera.gene.GenePool;
import com.chimera.gene.GenePoolRegistry;
import com.chimera.gene.GeneRegistry;
import com.chimera.network.BiopediaEntry;
import com.chimera.network.BiopediaEntry.BiopediaEntryDetails;
import com.chimera.network.OpenBiopediaPayload;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

// Biopedia+Oath work order Milestone 3. Deliberately has zero references to client-only classes
// (same reasoning as TheOathItem) - the catalog is built here server-side, since
// DISCOVERED_GENES is server-authoritative with no client sync, and sent down via
// OpenBiopediaPayload. The actual screen lives entirely in ChimeraModClient.
public class TheBiopediaItem extends Item {

    public TheBiopediaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            Set<ResourceLocation> discovered = player.getData(ChimeraAttachments.DISCOVERED_GENES.get());
            List<BiopediaEntry> entries = new ArrayList<>();
            for (Map.Entry<ResourceLocation, Gene> geneEntry : GeneRegistry.getAll().entrySet()) {
                ResourceLocation geneId = geneEntry.getKey();
                Gene gene = geneEntry.getValue();
                Optional<BiopediaEntryDetails> details = discovered.contains(geneId)
                        ? Optional.of(buildDetails(geneId, gene))
                        : Optional.empty();
                entries.add(new BiopediaEntry(geneId, gene.tier(), details));
            }
            entries.sort(Comparator.<BiopediaEntry>comparingInt(BiopediaEntry::tier)
                    .thenComparing(entry -> entry.geneId().toString()));

            PacketDistributor.sendToPlayer((ServerPlayer) player, new OpenBiopediaPayload(entries));
        }
        return InteractionResultHolder.success(stack);
    }

    private static BiopediaEntryDetails buildDetails(ResourceLocation geneId, Gene gene) {
        List<ResourceLocation> mobs = new ArrayList<>();
        for (Map.Entry<ResourceLocation, GenePool> poolEntry : GenePoolRegistry.getAll().entrySet()) {
            boolean carries = poolEntry.getValue().genes().stream().anyMatch(entry -> entry.gene().equals(geneId));
            if (carries) {
                mobs.add(speciesFromPoolId(poolEntry.getKey()));
            }
        }

        List<String> upsideLines = new ArrayList<>();
        List<String> drawbackLines = new ArrayList<>();
        for (GeneEffect effect : gene.effects()) {
            List<String> lines = describeAcrossStars(effect);
            if (lines.isEmpty()) {
                continue;
            }
            if (effect.drawback()) {
                drawbackLines.addAll(lines);
            } else {
                upsideLines.addAll(lines);
            }
        }

        return new BiopediaEntryDetails(mobs, gene.requiresAnima(), upsideLines, drawbackLines);
    }

    // A catalog entry isn't tied to one rolled GeneInstance's star level, so a single
    // describe(1) call was silently hiding the star-scaling every trait actually has (worsens
    // or eases per NOTES.md's convention). Show both bookends when they actually differ; a
    // non-scaling effect (base == per-level result) collapses back to one line.
    private static List<String> describeAcrossStars(GeneEffect effect) {
        String atOneStar = effect.describe(1).getString();
        if (atOneStar.isEmpty()) {
            return List.of();
        }
        String atMaxStar = effect.describe(Gene.MAX_STAR_LEVEL).getString();
        if (atOneStar.equals(atMaxStar)) {
            return List.of(atOneStar);
        }
        return List.of("1★ " + atOneStar, Gene.MAX_STAR_LEVEL + "★ " + atMaxStar);
    }

    // Inverse of GenePoolRegistry.get(EntityType) - a pool id "chimera:minecraft/cow" splits
    // back into the species id "minecraft:cow".
    private static ResourceLocation speciesFromPoolId(ResourceLocation poolId) {
        String path = poolId.getPath();
        int slash = path.indexOf('/');
        return ResourceLocation.fromNamespaceAndPath(path.substring(0, slash), path.substring(slash + 1));
    }
}
