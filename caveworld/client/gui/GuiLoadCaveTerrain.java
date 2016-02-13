/*
 * Caveworld
 *
 * Copyright (c) 2016 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package caveworld.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.I18n;

@SideOnly(Side.CLIENT)
public class GuiLoadCaveTerrain extends GuiDownloadCaveTerrain
{
	public GuiLoadCaveTerrain(NetHandlerPlayClient handler)
	{
		super(handler);
	}

	@Override
	public String getInfoText()
	{
		return I18n.format("caveworld.terrain.load");
	}

	@Override
	public void updateScreen()
	{
		if (mc.thePlayer != null && mc.thePlayer.onGround)
		{
			mc.displayGuiScreen(null);
		}

		super.updateScreen();
	}
}