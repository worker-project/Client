package net.minecraft.network.protocol.login;

import java.security.PublicKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener>
{
    private final String serverId;
    private final byte[] publicKey;
    private final byte[] nonce;

    public ClientboundHelloPacket(String pServerId, byte[] pPublicKey, byte[] pNonce)
    {
        this.serverId = pServerId;
        this.publicKey = pPublicKey;
        this.nonce = pNonce;
    }

    public ClientboundHelloPacket(FriendlyByteBuf pBuffer)
    {
        this.serverId = pBuffer.readUtf(20);
        this.publicKey = pBuffer.readByteArray();
        this.nonce = pBuffer.readByteArray();
    }

    public void write(FriendlyByteBuf pBuffer)
    {
        pBuffer.writeUtf(this.serverId);
        pBuffer.a(this.publicKey);
        pBuffer.a(this.nonce);
    }

    public void handle(ClientLoginPacketListener pHandler)
    {
        pHandler.handleHello(this);
    }

    public String getServerId()
    {
        return this.serverId;
    }

    public PublicKey getPublicKey() throws CryptException
    {
        return Crypt.a(this.publicKey);
    }

    public byte[] getNonce()
    {
        return this.nonce;
    }
}
