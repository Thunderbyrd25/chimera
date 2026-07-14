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

// "Helix": once the output genome is identified, its rolled traits render as a stack of
// star-pip + name lines under the output slot (where the result actually is). Trait count
// isn't knowable until the Analyzer actually finishes rolling, so before that it just shows a
// placeholder - and nothing at all while the machine is completely idle.
@OnlyIn(Dist.CLIENT)
public class GenomeAnalyzerScreen extends AbstractContainerScreen<GenomeAnalyzerMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "textures/gui/container/genome_analyzer.png");

    // Output slot is at x=116, 16px wide - its horizontal center.
    private static final int OUTPUT_CENTER_X = 124;
    private static final int HELIX_FIRST_ROW_Y = 52;
    private static final int HELIX_ROW_HEIGHT = 9;

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
        ItemStack input = menu.getInputStack();
        ItemStack output = menu.getOutputStack();
        if (input.isEmpty() && output.isEmpty()) {
            return;
        }

        boolean identified = !output.isEmpty() && Boolean.TRUE.equals(output.get(ChimeraDataComponents.IDENTIFIED.get()));
        if (!identified) {
            Component placeholder = Component.translatable("gui.chimera.genome_analyzer.analyzing").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
            drawCentered(guiGraphics, placeholder, HELIX_FIRST_ROW_Y);
            return;
        }

        List<GeneInstance> traits = output.get(ChimeraDataComponents.TRAITS.get());
        if (traits == null) {
            return;
        }

        for (int i = 0; i < traits.size(); i++) {
            drawCentered(guiGraphics, TraitDisplay.traitLine(traits.get(i)), HELIX_FIRST_ROW_Y + i * HELIX_ROW_HEIGHT);
        }
    }

    // Centers on the output slot, clamped so long trait names can't run off either edge of the
    // 176px-wide panel.
    private void drawCentered(GuiGraphics guiGraphics, Component text, int relativeY) {
        int width = font.width(text);
        int x = leftPos + OUTPUT_CENTER_X - width / 2;
        x = Math.min(x, leftPos + imageWidth - 4 - width);
        x = Math.max(x, leftPos + 4);
        guiGraphics.drawString(font, text, x, topPos + relativeY, 0x404040, false);
    }
}
