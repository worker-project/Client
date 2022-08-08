package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SolidBucketItem extends BlockItem implements DispensibleContainerItem
{
    private final SoundEvent placeSound;

    public SolidBucketItem(Block pBlock, SoundEvent pPlaceSound, Item.Properties pProperties)
    {
        super(pBlock, pProperties);
        this.placeSound = pPlaceSound;
    }

    public InteractionResult useOn(UseOnContext pContext)
    {
        InteractionResult interactionresult = super.useOn(pContext);
        Player player = pContext.getPlayer();

        if (interactionresult.consumesAction() && player != null && !player.isCreative())
        {
            InteractionHand interactionhand = pContext.getHand();
            player.setItemInHand(interactionhand, Items.BUCKET.getDefaultInstance());
        }

        return interactionresult;
    }

    public String getDescriptionId()
    {
        return this.getOrCreateDescriptionId();
    }

    protected SoundEvent getPlaceSound(BlockState pState)
    {
        return this.placeSound;
    }

    public boolean emptyContents(@Nullable Player pPlayer, Level pLevel, BlockPos pPos, @Nullable BlockHitResult pResult)
    {
        if (pLevel.isInWorldBounds(pPos) && pLevel.isEmptyBlock(pPos))
        {
            if (!pLevel.isClientSide)
            {
                pLevel.setBlock(pPos, this.getBlock().defaultBlockState(), 3);
            }

            pLevel.playSound(pPlayer, pPos, this.placeSound, SoundSource.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        else
        {
            return false;
        }
    }
}
