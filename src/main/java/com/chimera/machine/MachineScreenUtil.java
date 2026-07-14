package com.chimera.machine;

import net.minecraft.client.gui.GuiGraphics;

// Small shared helper so the three machine screens don't each hand-roll the same bordered
// progress bar (1px dark outline + empty track + colored fill).
final class MachineScreenUtil {

    private MachineScreenUtil() {}

    static void drawProgressBar(GuiGraphics guiGraphics, int x, int y, int width, int height, int progress, int maxProgress, int fillColor) {
        guiGraphics.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF373737);
        guiGraphics.fill(x, y, x + width, y + height, 0xFF8B8B8B);

        int filled = maxProgress > 0 ? (int) ((long) width * progress / maxProgress) : 0;
        if (filled > 0) {
            guiGraphics.fill(x, y, x + filled, y + height, fillColor);
        }
    }
}
