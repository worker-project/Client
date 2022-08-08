package net.minecraft.world.level.block.grower;

import java.util.Random;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class SpruceTreeGrower extends AbstractMegaTreeGrower
{
    protected Holder <? extends ConfiguredFeature <? , ? >> getConfiguredFeature(Random pRandom, boolean pLargeHive)
    {
        return TreeFeatures.SPRUCE;
    }

    protected Holder <? extends ConfiguredFeature <? , ? >> getConfiguredMegaFeature(Random pRandom)
    {
        return pRandom.nextBoolean() ? TreeFeatures.MEGA_SPRUCE : TreeFeatures.MEGA_PINE;
    }
}
