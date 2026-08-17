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
public class GestationVatScreen extends AbstractContainerScreen<GestationVatMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ChimeraMod.MODID, "textures/gui/container/gestation_vat.png");

    public GestationVatScreen(GestationVatMenu menu, Inventory playerInventory, Component title) {
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

        for (int i = 0; i < 3; i++) {
            int x = leftPos + GestationVatMenu.INPUT_SLOT_START_X + i * GestationVatMenu.INPUT_SLOT_SPACING;
            int y = topPos + GestationVatMenu.INPUT_SLOT_Y;
            guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
            guiGraphics.fill(x, y, x + 16, y + 16, 0xFFA2A2A2);
        }

        MachineScreenUtil.drawProgressBar(guiGraphics, leftPos + 76, topPos + 38, 24, 4, menu.getProgress(), menu.getMaxProgress(), 0xFF3AAFC4);
        MachineScreenUtil.drawSlotBox(guiGraphics, leftPos + GestationVatMenu.FUEL_X, topPos + GestationVatMenu.FUEL_Y);
        MachineScreenUtil.drawUpgradeRail(guiGraphics, leftPos, topPos, menu.getUpgradeSlotCount());
    }
}
