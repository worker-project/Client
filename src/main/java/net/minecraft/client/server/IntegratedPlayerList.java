package net.minecraft.client.server;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;

public class IntegratedPlayerList extends PlayerList
{
    private CompoundTag playerData;

    public IntegratedPlayerList(IntegratedServer pServer, RegistryAccess.Frozen pRegistryHolder, PlayerDataStorage pPlayerIo)
    {
        super(pServer, pRegistryHolder, pPlayerIo, 8);
        this.setViewDistance(10);
    }

    protected void save(ServerPlayer pPlayer)
    {
        if (pPlayer.getName().getString().equals(this.getServer().getSingleplayerName()))
        {
            this.playerData = pPlayer.saveWithoutId(new CompoundTag());
        }

        super.save(pPlayer);
    }

    public Component canPlayerLogin(SocketAddress pSocketAddress, GameProfile pGameProfile)
    {
        return (Component)(pGameProfile.getName().equalsIgnoreCase(this.getServer().getSingleplayerName()) && this.getPlayerByName(pGameProfile.getName()) != null ? new TranslatableComponent("multiplayer.disconnect.name_taken") : super.canPlayerLogin(pSocketAddress, pGameProfile));
    }

    public IntegratedServer getServer()
    {
        return (IntegratedServer)super.getServer();
    }

    public CompoundTag getSingleplayerData()
    {
        return this.playerData;
    }
}
