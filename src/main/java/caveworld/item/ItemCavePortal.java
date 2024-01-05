package caveworld.item;

import caveworld.block.CaveBlocks;
import caveworld.core.CaveAchievementList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPortal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Facing;
import net.minecraft.world.World;

public class ItemCavePortal extends ItemBlock
{
	public ItemCavePortal(Block block)
	{
		super(block);
		this.setMaxStackSize(1);
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote && field_150939_a instanceof BlockPortal)
		{
			x += Facing.offsetsXForSide[side];
			y += Facing.offsetsYForSide[side];
			z += Facing.offsetsZForSide[side];

			if (((BlockPortal)field_150939_a).func_150000_e(world, x, y, z))
			{
				world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, field_150939_a.stepSound.func_150496_b(), 1.0F, 2.0F);

				if (!player.capabilities.isCreativeMode && --itemstack.stackSize <= 0)
				{
					player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
				}

				if (field_150939_a == CaveBlocks.caveworld_portal)
				{
					player.triggerAchievement(CaveAchievementList.portal);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return false;
	}
}