package net.minecraft.client.gui.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.WarningScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class DisclaimerScreen extends WarningScreen {
    private static final Component TITLE = (new TextComponent("USAGE DISCLAIMER")).withStyle(ChatFormatting.BOLD);
    private static final Component CONTENT = new TextComponent("""
            By using WorkerAI client you agree on multiple terms:\s
                - We won't be responsible for any damages caused to your account.\s
                - No sharing is allowed otherwise you will lose all your access.
                - We aren't under no circumstances related to Mojang and Microsoft.""");
    private static final Component CHECK = new TextComponent("I fully understand!");

    public DisclaimerScreen(Screen pPrevious)
    {
        super(TITLE, CONTENT, CHECK, pPrevious);
    }

    protected void initButtons(int p_210904_)
    {
        this.addRenderableWidget(new Button(this.width / 2 - 155, 100 + p_210904_, 150, 20, new TextComponent("Confirm"), (p_210908_) ->
        {
            if (this.stopShowing.selected())
            {
                this.minecraft.options.disclaimerAccepted = true;
                this.minecraft.options.save();
                this.minecraft.setScreen(previous);
            }
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, 100 + p_210904_, 150, 20, new TextComponent("Deny"), (p_210906_) ->
        {
            this.minecraft.stop();
        }));
    }
}
