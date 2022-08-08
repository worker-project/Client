package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetPassengersPacket implements Packet<ClientGamePacketListener>
{
    private final int vehicle;
    private final int[] passengers;

    public ClientboundSetPassengersPacket(Entity pBuffer)
    {
        this.vehicle = pBuffer.getId();
        List<Entity> list = pBuffer.getPassengers();
        this.passengers = new int[list.size()];

        for (int i = 0; i < list.size(); ++i)
        {
            this.passengers[i] = list.get(i).getId();
        }
    }

    public ClientboundSetPassengersPacket(FriendlyByteBuf pBuffer)
    {
        this.vehicle = pBuffer.readVarInt();
        this.passengers = pBuffer.readVarIntArray();
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeVarInt(this.vehicle);
        pBuffer.a(this.passengers);
    }

    public void handle(ClientGamePacketListener pHandler)
    {
        pHandler.handleSetEntityPassengersPacket(this);
    }

    public int[] getPassengers()
    {
        return this.passengers;
    }

    public int getVehicle()
    {
        return this.vehicle;
    }
}
