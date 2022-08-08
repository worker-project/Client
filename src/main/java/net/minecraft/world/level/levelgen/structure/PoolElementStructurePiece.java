package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.slf4j.Logger;

public class PoolElementStructurePiece extends StructurePiece
{
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final StructurePoolElement element;
    protected BlockPos position;
    private final int groundLevelDelta;
    protected final Rotation rotation;
    private final List<JigsawJunction> junctions = Lists.newArrayList();
    private final StructureManager structureManager;

    public PoolElementStructurePiece(StructureManager pStructureManager, StructurePoolElement pElement, BlockPos pPosition, int pGroundLevelDelta, Rotation pRotation, BoundingBox pBox)
    {
        super(StructurePieceType.JIGSAW, 0, pBox);
        this.structureManager = pStructureManager;
        this.element = pElement;
        this.position = pPosition;
        this.groundLevelDelta = pGroundLevelDelta;
        this.rotation = pRotation;
    }

    public PoolElementStructurePiece(StructurePieceSerializationContext p_192406_, CompoundTag p_192407_)
    {
        super(StructurePieceType.JIGSAW, p_192407_);
        this.structureManager = p_192406_.structureManager();
        this.position = new BlockPos(p_192407_.getInt("PosX"), p_192407_.getInt("PosY"), p_192407_.getInt("PosZ"));
        this.groundLevelDelta = p_192407_.getInt("ground_level_delta");
        DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, p_192406_.registryAccess());
        this.element = StructurePoolElement.CODEC.parse(dynamicops, p_192407_.getCompound("pool_element")).resultOrPartial(LOGGER::error).orElseThrow(() ->
        {
            return new IllegalStateException("Invalid pool element found");
        });
        this.rotation = Rotation.valueOf(p_192407_.getString("rotation"));
        this.boundingBox = this.element.getBoundingBox(this.structureManager, this.position, this.rotation);
        ListTag listtag = p_192407_.getList("junctions", 10);
        this.junctions.clear();
        listtag.forEach((p_204943_) ->
        {
            this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(dynamicops, p_204943_)));
        });
    }

    protected void addAdditionalSaveData(StructurePieceSerializationContext p_192425_, CompoundTag p_192426_)
    {
        p_192426_.putInt("PosX", this.position.getX());
        p_192426_.putInt("PosY", this.position.getY());
        p_192426_.putInt("PosZ", this.position.getZ());
        p_192426_.putInt("ground_level_delta", this.groundLevelDelta);
        DynamicOps<Tag> dynamicops = RegistryOps.create(NbtOps.INSTANCE, p_192425_.registryAccess());
        StructurePoolElement.CODEC.encodeStart(dynamicops, this.element).resultOrPartial(LOGGER::error).ifPresent((p_163125_) ->
        {
            p_192426_.put("pool_element", p_163125_);
        });
        p_192426_.putString("rotation", this.rotation.name());
        ListTag listtag = new ListTag();

        for (JigsawJunction jigsawjunction : this.junctions)
        {
            listtag.add(jigsawjunction.serialize(dynamicops).getValue());
        }

        p_192426_.put("junctions", listtag);
    }

    public void postProcess(WorldGenLevel p_192409_, StructureFeatureManager p_192410_, ChunkGenerator p_192411_, Random p_192412_, BoundingBox p_192413_, ChunkPos p_192414_, BlockPos p_192415_)
    {
        this.place(p_192409_, p_192410_, p_192411_, p_192412_, p_192413_, p_192415_, false);
    }

    public void place(WorldGenLevel p_192417_, StructureFeatureManager p_192418_, ChunkGenerator p_192419_, Random p_192420_, BoundingBox p_192421_, BlockPos p_192422_, boolean p_192423_)
    {
        this.element.place(this.structureManager, p_192417_, p_192418_, p_192419_, this.position, p_192422_, this.rotation, p_192421_, p_192420_, p_192423_);
    }

    public void move(int pX, int pY, int pZ)
    {
        super.move(pX, pY, pZ);
        this.position = this.position.offset(pX, pY, pZ);
    }

    public Rotation getRotation()
    {
        return this.rotation;
    }

    public String toString()
    {
        return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
    }

    public StructurePoolElement getElement()
    {
        return this.element;
    }

    public BlockPos getPosition()
    {
        return this.position;
    }

    public int getGroundLevelDelta()
    {
        return this.groundLevelDelta;
    }

    public void addJunction(JigsawJunction pJunction)
    {
        this.junctions.add(pJunction);
    }

    public List<JigsawJunction> getJunctions()
    {
        return this.junctions;
    }
}
