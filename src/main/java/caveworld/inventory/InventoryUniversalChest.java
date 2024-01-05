package caveworld.inventory;

import caveworld.block.CaveBlocks;
import caveworld.entity.TileEntityUniversalChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class InventoryUniversalChest extends InventoryBasic
{
	private TileEntityUniversalChest associatedChest;

	public InventoryUniversalChest()
	{
		super("UniversalChest", false, 9 * 3);
	}

	public void loadInventoryFromNBT(NBTTagList list)
	{
		for (int slot = 0; slot < getSizeInventory(); ++slot)
		{
			setInventorySlotContents(slot, null);
		}

		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound nbttag = list.getCompoundTagAt(i);
			int slot = nbttag.getByte("Slot") & 255;

			if (slot >= 0 && slot < getSizeInventory())
			{
				setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(nbttag));
			}
		}
	}

	public NBTTagList saveInventoryToNBT()
	{
		NBTTagList list = new NBTTagList();

		for (int slot = 0; slot < getSizeInventory(); ++slot)
		{
			ItemStack itemstack = getStackInSlot(slot);

			if (itemstack != null)
			{
				NBTTagCompound nbttag = new NBTTagCompound();
				nbttag.setByte("Slot", (byte)slot);
				itemstack.writeToNBT(nbttag);
				list.appendTag(nbttag);
			}
		}

		return list;
	}

	public InventoryUniversalChest setAssociatedChest(TileEntityUniversalChest chest)
	{
		associatedChest = chest;

		return this;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		if (associatedChest != null)
		{
			return associatedChest.isUseableByPlayer(player);
		}

		return super.isUseableByPlayer(player);
	}

	@Override
	public void openInventory()
	{
		if (associatedChest != null)
		{
			associatedChest.openInventory();
		}

		super.openInventory();
	}

	@Override
	public void closeInventory()
	{
		if (associatedChest != null)
		{
			associatedChest.closeInventory();
		}

		super.closeInventory();

		associatedChest = null;
	}

	@Override
	public String getInventoryName()
	{
		return CaveBlocks.universal_chest.getUnlocalizedName() + ".name";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}
}