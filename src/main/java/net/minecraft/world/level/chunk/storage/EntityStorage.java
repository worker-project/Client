package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.ChunkEntities;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import org.slf4j.Logger;

public class EntityStorage implements EntityPersistentStorage<Entity>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITIES_TAG = "Entities";
    private static final String POSITION_TAG = "Position";
    private final ServerLevel level;
    private final IOWorker worker;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final ProcessorMailbox<Runnable> entityDeserializerQueue;
    protected final DataFixer fixerUpper;

    public EntityStorage(ServerLevel p_196924_, Path p_196925_, DataFixer p_196926_, boolean p_196927_, Executor p_196928_)
    {
        this.level = p_196924_;
        this.fixerUpper = p_196926_;
        this.entityDeserializerQueue = ProcessorMailbox.create(p_196928_, "entity-deserializer");
        this.worker = new IOWorker(p_196925_, p_196927_, "entities");
    }

    public CompletableFuture<ChunkEntities<Entity>> loadEntities(ChunkPos pPos)
    {
        return this.emptyChunks.contains(pPos.toLong()) ? CompletableFuture.completedFuture(emptyChunk(pPos)) : this.worker.loadAsync(pPos).thenApplyAsync((p_156557_) ->
        {
            if (p_156557_ == null)
            {
                this.emptyChunks.add(pPos.toLong());
                return emptyChunk(pPos);
            }
            else {
                try {
                    ChunkPos chunkpos = readChunkPos(p_156557_);

                    if (!Objects.equals(pPos, chunkpos))
                    {
                        LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", pPos, pPos, chunkpos);
                    }
                }
                catch (Exception exception)
                {
                    LOGGER.warn("Failed to parse chunk {} position info", pPos, exception);
                }

                CompoundTag compoundtag = this.upgradeChunkTag(p_156557_);
                ListTag listtag = compoundtag.getList("Entities", 10);
                List<Entity> list = EntityType.loadEntitiesRecursive(listtag, this.level).collect(ImmutableList.toImmutableList());
                return new ChunkEntities<>(pPos, list);
            }
        }, this.entityDeserializerQueue::tell);
    }

    private static ChunkPos readChunkPos(CompoundTag pTag)
    {
        int[] aint = pTag.getIntArray("Position");
        return new ChunkPos(aint[0], aint[1]);
    }

    private static void writeChunkPos(CompoundTag pTag, ChunkPos pPos)
    {
        pTag.put("Position", new IntArrayTag(new int[] {pPos.x, pPos.z}));
    }

    private static ChunkEntities<Entity> emptyChunk(ChunkPos pPos)
    {
        return new ChunkEntities<>(pPos, ImmutableList.of());
    }

    public void storeEntities(ChunkEntities<Entity> pEntities)
    {
        ChunkPos chunkpos = pEntities.getPos();

        if (pEntities.isEmpty())
        {
            if (this.emptyChunks.add(chunkpos.toLong()))
            {
                this.worker.store(chunkpos, (CompoundTag)null);
            }
        }
        else
        {
            ListTag listtag = new ListTag();
            pEntities.getEntities().forEach((p_156567_) ->
            {
                CompoundTag compoundtag1 = new CompoundTag();

                if (p_156567_.save(compoundtag1))
                {
                    listtag.add(compoundtag1);
                }
            });
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
            compoundtag.put("Entities", listtag);
            writeChunkPos(compoundtag, chunkpos);
            this.worker.store(chunkpos, compoundtag).exceptionally((p_156554_) ->
            {
                LOGGER.error("Failed to store chunk {}", chunkpos, p_156554_);
                return null;
            });
            this.emptyChunks.remove(chunkpos.toLong());
        }
    }

    public void flush(boolean p_182487_)
    {
        this.worker.synchronize(p_182487_).join();
        this.entityDeserializerQueue.runAll();
    }

    private CompoundTag upgradeChunkTag(CompoundTag pTag)
    {
        int i = getVersion(pTag);
        return NbtUtils.update(this.fixerUpper, DataFixTypes.ENTITY_CHUNK, pTag, i);
    }

    public static int getVersion(CompoundTag pTag)
    {
        return pTag.contains("DataVersion", 99) ? pTag.getInt("DataVersion") : -1;
    }

    public void close() throws IOException
    {
        this.worker.close();
    }
}
