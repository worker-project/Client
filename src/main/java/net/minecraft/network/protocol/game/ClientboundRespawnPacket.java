package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class ClientboundRespawnPacket implements Packet<ClientGamePacketListener>
{
    private final Holder<DimensionType> dimensionType;
    private final ResourceKey<Level> dimension;
    private final long seed;
    private final GameType playerGameType;
    @Nullable
    private final GameType previousPlayerGameType;
    private final boolean isDebug;
    private final boolean isFlat;
    private final boolean keepAllPlayerData;

    public ClientboundRespawnPacket(Holder<DimensionType> pDimensionType, ResourceKey<Level> pDimension, long pSeed, GameType p_206646_, @Nullable GameType pPlayerGameType, boolean pPreviousPlayerGameType, boolean pIsDebug, boolean pIsFlat)
    {
        this.dimensionType = pDimensionType;
        this.dimension = pDimension;
        this.seed = pSeed;
        this.playerGameType = p_206646_;
        this.previousPlayerGameType = pPlayerGameType;
        this.isDebug = pPreviousPlayerGameType;
        this.isFlat = pIsDebug;
        this.keepAllPlayerData = pIsFlat;
    }

    public ClientboundRespawnPacket(FriendlyByteBuf pBuffer)
    {
        this.dimensionType = pBuffer.readWithCodec(DimensionType.CODEC);
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, pBuffer.readResourceLocation());
        this.seed = pBuffer.readLong();
        this.playerGameType = GameType.byId(pBuffer.readUnsignedByte());
        this.previousPlayerGameType = GameType.byNullableId(pBuffer.readByte());
        this.isDebug = pBuffer.readBoolean();
        this.isFlat = pBuffer.readBoolean();
        this.keepAllPlayerData = pBuffer.readBoolean();
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeWithCodec(DimensionType.CODEC, this.dimensionType);
        pBuffer.writeResourceLocation(this.dimension.location());
        pBuffer.writeLong(this.seed);
        pBuffer.writeByte(this.playerGameType.getId());
        pBuffer.writeByte(GameType.getNullableId(this.previousPlayerGameType));
        pBuffer.writeBoolean(this.isDebug);
        pBuffer.writeBoolean(this.isFlat);
        pBuffer.writeBoolean(this.keepAllPlayerData);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleRespawn(this);
    }

    public Holder<DimensionType> getDimensionType()
    {
        return this.dimensionType;
    }

    public ResourceKey<Level> getDimension()
    {
        return this.dimension;
    }

    public long getSeed()
    {
        return this.seed;
    }

    public GameType getPlayerGameType()
    {
        return this.playerGameType;
    }

    @Nullable
    public GameType getPreviousPlayerGameType()
    {
        return this.previousPlayerGameType;
    }

    public boolean isDebug()
    {
        return this.isDebug;
    }

    public boolean isFlat()
    {
        return this.isFlat;
    }

    public boolean shouldKeepAllPlayerData()
    {
        return this.keepAllPlayerData;
    }
}
