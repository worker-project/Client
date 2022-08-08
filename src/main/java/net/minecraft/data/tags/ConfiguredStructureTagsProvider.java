package net.minecraft.data.tags;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.ConfiguredStructureTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;

public class ConfiguredStructureTagsProvider extends TagsProvider < ConfiguredStructureFeature <? , ? >>
{
    public ConfiguredStructureTagsProvider(DataGenerator p_211098_)
    {
        super(p_211098_, BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE);
    }

    protected void addTags()
    {
        this.tag(ConfiguredStructureTags.VILLAGE).a(BuiltinStructures.VILLAGE_PLAINS).a(BuiltinStructures.VILLAGE_DESERT).a(BuiltinStructures.VILLAGE_SAVANNA).a(BuiltinStructures.VILLAGE_SNOWY).a(BuiltinStructures.VILLAGE_TAIGA);
        this.tag(ConfiguredStructureTags.MINESHAFT).a(BuiltinStructures.MINESHAFT).a(BuiltinStructures.MINESHAFT_MESA);
        this.tag(ConfiguredStructureTags.OCEAN_RUIN).a(BuiltinStructures.OCEAN_RUIN_COLD).a(BuiltinStructures.OCEAN_RUIN_WARM);
        this.tag(ConfiguredStructureTags.SHIPWRECK).a(BuiltinStructures.SHIPWRECK).a(BuiltinStructures.SHIPWRECK_BEACHED);
        this.tag(ConfiguredStructureTags.RUINED_PORTAL).a(BuiltinStructures.RUINED_PORTAL_DESERT).a(BuiltinStructures.RUINED_PORTAL_JUNGLE).a(BuiltinStructures.RUINED_PORTAL_MOUNTAIN).a(BuiltinStructures.RUINED_PORTAL_NETHER).a(BuiltinStructures.RUINED_PORTAL_OCEAN).a(BuiltinStructures.RUINED_PORTAL_STANDARD).a(BuiltinStructures.RUINED_PORTAL_SWAMP);
        this.tag(ConfiguredStructureTags.EYE_OF_ENDER_LOCATED).a(BuiltinStructures.STRONGHOLD);
        this.tag(ConfiguredStructureTags.DOLPHIN_LOCATED).addTag(ConfiguredStructureTags.OCEAN_RUIN).addTag(ConfiguredStructureTags.SHIPWRECK);
        this.tag(ConfiguredStructureTags.ON_WOODLAND_EXPLORER_MAPS).a(BuiltinStructures.WOODLAND_MANSION);
        this.tag(ConfiguredStructureTags.ON_OCEAN_EXPLORER_MAPS).a(BuiltinStructures.OCEAN_MONUMENT);
        this.tag(ConfiguredStructureTags.ON_TREASURE_MAPS).a(BuiltinStructures.BURIED_TREASURE);
    }

    public String getName()
    {
        return "Configured Structure Feature Tags";
    }
}
