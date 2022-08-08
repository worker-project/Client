package net.minecraft.data.tags;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class BiomeTagsProvider extends TagsProvider<Biome>
{
    public BiomeTagsProvider(DataGenerator p_211094_)
    {
        super(p_211094_, BuiltinRegistries.BIOME);
    }

    protected void addTags()
    {
        this.tag(BiomeTags.IS_DEEP_OCEAN).a(Biomes.DEEP_FROZEN_OCEAN).a(Biomes.DEEP_COLD_OCEAN).a(Biomes.DEEP_OCEAN).a(Biomes.DEEP_LUKEWARM_OCEAN);
        this.tag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_DEEP_OCEAN).a(Biomes.FROZEN_OCEAN).a(Biomes.OCEAN).a(Biomes.COLD_OCEAN).a(Biomes.LUKEWARM_OCEAN).a(Biomes.WARM_OCEAN);
        this.tag(BiomeTags.IS_BEACH).a(Biomes.BEACH).a(Biomes.SNOWY_BEACH);
        this.tag(BiomeTags.IS_RIVER).a(Biomes.RIVER).a(Biomes.FROZEN_RIVER);
        this.tag(BiomeTags.IS_MOUNTAIN).a(Biomes.MEADOW).a(Biomes.FROZEN_PEAKS).a(Biomes.JAGGED_PEAKS).a(Biomes.STONY_PEAKS).a(Biomes.SNOWY_SLOPES);
        this.tag(BiomeTags.IS_BADLANDS).a(Biomes.BADLANDS).a(Biomes.ERODED_BADLANDS).a(Biomes.WOODED_BADLANDS);
        this.tag(BiomeTags.IS_HILL).a(Biomes.WINDSWEPT_HILLS).a(Biomes.WINDSWEPT_FOREST).a(Biomes.WINDSWEPT_GRAVELLY_HILLS);
        this.tag(BiomeTags.IS_TAIGA).a(Biomes.TAIGA).a(Biomes.SNOWY_TAIGA).a(Biomes.OLD_GROWTH_PINE_TAIGA).a(Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        this.tag(BiomeTags.IS_JUNGLE).a(Biomes.BAMBOO_JUNGLE).a(Biomes.JUNGLE).a(Biomes.SPARSE_JUNGLE);
        this.tag(BiomeTags.IS_FOREST).a(Biomes.FOREST).a(Biomes.FLOWER_FOREST).a(Biomes.BIRCH_FOREST).a(Biomes.OLD_GROWTH_BIRCH_FOREST).a(Biomes.DARK_FOREST).a(Biomes.GROVE);
        this.tag(BiomeTags.IS_NETHER).a(Biomes.NETHER_WASTES).a(Biomes.BASALT_DELTAS).a(Biomes.SOUL_SAND_VALLEY).a(Biomes.CRIMSON_FOREST).a(Biomes.WARPED_FOREST);
        this.tag(BiomeTags.HAS_BURIED_TREASURE).addTag(BiomeTags.IS_BEACH);
        this.tag(BiomeTags.HAS_DESERT_PYRAMID).a(Biomes.DESERT);
        this.tag(BiomeTags.HAS_IGLOO).a(Biomes.SNOWY_TAIGA).a(Biomes.SNOWY_PLAINS).a(Biomes.SNOWY_SLOPES);
        this.tag(BiomeTags.HAS_JUNGLE_TEMPLE).a(Biomes.BAMBOO_JUNGLE).a(Biomes.JUNGLE);
        this.tag(BiomeTags.HAS_MINESHAFT).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER).addTag(BiomeTags.IS_BEACH).addTag(BiomeTags.IS_MOUNTAIN).addTag(BiomeTags.IS_HILL).addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_FOREST).a(Biomes.STONY_SHORE);
        this.tag(BiomeTags.HAS_MINESHAFT).a(Biomes.MUSHROOM_FIELDS).a(Biomes.ICE_SPIKES).a(Biomes.WINDSWEPT_SAVANNA).a(Biomes.DESERT).a(Biomes.SAVANNA).a(Biomes.SNOWY_PLAINS).a(Biomes.PLAINS).a(Biomes.SUNFLOWER_PLAINS).a(Biomes.SWAMP).a(Biomes.SAVANNA_PLATEAU).a(Biomes.DRIPSTONE_CAVES).a(Biomes.LUSH_CAVES);
        this.tag(BiomeTags.HAS_MINESHAFT_MESA).addTag(BiomeTags.IS_BADLANDS);
        this.tag(BiomeTags.HAS_OCEAN_MONUMENT).addTag(BiomeTags.IS_DEEP_OCEAN);
        this.tag(BiomeTags.HAS_OCEAN_RUIN_COLD).a(Biomes.FROZEN_OCEAN).a(Biomes.COLD_OCEAN).a(Biomes.OCEAN).a(Biomes.DEEP_FROZEN_OCEAN).a(Biomes.DEEP_COLD_OCEAN).a(Biomes.DEEP_OCEAN);
        this.tag(BiomeTags.HAS_OCEAN_RUIN_WARM).a(Biomes.LUKEWARM_OCEAN).a(Biomes.WARM_OCEAN).a(Biomes.DEEP_LUKEWARM_OCEAN);
        this.tag(BiomeTags.HAS_PILLAGER_OUTPOST).a(Biomes.DESERT).a(Biomes.PLAINS).a(Biomes.SAVANNA).a(Biomes.SNOWY_PLAINS).a(Biomes.TAIGA).addTag(BiomeTags.IS_MOUNTAIN).a(Biomes.GROVE);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_DESERT).a(Biomes.DESERT);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_JUNGLE).addTag(BiomeTags.IS_JUNGLE);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_OCEAN).addTag(BiomeTags.IS_OCEAN);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_SWAMP).a(Biomes.SWAMP);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN).addTag(BiomeTags.IS_BADLANDS).addTag(BiomeTags.IS_HILL).a(Biomes.SAVANNA_PLATEAU).a(Biomes.WINDSWEPT_SAVANNA).a(Biomes.STONY_SHORE).addTag(BiomeTags.IS_MOUNTAIN);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_STANDARD).addTag(BiomeTags.IS_BEACH).addTag(BiomeTags.IS_RIVER).addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_FOREST).a(Biomes.MUSHROOM_FIELDS).a(Biomes.ICE_SPIKES).a(Biomes.DRIPSTONE_CAVES).a(Biomes.LUSH_CAVES).a(Biomes.SAVANNA).a(Biomes.SNOWY_PLAINS).a(Biomes.PLAINS).a(Biomes.SUNFLOWER_PLAINS);
        this.tag(BiomeTags.HAS_SHIPWRECK_BEACHED).addTag(BiomeTags.IS_BEACH);
        this.tag(BiomeTags.HAS_SHIPWRECK).addTag(BiomeTags.IS_OCEAN);
        this.tag(BiomeTags.HAS_SWAMP_HUT).a(Biomes.SWAMP);
        this.tag(BiomeTags.HAS_VILLAGE_DESERT).a(Biomes.DESERT);
        this.tag(BiomeTags.HAS_VILLAGE_PLAINS).a(Biomes.PLAINS).a(Biomes.MEADOW);
        this.tag(BiomeTags.HAS_VILLAGE_SAVANNA).a(Biomes.SAVANNA);
        this.tag(BiomeTags.HAS_VILLAGE_SNOWY).a(Biomes.SNOWY_PLAINS);
        this.tag(BiomeTags.HAS_VILLAGE_TAIGA).a(Biomes.TAIGA);
        this.tag(BiomeTags.HAS_WOODLAND_MANSION).a(Biomes.DARK_FOREST);
        this.tag(BiomeTags.HAS_STRONGHOLD).a(Biomes.PLAINS).a(Biomes.SUNFLOWER_PLAINS).a(Biomes.SNOWY_PLAINS).a(Biomes.ICE_SPIKES).a(Biomes.DESERT).a(Biomes.FOREST).a(Biomes.FLOWER_FOREST).a(Biomes.BIRCH_FOREST).a(Biomes.DARK_FOREST).a(Biomes.OLD_GROWTH_BIRCH_FOREST).a(Biomes.OLD_GROWTH_PINE_TAIGA).a(Biomes.OLD_GROWTH_SPRUCE_TAIGA).a(Biomes.TAIGA).a(Biomes.SNOWY_TAIGA).a(Biomes.SAVANNA).a(Biomes.SAVANNA_PLATEAU).a(Biomes.WINDSWEPT_HILLS).a(Biomes.WINDSWEPT_GRAVELLY_HILLS).a(Biomes.WINDSWEPT_FOREST).a(Biomes.WINDSWEPT_SAVANNA).a(Biomes.JUNGLE).a(Biomes.SPARSE_JUNGLE).a(Biomes.BAMBOO_JUNGLE).a(Biomes.BADLANDS).a(Biomes.ERODED_BADLANDS).a(Biomes.WOODED_BADLANDS).a(Biomes.MEADOW).a(Biomes.GROVE).a(Biomes.SNOWY_SLOPES).a(Biomes.FROZEN_PEAKS).a(Biomes.JAGGED_PEAKS).a(Biomes.STONY_PEAKS).a(Biomes.MUSHROOM_FIELDS).a(Biomes.DRIPSTONE_CAVES).a(Biomes.LUSH_CAVES);
        this.tag(BiomeTags.HAS_NETHER_FORTRESS).addTag(BiomeTags.IS_NETHER);
        this.tag(BiomeTags.HAS_NETHER_FOSSIL).a(Biomes.SOUL_SAND_VALLEY);
        this.tag(BiomeTags.HAS_BASTION_REMNANT).a(Biomes.CRIMSON_FOREST).a(Biomes.NETHER_WASTES).a(Biomes.SOUL_SAND_VALLEY).a(Biomes.WARPED_FOREST);
        this.tag(BiomeTags.HAS_RUINED_PORTAL_NETHER).addTag(BiomeTags.IS_NETHER);
        this.tag(BiomeTags.HAS_END_CITY).a(Biomes.END_HIGHLANDS).a(Biomes.END_MIDLANDS);
    }

    public String getName()
    {
        return "Biome Tags";
    }
}
