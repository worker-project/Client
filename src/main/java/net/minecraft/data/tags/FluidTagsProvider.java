package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidTagsProvider extends TagsProvider<Fluid>
{
    public FluidTagsProvider(DataGenerator pGenerator)
    {
        super(pGenerator, Registry.FLUID);
    }

    protected void addTags()
    {
        this.tag(FluidTags.WATER).a(Fluids.WATER, Fluids.FLOWING_WATER);
        this.tag(FluidTags.LAVA).a(Fluids.LAVA, Fluids.FLOWING_LAVA);
    }

    public String getName()
    {
        return "Fluid Tags";
    }
}
