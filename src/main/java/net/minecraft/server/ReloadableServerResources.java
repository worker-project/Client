package net.minecraft.server;

import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Unit;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.slf4j.Logger;

public class ReloadableServerResources
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final Commands commands;
    private final RecipeManager recipes = new RecipeManager();
    private final TagManager tagManager;
    private final PredicateManager predicateManager = new PredicateManager();
    private final LootTables lootTables = new LootTables(this.predicateManager);
    private final ItemModifierManager itemModifierManager = new ItemModifierManager(this.predicateManager, this.lootTables);
    private final ServerAdvancementManager advancements = new ServerAdvancementManager(this.predicateManager);
    private final ServerFunctionLibrary functionLibrary;

    public ReloadableServerResources(RegistryAccess.Frozen p_206857_, Commands.CommandSelection p_206858_, int p_206859_)
    {
        this.tagManager = new TagManager(p_206857_);
        this.commands = new Commands(p_206858_);
        this.functionLibrary = new ServerFunctionLibrary(p_206859_, this.commands.getDispatcher());
    }

    public ServerFunctionLibrary getFunctionLibrary()
    {
        return this.functionLibrary;
    }

    public PredicateManager getPredicateManager()
    {
        return this.predicateManager;
    }

    public LootTables getLootTables()
    {
        return this.lootTables;
    }

    public ItemModifierManager getItemModifierManager()
    {
        return this.itemModifierManager;
    }

    public RecipeManager getRecipeManager()
    {
        return this.recipes;
    }

    public Commands getCommands()
    {
        return this.commands;
    }

    public ServerAdvancementManager getAdvancements()
    {
        return this.advancements;
    }

    public List<PreparableReloadListener> listeners()
    {
        return List.of(this.tagManager, this.predicateManager, this.recipes, this.lootTables, this.itemModifierManager, this.functionLibrary, this.advancements);
    }

    public static CompletableFuture<ReloadableServerResources> loadResources(ResourceManager p_206862_, RegistryAccess.Frozen p_206863_, Commands.CommandSelection p_206864_, int p_206865_, Executor p_206866_, Executor p_206867_)
    {
        ReloadableServerResources reloadableserverresources = new ReloadableServerResources(p_206863_, p_206864_, p_206865_);
        return SimpleReloadInstance.create(p_206862_, reloadableserverresources.listeners(), p_206866_, p_206867_, DATA_RELOAD_INITIAL_TASK, LOGGER.isDebugEnabled()).done().thenApply((p_206880_) ->
        {
            return reloadableserverresources;
        });
    }

    public void updateRegistryTags(RegistryAccess p_206869_)
    {
        this.tagManager.getResult().forEach((p_206884_) ->
        {
            updateRegistryTags(p_206869_, p_206884_);
        });
        Blocks.rebuildCache();
    }

    private static <T> void updateRegistryTags(RegistryAccess p_206871_, TagManager.LoadResult<T> p_206872_)
    {
        ResourceKey <? extends Registry<T >> resourcekey = p_206872_.key();
        Map<TagKey<T>, List<Holder<T>>> map = p_206872_.tags().entrySet().stream().collect(Collectors.toUnmodifiableMap((p_206877_) ->
        {
            return TagKey.create(resourcekey, p_206877_.getKey());
        }, (p_206874_) ->
        {
            return p_206874_.getValue().getValues();
        }));
        p_206871_.registryOrThrow(resourcekey).bindTags(map);
    }
}
