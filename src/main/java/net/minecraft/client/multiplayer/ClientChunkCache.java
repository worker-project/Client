package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.optifine.ChunkOF;
import net.optifine.reflect.Reflector;
import org.slf4j.Logger;

public class ClientChunkCache extends ChunkSource
{
    static final Logger LOGGER = LogUtils.getLogger();
    private final LevelChunk emptyChunk;
    private final LevelLightEngine lightEngine;
    volatile ClientChunkCache.Storage storage;
    final ClientLevel level;

    public ClientChunkCache(ClientLevel pLevel, int pViewDistance)
    {
        this.level = pLevel;
        this.emptyChunk = new EmptyLevelChunk(pLevel, new ChunkPos(0, 0), pLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getHolderOrThrow(Biomes.PLAINS));
        this.lightEngine = new LevelLightEngine(this, true, pLevel.dimensionType().hasSkyLight());
        this.storage = new ClientChunkCache.Storage(calculateStorageRange(pViewDistance));
    }

    public LevelLightEngine getLightEngine()
    {
        return this.lightEngine;
    }

    private static boolean isValidChunk(@Nullable LevelChunk pChunk, int pX, int pZ)
    {
        if (pChunk == null)
        {
            return false;
        }
        else
        {
            ChunkPos chunkpos = pChunk.getPos();
            return chunkpos.x == pX && chunkpos.z == pZ;
        }
    }

    public void drop(int pX, int pZ)
    {
        if (this.storage.inRange(pX, pZ))
        {
            int i = this.storage.getIndex(pX, pZ);
            LevelChunk levelchunk = this.storage.getChunk(i);

            if (isValidChunk(levelchunk, pX, pZ))
            {
                if (Reflector.ChunkEvent_Unload_Constructor.exists())
                {
                    Reflector.postForgeBusEvent(Reflector.ChunkEvent_Unload_Constructor, levelchunk);
                }

                levelchunk.setLoaded(false);
                this.storage.replace(i, levelchunk, (LevelChunk)null);
            }
        }
    }

