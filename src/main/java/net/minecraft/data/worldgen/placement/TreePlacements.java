package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class TreePlacements
{
    public static final Holder<PlacedFeature> CRIMSON_FUNGI = PlacementUtils.a("crimson_fungi", TreeFeatures.CRIMSON_FUNGUS, CountOnEveryLayerPlacement.of(8), BiomeFilter.biome());
    public static final Holder<PlacedFeature> WARPED_FUNGI = PlacementUtils.a("warped_fungi", TreeFeatures.WARPED_FUNGUS, CountOnEveryLayerPlacement.of(8), BiomeFilter.biome());
    public static final Holder<PlacedFeature> OAK_CHECKED = PlacementUtils.a("oak_checked", TreeFeatures.OAK, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
    public static final Holder<PlacedFeature> DARK_OAK_CHECKED = PlacementUtils.a("dark_oak_checked", TreeFeatures.DARK_OAK, PlacementUtils.filteredByBlockSurvival(Blocks.DARK_OAK_SAPLING));
    public static final Holder<PlacedFeature> BIRCH_CHECKED = PlacementUtils.a("birch_checked", TreeFeatures.BIRCH, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
    public static final Holder<PlacedFeature> ACACIA_CHECKED = PlacementUtils.a("acacia_checked", TreeFeatures.ACACIA, PlacementUtils.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
    public static final Holder<PlacedFeature> SPRUCE_CHECKED = PlacementUtils.a("spruce_checked", TreeFeatures.SPRUCE, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
    public static final BlockPredicate SNOW_TREE_PREDICATE = BlockPredicate.matchesBlocks(List.of(Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW), new BlockPos(0, -1, 0));
    public static final List<PlacementModifier> SNOW_TREE_FILTER_DECORATOR = List.of(EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.not(BlockPredicate.matchesBlock(Blocks.POWDER_SNOW, BlockPos.ZERO)), 8), BlockPredicateFilter.forPredicate(SNOW_TREE_PREDICATE));
    public static final Holder<PlacedFeature> PINE_ON_SNOW = PlacementUtils.register("pine_on_snow", TreeFeatures.PINE, SNOW_TREE_FILTER_DECORATOR);
    public static final Holder<PlacedFeature> SPRUCE_ON_SNOW = PlacementUtils.register("spruce_on_snow", TreeFeatures.SPRUCE, SNOW_TREE_FILTER_DECORATOR);
    public static final Holder<PlacedFeature> PINE_CHECKED = PlacementUtils.a("pine_checked", TreeFeatures.PINE, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
    public static final Holder<PlacedFeature> JUNGLE_TREE_CHECKED = PlacementUtils.a("jungle_tree", TreeFeatures.JUNGLE_TREE, PlacementUtils.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING));
    public static final Holder<PlacedFeature> FANCY_OAK_CHECKED = PlacementUtils.a("fancy_oak_checked", TreeFeatures.FANCY_OAK, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
    public static final Holder<PlacedFeature> MEGA_JUNGLE_TREE_CHECKED = PlacementUtils.a("mega_jungle_tree_checked", TreeFeatures.MEGA_JUNGLE_TREE, PlacementUtils.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING));
    public static final Holder<PlacedFeature> MEGA_SPRUCE_CHECKED = PlacementUtils.a("mega_spruce_checked", TreeFeatures.MEGA_SPRUCE, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
    public static final Holder<PlacedFeature> MEGA_PINE_CHECKED = PlacementUtils.a("mega_pine_checked", TreeFeatures.MEGA_PINE, PlacementUtils.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
    public static final Holder<PlacedFeature> JUNGLE_BUSH = PlacementUtils.a("jungle_bush", TreeFeatures.JUNGLE_BUSH, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
    public static final Holder<PlacedFeature> SUPER_BIRCH_BEES_0002 = PlacementUtils.a("super_birch_bees_0002", TreeFeatures.SUPER_BIRCH_BEES_0002, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
    public static final Holder<PlacedFeature> SUPER_BIRCH_BEES = PlacementUtils.a("super_birch_bees", TreeFeatures.SUPER_BIRCH_BEES, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
    public static final Holder<PlacedFeature> OAK_BEES_0002 = PlacementUtils.a("oak_bees_0002", TreeFeatures.OAK_BEES_0002, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
    public static final Holder<PlacedFeature> OAK_BEES_002 = PlacementUtils.a("oak_bees_002", TreeFeatures.OAK_BEES_002, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
    public static final Holder<PlacedFeature> BIRCH_BEES_0002_PLACED = PlacementUtils.a("birch_bees_0002", TreeFeatures.BIRCH_BEES_0002, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
    public static final Holder<PlacedFeature> BIRCH_BEES_002 = PlacementUtils.a("birch_bees_002", TreeFeatures.BIRCH_BEES_002, PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
    public static final Holder<PlacedFeature> FANCY_OAK_BEES_0002 = PlacementUtils.a("fancy_oak_bees_0002", TreeFeatures.FANCY_OAK_BEES_0002, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
    public static final Holder<PlacedFeature> FANCY_OAK_BEES_002 = PlacementUtils.a("fancy_oak_bees_002", TreeFeatures.FANCY_OAK_BEES_002, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
    public static final Holder<PlacedFeature> FANCY_OAK_BEES = PlacementUtils.a("fancy_oak_bees", TreeFeatures.FANCY_OAK_BEES, PlacementUtils.filteredByBlockSurvival(Blocks.OAK_SAPLING));
}
