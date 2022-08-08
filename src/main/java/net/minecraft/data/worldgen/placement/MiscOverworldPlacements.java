package net.minecraft.data.worldgen.placement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.heightproviders.VeryBiasedToBottomHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdFilter;

public class MiscOverworldPlacements
{
    public static final Holder<PlacedFeature> ICE_SPIKE = PlacementUtils.a("ice_spike", MiscOverworldFeatures.ICE_SPIKE, CountPlacement.of(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
    public static final Holder<PlacedFeature> ICE_PATCH = PlacementUtils.a("ice_patch", MiscOverworldFeatures.ICE_PATCH, CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
    public static final Holder<PlacedFeature> FOREST_ROCK = PlacementUtils.a("forest_rock", MiscOverworldFeatures.FOREST_ROCK, CountPlacement.of(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
    public static final Holder<PlacedFeature> ICEBERG_PACKED = PlacementUtils.a("iceberg_packed", MiscOverworldFeatures.ICEBERG_PACKED, RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), BiomeFilter.biome());
    public static final Holder<PlacedFeature> ICEBERG_BLUE = PlacementUtils.a("iceberg_blue", MiscOverworldFeatures.ICEBERG_BLUE, RarityFilter.onAverageOnceEvery(200), InSquarePlacement.spread(), BiomeFilter.biome());
    public static final Holder<PlacedFeature> BLUE_ICE = PlacementUtils.a("blue_ice", MiscOverworldFeatures.BLUE_ICE, CountPlacement.of(UniformInt.of(0, 19)), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(61)), BiomeFilter.biome());
    public static final Holder<PlacedFeature> LAKE_LAVA_UNDERGROUND = PlacementUtils.a("lake_lava_underground", MiscOverworldFeatures.LAKE_LAVA, RarityFilter.onAverageOnceEvery(9), InSquarePlacement.spread(), HeightRangePlacement.of(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())), EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.allOf(BlockPredicate.not(BlockPredicate.ONLY_IN_AIR_PREDICATE), BlockPredicate.insideWorld(new BlockPos(0, -5, 0))), 32), SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -5), BiomeFilter.biome());
    public static final Holder<PlacedFeature> LAKE_LAVA_SURFACE = PlacementUtils.a("lake_lava_surface", MiscOverworldFeatures.LAKE_LAVA, RarityFilter.onAverageOnceEvery(200), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
    public static final Holder<PlacedFeature> DISK_CLAY = PlacementUtils.a("disk_clay", MiscOverworldFeatures.DISK_CLAY, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome());
    public static final Holder<PlacedFeature> DISK_GRAVEL = PlacementUtils.a("disk_gravel", MiscOverworldFeatures.DISK_GRAVEL, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome());
    public static final Holder<PlacedFeature> DISK_SAND = PlacementUtils.a("disk_sand", MiscOverworldFeatures.DISK_SAND, CountPlacement.of(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_TOP_SOLID, BiomeFilter.biome());
    public static final Holder<PlacedFeature> FREEZE_TOP_LAYER = PlacementUtils.a("freeze_top_layer", MiscOverworldFeatures.FREEZE_TOP_LAYER, BiomeFilter.biome());
    public static final Holder<PlacedFeature> VOID_START_PLATFORM = PlacementUtils.a("void_start_platform", MiscOverworldFeatures.VOID_START_PLATFORM, BiomeFilter.biome());
    public static final Holder<PlacedFeature> DESERT_WELL = PlacementUtils.a("desert_well", MiscOverworldFeatures.DESERT_WELL, RarityFilter.onAverageOnceEvery(1000), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
    public static final Holder<PlacedFeature> SPRING_LAVA = PlacementUtils.a("spring_lava", MiscOverworldFeatures.SPRING_LAVA_OVERWORLD, CountPlacement.of(20), InSquarePlacement.spread(), HeightRangePlacement.of(VeryBiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)), BiomeFilter.biome());
    public static final Holder<PlacedFeature> SPRING_LAVA_FROZEN = PlacementUtils.a("spring_lava_frozen", MiscOverworldFeatures.SPRING_LAVA_FROZEN, CountPlacement.of(20), InSquarePlacement.spread(), HeightRangePlacement.of(VeryBiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)), BiomeFilter.biome());
    public static final Holder<PlacedFeature> SPRING_WATER = PlacementUtils.a("spring_water", MiscOverworldFeatures.SPRING_WATER, CountPlacement.of(25), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(192)), BiomeFilter.biome());
}
