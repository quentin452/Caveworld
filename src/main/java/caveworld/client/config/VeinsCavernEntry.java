package caveworld.client.config;

import caveworld.api.CaveworldAPI;
import caveworld.config.Config;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.GuiConfigEntries;
import cpw.mods.fml.client.config.GuiConfigEntries.CategoryEntry;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiScreen;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@SideOnly(Side.CLIENT)
public class VeinsCavernEntry extends CategoryEntry
{
	public VeinsCavernEntry(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement prop)
	{
		super(owningScreen, owningEntryList, prop);
	}

	@Override
	protected GuiScreen buildChildScreen()
	{
		return new GuiVeinsEntry(owningScreen, CaveworldAPI.veinCavernManager);
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
			FileUtils.forceDelete(new File(Config.veinsCavernCfg.toString()));
		}
		catch (IOException e)
		{
			e.printStackTrace();

			return;
		}

		CaveworldAPI.clearCavernVeins();

		Config.veinsCavernCfg = null;
		Config.syncVeinsCavernCfg();

		if (childScreen instanceof GuiVeinsEntry)
		{
			GuiVeinsEntry gui = (GuiVeinsEntry)childScreen;

			if (gui.veinList != null)
			{
				gui.veinList.veins.clear();
				gui.veinList.veins.addAll(CaveworldAPI.getCavernVeins());
				gui.veinList.contents.clear();
				gui.veinList.contents.addAll(gui.veinList.veins);
			}
		}
	}
}
