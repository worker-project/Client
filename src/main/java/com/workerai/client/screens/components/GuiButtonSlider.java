package com.workerai.client.screens.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.workerai.client.screens.utils.IntegerOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static com.workerai.client.modules.utils.ModuleScreenManager.*;

public class GuiButtonSlider extends Button {
    public IntegerOption integerOption;
    public float hoveredTime;
    private float sliderValue;
    private final Component description;
    private final IResponder responder;

    public GuiButtonSlider(int pX, int pY, int pWidth, int pHeight, Component pTitle, Component pDescription, IntegerOption option, IResponder responder, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pTitle, pOnPress);

        this.sliderValue = option.normalizeValue(option.getValue());
        this.integerOption = option;
        this.responder = responder;
        this.description = pDescription;
    }

    @Override
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        fill(pPoseStack, this.x - 5, this.y - 15, this.x + this.width + 5, this.y + this.height + 4, CUSTOM_LIGHT_GRAY);
        fill(pPoseStack, this.x, this.y, this.x + this.width, this.y + this.height, CUSTOM_DARK_GRAY);

        drawCenteredString(pPoseStack, Minecraft.getInstance().font, this.getMessage(), this.x + this.width / 2, this.y - 12, CUSTOM_WHITE);
        drawCenteredString(pPoseStack, Minecraft.getInstance().font, integerOption.getValue() + " minutes", this.x + (this.width / 2), this.y + (this.height / 2) - 5, CUSTOM_WHITE);

        fill(pPoseStack, this.x + (int) (this.sliderValue * (float) (this.width - 4)), this.y, this.x + (int) (this.sliderValue * (float) (this.width - 4)) + 4, this.y + this.height, CUSTOM_YELLOW);
    }

    @Override
    public void onClick(double pMouseX, double p_93372_) {
        this.sliderValue = (float) (pMouseX - (this.x + 4)) / (float) (this.width - 8);
        this.sliderValue = Mth.clamp(this.sliderValue, 0.0F, 1.0F);
        float f = this.integerOption.denormalizeValue(this.sliderValue);
        integerOption.setValue((int) f);
        this.sliderValue = this.integerOption.normalizeValue(f);
        responder.onChange(this.integerOption);
    }

    @Override
    protected void onDrag(double pMouseX, double p_93637_, double pMouseY, double p_93639_) {
        this.sliderValue = (float) (pMouseX - (this.x + 4)) / (float) (this.width - 8);
        this.sliderValue = Mth.clamp(this.sliderValue, 0.0F, 1.0F);
        float f = this.integerOption.denormalizeValue(this.sliderValue);
        integerOption.setValue((int) f);
        this.sliderValue = this.integerOption.normalizeValue(f);
        responder.onChange(this.integerOption);
    }

    public interface IResponder {
        void onChange(IntegerOption slider);
    }

    public Component getDescription() {
        return description;
    }
}
