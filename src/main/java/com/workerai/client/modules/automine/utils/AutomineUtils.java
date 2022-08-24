package com.workerai.client.modules.automine.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public class AutomineUtils {
	/*private static final Minecraft mc = Minecraft.getINSTANCE();
	public static boolean hasBlockInPointer(Block blockMining) {

		if (mc.mouseHandler. != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			BlockPos blockpos = mc.objectMouseOver.getBlockPos();

			return mc.theWorld.getBlockState(blockpos).getBlock() == blockMining;
		}
		return false;
	}

	public static int getCobblestoneCount() {
		int i = 0;
		for (int j = 0; j < mc.thePlayer.inventory.mainInventory.length; ++j) {
			ItemStack itemstack = mc.thePlayer.inventory.mainInventory[j];
			if (itemstack != null && itemstack.getItem() != null) {
				Block block = Block.getBlockFromItem(itemstack.getItem());

				if (block != null && block == Blocks.COBBLESTONE && !itemstack.isEnchanted()) {
					i += itemstack.getCount();
				}
			}
		}
		return i;
	}

	public static int getEmptySlotforCheck(ItemStack add) {
		int i = 0;
		for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
			if (stack == null )
				i++;
		}
		return i;
	}

	public static int getEmptySlotforPrint(Item add) {
		int i = 0;
		for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
			if (stack == null)
				i+=64;
			else if(stack.getItem().equals(add) && !stack.isItemEnchanted())
					i+= ( 64 - stack.stackSize );
		}
		return i;
	}

	public static boolean isEnchantedCobblestone(ItemStack item) {
		return item != null && item.getItem() != null && Block.getBlockFromItem(item.getItem()) == Blocks.cobblestone && item.isItemEnchanted();
	}

	public static boolean isCraftedItemEnchantedCobblestone() {
		ItemStack itemstack = (ItemStack) mc.thePlayer.openContainer.getInventory().get(23);

		return isEnchantedCobblestone(itemstack);
	}

	public static boolean isInCraftingMenu() {
		if (mc.thePlayer.openContainer.windowId == 0) {
			WorkerAI.getINSTANCE().getLogger().info("gui is not opened");
			return false;
		} else {
			List<Integer> list = new ArrayList<>();
			for (int i = 0; i < mc.thePlayer.openContainer.getInventory().size(); ++i) {
				ItemStack itemstack = (ItemStack) mc.thePlayer.openContainer.getInventory().get(i);
				if (itemstack == null) {
					WorkerAI.getINSTANCE().getLogger().info(i);
					list.add(i);
				}
			}

			return list.contains(10) && list.contains(11) && list.contains(12)
					&& list.contains(19) && list.contains(20) && list.contains(21)
					&& list.contains(28) && list.contains(29) && list.contains(30);
		}
	}

	public static boolean isEnderchestFull() {
		for (int i = 0; i < 5; i++) {
			WorkerAI.getINSTANCE().getLogger().info(String.format("Enderchest Size: %s", retrieveContainerSlotSize()));
		}
		boolean flag = true;

		for (int i = 0; i < mc.thePlayer.openContainer.getInventory().size(); ++i) {
			ItemStack itemstack = (ItemStack) mc.thePlayer.openContainer.getInventory().get(i);

			if ((itemstack == null || itemstack != null && itemstack.getItem() != null
					&& Block.getBlockFromItem(itemstack.getItem()) == Blocks.cobblestone && itemstack.isItemEnchanted()
					&& itemstack.stackSize < 64) && i < retrieveContainerSlotSize()) {
				flag = false;
			}
		}

		return flag;
	}

	public static boolean hasFullEnderChest() {
		boolean flag = true;
		for (int index = 0; index < mc.thePlayer.getInventoryEnderChest().getSizeInventory(); ++index) {
			ItemStack stack = mc.thePlayer.getInventoryEnderChest().getStackInSlot(index);
			if(stack == null){
				flag = false;
			}

		}
		if(flag)
			mc.thePlayer.closeScreen();
		return flag;
	}

	public static int retrieveContainerSlotSize() {
		Container con = mc.thePlayer.openContainer;
		if (con == null)
			return 0;
		else {
			int count = 0;
			for (Slot s : con.inventorySlots) {
				if (s.inventory != mc.thePlayer.inventory) {
					count++;
				}
			}
			return count;
		}
	}

	public static int getEmptyOrECobbleSlot()  {
		for (int i = retrieveContainerSlotSize(); i < mc.thePlayer.openContainer.getInventory().size(); ++i) {
			ItemStack itemstack = mc.thePlayer.openContainer.getInventory().get(i);

			if((isEnchantedCobblestone(itemstack) && itemstack.stackSize < 64)) {
				return i;
			}
		}

		for (int i = retrieveContainerSlotSize(); i < mc.thePlayer.openContainer.getInventory().size(); ++i) {
			ItemStack itemstack = mc.thePlayer.openContainer.getInventory().get(i);

			if((itemstack == null || itemstack.getItem() == null)) {
				return i;
			}
		}
		return -999;
	}

	public static void stockInEnderChest(Item item) {
		Thread t = new Thread(() -> {
			try{
				if(Minecraft.getMinecraft().currentScreen != null)
					mc.thePlayer.closeScreen();
				mc.thePlayer.sendChatMessage("/ec");
				Thread.sleep(1000L);


				List<Integer> slots = getSlotEnchantedCobbleInInventory();

				for (int i = 0; i < slots.size(); ++i) {
					int slot = (Integer) slots.get(i);
					mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, 0, 1, mc.thePlayer);
					Thread.sleep(100L);
				}
				mc.thePlayer.closeScreen();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, item.getUnlocalizedName() + ".stock");
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public List<Integer> getSlotEnchantedCobble() {
		List<Integer> slots = new ArrayList<>();

		for (int i = 0; i < mc.thePlayer.inventory.mainInventory.length; ++i) {
			ItemStack itemstack = mc.thePlayer.inventory.mainInventory[i];

			if (isEnchantedCobblestone(itemstack)) {
				slots.add(i);
			}
		}
		return slots;
	}

	private static List<Integer> getSlotEnchantedCobbleInInventory() {
		List<Integer> slots = new ArrayList<>();

		for (int i = retrieveContainerSlotSize(); i < mc.thePlayer.openContainer.getInventory().size(); ++i) {
			ItemStack itemstack = mc.thePlayer.openContainer.getInventory().get(i);

			if (isEnchantedCobblestone(itemstack)) {
				slots.add(i);
			}
		}
		return slots;
	}

	public static void stockInEnderChest(Block block) {
		stockInEnderChest(Item.getItemFromBlock(block));
	}*/
}
