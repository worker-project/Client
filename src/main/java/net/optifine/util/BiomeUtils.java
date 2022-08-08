package net.optifine.util;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.optifine.config.BiomeId;
import net.optifine.override.ChunkCacheOF;

public class BiomeUtils
{
    private static Registry<Biome> biomeRegistry = getBiomeRegistry(Minecraft.getInstance().level);
    public static Biome PLAINS = biomeRegistry.get(Biomes.PLAINS);
    public static Biome SWAMP = biomeRegistry.get(Biomes.SWAMP);
    public static Biome SWAMP_HILLS = SWAMP;

    public static void onWorldChanged(Level worldIn)
    {
        biomeRegistry = getBiomeRegistry(worldIn);
        PLAINS = biomeRegistry.get(Biomes.PLAINS);
        SWAMP = biomeRegistry.get(Biomes.SWAMP);
        SWAMP_HILLS = SWAMP;
    }

    private static Biome getBiomeSafe(Registry<Biome> registry, ResourceKey<Biome> biomeKey, Supplier<Biome> biomeDefault)
    {
        Biome biome = registry.get(biomeKey);

        if (biome == null)
        {
            biome = biomeDefault.get();
        }

        return biome;
    }

    public static Registry<Biome> getBiomeRegistry(Level worldIn)
    {
        return worldIn != null ? worldIn.registryAccess().ownedRegistryOrThrow(Registry.BIOME_REGISTRY) : BuiltinRegistries.BIOME;
    }

    public static Registry<Biome> getBiomeRegistry()
    {
        return biomeRegistry;
    }

    public static ResourceLocation getLocation(Biome biome)
    {
        return getBiomeRegistry().getKey(biome);
    }

    public static int getId(Biome biome)
    {
        return getBiomeRegistry().getId(biome);
    }

    public static int getId(ResourceLocation loc)
    {
        Biome biome = getBiome(loc);
        return getBiomeRegistry().getId(biome);
    }

    public static BiomeId getBiomeId(ResourceLocation loc)
    {
        return BiomeId.make(loc);
    }

    public static Biome getBiome(ResourceLocation loc)
    {
        return getBiomeRegistry().get(loc);
    }

    public static Set<ResourceLocation> getLocations()
    {
        return getBiomeRegistry().keySet();
    }

    public static List<Biome> getBiomes()
    {
        return Lists.newArrayList(biomeRegistry);
    }

    public static List<BiomeId> getBiomeIds()
    {
        return getBiomeIds(getLocations());
    }

    public static List<BiomeId> getBiomeIds(Collection<ResourceLocation> locations)
    {
        List<BiomeId> list = new ArrayList<>();

        for (ResourceLocation resourcelocation : locations)
        {
            BiomeId biomeid = BiomeId.make(resourcelocation);

            if (biomeid != null)
            {
                list.add(biomeid);
            }
        }

        return list;
    }

    public static Biome getBiome(BlockAndTintGetter lightReader, BlockPos blockPos)
    {
        Biome biome = PLAINS;

        if (lightReader instanceof ChunkCacheOF)
        {
            biome = ((ChunkCacheOF)lightReader).getBiome(blockPos);
        }
        else if (lightReader instanceof LevelReader)
        {
            biome = ((LevelReader)lightReader).getBiome(blockPos).value();
        }

        return biome;
    }
}
