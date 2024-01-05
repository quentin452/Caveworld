package caveworld.item;

import caveworld.core.Caveworld;
import caveworld.inventory.InventoryCaverBackpack;
import caveworld.network.CaveNetworkRegistry;
import caveworld.network.server.OpenGuiMessage;
import caveworld.util.CaveUtils;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.common.Optional.InterfaceList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCaverBackpack extends Item
{
	public ItemCaverBackpack(String name)
	{
		super();
		this.setUnlocalizedName(name);
		this.setTextureName("caveworld:caver_backpack");
		this.setMaxStackSize(1);
		this.setFull3D();
		this.setCreativeTab(Caveworld.tabCaveworld);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
	{
		if (!world.isRemote)
		{
			player.openGui(Caveworld.instance, 0, world, 0, 0, 0);

			world.playSoundAtEntity(player, "random.click", 0.6F, 1.5F);
		}

		return super.onItemRightClick(itemstack, world, player);
	}

	@Override
	public EnumRarity getRarity(ItemStack itemstack)
	{
		return ItemCavenium.cavenium;
	}

	public IInventory getInventory(ItemStack itemstack)
	{
		return new InventoryCaverBackpack(itemstack);
	}

	public void carryInventory(IInventory from, IInventory to)
	{
		int i = 0;

		for (int j = 0; j < to.getSizeInventory(); ++j)
		{
			if (to.getStackInSlot(j) != null)
			{
				++i;
				break;
			}
		}

		if (i > 0)
		{
			for (int j = 0; j < from.getSizeInventory(); ++j)
			{
				ItemStack item = from.getStackInSlot(j);

				if (item != null)
				{
					CaveUtils.addItemStackToInventory(to, item);

					if (item.stackSize <= 0)
					{
						from.setInventorySlotContents(j, null);
					}
				}
			}
		}
		else
		{
			int size = to.getSizeInventory();

			for (int j = 0; j < from.getSizeInventory(); ++j)
			{
				if (j > size)
				{
					continue;
				}

				ItemStack item = from.getStackInSlot(j);

				if (item != null && to.isItemValidForSlot(j, item))
				{
					to.setInventorySlotContents(j, item);

					from.setInventorySlotContents(j, null);
				}
			}
		}

		from.markDirty();
		to.markDirty();
	}
}
