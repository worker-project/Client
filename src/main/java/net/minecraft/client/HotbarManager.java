package net.minecraft.client;

import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import java.io.File;
import net.minecraft.SharedConstants;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import org.slf4j.Logger;

public class HotbarManager
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int NUM_HOTBAR_GROUPS = 9;
    private final File optionsFile;
    private final DataFixer fixerUpper;
    private final Hotbar[] hotbars = new Hotbar[9];
    private boolean loaded;

    public HotbarManager(File pGameDirectory, DataFixer pFixerUpper)
    {
        this.optionsFile = new File(pGameDirectory, "hotbar.nbt");
        this.fixerUpper = pFixerUpper;

        for (int i = 0; i < 9; ++i)
        {
            this.hotbars[i] = new Hotbar();
        }
    }

    private void load()
    {
        try
        {
            CompoundTag compoundtag = NbtIo.read(this.optionsFile);

            if (compoundtag == null)
            {
                return;
            }

            if (!compoundtag.contains("DataVersion", 99))
            {
                compoundtag.putInt("DataVersion", 1343);
            }

            compoundtag = NbtUtils.update(this.fixerUpper, DataFixTypes.HOTBAR, compoundtag, compoundtag.getInt("DataVersion"));

            for (int i = 0; i < 9; ++i)
            {
                this.hotbars[i].fromTag(compoundtag.getList(String.valueOf(i), 10));
            }
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to load creative mode options", (Throwable)exception);
        }
    }

    public void save()
    {
        try
        {
            CompoundTag compoundtag = new CompoundTag();
            compoundtag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());

            for (int i = 0; i < 9; ++i)
            {
                compoundtag.put(String.valueOf(i), this.get(i).createTag());
            }

            NbtIo.write(compoundtag, this.optionsFile);
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to save creative mode options", (Throwable)exception);
        }
    }

    public Hotbar get(int pIndex)
    {
        if (!this.loaded)
        {
            this.load();
            this.loaded = true;
        }

        return this.hotbars[pIndex];
    }
}
