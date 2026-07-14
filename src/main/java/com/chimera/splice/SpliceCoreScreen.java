package com.chimera.splice;

import com.chimera.ChimeraMod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpliceCoreScreen extends AbstractContainerScreen<SpliceCoreMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "textures/gui/container/splice_core.png");

    public SpliceCoreScreen(SpliceCoreMenu menu, Inventory playerInventory, Component title) {
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

        // Cassette slot boxes are drawn here rather than baked into the texture: Mk1/Mk2/Mk3
        // each use a different slot count and x-spacing (see SpliceCoreMenu), so a single
        // static image can't have the right boxes for every tier without either shipping three
        // textures or overlapping unrelated tiers' positions (the previous bug).
        int startX = menu.getCassetteSlotStartX();
        for (int i = 0; i < menu.getSlotCount(); i++) {
            int x = leftPos + startX + i * 18;
            int y = topPos + 35;
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
            guiGraphics.fill(x, y, x + 16, y + 16, 0xFFA2A2A2);
        }
    }
}
