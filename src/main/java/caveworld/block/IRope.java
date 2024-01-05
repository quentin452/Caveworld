package caveworld.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IRope
{
	public int getKnotMetadata(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ);

	public void setUnderRopes(World world, int x, int y, int z);

	public int getRopesLength(World world, int x, int y, int z);
}