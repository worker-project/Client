package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ServerboundMoveVehiclePacket implements Packet<ServerGamePacketListener>
{
    private final double x;
    private final double y;
    private final double z;
    private final float yRot;
    private final float xRot;

    public ServerboundMoveVehiclePacket(Entity pBuffer)
    {
        this.x = pBuffer.getX();
        this.y = pBuffer.getY();
        this.z = pBuffer.getZ();
        this.yRot = pBuffer.getYRot();
        this.xRot = pBuffer.getXRot();
    }

    public ServerboundMoveVehiclePacket(FriendlyByteBuf pBuffer)
    {
        this.x = pBuffer.readDouble();
        this.y = pBuffer.readDouble();
        this.z = pBuffer.readDouble();
        this.yRot = pBuffer.readFloat();
        this.xRot = pBuffer.readFloat();
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeDouble(this.x);
        pBuffer.writeDouble(this.y);
        pBuffer.writeDouble(this.z);
        pBuffer.writeFloat(this.yRot);
        pBuffer.writeFloat(this.xRot);
    }

    public void handle(ServerGamePacketListener pHandler)
    {
        pHandler.handleMoveVehicle(this);
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public float getYRot()
    {
        return this.yRot;
    }

    public float getXRot()
    {
        return this.xRot;
    }
}