    @Nullable
    public LevelChunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad)
    {
        if (this.storage.inRange(pChunkX, pChunkZ))
        {
            LevelChunk levelchunk = this.storage.getChunk(this.storage.getIndex(pChunkX, pChunkZ));

            if (isValidChunk(levelchunk, pChunkX, pChunkZ))
            {
                return levelchunk;
            }
        }

        return pLoad ? this.emptyChunk : null;
    }

    public BlockGetter getLevel()
    {
        return this.level;
    }

    @Nullable
    public LevelChunk replaceWithPacketData(int p_194117_, int p_194118_, FriendlyByteBuf p_194119_, CompoundTag p_194120_, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> p_194121_)
    {
        if (!this.storage.inRange(p_194117_, p_194118_))
        {
            LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", p_194117_, p_194118_);
            return null;
        }
        else
        {
            int i = this.storage.getIndex(p_194117_, p_194118_);
            LevelChunk levelchunk = this.storage.chunks.get(i);
            ChunkPos chunkpos = new ChunkPos(p_194117_, p_194118_);

            if (!isValidChunk(levelchunk, p_194117_, p_194118_))
            {
                if (levelchunk != null)
                {
                    levelchunk.setLoaded(false);
                }

                levelchunk = new ChunkOF(this.level, chunkpos);
                levelchunk.replaceWithPacketData(p_194119_, p_194120_, p_194121_);
                this.storage.replace(i, levelchunk);
            }
            else
            {
                levelchunk.replaceWithPacketData(p_194119_, p_194120_, p_194121_);
            }

            this.level.onChunkLoaded(chunkpos);

            if (Reflector.ChunkEvent_Load_Constructor.exists())
            {
                Reflector.postForgeBusEvent(Reflector.ChunkEvent_Load_Constructor, levelchunk);
            }

            levelchunk.setLoaded(true);
            return levelchunk;
        }
    }

    public void tick(BooleanSupplier p_202421_, boolean p_202422_)
    {
    }

    public void updateViewCenter(int pX, int pZ)
    {
        this.storage.viewCenterX = pX;
        this.storage.viewCenterZ = pZ;
    }

    public void updateViewRadius(int pViewDistance)
    {
        int i = this.storage.chunkRadius;
        int j = calculateStorageRange(pViewDistance);

        if (i != j)
        {
            ClientChunkCache.Storage clientchunkcache$storage = new ClientChunkCache.Storage(j);
            clientchunkcache$storage.viewCenterX = this.storage.viewCenterX;
            clientchunkcache$storage.viewCenterZ = this.storage.viewCenterZ;

            for (int k = 0; k < this.storage.chunks.length(); ++k)
            {
                LevelChunk levelchunk = this.storage.chunks.get(k);

                if (levelchunk != null)
                {
                    ChunkPos chunkpos = levelchunk.getPos();

                    if (clientchunkcache$storage.inRange(chunkpos.x, chunkpos.z))
                    {
                        clientchunkcache$storage.replace(clientchunkcache$storage.getIndex(chunkpos.x, chunkpos.z), levelchunk);
                    }
                }
            }

            this.storage = clientchunkcache$storage;
        }
    }

    private static int calculateStorageRange(int pViewDistance)
    {
        return Math.max(2, pViewDistance) + 3;
    }

    public String gatherStats()
    {
        return this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
    }

    public int getLoadedChunksCount()
    {
        return this.storage.chunkCount;
    }

    public void onLightUpdate(LightLayer pType, SectionPos pPos)
    {
        Minecraft.getInstance().levelRenderer.setSectionDirty(pPos.x(), pPos.y(), pPos.z());
    }

    final class Storage
    {
        final AtomicReferenceArray<LevelChunk> chunks;
        final int chunkRadius;
        private final int viewRange;
        volatile int viewCenterX;
        volatile int viewCenterZ;
        int chunkCount;

        Storage(int p_104474_)
        {
            this.chunkRadius = p_104474_;
            this.viewRange = p_104474_ * 2 + 1;
            this.chunks = new AtomicReferenceArray<>(this.viewRange * this.viewRange);
        }

        int getIndex(int pX, int pZ)
        {
            return Math.floorMod(pZ, this.viewRange) * this.viewRange + Math.floorMod(pX, this.viewRange);
        }

        protected void replace(int pChunkIndex, @Nullable LevelChunk pChunk)
        {
            LevelChunk levelchunk = this.chunks.getAndSet(pChunkIndex, pChunk);

            if (levelchunk != null)
            {
                --this.chunkCount;
                ClientChunkCache.this.level.unload(levelchunk);
            }

            if (pChunk != null)
            {
                ++this.chunkCount;
            }
        }

        protected LevelChunk replace(int pChunkIndex, LevelChunk pChunk, @Nullable LevelChunk pReplaceWith)
        {
            if (this.chunks.compareAndSet(pChunkIndex, pChunk, pReplaceWith) && pReplaceWith == null)
            {
                --this.chunkCount;
            }

            ClientChunkCache.this.level.unload(pChunk);
            return pChunk;
        }

        boolean inRange(int pX, int pZ)
        {
            return Math.abs(pX - this.viewCenterX) <= this.chunkRadius && Math.abs(pZ - this.viewCenterZ) <= this.chunkRadius;
        }

        @Nullable
        protected LevelChunk getChunk(int pChunkIndex)
        {
            return this.chunks.get(pChunkIndex);
        }

        private void dumpChunks(String p_171623_)
        {
            try
            {
                FileOutputStream fileoutputstream = new FileOutputStream(p_171623_);

                try
                {
                    int i = ClientChunkCache.this.storage.chunkRadius;

                    for (int j = this.viewCenterZ - i; j <= this.viewCenterZ + i; ++j)
                    {
                        for (int k = this.viewCenterX - i; k <= this.viewCenterX + i; ++k)
                        {
                            LevelChunk levelchunk = ClientChunkCache.this.storage.chunks.get(ClientChunkCache.this.storage.getIndex(k, j));

                            if (levelchunk != null)
                            {
                                ChunkPos chunkpos = levelchunk.getPos();
                                fileoutputstream.write((chunkpos.x + "\t" + chunkpos.z + "\t" + levelchunk.isEmpty() + "\n").getBytes(StandardCharsets.UTF_8));
                            }
                        }
                    }
                }
                catch (Throwable throwable11)
                {
                    try
                    {
                        fileoutputstream.close();
                    }
                    catch (Throwable throwable1)
                    {
                        throwable11.addSuppressed(throwable1);
                    }

                    throw throwable11;
                }

                fileoutputstream.close();
            }
            catch (IOException ioexception1)
            {
                ClientChunkCache.LOGGER.error("Failed to dump chunks to file {}", p_171623_, ioexception1);
            }
        }
    }
}
