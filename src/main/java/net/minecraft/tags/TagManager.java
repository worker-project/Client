package net.minecraft.tags;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class TagManager implements PreparableReloadListener
{
    private static final Map < ResourceKey <? extends Registry<? >> , String > CUSTOM_REGISTRY_DIRECTORIES = Map.of(Registry.BLOCK_REGISTRY, "tags/blocks", Registry.ENTITY_TYPE_REGISTRY, "tags/entity_types", Registry.FLUID_REGISTRY, "tags/fluids", Registry.GAME_EVENT_REGISTRY, "tags/game_events", Registry.ITEM_REGISTRY, "tags/items");
    private final RegistryAccess registryAccess;
    private List < TagManager.LoadResult<? >> results = List.of();

    public TagManager(RegistryAccess pRegistryAccess)
    {
        this.registryAccess = pRegistryAccess;
    }

    public List < TagManager.LoadResult<? >> getResult()
    {
        return this.results;
    }

    public static String getTagDir(ResourceKey <? extends Registry<? >> p_203919_)
    {
        String s = CUSTOM_REGISTRY_DIRECTORIES.get(p_203919_);
        return s != null ? s : "tags/" + p_203919_.location().getPath();
    }

    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier pStage, ResourceManager pResourceManager, ProfilerFiller pPreparationsProfiler, ProfilerFiller pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor)
    {
        List <? extends CompletableFuture <? extends TagManager.LoadResult<? >>> list = this.registryAccess.registries().map((p_203927_) ->
        {
            return this.createLoader(pResourceManager, pBackgroundExecutor, p_203927_);
        }).toList();
        return CompletableFuture.allOf(list.toArray((p_203906_) ->
        {
            return new CompletableFuture[p_203906_];
        })).thenCompose(pStage::wait).thenAcceptAsync((p_203917_) ->
        {
            this.results = list.stream().map(CompletableFuture::join).collect(Collectors.toUnmodifiableList());
        }, pGameExecutor);
    }

    private <T> CompletableFuture<TagManager.LoadResult<T>> createLoader(ResourceManager pResourceManager, Executor pBackgroundExecutor, RegistryAccess.RegistryEntry<T> pHelper)
    {
        ResourceKey <? extends Registry<T >> resourcekey = pHelper.key();
        Registry<T> registry = pHelper.value();
        TagLoader<Holder<T>> tagloader = new TagLoader<>((p_203914_) ->
        {
            return registry.getHolder(ResourceKey.create(resourcekey, p_203914_));
        }, getTagDir(resourcekey));
        return CompletableFuture.supplyAsync(() ->
        {
            return new TagManager.LoadResult<>(resourcekey, tagloader.loadAndBuild(pResourceManager));
        }, pBackgroundExecutor);
    }

    public static record LoadResult<T>(ResourceKey <? extends Registry<T >> key, Map<ResourceLocation, Tag<Holder<T>>> tags)
    {
    }
}
