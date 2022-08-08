package net.minecraft.data;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.NoiseData;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.StructureSets;
import net.minecraft.data.worldgen.biome.Biomes;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.slf4j.Logger;

public class BuiltinRegistries
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map < ResourceLocation, Supplier <? extends Holder<? >>> LOADERS = Maps.newLinkedHashMap();
    private static final WritableRegistry < WritableRegistry<? >> WRITABLE_REGISTRY = new MappedRegistry<>(ResourceKey.createRegistryKey(new ResourceLocation("root")), Lifecycle.experimental(), (Function < WritableRegistry<?>, Holder.Reference < WritableRegistry<? >>>)null);
    public static final Registry <? extends Registry<? >> REGISTRY = WRITABLE_REGISTRY;
    public static final Registry < ConfiguredWorldCarver<? >> CONFIGURED_CARVER = registerSimple(Registry.CONFIGURED_CARVER_REGISTRY, () ->
    {
        return Carvers.CAVE;
    });
    public static final Registry < ConfiguredFeature <? , ? >> CONFIGURED_FEATURE = registerSimple(Registry.CONFIGURED_FEATURE_REGISTRY, FeatureUtils::bootstrap);
    public static final Registry<PlacedFeature> PLACED_FEATURE = registerSimple(Registry.PLACED_FEATURE_REGISTRY, PlacementUtils::bootstrap);
    public static final Registry < ConfiguredStructureFeature <? , ? >> CONFIGURED_STRUCTURE_FEATURE = registerSimple(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, StructureFeatures::bootstrap);
    public static final Registry<StructureSet> STRUCTURE_SETS = registerSimple(Registry.STRUCTURE_SET_REGISTRY, StructureSets::bootstrap);
    public static final Registry<StructureProcessorList> PROCESSOR_LIST = registerSimple(Registry.PROCESSOR_LIST_REGISTRY, () ->
    {
        return ProcessorLists.ZOMBIE_PLAINS;
    });
    public static final Registry<StructureTemplatePool> TEMPLATE_POOL = registerSimple(Registry.TEMPLATE_POOL_REGISTRY, Pools::bootstrap);
    public static final Registry<Biome> BIOME = registerSimple(Registry.BIOME_REGISTRY, Biomes::bootstrap);
    public static final Registry<NormalNoise.NoiseParameters> NOISE = registerSimple(Registry.NOISE_REGISTRY, NoiseData::bootstrap);
    public static final Registry<DensityFunction> DENSITY_FUNCTION = registerSimple(Registry.DENSITY_FUNCTION_REGISTRY, NoiseRouterData::bootstrap);
    public static final Registry<NoiseGeneratorSettings> NOISE_GENERATOR_SETTINGS = registerSimple(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, NoiseGeneratorSettings::bootstrap);
    public static final RegistryAccess ACCESS;

    private static <T> Registry<T> registerSimple(ResourceKey <? extends Registry<T >> pRegistryKey, Supplier <? extends Holder <? extends T >> pDefaultSupplier)
    {
        return registerSimple(pRegistryKey, Lifecycle.stable(), pDefaultSupplier);
    }

    private static <T> Registry<T> registerSimple(ResourceKey <? extends Registry<T >> pRegistryKey, Lifecycle pLifecycle, Supplier <? extends Holder <? extends T >> pDefaultSupplier)
    {
        return internalRegister(pRegistryKey, new MappedRegistry<>(pRegistryKey, pLifecycle, (Function<T, Holder.Reference<T>>)null), pDefaultSupplier, pLifecycle);
    }

    private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey <? extends Registry<T >> pRegistryKey, R pRegistry, Supplier <? extends Holder <? extends T >> pDefaultSupplier, Lifecycle pLifecycle)
    {
        ResourceLocation resourcelocation = pRegistryKey.location();
        LOADERS.put(resourcelocation, pDefaultSupplier);
        WRITABLE_REGISTRY.register((ResourceKey)pRegistryKey, pRegistry, pLifecycle);
        return pRegistry;
    }

    public static <V extends T, T> Holder<V> registerExact(Registry<T> p_206381_, String p_206382_, V p_206383_)
    {
        return register((Registry<V>)p_206381_, new ResourceLocation(p_206382_), p_206383_);
    }

    public static <T> Holder<T> register(Registry<T> pRegistry, String pId, T pValue)
    {
        return register(pRegistry, new ResourceLocation(pId), pValue);
    }

    public static <T> Holder<T> register(Registry<T> pRegistry, ResourceLocation pId, T pValue)
    {
        return register(pRegistry, ResourceKey.create(pRegistry.key(), pId), pValue);
    }

    public static <T> Holder<T> register(Registry<T> pRegistry, ResourceKey<T> pId, T pValue)
    {
        return ((WritableRegistry)pRegistry).register(pId, pValue, Lifecycle.stable());
    }

    public static void bootstrap()
    {
    }

    static
    {
        LOADERS.forEach((p_206393_, p_206394_) ->
        {
            if (!p_206394_.get().isBound())
            {
                LOGGER.error("Unable to bootstrap registry '{}'", (Object)p_206393_);
            }
        });
        Registry.checkRegistry(WRITABLE_REGISTRY);
        ACCESS = RegistryAccess.fromRegistryOfRegistries(REGISTRY);
    }
}
