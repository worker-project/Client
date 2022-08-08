package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener>
{
    private final int id;

    public ServerboundAcceptTeleportationPacket(int pId)
    {
        this.id = pId;
    }

    public ServerboundAcceptTeleportationPacket(FriendlyByteBuf pId)
    {
        this.id = pId.readVarInt();
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeVarInt(this.id);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleAcceptTeleportPacket(this);
    }

    public int getId()
    {
        return this.id;
    }
}
