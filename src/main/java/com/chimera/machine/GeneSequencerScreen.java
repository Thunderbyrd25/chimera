package com.chimera.machine;

import com.chimera.ChimeraMod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GeneSequencerScreen extends AbstractContainerScreen<GeneSequencerMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "textures/gui/container/gene_sequencer.png");

    public GeneSequencerScreen(GeneSequencerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        MachineScreenUtil.drawProgressBar(guiGraphics, leftPos + 76, topPos + 38, 24, 4, menu.getProgress(), menu.getMaxProgress(), 0xFF63C43A);
        MachineScreenUtil.drawSlotBox(guiGraphics, leftPos + GeneSequencerMenu.FUEL_X, topPos + GeneSequencerMenu.FUEL_Y);
        // Byproduct economy work order Milestone 1: both byproduct slot outlines are drawn
        // dynamically (like the fuel slot always has been) rather than baked into the background
        // PNG, since centering them as a pair under the output slot moved them off the old
        // baked-in position - the old baked box was erased from the texture to match.
        MachineScreenUtil.drawSlotBox(guiGraphics, leftPos + GeneSequencerMenu.BYPRODUCT_GENERIC_X, topPos + GeneSequencerMenu.BYPRODUCT_Y);
        MachineScreenUtil.drawSlotBox(guiGraphics, leftPos + GeneSequencerMenu.BYPRODUCT_SPECIFIC_X, topPos + GeneSequencerMenu.BYPRODUCT_Y);
        MachineScreenUtil.drawUpgradeRail(guiGraphics, leftPos, topPos, menu.getUpgradeSlotCount());
    }
}
