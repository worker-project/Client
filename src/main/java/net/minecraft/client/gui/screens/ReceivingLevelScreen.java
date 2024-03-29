package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.optifine.CustomLoadingScreen;
import net.optifine.CustomLoadingScreens;

public class ReceivingLevelScreen extends Screen
{
    private static final Component DOWNLOADING_TERRAIN_TEXT = new TranslatableComponent("multiplayer.downloadingTerrain");
    private static final long CHUNK_LOADING_START_WAIT_LIMIT_MS = 2000L;
    private boolean loadingPacketsReceived = false;
    private boolean oneTickSkipped = false;
    private final long createdAt = System.currentTimeMillis();
    private CustomLoadingScreen customLoadingScreen = CustomLoadingScreens.getCustomLoadingScreen();

    public ReceivingLevelScreen()
    {
        super(NarratorChatListener.NO_TITLE);
    }

    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        if (this.customLoadingScreen != null)
        {
            this.customLoadingScreen.drawBackground(this.width, this.height);
        }
        else
        {
            this.renderDirtBackground(0);
        }

        drawCenteredString(pPoseStack, this.font, DOWNLOADING_TERRAIN_TEXT, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    public void tick()
    {
        boolean flag = this.oneTickSkipped || System.currentTimeMillis() > this.createdAt + 2000L;

        if (flag && this.minecraft != null && this.minecraft.player != null)
        {
            BlockPos blockpos = this.minecraft.player.blockPosition();
            boolean flag1 = this.minecraft.level != null && this.minecraft.level.isOutsideBuildHeight(blockpos.getY());

            if (flag1 || this.minecraft.levelRenderer.isChunkCompiled(blockpos))
            {
                this.onClose();
            }

            if (this.loadingPacketsReceived)
            {
                this.oneTickSkipped = true;
            }
        }
    }

    public void loadingPacketsReceived()
    {
        this.loadingPacketsReceived = true;
    }

    public boolean isPauseScreen()
    {
        return false;
    }
}
