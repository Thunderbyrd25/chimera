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
public class CentrifugeScreen extends AbstractContainerScreen<CentrifugeMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "textures/gui/container/centrifuge.png");

    public CentrifugeScreen(CentrifugeMenu menu, Inventory playerInventory, Component title) {
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

        MachineScreenUtil.drawProgressBar(guiGraphics, leftPos + CentrifugeMenu.PROGRESS_X, topPos + 32, 24, 4, menu.getProgress(), menu.getMaxProgress(), 0xFFC48A3A);
        MachineScreenUtil.drawSlotBox(guiGraphics, leftPos + CentrifugeMenu.FUEL_X, topPos + CentrifugeMenu.FUEL_Y);
        MachineScreenUtil.drawUpgradeRail(guiGraphics, leftPos, topPos, menu.getUpgradeSlotCount());
    }
}
