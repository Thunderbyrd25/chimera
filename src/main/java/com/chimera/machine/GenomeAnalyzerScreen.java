package com.chimera.machine;

import java.util.List;

import com.chimera.ChimeraDataComponents;
import com.chimera.ChimeraMod;
import com.chimera.gene.GeneInstance;
import com.chimera.gene.TraitDisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// "Helix": once the output genome is identified, its rolled traits render as a double-helix
// style column between the input/output slots (alternating left/right, star pips + name).
// Trait count isn't knowable until the Analyzer actually finishes rolling, so while the
// genome is still unidentified this just shows a placeholder instead of guessing a slot count.
@OnlyIn(Dist.CLIENT)
public class GenomeAnalyzerScreen extends AbstractContainerScreen<GenomeAnalyzerMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "textures/gui/container/genome_analyzer.png");

    private static final int HELIX_CENTER_X = 88;
    private static final int HELIX_FIRST_ROW_Y = 52;
    private static final int HELIX_ROW_HEIGHT = 8;
    private static final int HELIX_SIDE_OFFSET = 4;

    public GenomeAnalyzerScreen(GenomeAnalyzerMenu menu, Inventory playerInventory, Component title) {
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

        MachineScreenUtil.drawProgressBar(guiGraphics, leftPos + 76, topPos + 38, 24, 4, menu.getProgress(), menu.getMaxProgress(), 0xFF4A9BD6);

        renderHelix(guiGraphics);
    }

    private void renderHelix(GuiGraphics guiGraphics) {
        ItemStack output = menu.getOutputStack();
        boolean identified = !output.isEmpty() && Boolean.TRUE.equals(output.get(ChimeraDataComponents.IDENTIFIED.get()));

        if (!identified) {
            Component placeholder = Component.translatable("gui.chimera.genome_analyzer.analyzing").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            int width = font.width(placeholder);
            guiGraphics.drawString(font, placeholder, leftPos + HELIX_CENTER_X - width / 2, topPos + HELIX_FIRST_ROW_Y, 0x404040, false);
            return;
        }

        List<GeneInstance> traits = output.get(ChimeraDataComponents.TRAITS.get());
        if (traits == null) {
            return;
        }

        for (int i = 0; i < traits.size(); i++) {
            Component line = TraitDisplay.traitLine(traits.get(i));
            int width = font.width(line);
            boolean leftSide = i % 2 == 0;
            int x = leftSide ? leftPos + HELIX_CENTER_X - HELIX_SIDE_OFFSET - width : leftPos + HELIX_CENTER_X + HELIX_SIDE_OFFSET;
            int y = topPos + HELIX_FIRST_ROW_Y + i * HELIX_ROW_HEIGHT;
            guiGraphics.drawString(font, line, x, y, 0x404040, false);
        }
    }
}
