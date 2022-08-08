package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class DragonBreathParticle extends TextureSheetParticle
{
    private static final int COLOR_MIN = 11993298;
    private static final int COLOR_MAX = 14614777;
    private static final float COLOR_MIN_RED = 0.7176471F;
    private static final float COLOR_MIN_GREEN = 0.0F;
    private static final float COLOR_MIN_BLUE = 0.8235294F;
    private static final float COLOR_MAX_RED = 0.8745098F;
    private static final float COLOR_MAX_GREEN = 0.0F;
    private static final float COLOR_MAX_BLUE = 0.9764706F;
    private boolean hasHitGround;
    private final SpriteSet sprites;

    DragonBreathParticle(ClientLevel pLevel, double pX, double p_106007_, double pY, double p_106009_, double pZ, double p_106011_, SpriteSet pXSpeed)
    {
        super(pLevel, pX, p_106007_, pY);
        this.friction = 0.96F;
        this.xd = p_106009_;
        this.yd = pZ;
        this.zd = p_106011_;
        this.rCol = Mth.nextFloat(this.random, 0.7176471F, 0.8745098F);
        this.gCol = Mth.nextFloat(this.random, 0.0F, 0.0F);
        this.bCol = Mth.nextFloat(this.random, 0.8235294F, 0.9764706F);
        this.quadSize *= 0.75F;
        this.lifetime = (int)(20.0D / ((double)this.random.nextFloat() * 0.8D + 0.2D));
        this.hasHitGround = false;
        this.hasPhysics = false;
        this.sprites = pXSpeed;
        this.setSpriteFromAge(pXSpeed);
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime)
        {
            this.remove();
        }
        else
        {
            this.setSpriteFromAge(this.sprites);

            if (this.onGround)
            {
                this.yd = 0.0D;
                this.hasHitGround = true;
            }

            if (this.hasHitGround)
            {
                this.yd += 0.002D;
            }

            this.move(this.xd, this.yd, this.zd);

            if (this.y == this.yo)
            {
                this.xd *= 1.1D;
                this.zd *= 1.1D;
            }

            this.xd *= (double)this.friction;
            this.zd *= (double)this.friction;

            if (this.hasHitGround)
            {
                this.yd *= (double)this.friction;
            }
        }
    }

    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public float getQuadSize(float pScaleFactor)
    {
        return this.quadSize * Mth.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType>
    {
        private final SpriteSet sprites;

        public Provider(SpriteSet pSprites)
        {
            this.sprites = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double p_106043_, double pY, double p_106045_, double pZ, double p_106047_)
        {
            return new DragonBreathParticle(pLevel, pX, p_106043_, pY, p_106045_, pZ, p_106047_, this.sprites);
        }
    }
}
