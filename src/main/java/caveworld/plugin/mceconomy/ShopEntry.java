package caveworld.plugin.mceconomy;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiConfigEntries.CategoryEntry;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
public class ShopEntry extends CategoryEntry
{
	public ShopEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
	{
		super(owningScreen, owningEntryList, prop);
	}

	@Override
	protected GuiScreen buildChildScreen()
	{
		return new GuiShopEntry(owningScreen, MCEconomyPlugin.productManager);
	}

	@Override
	public boolean isDefault()
	{
		return false;
	}

	@Override
	public void setToDefault()
	{
		try
		{
			FileUtils.forceDelete(new File(MCEconomyPlugin.shopCfg.toString()));
		}
		catch (IOException e)
		{
			e.printStackTrace();

			return;
		}

		MCEconomyPlugin.productManager.clearProducts();

		MCEconomyPlugin.shopCfg = null;
		MCEconomyPlugin.syncShopCfg();

		if (childScreen instanceof GuiShopEntry)
		{
			GuiShopEntry gui = (GuiShopEntry)childScreen;

			if (gui.productList != null)
			{
				gui.productList.products.clear();
				gui.productList.products.addAll(MCEconomyPlugin.productManager.getProducts());
				gui.productList.contents.clear();
				gui.productList.contents.addAll(gui.productList.products);
			}
		}
	}
}