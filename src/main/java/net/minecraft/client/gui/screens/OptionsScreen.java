package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.Difficulty;

public class OptionsScreen extends Screen
{
    private static final Option[] OPTION_SCREEN_OPTIONS = new Option[] {Option.FOV};
    private final Screen lastScreen;
    private final Options options;
    private CycleButton<Difficulty> difficultyButton;
    private LockIconButton lockButton;

    public OptionsScreen(Screen pLastScreen, Options pOptions)
    {
        super(new TranslatableComponent("options.title"));
        this.lastScreen = pLastScreen;
        this.options = pOptions;
    }

    protected void init()
    {
        int i = 0;

        for (Option option : OPTION_SCREEN_OPTIONS)
        {
            int j = this.width / 2 - 155 + i % 2 * 160;
            int k = this.height / 6 - 12 + 24 * (i >> 1);
            this.addRenderableWidget(option.createButton(this.minecraft.options, this.width / 2 - 75, k, 150));
            ++i;
        }

        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 + 48 - 6, 150, 20, new TranslatableComponent("options.sounds"), (p_96274_) ->
        {
            this.minecraft.setScreen(new SoundOptionsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 48 - 6, 150, 20, new TranslatableComponent("options.video"), (p_96272_) ->
        {
            this.minecraft.setScreen(new VideoSettingsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 + 72 - 6, 150, 20, new TranslatableComponent("options.controls"), (p_96270_) ->
        {
            this.minecraft.setScreen(new ControlsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 72 - 6, 150, 20, new TranslatableComponent("options.language"), (p_96268_) ->
        {
            this.minecraft.setScreen(new LanguageSelectScreen(this, this.options, this.minecraft.getLanguageManager()));
        }));
        this.addRenderableWidget(new Button(this.width / 2 + 5, this.height / 6 + 96 - 6, 150, 20, new TranslatableComponent("options.chat.title"), (p_96266_) ->
        {
            this.minecraft.setScreen(new ChatOptionsScreen(this, this.options));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height / 6 + 96 - 6, 150, 20, new TranslatableComponent("options.resourcepack"), (p_96263_) ->
        {
            this.minecraft.setScreen(new PackSelectionScreen(this, this.minecraft.getResourcePackRepository(), this::updatePackList, this.minecraft.getResourcePackDirectory(), new TranslatableComponent("resourcePack.title")));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 6 + 144 - 6, 200, 20, CommonComponents.GUI_DONE, (p_96257_) ->
        {
            this.minecraft.setScreen(this.lastScreen);
        }));
    }

    public static CycleButton<Difficulty> createDifficultyButton(int p_193847_, int p_193848_, int p_193849_, String p_193850_, Minecraft p_193851_)
    {
        return CycleButton.builder(Difficulty::getDisplayName).a(Difficulty.values()).withInitialValue(p_193851_.level.getDifficulty()).create(p_193848_ / 2 - 155 + p_193847_ % 2 * 160, p_193849_ / 6 - 12 + 24 * (p_193847_ >> 1), 150, 20, new TranslatableComponent(p_193850_), (p_193854_, p_193855_) ->
        {
            p_193851_.getConnection().send(new ServerboundChangeDifficultyPacket(p_193855_));
        });
    }

    private void updatePackList(PackRepository p_96245_)
    {
        List<String> list = ImmutableList.copyOf(this.options.resourcePacks);
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();

        for (Pack pack : p_96245_.getSelectedPacks())
        {
            if (!pack.isFixedPosition())
            {
                this.options.resourcePacks.add(pack.getId());

                if (!pack.getCompatibility().isCompatible())
                {
                    this.options.incompatibleResourcePacks.add(pack.getId());
                }
            }
        }

        this.options.save();
        List<String> list1 = ImmutableList.copyOf(this.options.resourcePacks);

        if (!list1.equals(list))
        {
            this.minecraft.reloadResourcePacks();
        }
    }

    private void lockCallback(boolean p_96261_)
    {
        this.minecraft.setScreen(this);

        if (p_96261_ && this.minecraft.level != null)
        {
            this.minecraft.getConnection().send(new ServerboundLockDifficultyPacket(true));
            this.lockButton.setLocked(true);
            this.lockButton.active = false;
            this.difficultyButton.active = false;
        }
    }

    public void removed()
    {
        this.options.save();
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        this.renderBackground(pPoseStack);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 15, 16777215);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }
}
