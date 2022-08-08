package net.minecraft.server.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.util.thread.ProcessorHandle;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec3;
import net.optifine.reflect.Reflector;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class ChunkMap extends ChunkStorage implements ChunkHolder.PlayerProvider
{
    private static final byte CHUNK_TYPE_REPLACEABLE = -1;
    private static final byte CHUNK_TYPE_UNKNOWN = 0;
    private static final byte CHUNK_TYPE_FULL = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CHUNK_SAVED_PER_TICK = 200;
    private static final int CHUNK_SAVED_EAGERLY_PER_TICK = 20;
    private static final int EAGER_CHUNK_SAVE_COOLDOWN_IN_MILLIS = 10000;
    private static final int MIN_VIEW_DISTANCE = 3;
    public static final int MAX_VIEW_DISTANCE = 33;
    public static final int MAX_CHUNK_DISTANCE = 65 + ChunkStatus.maxDistance();
    public static final int FORCED_TICKET_LEVEL = 31;
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> updatingChunkMap = new Long2ObjectLinkedOpenHashMap<>();
    private volatile Long2ObjectLinkedOpenHashMap<ChunkHolder> visibleChunkMap = this.updatingChunkMap.clone();
    private final Long2ObjectLinkedOpenHashMap<ChunkHolder> pendingUnloads = new Long2ObjectLinkedOpenHashMap<>();
    private final LongSet entitiesInLevel = new LongOpenHashSet();
    final ServerLevel level;
    private final ThreadedLevelLightEngine lightEngine;
    private final BlockableEventLoop<Runnable> mainThreadExecutor;
    private ChunkGenerator generator;
    private final Supplier<DimensionDataStorage> overworldDataStorage;
    private final PoiManager poiManager;
    final LongSet toDrop = new LongOpenHashSet();
    private boolean modified;
    private final ChunkTaskPriorityQueueSorter queueSorter;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> worldgenMailbox;
    private final ProcessorHandle<ChunkTaskPriorityQueueSorter.Message<Runnable>> mainThreadMailbox;
    private final ChunkProgressListener progressListener;
    private final ChunkStatusUpdateListener chunkStatusListener;
    private final ChunkMap.DistanceManager distanceManager;
    private final AtomicInteger tickingGenerated = new AtomicInteger();
    private final StructureManager structureManager;
    private final String storageName;
    private final PlayerMap playerMap = new PlayerMap();
    private final Int2ObjectMap<ChunkMap.TrackedEntity> entityMap = new Int2ObjectOpenHashMap<>();
    private final Long2ByteMap chunkTypeCache = new Long2ByteOpenHashMap();
    private final Long2LongMap chunkSaveCooldowns = new Long2LongOpenHashMap();
    private final Queue<Runnable> unloadQueue = Queues.newConcurrentLinkedQueue();
    int viewDistance;

    public ChunkMap(ServerLevel p_143040_, LevelStorageSource.LevelStorageAccess p_143041_, DataFixer p_143042_, StructureManager p_143043_, Executor p_143044_, BlockableEventLoop<Runnable> p_143045_, LightChunkGetter p_143046_, ChunkGenerator p_143047_, ChunkProgressListener p_143048_, ChunkStatusUpdateListener p_143049_, Supplier<DimensionDataStorage> p_143050_, int p_143051_, boolean p_143052_)
    {
        super(p_143041_.getDimensionPath(p_143040_.dimension()).resolve("region"), p_143042_, p_143052_);
        this.structureManager = p_143043_;
        Path path = p_143041_.getDimensionPath(p_143040_.dimension());
        this.storageName = path.getFileName().toString();
        this.level = p_143040_;
        this.generator = p_143047_;
        this.mainThreadExecutor = p_143045_;
        ProcessorMailbox<Runnable> processormailbox = ProcessorMailbox.create(p_143044_, "worldgen");
        ProcessorHandle<Runnable> processorhandle = ProcessorHandle.of("main", p_143045_::tell);
        this.progressListener = p_143048_;
        this.chunkStatusListener = p_143049_;
        ProcessorMailbox<Runnable> processormailbox1 = ProcessorMailbox.create(p_143044_, "light");
        this.queueSorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(processormailbox, processorhandle, processormailbox1), p_143044_, Integer.MAX_VALUE);
        this.worldgenMailbox = this.queueSorter.getProcessor(processormailbox, false);
        this.mainThreadMailbox = this.queueSorter.getProcessor(processorhandle, false);
        this.lightEngine = new ThreadedLevelLightEngine(p_143046_, this, this.level.dimensionType().hasSkyLight(), processormailbox1, this.queueSorter.getProcessor(processormailbox1, false));
        this.distanceManager = new ChunkMap.DistanceManager(p_143044_, p_143045_);
        this.overworldDataStorage = p_143050_;
        this.poiManager = new PoiManager(path.resolve("poi"), p_143042_, p_143052_, p_143040_);
        this.setViewDistance(p_143051_);
    }

    protected ChunkGenerator generator()
    {
        return this.generator;
    }

    public void debugReloadGenerator()
    {
        DataResult<JsonElement> dataresult = ChunkGenerator.CODEC.encodeStart(JsonOps.INSTANCE, this.generator);
        DataResult<ChunkGenerator> dataresult1 = dataresult.flatMap((p_183803_0_) ->
        {
            return ChunkGenerator.CODEC.parse(JsonOps.INSTANCE, p_183803_0_);
        });
        dataresult1.result().ifPresent((p_183807_1_) ->
        {
            this.generator = p_183807_1_;
        });
    }

    private static double euclideanDistanceSquared(ChunkPos pChunkPos, Entity pEntity)
    {
        double d0 = (double)SectionPos.sectionToBlockCoord(pChunkPos.x, 8);
        double d1 = (double)SectionPos.sectionToBlockCoord(pChunkPos.z, 8);
        double d2 = d0 - pEntity.getX();
        double d3 = d1 - pEntity.getZ();
        return d2 * d2 + d3 * d3;
    }

    public static boolean isChunkInRange(int p_200879_, int p_200880_, int p_200881_, int p_200882_, int p_200883_)
    {
        int i = Math.max(0, Math.abs(p_200879_ - p_200881_) - 1);
        int j = Math.max(0, Math.abs(p_200880_ - p_200882_) - 1);
        long k = (long)Math.max(0, Math.max(i, j) - 1);
        long l = (long)Math.min(i, j);
        long i1 = l * l + k * k;
        int j1 = p_200883_ - 1;
        int k1 = j1 * j1;
        return i1 <= (long)k1;
    }

    private static boolean isChunkOnRangeBorder(int p_183829_, int p_183830_, int p_183831_, int p_183832_, int p_183833_)
    {
        if (!isChunkInRange(p_183829_, p_183830_, p_183831_, p_183832_, p_183833_))
        {
            return false;
        }
        else if (!isChunkInRange(p_183829_ + 1, p_183830_, p_183831_, p_183832_, p_183833_))
        {
            return true;
        }
        else if (!isChunkInRange(p_183829_, p_183830_ + 1, p_183831_, p_183832_, p_183833_))
        {
            return true;
        }
        else if (!isChunkInRange(p_183829_ - 1, p_183830_, p_183831_, p_183832_, p_183833_))
        {
            return true;
        }
        else
        {
            return !isChunkInRange(p_183829_, p_183830_ - 1, p_183831_, p_183832_, p_183833_);
        }
    }

    protected ThreadedLevelLightEngine getLightEngine()
    {
        return this.lightEngine;
    }

    @Nullable
    protected ChunkHolder getUpdatingChunkIfPresent(long p_140175_)
    {
        return this.updatingChunkMap.get(p_140175_);
    }

    @Nullable
    protected ChunkHolder getVisibleChunkIfPresent(long p_140328_)
    {
        return this.visibleChunkMap.get(p_140328_);
    }

    protected IntSupplier getChunkQueueLevel(long p_140372_)
    {
        return () ->
        {
            ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_140372_);
            return chunkholder == null ? ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1 : Math.min(chunkholder.getQueueLevel(), ChunkTaskPriorityQueue.PRIORITY_LEVEL_COUNT - 1);
        };
    }

    public String getChunkDebugData(ChunkPos pPos)
    {
        ChunkHolder chunkholder = this.getVisibleChunkIfPresent(pPos.toLong());

        if (chunkholder == null)
        {
            return "null";
        }
        else
        {
            String s = chunkholder.getTicketLevel() + "\n";
            ChunkStatus chunkstatus = chunkholder.getLastAvailableStatus();
            ChunkAccess chunkaccess = chunkholder.getLastAvailable();

            if (chunkstatus != null)
            {
                s = s + "St: \u00a7" + chunkstatus.getIndex() + chunkstatus + "\u00a7r\n";
            }

            if (chunkaccess != null)
            {
                s = s + "Ch: \u00a7" + chunkaccess.getStatus().getIndex() + chunkaccess.getStatus() + "\u00a7r\n";
            }

            ChunkHolder.FullChunkStatus chunkholder$fullchunkstatus = chunkholder.getFullStatus();
            s = s + "\u00a7" + chunkholder$fullchunkstatus.ordinal() + chunkholder$fullchunkstatus;
            return s + "\u00a7r";
        }
    }

    private CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> getChunkRangeFuture(ChunkPos p_140211_, int p_140212_, IntFunction<ChunkStatus> p_140213_)
    {
        List<CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> list = new ArrayList<>();
        List<ChunkHolder> list1 = new ArrayList<>();
        int i = p_140211_.x;
        int j = p_140211_.z;

        for (int k = -p_140212_; k <= p_140212_; ++k)
        {
            for (int l = -p_140212_; l <= p_140212_; ++l)
            {
                int i1 = Math.max(Math.abs(l), Math.abs(k));
                final ChunkPos chunkpos = new ChunkPos(i + l, j + k);
                long j1 = chunkpos.toLong();
                ChunkHolder chunkholder = this.getUpdatingChunkIfPresent(j1);

                if (chunkholder == null)
                {
                    return CompletableFuture.completedFuture(Either.right(new ChunkHolder.ChunkLoadingFailure()
                    {
                        public String toString()
                        {
                            return "Unloaded " + chunkpos;
                        }
                    }));
                }

                ChunkStatus chunkstatus = p_140213_.apply(i1);
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = chunkholder.getOrScheduleFuture(chunkstatus, this);
                list1.add(chunkholder);
                list.add(completablefuture);
            }
        }

        CompletableFuture<List<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>>> completablefuture1 = Util.sequence(list);
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completablefuture2 = completablefuture1.thenApply((p_183726_4_) ->
        {
            List<ChunkAccess> list2 = Lists.newArrayList();
            int k1 = 0;

            for (final Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> either : p_183726_4_)
            {
                if (either == null)
                {
                    throw this.debugFuturesAndCreateReportedException(new IllegalStateException("At least one of the chunk futures were null"), "n/a");
                }

                Optional<ChunkAccess> optional = either.left();

                if (!optional.isPresent())
                {
                    final int l1 = k1;
                    return Either.right(new ChunkHolder.ChunkLoadingFailure()
                    {
                        public String toString()
                        {
                            return "Unloaded " + new ChunkPos(i + l1 % (p_140212_ * 2 + 1), j + l1 / (p_140212_ * 2 + 1)) + " " + either.right().get();
                        }
                    });
                }

                list2.add(optional.get());
                ++k1;
            }

            return Either.left(list2);
        });

        for (ChunkHolder chunkholder1 : list1)
        {
            chunkholder1.addSaveDependency("getChunkRangeFuture " + p_140211_ + " " + p_140212_, completablefuture2);
        }

        return completablefuture2;
    }

    public ReportedException debugFuturesAndCreateReportedException(IllegalStateException p_203752_, String p_203753_)
    {
        StringBuilder stringbuilder = new StringBuilder();
        Consumer<ChunkHolder> consumer = (p_203754_1_) ->
        {
            p_203754_1_.getAllFutures().forEach((p_203757_2_) -> {
                ChunkStatus chunkstatus = p_203757_2_.getFirst();
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = p_203757_2_.getSecond();

                if (completablefuture != null && completablefuture.isDone() && completablefuture.join() == null)
                {
                    stringbuilder.append((Object)p_203754_1_.getPos()).append(" - status: ").append((Object)chunkstatus).append(" future: ").append((Object)completablefuture).append(System.lineSeparator());
                }
            });
        };
        stringbuilder.append("Updating:").append(System.lineSeparator());
        this.updatingChunkMap.values().forEach(consumer);
        stringbuilder.append("Visible:").append(System.lineSeparator());
        this.visibleChunkMap.values().forEach(consumer);
        CrashReport crashreport = CrashReport.forThrowable(p_203752_, "Chunk loading");
        CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk loading");
        crashreportcategory.setDetail("Details", p_203753_);
        crashreportcategory.setDetail("Futures", stringbuilder);
        return new ReportedException(crashreport);
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareEntityTickingChunk(ChunkPos p_143118_)
    {
        return this.getChunkRangeFuture(p_143118_, 2, (p_203077_0_) ->
        {
            return ChunkStatus.FULL;
        }).thenApplyAsync((p_203085_0_) ->
        {
            return p_203085_0_.mapLeft((p_203091_0_) -> {
                return (LevelChunk)p_203091_0_.get(p_203091_0_.size() / 2);
            });
        }, this.mainThreadExecutor);
    }

    @Nullable
    ChunkHolder updateChunkScheduling(long pChunkPos, int p_140178_, @Nullable ChunkHolder pNewLevel, int pHolder)
    {
        if (pHolder > MAX_CHUNK_DISTANCE && p_140178_ > MAX_CHUNK_DISTANCE)
        {
            return pNewLevel;
        }
        else
        {
            if (pNewLevel != null)
            {
                pNewLevel.setTicketLevel(p_140178_);
            }

            if (pNewLevel != null)
            {
                if (p_140178_ > MAX_CHUNK_DISTANCE)
                {
                    this.toDrop.add(pChunkPos);
                }
                else
                {
                    this.toDrop.remove(pChunkPos);
                }
            }

            if (p_140178_ <= MAX_CHUNK_DISTANCE && pNewLevel == null)
            {
                pNewLevel = this.pendingUnloads.remove(pChunkPos);

                if (pNewLevel != null)
                {
                    pNewLevel.setTicketLevel(p_140178_);
                }
                else
                {
                    pNewLevel = new ChunkHolder(new ChunkPos(pChunkPos), p_140178_, this.level, this.lightEngine, this.queueSorter, this);
                }

                this.updatingChunkMap.put(pChunkPos, pNewLevel);
                this.modified = true;
            }

            return pNewLevel;
        }
    }

    public void close() throws IOException
    {
        try
        {
            this.queueSorter.close();
            this.poiManager.close();
        }
        finally
        {
            super.close();
        }
    }

    protected void saveAllChunks(boolean pFlush)
    {
        if (pFlush)
        {
            List<ChunkHolder> list = this.visibleChunkMap.values().stream().filter(ChunkHolder::wasAccessibleSinceLastSave).peek(ChunkHolder::refreshAccessibility).collect(Collectors.toList());
            MutableBoolean mutableboolean = new MutableBoolean();

            do
            {
                mutableboolean.setFalse();
                list.stream().map((p_203101_1_) ->
                {
                    CompletableFuture<ChunkAccess> completablefuture;

                    do {
                        completablefuture = p_203101_1_.getChunkToSave();
                        this.mainThreadExecutor.managedBlock(completablefuture::isDone);
                    }
                    while (completablefuture != p_203101_1_.getChunkToSave());

                    return completablefuture.join();
                }).filter((p_203087_0_) ->
                {
                    return p_203087_0_ instanceof ImposterProtoChunk || p_203087_0_ instanceof LevelChunk;
                }).filter(this::save).forEach((p_203049_1_) ->
                {
                    mutableboolean.setTrue();
                });
            }
            while (mutableboolean.isTrue());

            this.processUnloads(() ->
            {
                return true;
            });
            this.flushWorker();
        }
        else
        {
            this.visibleChunkMap.values().forEach(this::saveChunkIfNeeded);
        }
    }

    protected void tick(BooleanSupplier pHasMoreTime)
    {
        ProfilerFiller profilerfiller = this.level.getProfiler();
        profilerfiller.push("poi");
        this.poiManager.tick(pHasMoreTime);
        profilerfiller.popPush("chunk_unload");

        if (!this.level.noSave())
        {
            this.processUnloads(pHasMoreTime);
        }

        profilerfiller.pop();
    }

    public boolean hasWork()
    {
        return this.lightEngine.hasLightWork() || !this.pendingUnloads.isEmpty() || !this.updatingChunkMap.isEmpty() || this.poiManager.hasWork() || !this.toDrop.isEmpty() || !this.unloadQueue.isEmpty() || this.queueSorter.hasWork() || this.distanceManager.hasTickets();
    }

    private void processUnloads(BooleanSupplier pHasMoreTime)
    {
        LongIterator longiterator = this.toDrop.iterator();

        for (int i = 0; longiterator.hasNext() && (pHasMoreTime.getAsBoolean() || i < 200 || this.toDrop.size() > 2000); longiterator.remove())
        {
            long j = longiterator.nextLong();
            ChunkHolder chunkholder = this.updatingChunkMap.remove(j);

            if (chunkholder != null)
            {
                this.pendingUnloads.put(j, chunkholder);
                this.modified = true;
                ++i;
                this.scheduleUnload(j, chunkholder);
            }
        }

        int l = Math.max(0, this.unloadQueue.size() - 2000);
        Runnable runnable;

        while ((pHasMoreTime.getAsBoolean() || l > 0) && (runnable = this.unloadQueue.poll()) != null)
        {
            --l;
            runnable.run();
        }

        int k = 0;
        ObjectIterator<ChunkHolder> objectiterator = this.visibleChunkMap.values().iterator();

        while (k < 20 && pHasMoreTime.getAsBoolean() && objectiterator.hasNext())
        {
            if (this.saveChunkIfNeeded(objectiterator.next()))
            {
                ++k;
            }
        }
    }

    private void scheduleUnload(long pChunkPos, ChunkHolder p_140183_)
    {
        CompletableFuture<ChunkAccess> completablefuture = p_140183_.getChunkToSave();
        completablefuture.thenAcceptAsync((p_202998_5_) ->
        {
            CompletableFuture<ChunkAccess> completablefuture1 = p_140183_.getChunkToSave();

            if (completablefuture1 != completablefuture)
            {
                this.scheduleUnload(pChunkPos, p_140183_);
            }
            else if (this.pendingUnloads.remove(pChunkPos, p_140183_) && p_202998_5_ != null)
            {
                if (p_202998_5_ instanceof LevelChunk)
                {
                    ((LevelChunk)p_202998_5_).setLoaded(false);

                    if (Reflector.ChunkEvent_Unload_Constructor.exists())
                    {
                        Reflector.postForgeBusEvent(Reflector.ChunkEvent_Unload_Constructor, p_202998_5_);
                    }
                }

                this.save(p_202998_5_);

                if (this.entitiesInLevel.remove(pChunkPos) && p_202998_5_ instanceof LevelChunk)
                {
                    LevelChunk levelchunk = (LevelChunk)p_202998_5_;
                    this.level.unload(levelchunk);
                }

                this.lightEngine.updateChunkStatus(p_202998_5_.getPos());
                this.lightEngine.tryScheduleUpdate();
                this.progressListener.onStatusChange(p_202998_5_.getPos(), (ChunkStatus)null);
                this.chunkSaveCooldowns.remove(p_202998_5_.getPos().toLong());
            }
        }, this.unloadQueue::add).whenComplete((p_202994_1_, p_202994_2_) ->
        {
            if (p_202994_2_ != null)
            {
                LOGGER.error("Failed to save chunk {}", p_140183_.getPos(), p_202994_2_);
            }
        });
    }

    protected boolean promoteChunkMap()
    {
        if (!this.modified)
        {
            return false;
        }
        else
        {
            this.visibleChunkMap = this.updatingChunkMap.clone();
            this.modified = false;
            return true;
        }
    }

    public CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> schedule(ChunkHolder p_140293_, ChunkStatus p_140294_)
    {
        ChunkPos chunkpos = p_140293_.getPos();

        if (p_140294_ == ChunkStatus.EMPTY)
        {
            return this.scheduleChunkLoad(chunkpos);
        }
        else
        {
            if (p_140294_ == ChunkStatus.LIGHT)
            {
                this.distanceManager.addTicket(TicketType.LIGHT, chunkpos, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), chunkpos);
            }

            Optional<ChunkAccess> optional = p_140293_.getOrScheduleFuture(p_140294_.getParent(), this).getNow(ChunkHolder.UNLOADED_CHUNK).left();

            if (optional.isPresent() && optional.get().getStatus().isOrAfter(p_140294_))
            {
                CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = p_140294_.load(this.level, this.structureManager, this.lightEngine, (p_203079_2_) ->
                {
                    return this.protoChunkToFullChunk(p_140293_);
                }, optional.get());
                this.progressListener.onStatusChange(chunkpos, p_140294_);
                return completablefuture;
            }
            else
            {
                return this.scheduleChunkGeneration(p_140293_, p_140294_);
            }
        }
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkLoad(ChunkPos pChunkPos)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            try {
                this.level.getProfiler().incrementCounter("chunkLoad");
                CompoundTag compoundtag = this.readChunk(pChunkPos);

                if (compoundtag != null)
                {
                    boolean flag = compoundtag.contains("Status", 8);

                    if (flag)
                    {
                        ChunkAccess chunkaccess = ChunkSerializer.read(this.level, this.poiManager, pChunkPos, compoundtag);
                        this.markPosition(pChunkPos, chunkaccess.getStatus().getChunkType());
                        return Either.left(chunkaccess);
                    }

                    LOGGER.error("Chunk file at {} is missing level data, skipping", (Object)pChunkPos);
                }
            }
            catch (ReportedException reportedexception)
            {
                Throwable throwable = reportedexception.getCause();

                if (!(throwable instanceof IOException))
                {
                    this.markPositionReplaceable(pChunkPos);
                    throw reportedexception;
                }

                LOGGER.error("Couldn't load chunk {}", pChunkPos, throwable);
            }
            catch (Exception exception1)
            {
                LOGGER.error("Couldn't load chunk {}", pChunkPos, exception1);
            }

            this.markPositionReplaceable(pChunkPos);
            return Either.left(new ProtoChunk(pChunkPos, UpgradeData.EMPTY, this.level, this.level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), (BlendingData)null));
        }, this.mainThreadExecutor);
    }

    private void markPositionReplaceable(ChunkPos p_140423_)
    {
        this.chunkTypeCache.put(p_140423_.toLong(), (byte) - 1);
    }

    private byte markPosition(ChunkPos p_140230_, ChunkStatus.ChunkType p_140231_)
    {
        return this.chunkTypeCache.put(p_140230_.toLong(), (byte)(p_140231_ == ChunkStatus.ChunkType.PROTOCHUNK ? -1 : 1));
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> scheduleChunkGeneration(ChunkHolder pChunkHolder, ChunkStatus pChunkStatus)
    {
        ChunkPos chunkpos = pChunkHolder.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkRangeFuture(chunkpos, pChunkStatus.getRange(), (p_203070_2_) ->
        {
            return this.getDependencyStatus(pChunkStatus, p_203070_2_);
        });
        this.level.getProfiler().incrementCounter(() ->
        {
            return "chunkGenerate " + pChunkStatus.getName();
        });
        Executor executor = (p_203098_2_) ->
        {
            this.worldgenMailbox.tell(ChunkTaskPriorityQueueSorter.message(pChunkHolder, p_203098_2_));
        };
        return completablefuture.thenComposeAsync((p_203011_5_) ->
        {
            return p_203011_5_.map((p_203017_5_) -> {
                try {
                    CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture1 = pChunkStatus.generate(executor, this.level, this.generator, this.structureManager, this.lightEngine, (p_203060_2_) -> {
                        return this.protoChunkToFullChunk(pChunkHolder);
                    }, p_203017_5_, false);
                    this.progressListener.onStatusChange(chunkpos, pChunkStatus);
                    return completablefuture1;
                }
                catch (Exception exception1)
                {
                    exception1.getStackTrace();
                    CrashReport crashreport = CrashReport.forThrowable(exception1, "Exception generating new chunk");
                    CrashReportCategory crashreportcategory = crashreport.addCategory("Chunk to be generated");
                    crashreportcategory.setDetail("Location", String.format("%d,%d", chunkpos.x, chunkpos.z));
                    crashreportcategory.setDetail("Position hash", ChunkPos.asLong(chunkpos.x, chunkpos.z));
                    crashreportcategory.setDetail("Generator", this.generator);
                    this.mainThreadExecutor.execute(() ->
                    {
                        throw new ReportedException(crashreport);
                    });
                    throw new ReportedException(crashreport);
                }
            }, (p_203008_2_) -> {
                this.releaseLightTicket(chunkpos);
                return CompletableFuture.completedFuture(Either.right(p_203008_2_));
            });
        }, executor);
    }

    protected void releaseLightTicket(ChunkPos p_140376_)
    {
        this.mainThreadExecutor.tell(Util.name(() ->
        {
            this.distanceManager.removeTicket(TicketType.LIGHT, p_140376_, 33 + ChunkStatus.getDistance(ChunkStatus.LIGHT), p_140376_);
        }, () ->
        {
            return "release light ticket " + p_140376_;
        }));
    }

    private ChunkStatus getDependencyStatus(ChunkStatus p_140263_, int p_140264_)
    {
        ChunkStatus chunkstatus;

        if (p_140264_ == 0)
        {
            chunkstatus = p_140263_.getParent();
        }
        else
        {
            chunkstatus = ChunkStatus.getStatusAroundFullChunk(ChunkStatus.getDistance(p_140263_) + p_140264_);
        }

        return chunkstatus;
    }

    private static void postLoadProtoChunk(ServerLevel p_143065_, List<CompoundTag> p_143066_)
    {
        if (!p_143066_.isEmpty())
        {
            p_143065_.addWorldGenChunkEntities(EntityType.loadEntitiesRecursive(p_143066_, p_143065_));
        }
    }

    private CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> protoChunkToFullChunk(ChunkHolder p_140384_)
    {
        CompletableFuture<Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure>> completablefuture = p_140384_.getFutureIfPresentUnchecked(ChunkStatus.FULL.getParent());
        return completablefuture.thenApplyAsync((p_202985_2_) ->
        {
            ChunkStatus chunkstatus = ChunkHolder.getStatus(p_140384_.getTicketLevel());
            return !chunkstatus.isOrAfter(ChunkStatus.FULL) ? ChunkHolder.UNLOADED_CHUNK : p_202985_2_.mapLeft((p_202988_2_) -> {
                ChunkPos chunkpos = p_140384_.getPos();
                ProtoChunk protochunk = (ProtoChunk)p_202988_2_;
                LevelChunk levelchunk;

                if (protochunk instanceof ImposterProtoChunk)
                {
                    levelchunk = ((ImposterProtoChunk)protochunk).getWrapped();
                }
                else {
                    levelchunk = new LevelChunk(this.level, protochunk, (p_203035_2_) -> {
                        postLoadProtoChunk(this.level, protochunk.getEntities());
                    });
                    p_140384_.replaceProtoChunk(new ImposterProtoChunk(levelchunk, false));
                }

                levelchunk.setFullStatus(() -> {
                    return ChunkHolder.getFullChunkStatus(p_140384_.getTicketLevel());
                });
                levelchunk.runPostLoad();

                if (this.entitiesInLevel.add(chunkpos.toLong()))
                {
                    levelchunk.setLoaded(true);

                    try
                    {
                        Reflector.setFieldValue(p_140384_, Reflector.ForgeChunkHolder_currentlyLoading, levelchunk);
                        levelchunk.registerAllBlockEntitiesAfterLevelLoad();
                        levelchunk.registerTickContainerInLevel(this.level);
                        Reflector.postForgeBusEvent(Reflector.ChunkEvent_Load_Constructor, levelchunk);
                    }
                    finally
                    {
                        Reflector.setFieldValue(p_140384_, Reflector.ForgeChunkHolder_currentlyLoading, (Object)null);
                    }
                }

                return levelchunk;
            });
        }, (p_203093_2_) ->
        {
            this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_203093_2_, p_140384_.getPos().toLong(), p_140384_::getTicketLevel));
        });
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareTickingChunk(ChunkHolder p_143054_)
    {
        ChunkPos chunkpos = p_143054_.getPos();
        CompletableFuture<Either<List<ChunkAccess>, ChunkHolder.ChunkLoadingFailure>> completablefuture = this.getChunkRangeFuture(chunkpos, 1, (p_203058_0_) ->
        {
            return ChunkStatus.FULL;
        });
        CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> completablefuture1 = completablefuture.thenApplyAsync((p_203066_0_) ->
        {
            return p_203066_0_.mapLeft((p_212883_0_) -> {
                return (LevelChunk)p_212883_0_.get(p_212883_0_.size() / 2);
            });
        }, (p_203082_2_) ->
        {
            this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143054_, p_203082_2_));
        }).thenApplyAsync((p_212877_1_) ->
        {
            return p_212877_1_.ifLeft((p_212887_1_) -> {
                p_212887_1_.postProcessGeneration();
                this.level.startTickingChunk(p_212887_1_);
            });
        }, this.mainThreadExecutor);
        completablefuture1.thenAcceptAsync((p_212858_2_) ->
        {
            p_212858_2_.ifLeft((p_212861_2_) -> {
                this.tickingGenerated.getAndIncrement();
                MutableObject<ClientboundLevelChunkWithLightPacket> mutableobject = new MutableObject<>();
                this.getPlayers(chunkpos, false).forEach((p_212870_3_) -> {
                    this.playerLoadedChunk(p_212870_3_, mutableobject, p_212861_2_);
                });
            });
        }, (p_212874_2_) ->
        {
            this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143054_, p_212874_2_));
        });
        return completablefuture1;
    }

    public CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> prepareAccessibleChunk(ChunkHolder p_143110_)
    {
        return this.getChunkRangeFuture(p_143110_.getPos(), 1, ChunkStatus::getStatusAroundFullChunk).thenApplyAsync((p_212864_0_) ->
        {
            return p_212864_0_.mapLeft((p_212868_0_) -> {
                return (LevelChunk)p_212868_0_.get(p_212868_0_.size() / 2);
            });
        }, (p_212850_2_) ->
        {
            this.mainThreadMailbox.tell(ChunkTaskPriorityQueueSorter.message(p_143110_, p_212850_2_));
        });
    }

    public int getTickingGenerated()
    {
        return this.tickingGenerated.get();
    }

    private boolean saveChunkIfNeeded(ChunkHolder p_198875_)
    {
        if (!p_198875_.wasAccessibleSinceLastSave())
        {
            return false;
        }
        else
        {
            ChunkAccess chunkaccess = p_198875_.getChunkToSave().getNow((ChunkAccess)null);

            if (!(chunkaccess instanceof ImposterProtoChunk) && !(chunkaccess instanceof LevelChunk))
            {
                return false;
            }
            else
            {
                long i = chunkaccess.getPos().toLong();
                long j = this.chunkSaveCooldowns.getOrDefault(i, -1L);
                long k = System.currentTimeMillis();

                if (k < j)
                {
                    return false;
                }
                else
                {
                    boolean flag = this.save(chunkaccess);
                    p_198875_.refreshAccessibility();

                    if (flag)
                    {
                        this.chunkSaveCooldowns.put(i, k + 10000L);
                    }

                    return flag;
                }
            }
        }
    }

    private boolean save(ChunkAccess p_140259_)
    {
        this.poiManager.flush(p_140259_.getPos());

        if (!p_140259_.isUnsaved())
        {
            return false;
        }
        else
        {
            p_140259_.setUnsaved(false);
            ChunkPos chunkpos = p_140259_.getPos();

            try
            {
                ChunkStatus chunkstatus = p_140259_.getStatus();

                if (chunkstatus.getChunkType() != ChunkStatus.ChunkType.LEVELCHUNK)
                {
                    if (this.isExistingChunkFull(chunkpos))
                    {
                        return false;
                    }

                    if (chunkstatus == ChunkStatus.EMPTY && p_140259_.getAllStarts().values().stream().noneMatch(StructureStart::isValid))
                    {
                        return false;
                    }
                }

                this.level.getProfiler().incrementCounter("chunkSave");
                CompoundTag compoundtag = ChunkSerializer.write(this.level, p_140259_);

                if (Reflector.ChunkDataEvent_Save_Constructor.exists())
                {
                    Level level = (Level)Reflector.call(p_140259_, Reflector.ForgeIChunk_getWorldForge);
                    Reflector.postForgeBusEvent(Reflector.ChunkDataEvent_Save_Constructor, p_140259_, level != null ? level : this.level, compoundtag);
                }

                this.write(chunkpos, compoundtag);
                this.markPosition(chunkpos, chunkstatus.getChunkType());
                return true;
            }
            catch (Exception exception1)
            {
                LOGGER.error("Failed to save chunk {},{}", chunkpos.x, chunkpos.z, exception1);
                return false;
            }
        }
    }

    private boolean isExistingChunkFull(ChunkPos p_140426_)
    {
        byte b0 = this.chunkTypeCache.get(p_140426_.toLong());

        if (b0 != 0)
        {
            return b0 == 1;
        }
        else
        {
            CompoundTag compoundtag;

            try
            {
                compoundtag = this.readChunk(p_140426_);

                if (compoundtag == null)
                {
                    this.markPositionReplaceable(p_140426_);
                    return false;
                }
            }
            catch (Exception exception)
            {
                LOGGER.error("Failed to read chunk {}", p_140426_, exception);
                this.markPositionReplaceable(p_140426_);
                return false;
            }

            ChunkStatus.ChunkType chunkstatus$chunktype = ChunkSerializer.getChunkTypeFromTag(compoundtag);
            return this.markPosition(p_140426_, chunkstatus$chunktype) == 1;
        }
    }

    protected void setViewDistance(int pViewDistance)
    {
        int i = Mth.clamp(pViewDistance + 1, 3, 64);

        if (i != this.viewDistance)
        {
            int j = this.viewDistance;
            this.viewDistance = i;
            this.distanceManager.updatePlayerTickets(this.viewDistance + 1);

            for (ChunkHolder chunkholder : this.updatingChunkMap.values())
            {
                ChunkPos chunkpos = chunkholder.getPos();
                MutableObject<ClientboundLevelChunkWithLightPacket> mutableobject = new MutableObject<>();
                this.getPlayers(chunkpos, false).forEach((p_212853_4_) ->
                {
                    SectionPos sectionpos = p_212853_4_.getLastSectionPos();
                    boolean flag = isChunkInRange(chunkpos.x, chunkpos.z, sectionpos.x(), sectionpos.z(), j);
                    boolean flag1 = isChunkInRange(chunkpos.x, chunkpos.z, sectionpos.x(), sectionpos.z(), this.viewDistance);
                    this.updateChunkTracking(p_212853_4_, chunkpos, mutableobject, flag, flag1);
                });
            }
        }
    }

    protected void updateChunkTracking(ServerPlayer p_183755_, ChunkPos p_183756_, MutableObject<ClientboundLevelChunkWithLightPacket> p_183757_, boolean p_183758_, boolean p_183759_)
    {
        if (p_183755_.level == this.level)
        {
            if (Reflector.ForgeEventFactory_fireChunkWatch.exists())
            {
                Reflector.ForgeEventFactory_fireChunkWatch.call(p_183758_, p_183759_, p_183755_, p_183756_, this.level);
            }

            if (p_183759_ && !p_183758_)
            {
                ChunkHolder chunkholder = this.getVisibleChunkIfPresent(p_183756_.toLong());

                if (chunkholder != null)
                {
                    LevelChunk levelchunk = chunkholder.getTickingChunk();

                    if (levelchunk != null)
                    {
                        this.playerLoadedChunk(p_183755_, p_183757_, levelchunk);
                    }

                    DebugPackets.sendPoiPacketsForChunk(this.level, p_183756_);
                }
            }

            if (!p_183759_ && p_183758_)
            {
                p_183755_.untrackChunk(p_183756_);
            }
        }
    }

    public int size()
    {
        return this.visibleChunkMap.size();
    }

    public net.minecraft.server.level.DistanceManager getDistanceManager()
    {
        return this.distanceManager;
    }

    protected Iterable<ChunkHolder> getChunks()
    {
        return Iterables.unmodifiableIterable(this.visibleChunkMap.values());
    }

    void dumpChunks(Writer p_140275_) throws IOException
    {
        CsvOutput csvoutput = CsvOutput.builder().addColumn("x").addColumn("z").addColumn("level").addColumn("in_memory").addColumn("status").addColumn("full_status").addColumn("accessible_ready").addColumn("ticking_ready").addColumn("entity_ticking_ready").addColumn("ticket").addColumn("spawning").addColumn("block_entity_count").addColumn("ticking_ticket").addColumn("ticking_level").addColumn("block_ticks").addColumn("fluid_ticks").build(p_140275_);
        TickingTracker tickingtracker = this.distanceManager.tickingTracker();

        for (Entry<ChunkHolder> entry : this.visibleChunkMap.long2ObjectEntrySet())
        {
            long i = entry.getLongKey();
            ChunkPos chunkpos = new ChunkPos(i);
            ChunkHolder chunkholder = entry.getValue();
            Optional<ChunkAccess> optional = Optional.ofNullable(chunkholder.getLastAvailable());
            Optional<LevelChunk> optional1 = optional.flatMap((p_212879_0_) ->
            {
                return p_212879_0_ instanceof LevelChunk ? Optional.of((LevelChunk)p_212879_0_) : Optional.empty();
            });
            csvoutput.a(chunkpos.x, chunkpos.z, chunkholder.getTicketLevel(), optional.isPresent(), optional.map(ChunkAccess::getStatus).orElse((ChunkStatus)null), optional1.map(LevelChunk::getFullStatus).orElse((ChunkHolder.FullChunkStatus)null), printFuture(chunkholder.getFullChunkFuture()), printFuture(chunkholder.getTickingChunkFuture()), printFuture(chunkholder.getEntityTickingChunkFuture()), this.distanceManager.getTicketDebugString(i), this.anyPlayerCloseEnoughForSpawning(chunkpos), optional1.map((p_203073_0_) ->
            {
                return p_203073_0_.getBlockEntities().size();
            }).orElse(0), tickingtracker.getTicketDebugString(i), tickingtracker.getLevel(i), optional1.map((p_212885_0_) ->
            {
                return p_212885_0_.getBlockTicks().count();
            }).orElse(0), optional1.map((p_212881_0_) ->
            {
                return p_212881_0_.getFluidTicks().count();
            }).orElse(0));
        }
    }

    private static String printFuture(CompletableFuture<Either<LevelChunk, ChunkHolder.ChunkLoadingFailure>> p_140279_)
    {
        try
        {
            Either<LevelChunk, ChunkHolder.ChunkLoadingFailure> either = p_140279_.getNow((Either)null);
            return either != null ? either.map((p_212866_0_) ->
            {
                return "done";
            }, (p_212848_0_) ->
            {
                return "unloaded";
            }) : "not completed";
        }
        catch (CompletionException completionexception)
        {
            return "failed " + completionexception.getCause().getMessage();
        }
        catch (CancellationException cancellationexception1)
        {
            return "cancelled";
        }
    }

    @Nullable
    private CompoundTag readChunk(ChunkPos pPos) throws IOException
    {
        CompoundTag compoundtag = this.read(pPos);
        return compoundtag == null ? null : this.upgradeChunkTag(this.level.dimension(), this.overworldDataStorage, compoundtag, this.generator.getTypeNameForDataFixer());
    }

    boolean anyPlayerCloseEnoughForSpawning(ChunkPos p_183880_)
    {
        long i = p_183880_.toLong();

        if (!this.distanceManager.hasPlayersNearby(i))
        {
            return false;
        }
        else
        {
            for (ServerPlayer serverplayer : this.playerMap.getPlayers(i))
            {
                if (this.playerIsCloseEnoughForSpawning(serverplayer, p_183880_))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public List<ServerPlayer> getPlayersCloseForSpawning(ChunkPos p_183889_)
    {
        long i = p_183889_.toLong();

        if (!this.distanceManager.hasPlayersNearby(i))
        {
            return List.of();
        }
        else
        {
            Builder<ServerPlayer> builder = ImmutableList.builder();

            for (ServerPlayer serverplayer : this.playerMap.getPlayers(i))
            {
                if (this.playerIsCloseEnoughForSpawning(serverplayer, p_183889_))
                {
                    builder.add(serverplayer);
                }
            }

            return builder.build();
        }
    }

    private boolean playerIsCloseEnoughForSpawning(ServerPlayer p_183752_, ChunkPos p_183753_)
    {
        if (p_183752_.isSpectator())
        {
            return false;
        }
        else
        {
            double d0 = euclideanDistanceSquared(p_183753_, p_183752_);
            return d0 < 16384.0D;
        }
    }

    private boolean skipPlayer(ServerPlayer pPlayer)
    {
        return pPlayer.isSpectator() && !this.level.getGameRules().getBoolean(GameRules.RULE_SPECTATORSGENERATECHUNKS);
    }

    void updatePlayerStatus(ServerPlayer pPlayer, boolean pTrack)
    {
        boolean flag = this.skipPlayer(pPlayer);
        boolean flag1 = this.playerMap.ignoredOrUnknown(pPlayer);
        int i = SectionPos.blockToSectionCoord(pPlayer.getBlockX());
        int j = SectionPos.blockToSectionCoord(pPlayer.getBlockZ());

        if (pTrack)
        {
            this.playerMap.addPlayer(ChunkPos.asLong(i, j), pPlayer, flag);
            this.updatePlayerPos(pPlayer);

            if (!flag)
            {
                this.distanceManager.addPlayer(SectionPos.of(pPlayer), pPlayer);
            }
        }
        else
        {
            SectionPos sectionpos = pPlayer.getLastSectionPos();
            this.playerMap.removePlayer(sectionpos.chunk().toLong(), pPlayer);

            if (!flag1)
            {
                this.distanceManager.removePlayer(sectionpos, pPlayer);
            }
        }

        for (int l = i - this.viewDistance - 1; l <= i + this.viewDistance + 1; ++l)
        {
            for (int k = j - this.viewDistance - 1; k <= j + this.viewDistance + 1; ++k)
            {
                if (isChunkInRange(l, k, i, j, this.viewDistance))
                {
                    ChunkPos chunkpos = new ChunkPos(l, k);
                    this.updateChunkTracking(pPlayer, chunkpos, new MutableObject<>(), !pTrack, pTrack);
                }
            }
        }
    }

    private SectionPos updatePlayerPos(ServerPlayer p_140374_)
    {
        SectionPos sectionpos = SectionPos.of(p_140374_);
        p_140374_.setLastSectionPos(sectionpos);
        p_140374_.connection.send(new ClientboundSetChunkCacheCenterPacket(sectionpos.x(), sectionpos.z()));
        return sectionpos;
    }

    public void move(ServerPlayer pPlayer)
    {
        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values())
        {
            if (chunkmap$trackedentity.entity == pPlayer)
            {
                chunkmap$trackedentity.updatePlayers(this.level.players());
            }
            else
            {
                chunkmap$trackedentity.updatePlayer(pPlayer);
            }
        }

        int i2 = SectionPos.blockToSectionCoord(pPlayer.getBlockX());
        int j2 = SectionPos.blockToSectionCoord(pPlayer.getBlockZ());
        SectionPos sectionpos = pPlayer.getLastSectionPos();
        SectionPos sectionpos1 = SectionPos.of(pPlayer);
        long i = sectionpos.chunk().toLong();
        long j = sectionpos1.chunk().toLong();
        boolean flag = this.playerMap.ignored(pPlayer);
        boolean flag1 = this.skipPlayer(pPlayer);
        boolean flag2 = sectionpos.asLong() != sectionpos1.asLong();

        if (flag2 || flag != flag1)
        {
            this.updatePlayerPos(pPlayer);

            if (!flag)
            {
                this.distanceManager.removePlayer(sectionpos, pPlayer);
            }

            if (!flag1)
            {
                this.distanceManager.addPlayer(sectionpos1, pPlayer);
            }

            if (!flag && flag1)
            {
                this.playerMap.ignorePlayer(pPlayer);
            }

            if (flag && !flag1)
            {
                this.playerMap.unIgnorePlayer(pPlayer);
            }

            if (i != j)
            {
                this.playerMap.updatePlayer(i, j, pPlayer);
            }
        }

        int k = sectionpos.x();
        int l = sectionpos.z();

        if (Math.abs(k - i2) <= this.viewDistance * 2 && Math.abs(l - j2) <= this.viewDistance * 2)
        {
            int l2 = Math.min(i2, k) - this.viewDistance - 1;
            int j3 = Math.min(j2, l) - this.viewDistance - 1;
            int k3 = Math.max(i2, k) + this.viewDistance + 1;
            int l3 = Math.max(j2, l) + this.viewDistance + 1;

            for (int k1 = l2; k1 <= k3; ++k1)
            {
                for (int l1 = j3; l1 <= l3; ++l1)
                {
                    boolean flag5 = isChunkInRange(k1, l1, k, l, this.viewDistance);
                    boolean flag6 = isChunkInRange(k1, l1, i2, j2, this.viewDistance);
                    this.updateChunkTracking(pPlayer, new ChunkPos(k1, l1), new MutableObject<>(), flag5, flag6);
                }
            }
        }
        else
        {
            for (int i1 = k - this.viewDistance - 1; i1 <= k + this.viewDistance + 1; ++i1)
            {
                for (int j1 = l - this.viewDistance - 1; j1 <= l + this.viewDistance + 1; ++j1)
                {
                    if (isChunkInRange(i1, j1, k, l, this.viewDistance))
                    {
                        boolean flag3 = true;
                        boolean flag4 = false;
                        this.updateChunkTracking(pPlayer, new ChunkPos(i1, j1), new MutableObject<>(), true, false);
                    }
                }
            }

            for (int k2 = i2 - this.viewDistance - 1; k2 <= i2 + this.viewDistance + 1; ++k2)
            {
                for (int i3 = j2 - this.viewDistance - 1; i3 <= j2 + this.viewDistance + 1; ++i3)
                {
                    if (isChunkInRange(k2, i3, i2, j2, this.viewDistance))
                    {
                        boolean flag7 = false;
                        boolean flag8 = true;
                        this.updateChunkTracking(pPlayer, new ChunkPos(k2, i3), new MutableObject<>(), false, true);
                    }
                }
            }
        }
    }

    public List<ServerPlayer> getPlayers(ChunkPos p_183801_, boolean p_183802_)
    {
        Set<ServerPlayer> set = this.playerMap.getPlayers(p_183801_.toLong());
        Builder<ServerPlayer> builder = ImmutableList.builder();

        for (ServerPlayer serverplayer : set)
        {
            SectionPos sectionpos = serverplayer.getLastSectionPos();

            if (p_183802_ && isChunkOnRangeBorder(p_183801_.x, p_183801_.z, sectionpos.x(), sectionpos.z(), this.viewDistance) || !p_183802_ && isChunkInRange(p_183801_.x, p_183801_.z, sectionpos.x(), sectionpos.z(), this.viewDistance))
            {
                builder.add(serverplayer);
            }
        }

        return builder.build();
    }

    protected void addEntity(Entity pEntity)
    {
        boolean flag = pEntity instanceof EnderDragonPart;

        if (Reflector.PartEntity.exists())
        {
            flag = Reflector.PartEntity.isInstance(pEntity);
        }

        if (!flag)
        {
            EntityType<?> entitytype = pEntity.getType();
            int i = entitytype.clientTrackingRange() * 16;

            if (i != 0)
            {
                int j = entitytype.updateInterval();

                if (this.entityMap.containsKey(pEntity.getId()))
                {
                    throw(IllegalStateException)Util.pauseInIde(new IllegalStateException("Entity is already tracked!"));
                }

                ChunkMap.TrackedEntity chunkmap$trackedentity = new ChunkMap.TrackedEntity(pEntity, i, j, entitytype.trackDeltas());
                this.entityMap.put(pEntity.getId(), chunkmap$trackedentity);
                chunkmap$trackedentity.updatePlayers(this.level.players());

                if (pEntity instanceof ServerPlayer)
                {
                    ServerPlayer serverplayer = (ServerPlayer)pEntity;
                    this.updatePlayerStatus(serverplayer, true);

                    for (ChunkMap.TrackedEntity chunkmap$trackedentity1 : this.entityMap.values())
                    {
                        if (chunkmap$trackedentity1.entity != serverplayer)
                        {
                            chunkmap$trackedentity1.updatePlayer(serverplayer);
                        }
                    }
                }
            }
        }
    }

    protected void removeEntity(Entity pEntity)
    {
        if (pEntity instanceof ServerPlayer)
        {
            ServerPlayer serverplayer = (ServerPlayer)pEntity;
            this.updatePlayerStatus(serverplayer, false);

            for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values())
            {
                chunkmap$trackedentity.removePlayer(serverplayer);
            }
        }

        ChunkMap.TrackedEntity chunkmap$trackedentity1 = this.entityMap.remove(pEntity.getId());

        if (chunkmap$trackedentity1 != null)
        {
            chunkmap$trackedentity1.broadcastRemoved();
        }
    }

    protected void tick()
    {
        List<ServerPlayer> list = Lists.newArrayList();
        List<ServerPlayer> list1 = this.level.players();

        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values())
        {
            SectionPos sectionpos = chunkmap$trackedentity.lastSectionPos;
            SectionPos sectionpos1 = SectionPos.of(chunkmap$trackedentity.entity);
            boolean flag = !Objects.equals(sectionpos, sectionpos1);

            if (flag)
            {
                chunkmap$trackedentity.updatePlayers(list1);
                Entity entity = chunkmap$trackedentity.entity;

                if (entity instanceof ServerPlayer)
                {
                    list.add((ServerPlayer)entity);
                }

                chunkmap$trackedentity.lastSectionPos = sectionpos1;
            }

            if (flag || this.distanceManager.inEntityTickingRange(sectionpos1.chunk().toLong()))
            {
                chunkmap$trackedentity.serverEntity.sendChanges();
            }
        }

        if (!list.isEmpty())
        {
            for (ChunkMap.TrackedEntity chunkmap$trackedentity1 : this.entityMap.values())
            {
                chunkmap$trackedentity1.updatePlayers(list);
            }
        }
    }

    public void broadcast(Entity p_140202_, Packet<?> p_140203_)
    {
        ChunkMap.TrackedEntity chunkmap$trackedentity = this.entityMap.get(p_140202_.getId());

        if (chunkmap$trackedentity != null)
        {
            chunkmap$trackedentity.broadcast(p_140203_);
        }
    }

    protected void broadcastAndSend(Entity p_140334_, Packet<?> p_140335_)
    {
        ChunkMap.TrackedEntity chunkmap$trackedentity = this.entityMap.get(p_140334_.getId());

        if (chunkmap$trackedentity != null)
        {
            chunkmap$trackedentity.broadcastAndSend(p_140335_);
        }
    }

    private void playerLoadedChunk(ServerPlayer p_183761_, MutableObject<ClientboundLevelChunkWithLightPacket> p_183762_, LevelChunk p_183763_)
    {
        if (p_183762_.getValue() == null)
        {
            p_183762_.setValue(new ClientboundLevelChunkWithLightPacket(p_183763_, this.lightEngine, (BitSet)null, (BitSet)null, true));
        }

        p_183761_.trackChunk(p_183763_.getPos(), p_183762_.getValue());
        DebugPackets.sendPoiPacketsForChunk(this.level, p_183763_.getPos());
        List<Entity> list = Lists.newArrayList();
        List<Entity> list1 = Lists.newArrayList();

        for (ChunkMap.TrackedEntity chunkmap$trackedentity : this.entityMap.values())
        {
            Entity entity = chunkmap$trackedentity.entity;

            if (entity != p_183761_ && entity.chunkPosition().equals(p_183763_.getPos()))
            {
                chunkmap$trackedentity.updatePlayer(p_183761_);

                if (entity instanceof Mob && ((Mob)entity).getLeashHolder() != null)
                {
                    list.add(entity);
                }

                if (!entity.getPassengers().isEmpty())
                {
                    list1.add(entity);
                }
            }
        }

        if (!list.isEmpty())
        {
            for (Entity entity1 : list)
            {
                p_183761_.connection.send(new ClientboundSetEntityLinkPacket(entity1, ((Mob)entity1).getLeashHolder()));
            }
        }

        if (!list1.isEmpty())
        {
            for (Entity entity2 : list1)
            {
                p_183761_.connection.send(new ClientboundSetPassengersPacket(entity2));
            }
        }
    }

    protected PoiManager getPoiManager()
    {
        return this.poiManager;
    }

    public String getStorageName()
    {
        return this.storageName;
    }

    void onFullChunkStatusChange(ChunkPos p_143076_, ChunkHolder.FullChunkStatus p_143077_)
    {
        this.chunkStatusListener.onChunkStatusChange(p_143076_, p_143077_);
    }

    class DistanceManager extends net.minecraft.server.level.DistanceManager
    {
        protected DistanceManager(Executor p_140459_, Executor p_140460_)
        {
            super(p_140459_, p_140460_);
        }

        protected boolean isChunkToRemove(long p_140462_)
        {
            return ChunkMap.this.toDrop.contains(p_140462_);
        }

        @Nullable
        protected ChunkHolder getChunk(long pChunkPos)
        {
            return ChunkMap.this.getUpdatingChunkIfPresent(pChunkPos);
        }

        @Nullable
        protected ChunkHolder updateChunkScheduling(long pChunkPos, int p_140465_, @Nullable ChunkHolder pNewLevel, int pHolder)
        {
            return ChunkMap.this.updateChunkScheduling(pChunkPos, p_140465_, pNewLevel, pHolder);
        }
    }

    class TrackedEntity
    {
        final ServerEntity serverEntity;
        final Entity entity;
        private final int range;
        SectionPos lastSectionPos;
        private final Set<ServerPlayerConnection> seenBy = Sets.newIdentityHashSet();

        public TrackedEntity(Entity p_140478_, int p_140479_, int p_140480_, boolean p_140481_)
        {
            this.serverEntity = new ServerEntity(ChunkMap.this.level, p_140478_, p_140480_, p_140481_, this::broadcast);
            this.entity = p_140478_;
            this.range = p_140479_;
            this.lastSectionPos = SectionPos.of(p_140478_);
        }

        public boolean equals(Object p_140506_)
        {
            if (p_140506_ instanceof ChunkMap.TrackedEntity)
            {
                return ((ChunkMap.TrackedEntity)p_140506_).entity.getId() == this.entity.getId();
            }
            else
            {
                return false;
            }
        }

        public int hashCode()
        {
            return this.entity.getId();
        }

        public void broadcast(Packet<?> p_140490_)
        {
            for (ServerPlayerConnection serverplayerconnection : this.seenBy)
            {
                serverplayerconnection.send(p_140490_);
            }
        }

        public void broadcastAndSend(Packet<?> p_140500_)
        {
            this.broadcast(p_140500_);

            if (this.entity instanceof ServerPlayer)
            {
                ((ServerPlayer)this.entity).connection.send(p_140500_);
            }
        }

        public void broadcastRemoved()
        {
            for (ServerPlayerConnection serverplayerconnection : this.seenBy)
            {
                this.serverEntity.removePairing(serverplayerconnection.getPlayer());
            }
        }

        public void removePlayer(ServerPlayer pPlayer)
        {
            if (this.seenBy.remove(pPlayer.connection))
            {
                this.serverEntity.removePairing(pPlayer);
            }
        }

        public void updatePlayer(ServerPlayer pPlayer)
        {
            if (pPlayer != this.entity)
            {
                Vec3 vec3 = pPlayer.position().subtract(this.serverEntity.sentPos());
                double d0 = (double)Math.min(this.getEffectiveRange(), (ChunkMap.this.viewDistance - 1) * 16);
                double d1 = vec3.x * vec3.x + vec3.z * vec3.z;
                double d2 = d0 * d0;
                boolean flag = d1 <= d2 && this.entity.broadcastToPlayer(pPlayer);

                if (flag)
                {
                    if (this.seenBy.add(pPlayer.connection))
                    {
                        this.serverEntity.addPairing(pPlayer);
                    }
                }
                else if (this.seenBy.remove(pPlayer.connection))
                {
                    this.serverEntity.removePairing(pPlayer);
                }
            }
        }

        private int scaledRange(int p_140484_)
        {
            return ChunkMap.this.level.getServer().getScaledTrackingDistance(p_140484_);
        }

        private int getEffectiveRange()
        {
            int i = this.range;

            for (Entity entity : this.entity.getIndirectPassengers())
            {
                int j = entity.getType().clientTrackingRange() * 16;

                if (j > i)
                {
                    i = j;
                }
            }

            return this.scaledRange(i);
        }

        public void updatePlayers(List<ServerPlayer> pPlayersList)
        {
            for (ServerPlayer serverplayer : pPlayersList)
            {
                this.updatePlayer(serverplayer);
            }
        }
    }
}
