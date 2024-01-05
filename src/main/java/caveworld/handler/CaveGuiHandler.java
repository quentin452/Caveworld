package caveworld.handler;

import caveworld.client.gui.GuiCaverBackpack;
import caveworld.inventory.ContainerCaverBackpack;
import caveworld.inventory.InventoryCaverBackpack;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class CaveGuiHandler implements IGuiHandler
{
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		switch (ID)
		{
			case 0:
				return new ContainerCaverBackpack(player.inventory, new InventoryCaverBackpack(player.getCurrentEquippedItem()));
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		switch (ID)
		{
			case 0:
				return new GuiCaverBackpack(player.inventory, new InventoryCaverBackpack(player.getCurrentEquippedItem()));
		}

		return null;
	}
}
