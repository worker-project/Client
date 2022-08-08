package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.utils.ClientInfos;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class PauseScreen extends Screen
{
    private static final String URL_FEEDBACK_SNAPSHOT = "https://aka.ms/snapshotfeedback?ref=game";
    private static final String URL_FEEDBACK_RELEASE = "https://aka.ms/javafeedback?ref=game";
    private static final String URL_BUGS = "https://aka.ms/snapshotbugs?ref=game";
    private final boolean showPauseMenu;

    public PauseScreen(boolean pShowPauseMenu)
    {
        super(pShowPauseMenu ? new TranslatableComponent("menu.game") : new TranslatableComponent("menu.paused"));
        this.showPauseMenu = pShowPauseMenu;
    }

    protected void init()
    {
        if (this.showPauseMenu)
        {
            this.createPauseMenu();
        }
    }

    private void createPauseMenu()
    {
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslatableComponent("menu.returnToGame"), (p_96337_) ->
        {
            this.minecraft.setScreen((Screen)null);
            this.minecraft.mouseHandler.grabMouse();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 204, 20, new TextComponent("WorkerIA Settings"), (p_96338_) ->
        {
            System.out.println("WorkerIA Settings");
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, new TextComponent("Website"), (p_96318_) ->
        {
            this.minecraft.setScreen(new ConfirmLinkScreen((p_169337_) -> {
                if (p_169337_)
                {
                    Util.getPlatform().openUri(ClientInfos.URL);
                }

                this.minecraft.setScreen(this);
            }, "https://" + ClientInfos.URL.toLowerCase(), true));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, new TranslatableComponent("menu.options"), (p_96323_) ->
        {
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 204, 20, new TranslatableComponent("menu.disconnect"), (p_96315_) ->
        {
            boolean flag = this.minecraft.isLocalServer();
            p_96315_.active = false;
            this.minecraft.level.disconnect();

            if (flag)
            {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
            }
            else {
                this.minecraft.clearLevel();
            }

            TitleScreen titlescreen = new TitleScreen();

            if (flag)
            {
                this.minecraft.setScreen(titlescreen);
            }
            else {
                this.minecraft.setScreen(new JoinMultiplayerScreen(titlescreen));
            }
        }));
    }

    public void tick()
    {
        super.tick();
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        if (this.showPauseMenu)
        {
            this.renderBackground(pPoseStack);
            drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 40, 16777215);
        }
        else
        {
            drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 10, 16777215);
        }

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
