package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;

public class RemotePlayer extends AbstractClientPlayer
{
    public RemotePlayer(ClientLevel p_108767_, GameProfile p_108768_)
    {
        super(p_108767_, p_108768_);
        this.maxUpStep = 1.0F;
        this.noPhysics = true;
    }

    public boolean shouldRenderAtSqrDistance(double pDistance)
    {
        double d0 = this.getBoundingBox().getSize() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 *= 64.0D * getViewScale();
        return pDistance < d0 * d0;
    }

    public boolean hurt(DamageSource pSource, float pAmount)
    {
        return true;
    }

    public void tick()
    {
        super.tick();
        this.calculateEntityAnimation(this, false);
    }

    public void aiStep()
    {
        if (this.lerpSteps > 0)
        {
            double d0 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
            double d1 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
            double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
            this.setYRot(this.getYRot() + (float)Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot()) / (float)this.lerpSteps);
            this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
            --this.lerpSteps;
            this.setPos(d0, d1, d2);
            this.setRot(this.getYRot(), this.getXRot());
        }

        if (this.lerpHeadSteps > 0)
        {
            this.yHeadRot += (float)(Mth.wrapDegrees(this.lyHeadRot - (double)this.yHeadRot) / (double)this.lerpHeadSteps);
            --this.lerpHeadSteps;
        }

        this.oBob = this.bob;
        this.updateSwingTime();
        float f;

        if (this.onGround && !this.isDeadOrDying())
        {
            f = (float)Math.min(0.1D, this.getDeltaMovement().horizontalDistance());
        }
        else
        {
            f = 0.0F;
        }

        this.bob += (f - this.bob) * 0.4F;
        this.level.getProfiler().push("push");
        this.pushEntities();
        this.level.getProfiler().pop();
    }

    protected void updatePlayerPose()
    {
    }

    public void sendMessage(Component pComponent, UUID pSenderUUID)
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (!minecraft.isBlocked(pSenderUUID))
        {
            minecraft.gui.getChat().addMessage(pComponent);
        }
    }
}
