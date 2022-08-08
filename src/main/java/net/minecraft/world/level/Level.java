package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Scoreboard;

public abstract class Level implements LevelAccessor, AutoCloseable
{
    public static final Codec<ResourceKey<Level>> RESOURCE_KEY_CODEC = ResourceLocation.CODEC.xmap(ResourceKey.elementKey(Registry.DIMENSION_REGISTRY), ResourceKey::location);
    public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("overworld"));
    public static final ResourceKey<Level> NETHER = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("the_nether"));
    public static final ResourceKey<Level> END = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("the_end"));
    public static final int MAX_LEVEL_SIZE = 30000000;
    public static final int LONG_PARTICLE_CLIP_RANGE = 512;
    public static final int SHORT_PARTICLE_CLIP_RANGE = 32;
    private static final Direction[] DIRECTIONS = Direction.values();
    public static final int MAX_BRIGHTNESS = 15;
    public static final int TICKS_PER_DAY = 24000;
    public static final int MAX_ENTITY_SPAWN_Y = 20000000;
    public static final int MIN_ENTITY_SPAWN_Y = -20000000;
    protected final List<TickingBlockEntity> blockEntityTickers = Lists.newArrayList();
    private final List<TickingBlockEntity> pendingBlockEntityTickers = Lists.newArrayList();
    private boolean tickingBlockEntities;
    private final Thread thread;
    private final boolean isDebug;
    private int skyDarken;
    protected int randValue = (new Random()).nextInt();
    protected final int addend = 1013904223;
    protected float oRainLevel;
    protected float rainLevel;
    protected float oThunderLevel;
    protected float thunderLevel;
    public final Random random = new Random();
    final DimensionType dimensionType;
    private final Holder<DimensionType> dimensionTypeRegistration;
    protected final WritableLevelData levelData;
    private final Supplier<ProfilerFiller> profiler;
    public final boolean isClientSide;
    private final WorldBorder worldBorder;
    private final BiomeManager biomeManager;
    private final ResourceKey<Level> dimension;
    private long subTickCount;

    protected Level(WritableLevelData pLevelData, ResourceKey<Level> pDimension, Holder<DimensionType> pDimensionType, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed)
    {
        this.profiler = pProfiler;
        this.levelData = pLevelData;
        this.dimensionTypeRegistration = pDimensionType;
        this.dimensionType = pDimensionType.value();
        this.dimension = pDimension;
        this.isClientSide = pIsClientSide;

        if (this.dimensionType.coordinateScale() != 1.0D)
        {
            this.worldBorder = new WorldBorder()
            {
                public double getCenterX()
                {
                    return super.getCenterX() / Level.this.dimensionType.coordinateScale();
                }
                public double getCenterZ()
                {
                    return super.getCenterZ() / Level.this.dimensionType.coordinateScale();
                }
            };
        }
        else
        {
            this.worldBorder = new WorldBorder();
        }

        this.thread = Thread.currentThread();
        this.biomeManager = new BiomeManager(this, pBiomeZoomSeed);
        this.isDebug = pIsDebug;
    }

    public boolean isClientSide()
    {
        return this.isClientSide;
    }

    @Nullable
    public MinecraftServer getServer()
    {
        return null;
    }

    public boolean isInWorldBounds(BlockPos pPos)
    {
        return !this.isOutsideBuildHeight(pPos) && isInWorldBoundsHorizontal(pPos);
    }

    public static boolean isInSpawnableBounds(BlockPos pPos)
    {
        return !isOutsideSpawnableHeight(pPos.getY()) && isInWorldBoundsHorizontal(pPos);
    }

    private static boolean isInWorldBoundsHorizontal(BlockPos pPos)
    {
        return pPos.getX() >= -30000000 && pPos.getZ() >= -30000000 && pPos.getX() < 30000000 && pPos.getZ() < 30000000;
    }

    private static boolean isOutsideSpawnableHeight(int pY)
    {
        return pY < -20000000 || pY >= 20000000;
    }

    public LevelChunk getChunkAt(BlockPos pPos)
    {
        return this.getChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()));
    }

    public LevelChunk getChunk(int pChunkX, int pChunkZ)
    {
        return (LevelChunk)this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL);
    }

    @Nullable
    public ChunkAccess getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull)
    {
        ChunkAccess chunkaccess = this.getChunkSource().getChunk(pX, pZ, pRequiredStatus, pNonnull);

        if (chunkaccess == null && pNonnull)
        {
            throw new IllegalStateException("Should always be able to create a chunk!");
        }
        else
        {
            return chunkaccess;
        }
    }

    public boolean setBlock(BlockPos pPos, BlockState pNewState, int pFlags)
    {
        return this.setBlock(pPos, pNewState, pFlags, 512);
    }

    public boolean setBlock(BlockPos pPos, BlockState pState, int pFlags, int pRecursionLeft)
    {
        if (this.isOutsideBuildHeight(pPos))
        {
            return false;
        }
        else if (!this.isClientSide && this.isDebug())
        {
            return false;
        }
        else
        {
            LevelChunk levelchunk = this.getChunkAt(pPos);
            Block block = pState.getBlock();
            BlockState blockstate = levelchunk.setBlockState(pPos, pState, (pFlags & 64) != 0);

            if (blockstate == null)
            {
                return false;
            }
            else
            {
                BlockState blockstate1 = this.getBlockState(pPos);

                if ((pFlags & 128) == 0 && blockstate1 != blockstate && (blockstate1.getLightBlock(this, pPos) != blockstate.getLightBlock(this, pPos) || blockstate1.getLightEmission() != blockstate.getLightEmission() || blockstate1.useShapeForLightOcclusion() || blockstate.useShapeForLightOcclusion()))
                {
                    this.getProfiler().push("queueCheckLight");
                    this.getChunkSource().getLightEngine().checkBlock(pPos);
                    this.getProfiler().pop();
                }

                if (blockstate1 == pState)
                {
                    if (blockstate != blockstate1)
                    {
                        this.setBlocksDirty(pPos, blockstate, blockstate1);
                    }

                    if ((pFlags & 2) != 0 && (!this.isClientSide || (pFlags & 4) == 0) && (this.isClientSide || levelchunk.getFullStatus() != null && levelchunk.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING)))
                    {
                        this.sendBlockUpdated(pPos, blockstate, pState, pFlags);
                    }

                    if ((pFlags & 1) != 0)
                    {
                        this.blockUpdated(pPos, blockstate.getBlock());

                        if (!this.isClientSide && pState.hasAnalogOutputSignal())
                        {
                            this.updateNeighbourForOutputSignal(pPos, block);
                        }
                    }

                    if ((pFlags & 16) == 0 && pRecursionLeft > 0)
                    {
                        int i = pFlags & -34;
                        blockstate.updateIndirectNeighbourShapes(this, pPos, i, pRecursionLeft - 1);
                        pState.updateNeighbourShapes(this, pPos, i, pRecursionLeft - 1);
                        pState.updateIndirectNeighbourShapes(this, pPos, i, pRecursionLeft - 1);
                    }

                    this.onBlockStateChange(pPos, blockstate, blockstate1);
                }

                return true;
            }
        }
    }

    public void onBlockStateChange(BlockPos pPos, BlockState pBlockState, BlockState pNewState)
    {
    }

    public boolean removeBlock(BlockPos pPos, boolean pIsMoving)
    {
        FluidState fluidstate = this.getFluidState(pPos);
        return this.setBlock(pPos, fluidstate.createLegacyBlock(), 3 | (pIsMoving ? 64 : 0));
    }

    public boolean destroyBlock(BlockPos pPos, boolean pDropBlock, @Nullable Entity pEntity, int pRecursionLeft)
    {
        BlockState blockstate = this.getBlockState(pPos);

        if (blockstate.isAir())
        {
            return false;
        }
        else
        {
            FluidState fluidstate = this.getFluidState(pPos);

            if (!(blockstate.getBlock() instanceof BaseFireBlock))
            {
                this.levelEvent(2001, pPos, Block.getId(blockstate));
            }

            if (pDropBlock)
            {
                BlockEntity blockentity = blockstate.hasBlockEntity() ? this.getBlockEntity(pPos) : null;
                Block.dropResources(blockstate, this, pPos, blockentity, pEntity, ItemStack.EMPTY);
            }

            boolean flag = this.setBlock(pPos, fluidstate.createLegacyBlock(), 3, pRecursionLeft);

            if (flag)
            {
                this.gameEvent(pEntity, GameEvent.BLOCK_DESTROY, pPos);
            }

            return flag;
        }
    }

    public void addDestroyBlockEffect(BlockPos pPos, BlockState pState)
    {
    }

    public boolean setBlockAndUpdate(BlockPos pPos, BlockState pState)
    {
        return this.setBlock(pPos, pState, 3);
    }

    public abstract void sendBlockUpdated(BlockPos pPos, BlockState pOldState, BlockState pNewState, int pFlags);

    public void setBlocksDirty(BlockPos pBlockPos, BlockState pOldState, BlockState pNewState)
    {
    }

    public void updateNeighborsAt(BlockPos pPos, Block pBlock)
    {
        this.neighborChanged(pPos.west(), pBlock, pPos);
        this.neighborChanged(pPos.east(), pBlock, pPos);
        this.neighborChanged(pPos.below(), pBlock, pPos);
        this.neighborChanged(pPos.above(), pBlock, pPos);
        this.neighborChanged(pPos.north(), pBlock, pPos);
        this.neighborChanged(pPos.south(), pBlock, pPos);
    }

    public void updateNeighborsAtExceptFromFacing(BlockPos pPos, Block pBlockType, Direction pSkipSide)
    {
        if (pSkipSide != Direction.WEST)
        {
            this.neighborChanged(pPos.west(), pBlockType, pPos);
        }

        if (pSkipSide != Direction.EAST)
        {
            this.neighborChanged(pPos.east(), pBlockType, pPos);
        }

        if (pSkipSide != Direction.DOWN)
        {
            this.neighborChanged(pPos.below(), pBlockType, pPos);
        }

        if (pSkipSide != Direction.UP)
        {
            this.neighborChanged(pPos.above(), pBlockType, pPos);
        }

        if (pSkipSide != Direction.NORTH)
        {
            this.neighborChanged(pPos.north(), pBlockType, pPos);
        }

        if (pSkipSide != Direction.SOUTH)
        {
            this.neighborChanged(pPos.south(), pBlockType, pPos);
        }
    }

    public void neighborChanged(BlockPos pPos, Block pBlock, BlockPos pFromPos)
    {
        if (!this.isClientSide)
        {
            BlockState blockstate = this.getBlockState(pPos);

            try
            {
                blockstate.neighborChanged(this, pPos, pBlock, pFromPos, false);
            }
            catch (Throwable throwable)
            {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while updating neighbours");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Block being updated");
                crashreportcategory.setDetail("Source block type", () ->
                {
                    try {
                        return String.format("ID #%s (%s // %s)", Registry.BLOCK.getKey(pBlock), pBlock.getDescriptionId(), pBlock.getClass().getCanonicalName());
                    }
                    catch (Throwable throwable1)
                    {
                        return "ID #" + Registry.BLOCK.getKey(pBlock);
                    }
                });
                CrashReportCategory.populateBlockDetails(crashreportcategory, this, pPos, blockstate);
                throw new ReportedException(crashreport);
            }
        }
    }

    public int getHeight(Heightmap.Types pHeightmapType, int pX, int pZ)
    {
        int i;

        if (pX >= -30000000 && pZ >= -30000000 && pX < 30000000 && pZ < 30000000)
        {
            if (this.hasChunk(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ)))
            {
                i = this.getChunk(SectionPos.blockToSectionCoord(pX), SectionPos.blockToSectionCoord(pZ)).getHeight(pHeightmapType, pX & 15, pZ & 15) + 1;
            }
            else
            {
                i = this.getMinBuildHeight();
            }
        }
        else
        {
            i = this.getSeaLevel() + 1;
        }

        return i;
    }

    public LevelLightEngine getLightEngine()
    {
        return this.getChunkSource().getLightEngine();
    }

    public BlockState getBlockState(BlockPos pPos)
    {
        if (this.isOutsideBuildHeight(pPos))
        {
            return Blocks.VOID_AIR.defaultBlockState();
        }
        else
        {
            LevelChunk levelchunk = this.getChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()));
            return levelchunk.getBlockState(pPos);
        }
    }

    public FluidState getFluidState(BlockPos pPos)
    {
        if (this.isOutsideBuildHeight(pPos))
        {
            return Fluids.EMPTY.defaultFluidState();
        }
        else
        {
            LevelChunk levelchunk = this.getChunkAt(pPos);
            return levelchunk.getFluidState(pPos);
        }
    }

    public boolean isDay()
    {
        return !this.dimensionType().hasFixedTime() && this.skyDarken < 4;
    }

    public boolean isNight()
    {
        return !this.dimensionType().hasFixedTime() && !this.isDay();
    }

    public void playSound(@Nullable Player pPlayer, BlockPos pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch)
    {
        this.playSound(pPlayer, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, pSound, pCategory, pVolume, pPitch);
    }

    public abstract void playSound(@Nullable Player pPlayer, double pX, double p_46545_, double pY, SoundEvent p_46547_, SoundSource pZ, float p_46549_, float pSound);

    public abstract void playSound(@Nullable Player pPlayer, Entity pPos, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch);

    public void playLocalSound(double pX, double p_46483_, double pY, SoundEvent p_46485_, SoundSource pZ, float p_46487_, float pSound, boolean pCategory)
    {
    }

    public void addParticle(ParticleOptions pParticleData, double pX, double p_46633_, double pY, double p_46635_, double pZ, double p_46637_)
    {
    }

    public void addParticle(ParticleOptions pParticleData, boolean pForceAlwaysRender, double pX, double p_46641_, double pY, double p_46643_, double pZ, double p_46645_)
    {
    }

    public void addAlwaysVisibleParticle(ParticleOptions pParticleData, double pX, double p_46686_, double pY, double p_46688_, double pZ, double p_46690_)
    {
    }

    public void addAlwaysVisibleParticle(ParticleOptions pParticleData, boolean pIgnoreRange, double pX, double p_46694_, double pY, double p_46696_, double pZ, double p_46698_)
    {
    }

    public float getSunAngle(float pPartialTicks)
    {
        float f = this.getTimeOfDay(pPartialTicks);
        return f * ((float)Math.PI * 2F);
    }

    public void addBlockEntityTicker(TickingBlockEntity pTicker)
    {
        (this.tickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(pTicker);
    }

    protected void tickBlockEntities()
    {
        ProfilerFiller profilerfiller = this.getProfiler();
        profilerfiller.push("blockEntities");
        this.tickingBlockEntities = true;

        if (!this.pendingBlockEntityTickers.isEmpty())
        {
            this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
            this.pendingBlockEntityTickers.clear();
        }

        Iterator<TickingBlockEntity> iterator = this.blockEntityTickers.iterator();

        while (iterator.hasNext())
        {
            TickingBlockEntity tickingblockentity = iterator.next();

            if (tickingblockentity.isRemoved())
            {
                iterator.remove();
            }
            else if (this.shouldTickBlocksAt(ChunkPos.asLong(tickingblockentity.getPos())))
            {
                tickingblockentity.tick();
            }
        }

        this.tickingBlockEntities = false;
        profilerfiller.pop();
    }

    public <T extends Entity> void guardEntityTick(Consumer<T> pConsumerEntity, T pEntity)
    {
        try
        {
            pConsumerEntity.accept(pEntity);
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Ticking entity");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being ticked");
            pEntity.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    public boolean shouldTickDeath(Entity p_186458_)
    {
        return true;
    }

    public boolean shouldTickBlocksAt(long p_186456_)
    {
        return true;
    }

    public Explosion explode(@Nullable Entity pEntity, double pX, double p_46514_, double pY, float p_46516_, Explosion.BlockInteraction pZ)
    {
        return this.explode(pEntity, (DamageSource)null, (ExplosionDamageCalculator)null, pX, p_46514_, pY, p_46516_, false, pZ);
    }

    public Explosion explode(@Nullable Entity pEntity, double pX, double p_46521_, double pY, float p_46523_, boolean pZ, Explosion.BlockInteraction p_46525_)
    {
        return this.explode(pEntity, (DamageSource)null, (ExplosionDamageCalculator)null, pX, p_46521_, pY, p_46523_, pZ, p_46525_);
    }

    public Explosion explode(@Nullable Entity pExploder, @Nullable DamageSource pDamageSource, @Nullable ExplosionDamageCalculator pContext, double pX, double p_46530_, double pY, float p_46532_, boolean pZ, Explosion.BlockInteraction p_46534_)
    {
        Explosion explosion = new Explosion(this, pExploder, pDamageSource, pContext, pX, p_46530_, pY, p_46532_, pZ, p_46534_);
        explosion.explode();
        explosion.finalizeExplosion(true);
        return explosion;
    }

    public abstract String gatherChunkSourceStats();

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pPos)
    {
        if (this.isOutsideBuildHeight(pPos))
        {
            return null;
        }
        else
        {
            return !this.isClientSide && Thread.currentThread() != this.thread ? null : this.getChunkAt(pPos).getBlockEntity(pPos, LevelChunk.EntityCreationType.IMMEDIATE);
        }
    }

    public void setBlockEntity(BlockEntity pBlockEntity)
    {
        BlockPos blockpos = pBlockEntity.getBlockPos();

        if (!this.isOutsideBuildHeight(blockpos))
        {
            this.getChunkAt(blockpos).addAndRegisterBlockEntity(pBlockEntity);
        }
    }

    public void removeBlockEntity(BlockPos pPos)
    {
        if (!this.isOutsideBuildHeight(pPos))
        {
            this.getChunkAt(pPos).removeBlockEntity(pPos);
        }
    }

    public boolean isLoaded(BlockPos pPos)
    {
        return this.isOutsideBuildHeight(pPos) ? false : this.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()));
    }

    public boolean loadedAndEntityCanStandOnFace(BlockPos pPos, Entity pEntity, Direction pDirection)
    {
        if (this.isOutsideBuildHeight(pPos))
        {
            return false;
        }
        else
        {
            ChunkAccess chunkaccess = this.getChunk(SectionPos.blockToSectionCoord(pPos.getX()), SectionPos.blockToSectionCoord(pPos.getZ()), ChunkStatus.FULL, false);
            return chunkaccess == null ? false : chunkaccess.getBlockState(pPos).entityCanStandOnFace(this, pPos, pEntity, pDirection);
        }
    }

    public boolean loadedAndEntityCanStandOn(BlockPos pPos, Entity pEntity)
    {
        return this.loadedAndEntityCanStandOnFace(pPos, pEntity, Direction.UP);
    }

    public void updateSkyBrightness()
    {
        double d0 = 1.0D - (double)(this.getRainLevel(1.0F) * 5.0F) / 16.0D;
        double d1 = 1.0D - (double)(this.getThunderLevel(1.0F) * 5.0F) / 16.0D;
        double d2 = 0.5D + 2.0D * Mth.clamp((double)Mth.cos(this.getTimeOfDay(1.0F) * ((float)Math.PI * 2F)), -0.25D, 0.25D);
        this.skyDarken = (int)((1.0D - d2 * d0 * d1) * 11.0D);
    }

    public void setSpawnSettings(boolean pHostile, boolean pPeaceful)
    {
        this.getChunkSource().setSpawnSettings(pHostile, pPeaceful);
    }

    protected void prepareWeather()
    {
        if (this.levelData.isRaining())
        {
            this.rainLevel = 1.0F;

            if (this.levelData.isThundering())
            {
                this.thunderLevel = 1.0F;
            }
        }
    }

    public void close() throws IOException
    {
        this.getChunkSource().close();
    }

    @Nullable
    public BlockGetter getChunkForCollisions(int pChunkX, int pChunkZ)
    {
        return this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, false);
    }

    public List<Entity> getEntities(@Nullable Entity pEntity, AABB pBoundingBox, Predicate <? super Entity > pPredicate)
    {
        this.getProfiler().incrementCounter("getEntities");
        List<Entity> list = Lists.newArrayList();
        this.getEntities().get(pBoundingBox, (p_151522_) ->
        {
            if (p_151522_ != pEntity && pPredicate.test(p_151522_))
            {
                list.add(p_151522_);
            }

            if (p_151522_ instanceof EnderDragon)
            {
                for (EnderDragonPart enderdragonpart : ((EnderDragon)p_151522_).getSubEntities())
                {
                    if (p_151522_ != pEntity && pPredicate.test(enderdragonpart))
                    {
                        list.add(enderdragonpart);
                    }
                }
            }
        });
        return list;
    }

    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> pEntity, AABB pBoundingBox, Predicate <? super T > pPredicate)
    {
        this.getProfiler().incrementCounter("getEntities");
        List<T> list = Lists.newArrayList();
        this.getEntities().get(pEntity, pBoundingBox, (p_151539_) ->
        {
            if (pPredicate.test(p_151539_))
            {
                list.add(p_151539_);
            }

            if (p_151539_ instanceof EnderDragon)
            {
                EnderDragon enderdragon = (EnderDragon)p_151539_;

                for (EnderDragonPart enderdragonpart : enderdragon.getSubEntities())
                {
                    T t = pEntity.tryCast(enderdragonpart);

                    if (t != null && pPredicate.test(t))
                    {
                        list.add(t);
                    }
                }
            }
        });
        return list;
    }

    @Nullable
    public abstract Entity getEntity(int pId);

    public void blockEntityChanged(BlockPos pPos)
    {
        if (this.hasChunkAt(pPos))
        {
            this.getChunkAt(pPos).setUnsaved(true);
        }
    }

    public int getSeaLevel()
    {
        return 63;
    }

    public int getDirectSignalTo(BlockPos pPos)
    {
        int i = 0;
        i = Math.max(i, this.getDirectSignal(pPos.below(), Direction.DOWN));

        if (i >= 15)
        {
            return i;
        }
        else
        {
            i = Math.max(i, this.getDirectSignal(pPos.above(), Direction.UP));

            if (i >= 15)
            {
                return i;
            }
            else
            {
                i = Math.max(i, this.getDirectSignal(pPos.north(), Direction.NORTH));

                if (i >= 15)
                {
                    return i;
                }
                else
                {
                    i = Math.max(i, this.getDirectSignal(pPos.south(), Direction.SOUTH));

                    if (i >= 15)
                    {
                        return i;
                    }
                    else
                    {
                        i = Math.max(i, this.getDirectSignal(pPos.west(), Direction.WEST));

                        if (i >= 15)
                        {
                            return i;
                        }
                        else
                        {
                            i = Math.max(i, this.getDirectSignal(pPos.east(), Direction.EAST));
                            return i >= 15 ? i : i;
                        }
                    }
                }
            }
        }
    }

    public boolean hasSignal(BlockPos pPos, Direction pSide)
    {
        return this.getSignal(pPos, pSide) > 0;
    }

    public int getSignal(BlockPos pPos, Direction pFacing)
    {
        BlockState blockstate = this.getBlockState(pPos);
        int i = blockstate.getSignal(this, pPos, pFacing);
        return blockstate.isRedstoneConductor(this, pPos) ? Math.max(i, this.getDirectSignalTo(pPos)) : i;
    }

    public boolean hasNeighborSignal(BlockPos pPos)
    {
        if (this.getSignal(pPos.below(), Direction.DOWN) > 0)
        {
            return true;
        }
        else if (this.getSignal(pPos.above(), Direction.UP) > 0)
        {
            return true;
        }
        else if (this.getSignal(pPos.north(), Direction.NORTH) > 0)
        {
            return true;
        }
        else if (this.getSignal(pPos.south(), Direction.SOUTH) > 0)
        {
            return true;
        }
        else if (this.getSignal(pPos.west(), Direction.WEST) > 0)
        {
            return true;
        }
        else
        {
            return this.getSignal(pPos.east(), Direction.EAST) > 0;
        }
    }

    public int getBestNeighborSignal(BlockPos pPos)
    {
        int i = 0;

        for (Direction direction : DIRECTIONS)
        {
            int j = this.getSignal(pPos.relative(direction), direction);

            if (j >= 15)
            {
                return 15;
            }

            if (j > i)
            {
                i = j;
            }
        }

        return i;
    }

    public void disconnect()
    {
    }

    public long getGameTime()
    {
        return this.levelData.getGameTime();
    }

    public long getDayTime()
    {
        return this.levelData.getDayTime();
    }

    public boolean mayInteract(Player pPlayer, BlockPos pPos)
    {
        return true;
    }

    public void broadcastEntityEvent(Entity pEntity, byte pState)
    {
    }

    public void blockEvent(BlockPos pPos, Block pBlock, int pEventID, int pEventParam)
    {
        this.getBlockState(pPos).triggerEvent(this, pPos, pEventID, pEventParam);
    }

    public LevelData getLevelData()
    {
        return this.levelData;
    }

    public GameRules getGameRules()
    {
        return this.levelData.getGameRules();
    }

    public float getThunderLevel(float pDelta)
    {
        return Mth.lerp(pDelta, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(pDelta);
    }

    public void setThunderLevel(float pStrength)
    {
        float f = Mth.clamp(pStrength, 0.0F, 1.0F);
        this.oThunderLevel = f;
        this.thunderLevel = f;
    }

    public float getRainLevel(float pDelta)
    {
        return Mth.lerp(pDelta, this.oRainLevel, this.rainLevel);
    }

    public void setRainLevel(float pStrength)
    {
        float f = Mth.clamp(pStrength, 0.0F, 1.0F);
        this.oRainLevel = f;
        this.rainLevel = f;
    }

    public boolean isThundering()
    {
        if (this.dimensionType().hasSkyLight() && !this.dimensionType().hasCeiling())
        {
            return (double)this.getThunderLevel(1.0F) > 0.9D;
        }
        else
        {
            return false;
        }
    }

    public boolean isRaining()
    {
        return (double)this.getRainLevel(1.0F) > 0.2D;
    }

    public boolean isRainingAt(BlockPos pPosition)
    {
        if (!this.isRaining())
        {
            return false;
        }
        else if (!this.canSeeSky(pPosition))
        {
            return false;
        }
        else if (this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, pPosition).getY() > pPosition.getY())
        {
            return false;
        }
        else
        {
            Biome biome = this.getBiome(pPosition).value();
            return biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.warmEnoughToRain(pPosition);
        }
    }

    public boolean isHumidAt(BlockPos pPos)
    {
        Biome biome = this.getBiome(pPos).value();
        return biome.isHumid();
    }

    @Nullable
    public abstract MapItemSavedData getMapData(String pMapName);

    public abstract void setMapData(String pMapId, MapItemSavedData pData);

    public abstract int getFreeMapId();

    public void globalLevelEvent(int pId, BlockPos pPos, int pData)
    {
    }

    public CrashReportCategory fillReportDetails(CrashReport pReport)
    {
        CrashReportCategory crashreportcategory = pReport.addCategory("Affected level", 1);
        crashreportcategory.setDetail("All players", () ->
        {
            return this.players().size() + " total; " + this.players();
        });
        crashreportcategory.setDetail("Chunk stats", this.getChunkSource()::gatherStats);
        crashreportcategory.setDetail("Level dimension", () ->
        {
            return this.dimension().location().toString();
        });

        try
        {
            this.levelData.fillCrashReportCategory(crashreportcategory, this);
        }
        catch (Throwable throwable)
        {
            crashreportcategory.setDetailError("Level Data Unobtainable", throwable);
        }

        return crashreportcategory;
    }

    public abstract void destroyBlockProgress(int pBreakerId, BlockPos pPos, int pProgress);

    public void createFireworks(double pX, double p_46476_, double pY, double p_46478_, double pZ, double p_46480_, @Nullable CompoundTag pMotionX)
    {
    }

    public abstract Scoreboard getScoreboard();

    public void updateNeighbourForOutputSignal(BlockPos pPos, Block pBlock)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockPos blockpos = pPos.relative(direction);

            if (this.hasChunkAt(blockpos))
            {
                BlockState blockstate = this.getBlockState(blockpos);

                if (blockstate.is(Blocks.COMPARATOR))
                {
                    blockstate.neighborChanged(this, blockpos, pBlock, pPos, false);
                }
                else if (blockstate.isRedstoneConductor(this, blockpos))
                {
                    blockpos = blockpos.relative(direction);
                    blockstate = this.getBlockState(blockpos);

                    if (blockstate.is(Blocks.COMPARATOR))
                    {
                        blockstate.neighborChanged(this, blockpos, pBlock, pPos, false);
                    }
                }
            }
        }
    }

    public DifficultyInstance getCurrentDifficultyAt(BlockPos pPos)
    {
        long i = 0L;
        float f = 0.0F;

        if (this.hasChunkAt(pPos))
        {
            f = this.getMoonBrightness();
            i = this.getChunkAt(pPos).getInhabitedTime();
        }

        return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), i, f);
    }

    public int getSkyDarken()
    {
        return this.skyDarken;
    }

    public void setSkyFlashTime(int pTimeFlash)
    {
    }

    public WorldBorder getWorldBorder()
    {
        return this.worldBorder;
    }

    public void sendPacketToServer(Packet<?> pPacket)
    {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    public DimensionType dimensionType()
    {
        return this.dimensionType;
    }

    public Holder<DimensionType> dimensionTypeRegistration()
    {
        return this.dimensionTypeRegistration;
    }

    public ResourceKey<Level> dimension()
    {
        return this.dimension;
    }

    public Random getRandom()
    {
        return this.random;
    }

    public boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState)
    {
        return pState.test(this.getBlockState(pPos));
    }

    public boolean isFluidAtPosition(BlockPos pPos, Predicate<FluidState> pPredicate)
    {
        return pPredicate.test(this.getFluidState(pPos));
    }

    public abstract RecipeManager getRecipeManager();

    public BlockPos getBlockRandomPos(int pX, int pY, int pZ, int pYMask)
    {
        this.randValue = this.randValue * 3 + 1013904223;
        int i = this.randValue >> 2;
        return new BlockPos(pX + (i & 15), pY + (i >> 16 & pYMask), pZ + (i >> 8 & 15));
    }

    public boolean noSave()
    {
        return false;
    }

    public ProfilerFiller getProfiler()
    {
        return this.profiler.get();
    }

    public Supplier<ProfilerFiller> getProfilerSupplier()
    {
        return this.profiler;
    }

    public BiomeManager getBiomeManager()
    {
        return this.biomeManager;
    }

    public final boolean isDebug()
    {
        return this.isDebug;
    }

    protected abstract LevelEntityGetter<Entity> getEntities();

    protected void postGameEventInRadius(@Nullable Entity pEntity, GameEvent pGameEvent, BlockPos pPos, int pNotificationRadius)
    {
        int i = SectionPos.blockToSectionCoord(pPos.getX() - pNotificationRadius);
        int j = SectionPos.blockToSectionCoord(pPos.getZ() - pNotificationRadius);
        int k = SectionPos.blockToSectionCoord(pPos.getX() + pNotificationRadius);
        int l = SectionPos.blockToSectionCoord(pPos.getZ() + pNotificationRadius);
        int i1 = SectionPos.blockToSectionCoord(pPos.getY() - pNotificationRadius);
        int j1 = SectionPos.blockToSectionCoord(pPos.getY() + pNotificationRadius);

        for (int k1 = i; k1 <= k; ++k1)
        {
            for (int l1 = j; l1 <= l; ++l1)
            {
                ChunkAccess chunkaccess = this.getChunkSource().getChunkNow(k1, l1);

                if (chunkaccess != null)
                {
                    for (int i2 = i1; i2 <= j1; ++i2)
                    {
                        chunkaccess.getEventDispatcher(i2).post(pGameEvent, pEntity, pPos);
                    }
                }
            }
        }
    }

    public long nextSubTickCount()
    {
        return (long)(this.subTickCount++);
    }
}
