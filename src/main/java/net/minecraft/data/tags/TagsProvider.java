package net.minecraft.data.tags;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagManager;
import org.slf4j.Logger;

public abstract class TagsProvider<T> implements DataProvider
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    protected final DataGenerator generator;
    protected final Registry<T> registry;
    private final Map<ResourceLocation, Tag.Builder> builders = Maps.newLinkedHashMap();

    protected TagsProvider(DataGenerator pGenerator, Registry<T> pRegistry)
    {
        this.generator = pGenerator;
        this.registry = pRegistry;
    }

    protected abstract void addTags();

    public void run(HashCache pCache)
    {
        this.builders.clear();
        this.addTags();
        this.builders.forEach((p_176835_, p_176836_) ->
        {
            List<Tag.BuilderEntry> list = p_176836_.getEntries().filter((p_176832_) -> {
                return !p_176832_.entry().verifyIfPresent(this.registry::containsKey, this.builders::containsKey);
            }).toList();

            if (!list.isEmpty())
            {
                throw new IllegalArgumentException(String.format("Couldn't define tag %s as it is missing following references: %s", p_176835_, list.stream().map(Objects::toString).collect(Collectors.joining(","))));
            }
            else {
                JsonObject jsonobject = p_176836_.serializeToJson();
                Path path = this.getPath(p_176835_);

                try {
                    String s = GSON.toJson((JsonElement)jsonobject);
                    String s1 = SHA1.hashUnencodedChars(s).toString();

                    if (!Objects.equals(pCache.getHash(path), s1) || !Files.exists(path))
                    {
                        Files.createDirectories(path.getParent());
                        BufferedWriter bufferedwriter = Files.newBufferedWriter(path);

                        try
                        {
                            bufferedwriter.write(s);
                        }
                        catch (Throwable throwable1)
                        {
                            if (bufferedwriter != null)
                            {
                                try
                                {
                                    bufferedwriter.close();
                                }
                                catch (Throwable throwable)
                                {
                                    throwable1.addSuppressed(throwable);
                                }
                            }

                            throw throwable1;
                        }

                        if (bufferedwriter != null)
                        {
                            bufferedwriter.close();
                        }
                    }

                    pCache.putNew(path, s1);
                }
                catch (IOException ioexception)
                {
                    LOGGER.error("Couldn't save tags to {}", path, ioexception);
                }
            }
        });
    }

    private Path getPath(ResourceLocation pId)
    {
        ResourceKey <? extends Registry<T >> resourcekey = this.registry.key();
        return this.generator.getOutputFolder().resolve("data/" + pId.getNamespace() + "/" + TagManager.getTagDir(resourcekey) + "/" + pId.getPath() + ".json");
    }

    protected TagsProvider.TagAppender<T> tag(TagKey<T> pTag)
    {
        Tag.Builder tag$builder = this.getOrCreateRawBuilder(pTag);
        return new TagsProvider.TagAppender<>(tag$builder, this.registry, "vanilla");
    }

    protected Tag.Builder getOrCreateRawBuilder(TagKey<T> pTag)
    {
        return this.builders.computeIfAbsent(pTag.location(), (p_176838_) ->
        {
            return new Tag.Builder();
        });
    }

    protected static class TagAppender<T>
    {
        private final Tag.Builder builder;
        private final Registry<T> registry;
        private final String source;

        TagAppender(Tag.Builder pBuilder, Registry<T> pRegistry, String pSource)
        {
            this.builder = pBuilder;
            this.registry = pRegistry;
            this.source = pSource;
        }

        public TagsProvider.TagAppender<T> add(T pItem)
        {
            this.builder.addElement(this.registry.getKey(pItem), this.source);
            return this;
        }

        @SafeVarargs
        public final TagsProvider.TagAppender<T> a(ResourceKey<T>... p_211102_)
        {
            for (ResourceKey<T> resourcekey : p_211102_)
            {
                this.builder.addElement(resourcekey.location(), this.source);
            }

            return this;
        }

        public TagsProvider.TagAppender<T> addOptional(ResourceLocation pLocation)
        {
            this.builder.addOptionalElement(pLocation, this.source);
            return this;
        }

        public TagsProvider.TagAppender<T> addTag(TagKey<T> pTag)
        {
            this.builder.addTag(pTag.location(), this.source);
            return this;
        }

        public TagsProvider.TagAppender<T> addOptionalTag(ResourceLocation pLocation)
        {
            this.builder.addOptionalTag(pLocation, this.source);
            return this;
        }

        @SafeVarargs
        public final TagsProvider.TagAppender<T> a(T... p_126585_)
        {
            Stream.<T>of(p_126585_).map(this.registry::getKey).forEach((p_126587_) ->
            {
                this.builder.addElement(p_126587_, this.source);
            });
            return this;
        }
    }
}
