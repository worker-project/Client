package net.minecraft.world.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.slf4j.Logger;

public final class ItemStack
{
    public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create((p_41697_) ->
    {
        return p_41697_.group(Registry.ITEM.byNameCodec().fieldOf("id").forGetter((p_150946_) -> {
            return p_150946_.item;
        }), Codec.INT.fieldOf("Count").forGetter((p_150941_) -> {
            return p_150941_.count;
        }), CompoundTag.CODEC.optionalFieldOf("tag").forGetter((p_150939_) -> {
            return Optional.ofNullable(p_150939_.tag);
        })).apply(p_41697_, ItemStack::new);
    });
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ItemStack EMPTY = new ItemStack((Item)null);
    public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), (p_41704_) ->
    {
        p_41704_.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    });
    public static final String TAG_ENCH = "Enchantments";
    public static final String TAG_DISPLAY = "display";
    public static final String TAG_DISPLAY_NAME = "Name";
    public static final String TAG_LORE = "Lore";
    public static final String TAG_DAMAGE = "Damage";
    public static final String TAG_COLOR = "color";
    private static final String TAG_UNBREAKABLE = "Unbreakable";
    private static final String TAG_REPAIR_COST = "RepairCost";
    private static final String TAG_CAN_DESTROY_BLOCK_LIST = "CanDestroy";
    private static final String TAG_CAN_PLACE_ON_BLOCK_LIST = "CanPlaceOn";
    private static final String TAG_HIDE_FLAGS = "HideFlags";
    private static final int DONT_HIDE_TOOLTIP = 0;
    private static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);
    private int count;
    private int popTime;

    @Deprecated
    private final Item item;
    @Nullable
    private CompoundTag tag;
    private boolean emptyCacheFlag;
    @Nullable
    private Entity entityRepresentation;
    @Nullable
    private AdventureModeCheck adventureBreakCheck;
    @Nullable
    private AdventureModeCheck adventurePlaceCheck;

    public Optional<TooltipComponent> getTooltipImage()
    {
        return this.getItem().getTooltipImage(this);
    }

    public ItemStack(ItemLike pCompoundTag)
    {
        this(pCompoundTag, 1);
    }

    public ItemStack(Holder<Item> pCompoundTag)
    {
        this(pCompoundTag.value(), 1);
    }

    private ItemStack(ItemLike p_41604_, int p_41605_, Optional<CompoundTag> p_41606_)
    {
        this(p_41604_, p_41605_);
        p_41606_.ifPresent(this::setTag);
    }

    public ItemStack(ItemLike pItem, int pCount)
    {
        this.item = pItem == null ? null : pItem.asItem();
        this.count = pCount;

        if (this.item != null && this.item.canBeDepleted())
        {
            this.setDamageValue(this.getDamageValue());
        }

        this.updateEmptyCacheFlag();
    }

    private void updateEmptyCacheFlag()
    {
        this.emptyCacheFlag = false;
        this.emptyCacheFlag = this.isEmpty();
    }

    private ItemStack(CompoundTag pCompoundTag)
    {
        this.item = Registry.ITEM.get(new ResourceLocation(pCompoundTag.getString("id")));
        this.count = pCompoundTag.getByte("Count");

        if (pCompoundTag.contains("tag", 10))
        {
            this.tag = pCompoundTag.getCompound("tag");
            this.getItem().verifyTagAfterLoad(this.tag);
        }

        if (this.getItem().canBeDepleted())
        {
            this.setDamageValue(this.getDamageValue());
        }

        this.updateEmptyCacheFlag();
    }

    public static ItemStack of(CompoundTag pCompoundTag)
    {
        try
        {
            return new ItemStack(pCompoundTag);
        }
        catch (RuntimeException runtimeexception)
        {
            LOGGER.debug("Tried to load invalid item: {}", pCompoundTag, runtimeexception);
            return EMPTY;
        }
    }

    public boolean isEmpty()
    {
        if (this == EMPTY)
        {
            return true;
        }
        else if (this.getItem() != null && !this.is(Items.AIR))
        {
            return this.count <= 0;
        }
        else
        {
            return true;
        }
    }

    public ItemStack split(int pAmount)
    {
        int i = Math.min(pAmount, this.count);
        ItemStack itemstack = this.copy();
        itemstack.setCount(i);
        this.shrink(i);
        return itemstack;
    }

    public Item getItem()
    {
        return this.emptyCacheFlag ? Items.AIR : this.item;
    }

    public boolean is(TagKey<Item> pTag)
    {
        return this.getItem().builtInRegistryHolder().is(pTag);
    }

    public boolean is(Item pTag)
    {
        return this.getItem() == pTag;
    }

    public Stream<TagKey<Item>> getTags()
    {
        return this.getItem().builtInRegistryHolder().tags();
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        Player player = pContext.getPlayer();
        BlockPos blockpos = pContext.getClickedPos();
        BlockInWorld blockinworld = new BlockInWorld(pContext.getLevel(), blockpos, false);

        if (player != null && !player.getAbilities().mayBuild && !this.hasAdventureModePlaceTagForBlock(pContext.getLevel().registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY), blockinworld))
        {
            return InteractionResult.PASS;
        }
        else
        {
            Item item = this.getItem();
            InteractionResult interactionresult = item.useOn(pContext);

            if (player != null && interactionresult.shouldAwardStats())
            {
                player.awardStat(Stats.ITEM_USED.get(item));
            }

            return interactionresult;
        }
    }

    public float getDestroySpeed(BlockState pState)
    {
        return this.getItem().getDestroySpeed(this, pState);
    }

    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand)
    {
        return this.getItem().use(pLevel, pPlayer, pUsedHand);
    }

    public ItemStack finishUsingItem(Level pLevel, LivingEntity pLivingEntity)
    {
        return this.getItem().finishUsingItem(this, pLevel, pLivingEntity);
    }

    public CompoundTag save(CompoundTag pCompoundTag)
    {
        ResourceLocation resourcelocation = Registry.ITEM.getKey(this.getItem());
        pCompoundTag.putString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
        pCompoundTag.putByte("Count", (byte)this.count);

        if (this.tag != null)
        {
            pCompoundTag.put("tag", this.tag.copy());
        }

        return pCompoundTag;
    }

    public int getMaxStackSize()
    {
        return this.getItem().getMaxStackSize();
    }

    public boolean isStackable()
    {
        return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
    }

    public boolean isDamageableItem()
    {
        if (!this.emptyCacheFlag && this.getItem().getMaxDamage() > 0)
        {
            CompoundTag compoundtag = this.getTag();
            return compoundtag == null || !compoundtag.getBoolean("Unbreakable");
        }
        else
        {
            return false;
        }
    }

    public boolean isDamaged()
    {
        return this.isDamageableItem() && this.getDamageValue() > 0;
    }

    public int getDamageValue()
    {
        return this.tag == null ? 0 : this.tag.getInt("Damage");
    }

    public void setDamageValue(int pDamage)
    {
        this.getOrCreateTag().putInt("Damage", Math.max(0, pDamage));
    }

    public int getMaxDamage()
    {
        return this.getItem().getMaxDamage();
    }

    public boolean hurt(int pAmount, Random pRandom, @Nullable ServerPlayer pUser)
    {
        if (!this.isDamageableItem())
        {
            return false;
        }
        else
        {
            if (pAmount > 0)
            {
                int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, this);
                int j = 0;

                for (int k = 0; i > 0 && k < pAmount; ++k)
                {
                    if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(this, i, pRandom))
                    {
                        ++j;
                    }
                }

                pAmount -= j;

                if (pAmount <= 0)
                {
                    return false;
                }
            }

            if (pUser != null && pAmount != 0)
            {
                CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(pUser, this, this.getDamageValue() + pAmount);
            }

            int l = this.getDamageValue() + pAmount;
            this.setDamageValue(l);
            return l >= this.getMaxDamage();
        }
    }

    public <T extends LivingEntity> void hurtAndBreak(int pAmount, T pEntity, Consumer<T> pOnBroken)
    {
        if (!pEntity.level.isClientSide && (!(pEntity instanceof Player) || !((Player)pEntity).getAbilities().instabuild))
        {
            if (this.isDamageableItem())
            {
                if (this.hurt(pAmount, pEntity.getRandom(), pEntity instanceof ServerPlayer ? (ServerPlayer)pEntity : null))
                {
                    pOnBroken.accept(pEntity);
                    Item item = this.getItem();
                    this.shrink(1);

                    if (pEntity instanceof Player)
                    {
                        ((Player)pEntity).awardStat(Stats.ITEM_BROKEN.get(item));
                    }

                    this.setDamageValue(0);
                }
            }
        }
    }

    public boolean isBarVisible()
    {
        return this.item.isBarVisible(this);
    }

    public int getBarWidth()
    {
        return this.item.getBarWidth(this);
    }

    public int getBarColor()
    {
        return this.item.getBarColor(this);
    }

    public boolean overrideStackedOnOther(Slot pSlot, ClickAction pAction, Player pPlayer)
    {
        return this.getItem().overrideStackedOnOther(this, pSlot, pAction, pPlayer);
    }

    public boolean overrideOtherStackedOnMe(ItemStack pStack, Slot pSlot, ClickAction pAction, Player pPlayer, SlotAccess pAccess)
    {
        return this.getItem().overrideOtherStackedOnMe(this, pStack, pSlot, pAction, pPlayer, pAccess);
    }

    public void hurtEnemy(LivingEntity pEntity, Player pPlayer)
    {
        Item item = this.getItem();

        if (item.hurtEnemy(this, pEntity, pPlayer))
        {
            pPlayer.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public void mineBlock(Level pLevel, BlockState pState, BlockPos pPos, Player pPlayer)
    {
        Item item = this.getItem();

        if (item.mineBlock(this, pLevel, pState, pPos, pPlayer))
        {
            pPlayer.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    public boolean isCorrectToolForDrops(BlockState pState)
    {
        return this.getItem().isCorrectToolForDrops(pState);
    }

    public InteractionResult interactLivingEntity(Player pPlayer, LivingEntity pEntity, InteractionHand pUsedHand)
    {
        return this.getItem().interactLivingEntity(this, pPlayer, pEntity, pUsedHand);
    }

    public ItemStack copy()
    {
        if (this.isEmpty())
        {
            return EMPTY;
        }
        else
        {
            ItemStack itemstack = new ItemStack(this.getItem(), this.count);
            itemstack.setPopTime(this.getPopTime());

            if (this.tag != null)
            {
                itemstack.tag = this.tag.copy();
            }

            return itemstack;
        }
    }

    public static boolean tagMatches(ItemStack pStack, ItemStack pOther)
    {
        if (pStack.isEmpty() && pOther.isEmpty())
        {
            return true;
        }
        else if (!pStack.isEmpty() && !pOther.isEmpty())
        {
            if (pStack.tag == null && pOther.tag != null)
            {
                return false;
            }
            else
            {
                return pStack.tag == null || pStack.tag.equals(pOther.tag);
            }
        }
        else
        {
            return false;
        }
    }

    public static boolean matches(ItemStack pStack, ItemStack pOther)
    {
        if (pStack.isEmpty() && pOther.isEmpty())
        {
            return true;
        }
        else
        {
            return !pStack.isEmpty() && !pOther.isEmpty() ? pStack.matches(pOther) : false;
        }
    }

    private boolean matches(ItemStack pOther)
    {
        if (this.count != pOther.count)
        {
            return false;
        }
        else if (!this.is(pOther.getItem()))
        {
            return false;
        }
        else if (this.tag == null && pOther.tag != null)
        {
            return false;
        }
        else
        {
            return this.tag == null || this.tag.equals(pOther.tag);
        }
    }

    public static boolean isSame(ItemStack pStack, ItemStack pOther)
    {
        if (pStack == pOther)
        {
            return true;
        }
        else
        {
            return !pStack.isEmpty() && !pOther.isEmpty() ? pStack.sameItem(pOther) : false;
        }
    }

    public static boolean isSameIgnoreDurability(ItemStack pStack, ItemStack pOther)
    {
        if (pStack == pOther)
        {
            return true;
        }
        else
        {
            return !pStack.isEmpty() && !pOther.isEmpty() ? pStack.sameItemStackIgnoreDurability(pOther) : false;
        }
    }

    public boolean sameItem(ItemStack pOther)
    {
        return !pOther.isEmpty() && this.is(pOther.getItem());
    }

    public boolean sameItemStackIgnoreDurability(ItemStack pStack)
    {
        if (!this.isDamageableItem())
        {
            return this.sameItem(pStack);
        }
        else
        {
            return !pStack.isEmpty() && this.is(pStack.getItem());
        }
    }

    public static boolean isSameItemSameTags(ItemStack pStack, ItemStack pOther)
    {
        return pStack.is(pOther.getItem()) && tagMatches(pStack, pOther);
    }

    public String getDescriptionId()
    {
        return this.getItem().getDescriptionId(this);
    }

    public String toString()
    {
        return this.count + " " + this.getItem();
    }

    public void inventoryTick(Level pLevel, Entity pEntity, int pInventorySlot, boolean pIsCurrentItem)
    {
        if (this.popTime > 0)
        {
            --this.popTime;
        }

        if (this.getItem() != null)
        {
            this.getItem().inventoryTick(this, pLevel, pEntity, pInventorySlot, pIsCurrentItem);
        }
    }

    public void onCraftedBy(Level pLevel, Player pPlayer, int pAmount)
    {
        pPlayer.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), pAmount);
        this.getItem().onCraftedBy(this, pLevel, pPlayer);
    }

    public int getUseDuration()
    {
        return this.getItem().getUseDuration(this);
    }

    public UseAnim getUseAnimation()
    {
        return this.getItem().getUseAnimation(this);
    }

    public void releaseUsing(Level pLevel, LivingEntity pLivingEntity, int pTimeLeft)
    {
        this.getItem().releaseUsing(this, pLevel, pLivingEntity, pTimeLeft);
    }

    public boolean useOnRelease()
    {
        return this.getItem().useOnRelease(this);
    }

    public boolean hasTag()
    {
        return !this.emptyCacheFlag && this.tag != null && !this.tag.isEmpty();
    }

    @Nullable
    public CompoundTag getTag()
    {
        return this.tag;
    }

    public CompoundTag getOrCreateTag()
    {
        if (this.tag == null)
        {
            this.setTag(new CompoundTag());
        }

        return this.tag;
    }

    public CompoundTag getOrCreateTagElement(String pKey)
    {
        if (this.tag != null && this.tag.contains(pKey, 10))
        {
            return this.tag.getCompound(pKey);
        }
        else
        {
            CompoundTag compoundtag = new CompoundTag();
            this.addTagElement(pKey, compoundtag);
            return compoundtag;
        }
    }

    @Nullable
    public CompoundTag getTagElement(String pKey)
    {
        return this.tag != null && this.tag.contains(pKey, 10) ? this.tag.getCompound(pKey) : null;
    }

    public void removeTagKey(String pKey)
    {
        if (this.tag != null && this.tag.contains(pKey))
        {
            this.tag.remove(pKey);

            if (this.tag.isEmpty())
            {
                this.tag = null;
            }
        }
    }

    public ListTag getEnchantmentTags()
    {
        return this.tag != null ? this.tag.getList("Enchantments", 10) : new ListTag();
    }

    public void setTag(@Nullable CompoundTag p_41752_)
    {
        this.tag = p_41752_;

        if (this.getItem().canBeDepleted())
        {
            this.setDamageValue(this.getDamageValue());
        }

        if (p_41752_ != null)
        {
            this.getItem().verifyTagAfterLoad(p_41752_);
        }
    }

    public Component getHoverName()
    {
        CompoundTag compoundtag = this.getTagElement("display");

        if (compoundtag != null && compoundtag.contains("Name", 8))
        {
            try
            {
                Component component = Component.Serializer.fromJson(compoundtag.getString("Name"));

                if (component != null)
                {
                    return component;
                }

                compoundtag.remove("Name");
            }
            catch (Exception exception)
            {
                compoundtag.remove("Name");
            }
        }

        return this.getItem().getName(this);
    }

    public ItemStack setHoverName(@Nullable Component pNameComponent)
    {
        CompoundTag compoundtag = this.getOrCreateTagElement("display");

        if (pNameComponent != null)
        {
            compoundtag.putString("Name", Component.Serializer.toJson(pNameComponent));
        }
        else
        {
            compoundtag.remove("Name");
        }

        return this;
    }

    public void resetHoverName()
    {
        CompoundTag compoundtag = this.getTagElement("display");

        if (compoundtag != null)
        {
            compoundtag.remove("Name");

            if (compoundtag.isEmpty())
            {
                this.removeTagKey("display");
            }
        }

        if (this.tag != null && this.tag.isEmpty())
        {
            this.tag = null;
        }
    }

    public boolean hasCustomHoverName()
    {
        CompoundTag compoundtag = this.getTagElement("display");
        return compoundtag != null && compoundtag.contains("Name", 8);
    }

    public List<Component> getTooltipLines(@Nullable Player pPlayer, TooltipFlag pIsAdvanced)
    {
        List<Component> list = Lists.newArrayList();
        MutableComponent mutablecomponent = (new TextComponent("")).append(this.getHoverName()).withStyle(this.getRarity().color);

        if (this.hasCustomHoverName())
        {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
        }

        list.add(mutablecomponent);

        if (!pIsAdvanced.isAdvanced() && !this.hasCustomHoverName() && this.is(Items.FILLED_MAP))
        {
            Integer integer = MapItem.getMapId(this);

            if (integer != null)
            {
                list.add((new TextComponent("#" + integer)).withStyle(ChatFormatting.GRAY));
            }
        }

        int j = this.getHideFlags();

        if (shouldShowInTooltip(j, ItemStack.TooltipPart.ADDITIONAL))
        {
            this.getItem().appendHoverText(this, pPlayer == null ? null : pPlayer.level, list, pIsAdvanced);
        }

        if (this.hasTag())
        {
            if (shouldShowInTooltip(j, ItemStack.TooltipPart.ENCHANTMENTS))
            {
                appendEnchantmentNames(list, this.getEnchantmentTags());
            }

            if (this.tag.contains("display", 10))
            {
                CompoundTag compoundtag = this.tag.getCompound("display");

                if (shouldShowInTooltip(j, ItemStack.TooltipPart.DYE) && compoundtag.contains("color", 99))
                {
                    if (pIsAdvanced.isAdvanced())
                    {
                        list.add((new TranslatableComponent("item.color", String.format("#%06X", compoundtag.getInt("color")))).withStyle(ChatFormatting.GRAY));
                    }
                    else
                    {
                        list.add((new TranslatableComponent("item.dyed")).a(new ChatFormatting[] {ChatFormatting.GRAY, ChatFormatting.ITALIC}));
                    }
                }

                if (compoundtag.getTagType("Lore") == 9)
                {
                    ListTag listtag = compoundtag.getList("Lore", 8);

                    for (int i = 0; i < listtag.size(); ++i)
                    {
                        String s = listtag.getString(i);

                        try
                        {
                            MutableComponent mutablecomponent1 = Component.Serializer.fromJson(s);

                            if (mutablecomponent1 != null)
                            {
                                list.add(ComponentUtils.mergeStyles(mutablecomponent1, LORE_STYLE));
                            }
                        }
                        catch (Exception exception)
                        {
                            compoundtag.remove("Lore");
                        }
                    }
                }
            }
        }

        if (shouldShowInTooltip(j, ItemStack.TooltipPart.MODIFIERS))
        {
            for (EquipmentSlot equipmentslot : EquipmentSlot.values())
            {
                Multimap<Attribute, AttributeModifier> multimap = this.getAttributeModifiers(equipmentslot);

                if (!multimap.isEmpty())
                {
                    list.add(TextComponent.EMPTY);
                    list.add((new TranslatableComponent("item.modifiers." + equipmentslot.getName())).withStyle(ChatFormatting.GRAY));

                    for (Entry<Attribute, AttributeModifier> entry : multimap.entries())
                    {
                        AttributeModifier attributemodifier = entry.getValue();
                        double d0 = attributemodifier.getAmount();
                        boolean flag = false;

                        if (pPlayer != null)
                        {
                            if (attributemodifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID)
                            {
                                d0 += pPlayer.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                                d0 += (double)EnchantmentHelper.getDamageBonus(this, MobType.UNDEFINED);
                                flag = true;
                            }
                            else if (attributemodifier.getId() == Item.BASE_ATTACK_SPEED_UUID)
                            {
                                d0 += pPlayer.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                                flag = true;
                            }
                        }

                        double d1;

                        if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL)
                        {
                            if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE))
                            {
                                d1 = d0 * 10.0D;
                            }
                            else
                            {
                                d1 = d0;
                            }
                        }
                        else
                        {
                            d1 = d0 * 100.0D;
                        }

                        if (flag)
                        {
                            list.add((new TextComponent(" ")).append(new TranslatableComponent("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
                        }
                        else if (d0 > 0.0D)
                        {
                            list.add((new TranslatableComponent("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.BLUE));
                        }
                        else if (d0 < 0.0D)
                        {
                            d1 *= -1.0D;
                            list.add((new TranslatableComponent("attribute.modifier.take." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.RED));
                        }
                    }
                }
            }
        }

        if (this.hasTag())
        {
            if (shouldShowInTooltip(j, ItemStack.TooltipPart.UNBREAKABLE) && this.tag.getBoolean("Unbreakable"))
            {
                list.add((new TranslatableComponent("item.unbreakable")).withStyle(ChatFormatting.BLUE));
            }

            if (shouldShowInTooltip(j, ItemStack.TooltipPart.CAN_DESTROY) && this.tag.contains("CanDestroy", 9))
            {
                ListTag listtag1 = this.tag.getList("CanDestroy", 8);

                if (!listtag1.isEmpty())
                {
                    list.add(TextComponent.EMPTY);
                    list.add((new TranslatableComponent("item.canBreak")).withStyle(ChatFormatting.GRAY));

                    for (int k = 0; k < listtag1.size(); ++k)
                    {
                        list.addAll(expandBlockState(listtag1.getString(k)));
                    }
                }
            }

            if (shouldShowInTooltip(j, ItemStack.TooltipPart.CAN_PLACE) && this.tag.contains("CanPlaceOn", 9))
            {
                ListTag listtag2 = this.tag.getList("CanPlaceOn", 8);

                if (!listtag2.isEmpty())
                {
                    list.add(TextComponent.EMPTY);
                    list.add((new TranslatableComponent("item.canPlace")).withStyle(ChatFormatting.GRAY));

                    for (int l = 0; l < listtag2.size(); ++l)
                    {
                        list.addAll(expandBlockState(listtag2.getString(l)));
                    }
                }
            }
        }

        if (pIsAdvanced.isAdvanced())
        {
            if (this.isDamaged())
            {
                list.add(new TranslatableComponent("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
            }

            list.add((new TextComponent(Registry.ITEM.getKey(this.getItem()).toString())).withStyle(ChatFormatting.DARK_GRAY));

            if (this.hasTag())
            {
                list.add((new TranslatableComponent("item.nbt_tags", this.tag.getAllKeys().size())).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        return list;
    }

    private static boolean shouldShowInTooltip(int pHideFlags, ItemStack.TooltipPart pPart)
    {
        return (pHideFlags & pPart.getMask()) == 0;
    }

    private int getHideFlags()
    {
        return this.hasTag() && this.tag.contains("HideFlags", 99) ? this.tag.getInt("HideFlags") : 0;
    }

    public void hideTooltipPart(ItemStack.TooltipPart pPart)
    {
        CompoundTag compoundtag = this.getOrCreateTag();
        compoundtag.putInt("HideFlags", compoundtag.getInt("HideFlags") | pPart.getMask());
    }

    public static void appendEnchantmentNames(List<Component> pTooltipComponents, ListTag pStoredEnchantments)
    {
        for (int i = 0; i < pStoredEnchantments.size(); ++i)
        {
            CompoundTag compoundtag = pStoredEnchantments.getCompound(i);
            Registry.ENCHANTMENT.getOptional(EnchantmentHelper.getEnchantmentId(compoundtag)).ifPresent((p_41708_) ->
            {
                pTooltipComponents.add(p_41708_.getFullname(EnchantmentHelper.getEnchantmentLevel(compoundtag)));
            });
        }
    }

    private static Collection<Component> expandBlockState(String pStateString)
    {
        try
        {
            BlockStateParser blockstateparser = (new BlockStateParser(new StringReader(pStateString), true)).parse(true);
            BlockState blockstate = blockstateparser.getState();
            TagKey<Block> tagkey = blockstateparser.getTag();
            boolean flag = blockstate != null;
            boolean flag1 = tagkey != null;

            if (flag)
            {
                return Lists.newArrayList(blockstate.getBlock().getName().withStyle(ChatFormatting.DARK_GRAY));
            }

            if (flag1)
            {
                List<Component> list = Streams.stream(Registry.BLOCK.getTagOrEmpty(tagkey)).map((p_204120_) ->
                {
                    return p_204120_.value().getName();
                }).map((p_204125_) ->
                {
                    return p_204125_.withStyle(ChatFormatting.DARK_GRAY);
                }).collect(Collectors.toList());

                if (!list.isEmpty())
                {
                    return list;
                }
            }
        }
        catch (CommandSyntaxException commandsyntaxexception)
        {
        }

        return Lists.newArrayList((new TextComponent("missingno")).withStyle(ChatFormatting.DARK_GRAY));
    }

    public boolean hasFoil()
    {
        return this.getItem().isFoil(this);
    }

    public Rarity getRarity()
    {
        return this.getItem().getRarity(this);
    }

    public boolean isEnchantable()
    {
        if (!this.getItem().isEnchantable(this))
        {
            return false;
        }
        else
        {
            return !this.isEnchanted();
        }
    }

    public void enchant(Enchantment pEnchantment, int pLevel)
    {
        this.getOrCreateTag();

        if (!this.tag.contains("Enchantments", 9))
        {
            this.tag.put("Enchantments", new ListTag());
        }

        ListTag listtag = this.tag.getList("Enchantments", 10);
        listtag.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(pEnchantment), (byte)pLevel));
    }

    public boolean isEnchanted()
    {
        if (this.tag != null && this.tag.contains("Enchantments", 9))
        {
            return !this.tag.getList("Enchantments", 10).isEmpty();
        }
        else
        {
            return false;
        }
    }

    public void addTagElement(String pKey, Tag pTag)
    {
        this.getOrCreateTag().put(pKey, pTag);
    }

    public boolean isFramed()
    {
        return this.entityRepresentation instanceof ItemFrame;
    }

    public void setEntityRepresentation(@Nullable Entity pEntity)
    {
        this.entityRepresentation = pEntity;
    }

    @Nullable
    public ItemFrame getFrame()
    {
        return this.entityRepresentation instanceof ItemFrame ? (ItemFrame)this.getEntityRepresentation() : null;
    }

    @Nullable
    public Entity getEntityRepresentation()
    {
        return !this.emptyCacheFlag ? this.entityRepresentation : null;
    }

    public int getBaseRepairCost()
    {
        return this.hasTag() && this.tag.contains("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
    }

    public void setRepairCost(int pCost)
    {
        this.getOrCreateTag().putInt("RepairCost", pCost);
    }

    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot pSlot)
    {
        Multimap<Attribute, AttributeModifier> multimap;

        if (this.hasTag() && this.tag.contains("AttributeModifiers", 9))
        {
            multimap = HashMultimap.create();
            ListTag listtag = this.tag.getList("AttributeModifiers", 10);

            for (int i = 0; i < listtag.size(); ++i)
            {
                CompoundTag compoundtag = listtag.getCompound(i);

                if (!compoundtag.contains("Slot", 8) || compoundtag.getString("Slot").equals(pSlot.getName()))
                {
                    Optional<Attribute> optional = Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(compoundtag.getString("AttributeName")));

                    if (optional.isPresent())
                    {
                        AttributeModifier attributemodifier = AttributeModifier.load(compoundtag);

                        if (attributemodifier != null && attributemodifier.getId().getLeastSignificantBits() != 0L && attributemodifier.getId().getMostSignificantBits() != 0L)
                        {
                            multimap.put(optional.get(), attributemodifier);
                        }
                    }
                }
            }
        }
        else
        {
            multimap = this.getItem().getDefaultAttributeModifiers(pSlot);
        }

        return multimap;
    }

    public void addAttributeModifier(Attribute pAttribute, AttributeModifier pModifier, @Nullable EquipmentSlot pSlot)
    {
        this.getOrCreateTag();

        if (!this.tag.contains("AttributeModifiers", 9))
        {
            this.tag.put("AttributeModifiers", new ListTag());
        }

        ListTag listtag = this.tag.getList("AttributeModifiers", 10);
        CompoundTag compoundtag = pModifier.save();
        compoundtag.putString("AttributeName", Registry.ATTRIBUTE.getKey(pAttribute).toString());

        if (pSlot != null)
        {
            compoundtag.putString("Slot", pSlot.getName());
        }

        listtag.add(compoundtag);
    }

    public Component getDisplayName()
    {
        MutableComponent mutablecomponent = (new TextComponent("")).append(this.getHoverName());

        if (this.hasCustomHoverName())
        {
            mutablecomponent.withStyle(ChatFormatting.ITALIC);
        }

        MutableComponent mutablecomponent1 = ComponentUtils.wrapInSquareBrackets(mutablecomponent);

        if (!this.emptyCacheFlag)
        {
            mutablecomponent1.withStyle(this.getRarity().color).withStyle((p_204127_) ->
            {
                return p_204127_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this)));
            });
        }

        return mutablecomponent1;
    }

    public boolean hasAdventureModePlaceTagForBlock(Registry<Block> pTagContainer, BlockInWorld pBlock)
    {
        if (this.adventurePlaceCheck == null)
        {
            this.adventurePlaceCheck = new AdventureModeCheck("CanPlaceOn");
        }

        return this.adventurePlaceCheck.test(this, pTagContainer, pBlock);
    }

    public boolean hasAdventureModeBreakTagForBlock(Registry<Block> pTagContainer, BlockInWorld pBlock)
    {
        if (this.adventureBreakCheck == null)
        {
            this.adventureBreakCheck = new AdventureModeCheck("CanDestroy");
        }

        return this.adventureBreakCheck.test(this, pTagContainer, pBlock);
    }

    public int getPopTime()
    {
        return this.popTime;
    }

    public void setPopTime(int pPopTime)
    {
        this.popTime = pPopTime;
    }

    public int getCount()
    {
        return this.emptyCacheFlag ? 0 : this.count;
    }

    public void setCount(int pCount)
    {
        this.count = pCount;
        this.updateEmptyCacheFlag();
    }

    public void grow(int pIncrement)
    {
        this.setCount(this.count + pIncrement);
    }

    public void shrink(int pDecrement)
    {
        this.grow(-pDecrement);
    }

    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, int pCount)
    {
        this.getItem().onUseTick(pLevel, pLivingEntity, this, pCount);
    }

    public void onDestroyed(ItemEntity pItemEntity)
    {
        this.getItem().onDestroyed(pItemEntity);
    }

    public boolean isEdible()
    {
        return this.getItem().isEdible();
    }

    public SoundEvent getDrinkingSound()
    {
        return this.getItem().getDrinkingSound();
    }

    public SoundEvent getEatingSound()
    {
        return this.getItem().getEatingSound();
    }

    @Nullable
    public SoundEvent getEquipSound()
    {
        return this.getItem().getEquipSound();
    }

    public static enum TooltipPart
    {
        ENCHANTMENTS,
        MODIFIERS,
        UNBREAKABLE,
        CAN_DESTROY,
        CAN_PLACE,
        ADDITIONAL,
        DYE;

        private final int mask = 1 << this.ordinal();

        public int getMask()
        {
            return this.mask;
        }
    }
}
