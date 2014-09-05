/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package com.kegare.caveworld.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import com.kegare.caveworld.client.config.SelectBiomeEntry;
import com.kegare.caveworld.client.config.SelectBlockEntry;
import com.kegare.caveworld.client.config.SelectItemEntry;
import com.kegare.caveworld.client.config.VeinsEntry.VeinConfigEntry;
import com.kegare.caveworld.client.renderer.RenderPortalCaveworld;
import com.kegare.caveworld.core.CaveVeinManager;
import com.kegare.caveworld.core.CommonProxy;
import com.kegare.caveworld.core.Config;
import com.kegare.caveworld.plugin.mceconomy.MCEconomyPlugin;
import com.kegare.caveworld.plugin.mceconomy.ShopEntry.ShopProductEntry;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void initializeConfigClasses()
	{
		Config.selectBlockEntryClass = SelectBlockEntry.class;
		Config.selectItemEntryClass = SelectItemEntry.class;
		Config.selectBiomeEntryClass = SelectBiomeEntry.class;

		CaveVeinManager.veinEntryClass = VeinConfigEntry.class;

		MCEconomyPlugin.productEntryClass = ShopProductEntry.class;
	}

	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerBlockHandler(new RenderPortalCaveworld());
	}

	@Override
	public int getUniqueRenderType()
	{
		return RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public void displayClientGuiScreen(Object obj)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (obj instanceof GuiScreen && (mc.currentScreen == null || mc.currentScreen.getClass() != obj.getClass()))
		{
			mc.displayGuiScreen((GuiScreen)obj);
		}
	}
}