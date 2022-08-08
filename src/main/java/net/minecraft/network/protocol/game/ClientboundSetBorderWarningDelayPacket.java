package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDelayPacket implements Packet<ClientGamePacketListener>
{
    private final int warningDelay;

    public ClientboundSetBorderWarningDelayPacket(WorldBorder pBuffer)
    {
        this.warningDelay = pBuffer.getWarningTime();
    }

    public ClientboundSetBorderWarningDelayPacket(FriendlyByteBuf pBuffer)
    {
        this.warningDelay = pBuffer.readVarInt();
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeVarInt(this.warningDelay);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetBorderWarningDelay(this);
    }

    public int getWarningDelay()
    {
        return this.warningDelay;
    }
}
