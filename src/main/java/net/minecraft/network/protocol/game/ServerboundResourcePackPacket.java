package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundResourcePackPacket implements Packet<ServerGamePacketListener>
{
    private final ServerboundResourcePackPacket.Action action;

    public ServerboundResourcePackPacket(ServerboundResourcePackPacket.Action pBuffer)
    {
        this.action = pBuffer;
    }

    public ServerboundResourcePackPacket(FriendlyByteBuf pBuffer)
    {
        this.action = pBuffer.readEnum(ServerboundResourcePackPacket.Action.class);
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeEnum(this.action);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleResourcePackResponse(this);
    }

    public ServerboundResourcePackPacket.Action getAction()
    {
        return this.action;
    }

    public static enum Action
    {
        SUCCESSFULLY_LOADED,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED;
    }
}
