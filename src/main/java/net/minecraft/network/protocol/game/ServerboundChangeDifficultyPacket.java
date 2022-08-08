package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.Difficulty;

public class ServerboundChangeDifficultyPacket implements Packet<ServerGamePacketListener>
{
    private final Difficulty difficulty;

    public ServerboundChangeDifficultyPacket(Difficulty pBuffer)
    {
        this.difficulty = pBuffer;
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleChangeDifficulty(this);
    }

    public ServerboundChangeDifficultyPacket(FriendlyByteBuf pBuffer)
    {
        this.difficulty = Difficulty.byId(pBuffer.readUnsignedByte());
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeByte(this.difficulty.getId());
    }

    public Difficulty getDifficulty()
    {
        return this.difficulty;
    }
}
