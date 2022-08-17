package com.workerai.client.screens.utils;

import net.minecraft.util.Mth;

public class IntegerOption {
    private int value;
    private final int min;
    private final int max;
    private final int step;

    public IntegerOption(int value, int min, int max, int step) {
        this.value = value;
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getStep() {
        return step;
    }

    public float normalizeValue(float p_148266_1_) {
        return Mth.clamp((this.snapToStepClamp(p_148266_1_) - this.min) / (this.max - this.min), 0.0F, 1.0F);
    }

    public float denormalizeValue(float p_148262_1_) {
        return this.snapToStepClamp(this.min + (this.max - this.min) * Mth.clamp(p_148262_1_, 0.0F, 1.0F));
    }

    public float snapToStepClamp(float p_148268_1_) {
        p_148268_1_ = this.snapToStep(p_148268_1_);
        return Mth.clamp(p_148268_1_, this.min, this.max);
    }

    protected float snapToStep(float p_148264_1_) {
        if (this.step > 0.0F) {
            p_148264_1_ = this.step * (float) Math.round(p_148264_1_ / this.step);
        }
        return p_148264_1_;
    }
}
