package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class LevelStem
{
    public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create((p_63986_) ->
    {
        return p_63986_.group(DimensionType.CODEC.fieldOf("type").forGetter(LevelStem::typeHolder), ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)).apply(p_63986_, p_63986_.stable(LevelStem::new));
    });
    public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
    private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = ImmutableSet.of(OVERWORLD, NETHER, END);
    private final Holder<DimensionType> type;
    private final ChunkGenerator generator;

    public LevelStem(Holder<DimensionType> p_204519_, ChunkGenerator p_204520_)
    {
        this.type = p_204519_;
        this.generator = p_204520_;
    }

    public Holder<DimensionType> typeHolder()
    {
        return this.type;
    }

    public ChunkGenerator generator()
    {
        return this.generator;
    }

    public static Registry<LevelStem> sortMap(Registry<LevelStem> pMappedRegistry)
    {
        WritableRegistry<LevelStem> writableregistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), (Function<LevelStem, Holder.Reference<LevelStem>>)null);

        for (ResourceKey<LevelStem> resourcekey : BUILTIN_ORDER)
        {
            LevelStem levelstem = pMappedRegistry.get(resourcekey);

            if (levelstem != null)
            {
                writableregistry.register(resourcekey, levelstem, pMappedRegistry.lifecycle(levelstem));
            }
        }

        for (Entry<ResourceKey<LevelStem>, LevelStem> entry : pMappedRegistry.entrySet())
        {
            ResourceKey<LevelStem> resourcekey1 = entry.getKey();

            if (!BUILTIN_ORDER.contains(resourcekey1))
            {
                writableregistry.register(resourcekey1, entry.getValue(), pMappedRegistry.lifecycle(entry.getValue()));
            }
        }

        return writableregistry;
    }

    public static boolean stable(long pSeed, Registry<LevelStem> p_204524_)
    {
        if (p_204524_.size() != BUILTIN_ORDER.size())
        {
            return false;
        }
        else
        {
            Optional<LevelStem> optional = p_204524_.getOptional(OVERWORLD);
            Optional<LevelStem> optional1 = p_204524_.getOptional(NETHER);
            Optional<LevelStem> optional2 = p_204524_.getOptional(END);

            if (!optional.isEmpty() && !optional1.isEmpty() && !optional2.isEmpty())
            {
                if (!optional.get().typeHolder().is(DimensionType.OVERWORLD_LOCATION) && !optional.get().typeHolder().is(DimensionType.OVERWORLD_CAVES_LOCATION))
                {
                    return false;
                }
                else if (!optional1.get().typeHolder().is(DimensionType.NETHER_LOCATION))
                {
                    return false;
                }
                else if (!optional2.get().typeHolder().is(DimensionType.END_LOCATION))
                {
                    return false;
                }
                else if (optional1.get().generator() instanceof NoiseBasedChunkGenerator && optional2.get().generator() instanceof NoiseBasedChunkGenerator)
                {
                    NoiseBasedChunkGenerator noisebasedchunkgenerator = (NoiseBasedChunkGenerator)optional1.get().generator();
                    NoiseBasedChunkGenerator noisebasedchunkgenerator1 = (NoiseBasedChunkGenerator)optional2.get().generator();

                    if (!noisebasedchunkgenerator.stable(pSeed, NoiseGeneratorSettings.NETHER))
                    {
                        return false;
                    }
                    else if (!noisebasedchunkgenerator1.stable(pSeed, NoiseGeneratorSettings.END))
                    {
                        return false;
                    }
                    else if (!(noisebasedchunkgenerator.getBiomeSource() instanceof MultiNoiseBiomeSource))
                    {
                        return false;
                    }
                    else
                    {
                        MultiNoiseBiomeSource multinoisebiomesource = (MultiNoiseBiomeSource)noisebasedchunkgenerator.getBiomeSource();

                        if (!multinoisebiomesource.stable(MultiNoiseBiomeSource.Preset.NETHER))
                        {
                            return false;
                        }
                        else
                        {
                            BiomeSource biomesource = optional.get().generator().getBiomeSource();

                            if (biomesource instanceof MultiNoiseBiomeSource && !((MultiNoiseBiomeSource)biomesource).stable(MultiNoiseBiomeSource.Preset.OVERWORLD))
                            {
                                return false;
                            }
                            else if (!(noisebasedchunkgenerator1.getBiomeSource() instanceof TheEndBiomeSource))
                            {
                                return false;
                            }
                            else
                            {
                                TheEndBiomeSource theendbiomesource = (TheEndBiomeSource)noisebasedchunkgenerator1.getBiomeSource();
                                return theendbiomesource.stable(pSeed);
                            }
                        }
                    }
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
    }
}
