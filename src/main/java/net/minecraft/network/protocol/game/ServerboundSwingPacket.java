package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundSwingPacket implements Packet<ServerGamePacketListener>
{
    private final InteractionHand hand;

    public ServerboundSwingPacket(InteractionHand pBuffer)
    {
        this.hand = pBuffer;
    }

    public ServerboundSwingPacket(FriendlyByteBuf pBuffer)
    {
        this.hand = pBuffer.readEnum(InteractionHand.class);
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeEnum(this.hand);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleAnimate(this);
    }

    public InteractionHand getHand()
    {
        return this.hand;
    }
}
