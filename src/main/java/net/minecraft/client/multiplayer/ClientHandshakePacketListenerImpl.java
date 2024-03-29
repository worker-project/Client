package net.minecraft.client.multiplayer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.exceptions.InsufficientPrivilegesException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.logging.LogUtils;
import java.math.BigInteger;
import java.security.PublicKey;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.network.protocol.login.ClientboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import net.minecraft.network.protocol.login.ClientboundLoginDisconnectPacket;
import net.minecraft.network.protocol.login.ServerboundCustomQueryPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.HttpUtil;
import org.slf4j.Logger;

public class ClientHandshakePacketListenerImpl implements ClientLoginPacketListener
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    @Nullable
    private final Screen parent;
    private final Consumer<Component> updateStatus;
    private final Connection connection;
    private GameProfile localGameProfile;

    public ClientHandshakePacketListenerImpl(Connection pConnection, Minecraft pMinecraft, @Nullable Screen pParent, Consumer<Component> pUpdateStatus)
    {
        this.connection = pConnection;
        this.minecraft = pMinecraft;
        this.parent = pParent;
        this.updateStatus = pUpdateStatus;
    }

    public void handleHello(ClientboundHelloPacket pPacket)
    {
        Cipher cipher;
        Cipher cipher1;
        String s;
        ServerboundKeyPacket serverboundkeypacket;

        try
        {
            SecretKey secretkey = Crypt.generateSecretKey();
            PublicKey publickey = pPacket.getPublicKey();
            s = (new BigInteger(Crypt.digestData(pPacket.getServerId(), publickey, secretkey))).toString(16);
            cipher = Crypt.getCipher(2, secretkey);
            cipher1 = Crypt.getCipher(1, secretkey);
            serverboundkeypacket = new ServerboundKeyPacket(secretkey, publickey, pPacket.getNonce());
        }
        catch (CryptException cryptexception)
        {
            throw new IllegalStateException("Protocol error", cryptexception);
        }

        this.updateStatus.accept(new TranslatableComponent("connect.authorizing"));
        HttpUtil.DOWNLOAD_EXECUTOR.submit(() ->
        {
            Component component = this.authenticateServer(s);

            if (component != null)
            {
                if (this.minecraft.getCurrentServer() == null || !this.minecraft.getCurrentServer().isLan())
                {
                    this.connection.disconnect(component);
                    return;
                }

                LOGGER.warn(component.getString());
            }

            this.updateStatus.accept(new TranslatableComponent("connect.encrypting"));
            this.connection.send(serverboundkeypacket, (p_171627_) -> {
                this.connection.setEncryptionKey(cipher, cipher1);
            });
        });
    }

    @Nullable
    private Component authenticateServer(String pServerHash)
    {
        try
        {
            this.getMinecraftSessionService().joinServer(this.minecraft.getUser().getGameProfile(), this.minecraft.getUser().getAccessToken(), pServerHash);
            return null;
        }
        catch (AuthenticationUnavailableException authenticationunavailableexception)
        {
            return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.serversUnavailable"));
        }
        catch (InvalidCredentialsException invalidcredentialsexception)
        {
            return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.invalidSession"));
        }
        catch (InsufficientPrivilegesException insufficientprivilegesexception)
        {
            return new TranslatableComponent("disconnect.loginFailedInfo", new TranslatableComponent("disconnect.loginFailedInfo.insufficientPrivileges"));
        }
        catch (AuthenticationException authenticationexception)
        {
            return new TranslatableComponent("disconnect.loginFailedInfo", authenticationexception.getMessage());
        }
    }

    private MinecraftSessionService getMinecraftSessionService()
    {
        return this.minecraft.getMinecraftSessionService();
    }

    public void handleGameProfile(ClientboundGameProfilePacket pPacket)
    {
        this.updateStatus.accept(new TranslatableComponent("connect.joining"));
        this.localGameProfile = pPacket.getGameProfile();
        this.connection.setProtocol(ConnectionProtocol.PLAY);
        this.connection.setListener(new ClientPacketListener(this.minecraft, this.parent, this.connection, this.localGameProfile, this.minecraft.createTelemetryManager()));
    }

    public void onDisconnect(Component pReason)
    {
         this.minecraft.setScreen(new DisconnectedScreen(this.parent, CommonComponents.CONNECT_FAILED, pReason));
    }

    public Connection getConnection()
    {
        return this.connection;
    }

    public void handleDisconnect(ClientboundLoginDisconnectPacket pPacket)
    {
        this.connection.disconnect(pPacket.getReason());
    }

    public void handleCompression(ClientboundLoginCompressionPacket pPacket)
    {
        if (!this.connection.isMemoryConnection())
        {
            this.connection.setupCompression(pPacket.getCompressionThreshold(), false);
        }
    }

    public void handleCustomQuery(ClientboundCustomQueryPacket pPacket)
    {
        this.updateStatus.accept(new TranslatableComponent("connect.negotiating"));
        this.connection.send(new ServerboundCustomQueryPacket(pPacket.getTransactionId(), (FriendlyByteBuf)null));
    }
}
