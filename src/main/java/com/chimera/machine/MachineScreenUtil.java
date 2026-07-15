package com.chimera.machine;

import net.minecraft.client.gui.GuiGraphics;

// Small shared helper so the six machine screens don't each hand-roll the same bordered
// progress bar (1px dark outline + empty track + colored fill), or the same fuel/upgrade slot
// box drawing. The upgrade rail is always drawn at runtime rather than baked into each
// machine's static texture, since upgrade slot count (1-3) varies per machine instance - baking
// it in risks the same Mk1/Mk2/Mk3 overlap bug the Splice Core hit once already. The fuel slot
// is a single fixed box per machine, positioned by each Screen individually (low-left, below
// that machine's own inputs) since machines' input layouts differ.
final class MachineScreenUtil {

    // Shared by every machine's Menu (slot placement) and Screen (box drawing) so the two can
    // never drift out of sync. Upgrade-only now - fuel is decoupled onto its own position per
    // machine, see each Screen's own FUEL_X/FUEL_Y constants.
    static final int UPGRADE_RAIL_X = 152;
    static final int UPGRADE_RAIL_Y = 8;
    static final int UPGRADE_RAIL_SPACING = 18;

    private MachineScreenUtil() {}

    static void drawProgressBar(GuiGraphics guiGraphics, int x, int y, int width, int height, int progress, int maxProgress, int fillColor) {
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF373737);
        guiGraphics.fill(x, y, x + width, y + height, 0xFF8B8B8B);

        int filled = maxProgress > 0 ? (int) ((long) width * progress / maxProgress) : 0;
        if (filled > 0) {
            guiGraphics.fill(x, y, x + filled, y + height, fillColor);
        }
    }

    // One box per unlocked upgrade slot, on the right-edge rail - a fixed position that clears
    // every machine's own production slot layout.
    static void drawUpgradeRail(GuiGraphics guiGraphics, int leftPos, int topPos, int upgradeSlotCount) {
        for (int i = 0; i < upgradeSlotCount; i++) {
            drawSlotBox(guiGraphics, leftPos + UPGRADE_RAIL_X, topPos + UPGRADE_RAIL_Y + i * UPGRADE_RAIL_SPACING);
        }
    }

    static void drawSlotBox(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8B8B8B);
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFFA2A2A2);
    }
}
