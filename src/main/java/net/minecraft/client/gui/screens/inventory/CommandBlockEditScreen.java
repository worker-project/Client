package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class CommandBlockEditScreen extends AbstractCommandBlockEditScreen
{
    private final CommandBlockEntity autoCommandBlock;
    private CycleButton<CommandBlockEntity.Mode> modeButton;
    private CycleButton<Boolean> conditionalButton;
    private CycleButton<Boolean> autoexecButton;
    private CommandBlockEntity.Mode mode = CommandBlockEntity.Mode.REDSTONE;
    private boolean conditional;
    private boolean autoexec;

    public CommandBlockEditScreen(CommandBlockEntity pAutoCommandBlock)
    {
        this.autoCommandBlock = pAutoCommandBlock;
    }

    BaseCommandBlock getCommandBlock()
    {
        return this.autoCommandBlock.getCommandBlock();
    }

    int getPreviousY()
    {
        return 135;
    }

    protected void init()
    {
        super.init();
        this.modeButton = this.addRenderableWidget(CycleButton.<CommandBlockEntity.Mode>builder((p_169719_) ->
        {
            switch (p_169719_)
            {
                case SEQUENCE:
                    return new TranslatableComponent("advMode.mode.sequence");

                case AUTO:
                    return new TranslatableComponent("advMode.mode.auto");

                case REDSTONE:
                default:
                    return new TranslatableComponent("advMode.mode.redstone");
            }
        }).a(CommandBlockEntity.Mode.values()).displayOnlyValue().withInitialValue(this.mode).create(this.width / 2 - 50 - 100 - 4, 165, 100, 20, new TranslatableComponent("advMode.mode"), (p_169721_, p_169722_) ->
        {
            this.mode = p_169722_;
        }));
        this.conditionalButton = this.addRenderableWidget(CycleButton.booleanBuilder(new TranslatableComponent("advMode.mode.conditional"), new TranslatableComponent("advMode.mode.unconditional")).displayOnlyValue().withInitialValue(this.conditional).create(this.width / 2 - 50, 165, 100, 20, new TranslatableComponent("advMode.type"), (p_169727_, p_169728_) ->
        {
            this.conditional = p_169728_;
        }));
        this.autoexecButton = this.addRenderableWidget(CycleButton.booleanBuilder(new TranslatableComponent("advMode.mode.autoexec.bat"), new TranslatableComponent("advMode.mode.redstoneTriggered")).displayOnlyValue().withInitialValue(this.autoexec).create(this.width / 2 + 50 + 4, 165, 100, 20, new TranslatableComponent("advMode.triggering"), (p_169724_, p_169725_) ->
        {
            this.autoexec = p_169725_;
        }));
        this.enableControls(false);
    }

    private void enableControls(boolean pActive)
    {
        this.doneButton.active = pActive;
        this.outputButton.active = pActive;
        this.modeButton.active = pActive;
        this.conditionalButton.active = pActive;
        this.autoexecButton.active = pActive;
    }

    public void updateGui()
    {
        BaseCommandBlock basecommandblock = this.autoCommandBlock.getCommandBlock();
        this.commandEdit.setValue(basecommandblock.getCommand());
        boolean flag = basecommandblock.isTrackOutput();
        this.mode = this.autoCommandBlock.getMode();
        this.conditional = this.autoCommandBlock.isConditional();
        this.autoexec = this.autoCommandBlock.isAutomatic();
        this.outputButton.setValue(flag);
        this.modeButton.setValue(this.mode);
        this.conditionalButton.setValue(this.conditional);
        this.autoexecButton.setValue(this.autoexec);
        this.updatePreviousOutput(flag);
        this.enableControls(true);
    }

    public void resize(Minecraft pMinecraft, int pWidth, int pHeight)
    {
        super.resize(pMinecraft, pWidth, pHeight);
        this.enableControls(true);
    }

    protected void populateAndSendPacket(BaseCommandBlock pCommandBlock)
    {
        this.minecraft.getConnection().send(new ServerboundSetCommandBlockPacket(new BlockPos(pCommandBlock.getPosition()), this.commandEdit.getValue(), this.mode, pCommandBlock.isTrackOutput(), this.conditional, this.autoexec));
    }
}
