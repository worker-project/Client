package net.minecraft.server;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class Main
{
    private static final Logger LOGGER = LogUtils.getLogger();

    @DontObfuscate
    public static void main(String[] pArgs)
    {
        Thread.currentThread().setPriority(5);
        SharedConstants.tryDetectVersion();
        OptionParser optionparser = new OptionParser();
        OptionSpec<Void> optionspec = optionparser.accepts("nogui");
        OptionSpec<Void> optionspec1 = optionparser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpec<Void> optionspec3 = optionparser.accepts("bonusChest");
        OptionSpec<Void> optionspec4 = optionparser.accepts("forceUpgrade");
        OptionSpec<Void> optionspec5 = optionparser.accepts("eraseCache");
        OptionSpec<Void> optionspec6 = optionparser.accepts("safeMode", "Loads level with vanilla datapack only");
        OptionSpec<Void> optionspec7 = optionparser.accepts("help").forHelp();
        OptionSpec<String> optionspec8 = optionparser.accepts("singleplayer").withRequiredArg();
        OptionSpec<String> optionspec9 = optionparser.accepts("universe").withRequiredArg().defaultsTo(".");
        OptionSpec<String> optionspec10 = optionparser.accepts("world").withRequiredArg();
        OptionSpec<Integer> optionspec11 = optionparser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
        OptionSpec<String> optionspec12 = optionparser.accepts("serverId").withRequiredArg();
        OptionSpec<Void> optionspec13 = optionparser.accepts("jfrProfile");
        OptionSpec<String> optionspec14 = optionparser.nonOptions();

        try
        {
            OptionSet optionset = optionparser.parse(pArgs);

            if (optionset.has(optionspec7))
            {
                optionparser.printHelpOn(System.err);
                return;
            }

            CrashReport.preload();

            if (optionset.has(optionspec13))
            {
                JvmProfiler.INSTANCE.start(Environment.SERVER);
            }

            Bootstrap.bootStrap();
            Bootstrap.validate();
            Util.startTimerHackThread();
            Path path = Paths.get("server.properties");
            DedicatedServerSettings dedicatedserversettings = new DedicatedServerSettings(path);
            dedicatedserversettings.forceSave();
            Path path1 = Paths.get("eula.txt");
            Eula eula = new Eula(path1);

            if (optionset.has(optionspec1))
            {
                LOGGER.info("Initialized '{}' and '{}'", path.toAbsolutePath(), path1.toAbsolutePath());
                return;
            }

            if (!eula.hasAgreedToEULA())
            {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }

            File file1 = new File(optionset.valueOf(optionspec9));
            YggdrasilAuthenticationService yggdrasilauthenticationservice = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
            MinecraftSessionService minecraftsessionservice = yggdrasilauthenticationservice.createMinecraftSessionService();
            GameProfileRepository gameprofilerepository = yggdrasilauthenticationservice.createProfileRepository();
            GameProfileCache gameprofilecache = new GameProfileCache(gameprofilerepository, new File(file1, MinecraftServer.USERID_CACHE_FILE.getName()));
            String s = Optional.ofNullable(optionset.valueOf(optionspec10)).orElse(dedicatedserversettings.getProperties().levelName);
            LevelStorageSource levelstoragesource = LevelStorageSource.createDefault(file1.toPath());
            LevelStorageSource.LevelStorageAccess levelstoragesource$levelstorageaccess = levelstoragesource.createAccess(s);
            LevelSummary levelsummary = levelstoragesource$levelstorageaccess.getSummary();

            if (levelsummary != null)
            {
                if (levelsummary.requiresManualConversion())
                {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }

                if (!levelsummary.isCompatible())
                {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            }

            boolean flag = optionset.has(optionspec6);

            if (flag)
            {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }

            PackRepository packrepository = new PackRepository(PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(levelstoragesource$levelstorageaccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD));
            WorldStem worldstem;

            try
            {
                WorldStem.InitConfig worldstem$initconfig = new WorldStem.InitConfig(packrepository, Commands.CommandSelection.DEDICATED, dedicatedserversettings.getProperties().functionPermissionLevel, flag);
                worldstem = WorldStem.load(worldstem$initconfig, () ->
                {
                    DataPackConfig datapackconfig = levelstoragesource$levelstorageaccess.getDataPacks();
                    return datapackconfig == null ? DataPackConfig.DEFAULT : datapackconfig;
                }, (p_206543_, p_206544_) ->
                {
                    RegistryAccess.Writable registryaccess$writable = RegistryAccess.builtinCopy();
                    DynamicOps<Tag> dynamicops = RegistryOps.createAndLoad(NbtOps.INSTANCE, registryaccess$writable, p_206543_);
                    WorldData worlddata1 = levelstoragesource$levelstorageaccess.getDataTag(dynamicops, p_206544_, registryaccess$writable.allElementsLifecycle());

                    if (worlddata1 != null)
                    {
                        return Pair.of(worlddata1, registryaccess$writable.freeze());
                    }
                    else {
                        LevelSettings levelsettings;
                        WorldGenSettings worldgensettings;

                        DedicatedServerProperties dedicatedserverproperties = dedicatedserversettings.getProperties();
                        levelsettings = new LevelSettings(dedicatedserverproperties.levelName, dedicatedserverproperties.gamemode, dedicatedserverproperties.hardcore, dedicatedserverproperties.difficulty, false, new GameRules(), p_206544_);
                        worldgensettings = optionset.has(optionspec3) ? dedicatedserverproperties.getWorldGenSettings(registryaccess$writable).withBonusChest() : dedicatedserverproperties.getWorldGenSettings(registryaccess$writable);

                        PrimaryLevelData primaryleveldata = new PrimaryLevelData(levelsettings, worldgensettings, Lifecycle.stable());
                        return Pair.of(primaryleveldata, registryaccess$writable.freeze());
                    }
                }, Util.backgroundExecutor(), Runnable::run).get();
            }
            catch (Exception exception)
            {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)exception);
                packrepository.close();
                return;
            }

            worldstem.updateGlobals();
            RegistryAccess.Frozen registryaccess$frozen = worldstem.registryAccess();
            dedicatedserversettings.getProperties().getWorldGenSettings(registryaccess$frozen);
            WorldData worlddata = worldstem.worldData();

            if (optionset.has(optionspec4))
            {
                forceUpgrade(levelstoragesource$levelstorageaccess, DataFixers.getDataFixer(), optionset.has(optionspec5), () ->
                {
                    return true;
                }, worlddata.worldGenSettings());
            }

            levelstoragesource$levelstorageaccess.saveDataTag(registryaccess$frozen, worlddata);
            final DedicatedServer dedicatedserver = MinecraftServer.spin((p_206536_) ->
            {
                DedicatedServer dedicatedserver1 = new DedicatedServer(p_206536_, levelstoragesource$levelstorageaccess, packrepository, worldstem, dedicatedserversettings, DataFixers.getDataFixer(), minecraftsessionservice, gameprofilerepository, gameprofilecache, LoggerChunkProgressListener::new);
                dedicatedserver1.setSingleplayerName(optionset.valueOf(optionspec8));
                dedicatedserver1.setPort(optionset.valueOf(optionspec11));
                dedicatedserver1.setId(optionset.valueOf(optionspec12));
                boolean flag1 = !optionset.has(optionspec) && !optionset.valuesOf(optionspec14).contains("nogui");

                if (flag1 && !GraphicsEnvironment.isHeadless())
                {
                    dedicatedserver1.showGui();
                }

                return dedicatedserver1;
            });
            Thread thread = new Thread("Server Shutdown Thread")
            {
                public void run()
                {
                    dedicatedserver.halt(true);
                }
            };
            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
        }
        catch (Exception exception1)
        {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", (Throwable)exception1);
        }
    }

    private static void forceUpgrade(LevelStorageSource.LevelStorageAccess p_195489_, DataFixer p_195490_, boolean p_195491_, BooleanSupplier p_195492_, WorldGenSettings p_195493_)
    {
        LOGGER.info("Forcing world upgrade!");
        WorldUpgrader worldupgrader = new WorldUpgrader(p_195489_, p_195490_, p_195493_, p_195491_);
        Component component = null;

        while (!worldupgrader.isFinished())
        {
            Component component1 = worldupgrader.getStatus();

            if (component != component1)
            {
                component = component1;
                LOGGER.info(worldupgrader.getStatus().getString());
            }

            int i = worldupgrader.getTotalChunks();

            if (i > 0)
            {
                int j = worldupgrader.getConverted() + worldupgrader.getSkipped();
                LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float)j / (float)i * 100.0F), j, i);
            }

            if (!p_195492_.getAsBoolean())
            {
                worldupgrader.cancel();
            }
            else
            {
                try
                {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedexception)
                {
                }
            }
        }
    }
}
