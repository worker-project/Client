package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;

public class BanPlayerCommands
{
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType(new TranslatableComponent("commands.ban.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher)
    {
        pDispatcher.register(Commands.literal("ban").requires((p_136563_) ->
        {
            return p_136563_.hasPermission(3);
        }).then(Commands.argument("targets", GameProfileArgument.gameProfile()).executes((p_136569_) ->
        {
            return banPlayers(p_136569_.getSource(), GameProfileArgument.getGameProfiles(p_136569_, "targets"), (Component)null);
        }).then(Commands.argument("reason", MessageArgument.message()).executes((p_136561_) ->
        {
            return banPlayers(p_136561_.getSource(), GameProfileArgument.getGameProfiles(p_136561_, "targets"), MessageArgument.getMessage(p_136561_, "reason"));
        }))));
    }

    private static int banPlayers(CommandSourceStack pSource, Collection<GameProfile> pGameProfiles, @Nullable Component pReason) throws CommandSyntaxException
    {
        UserBanList userbanlist = pSource.getServer().getPlayerList().getBans();
        int i = 0;

        for (GameProfile gameprofile : pGameProfiles)
        {
            if (!userbanlist.isBanned(gameprofile))
            {
                UserBanListEntry userbanlistentry = new UserBanListEntry(gameprofile, (Date)null, pSource.getTextName(), (Date)null, pReason == null ? null : pReason.getString());
                userbanlist.add(userbanlistentry);
                ++i;
                pSource.sendSuccess(new TranslatableComponent("commands.ban.success", ComponentUtils.getDisplayName(gameprofile), userbanlistentry.getReason()), true);
                ServerPlayer serverplayer = pSource.getServer().getPlayerList().getPlayer(gameprofile.getId());

                if (serverplayer != null)
                {
                    serverplayer.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.banned"));
                }
            }
        }

        if (i == 0)
        {
            throw ERROR_ALREADY_BANNED.create();
        }
        else
        {
            return i;
        }
    }
}
