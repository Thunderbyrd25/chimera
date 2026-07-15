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
public class GenomeSplicerScreen extends AbstractContainerScreen<GenomeSplicerMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "textures/gui/container/genome_splicer.png");

    public GenomeSplicerScreen(GenomeSplicerMenu menu, Inventory playerInventory, Component title) {
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

        // Genome slot boxes drawn from the same constants GenomeSplicerMenu used to place the
        // real slots, so they can never disagree (see SpliceCoreScreen for the bug this avoids).
        for (int i = 0; i < GenomeSplicerBlockEntity.GENOME_SLOT_COUNT; i++) {
            int x = leftPos + GenomeSplicerMenu.GENOME_SLOT_START_X + i * GenomeSplicerMenu.GENOME_SLOT_SPACING;
            int y = topPos + GenomeSplicerMenu.GENOME_SLOT_Y;
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
            guiGraphics.fill(x, y, x + 16, y + 16, 0xFFA2A2A2);
        }

        MachineScreenUtil.drawProgressBar(guiGraphics, leftPos + 76, topPos + 38, 24, 4, menu.getProgress(), menu.getMaxProgress(), 0xFFC48A3A);
        MachineScreenUtil.drawSlotBox(guiGraphics, leftPos + GenomeSplicerMenu.FUEL_X, topPos + GenomeSplicerMenu.FUEL_Y);
        MachineScreenUtil.drawUpgradeRail(guiGraphics, leftPos, topPos, menu.getUpgradeSlotCount());
    }
}
