package caveworld.core;

import caveworld.api.BlockEntry;
import caveworld.api.CaverAPI;
import caveworld.api.CaveworldAPI;
import caveworld.block.CaveBlocks;
import caveworld.config.Config;
import caveworld.config.ConfigHelper;
import caveworld.config.manager.*;
import caveworld.entity.CaveEntityRegistry;
import caveworld.handler.CaveAPIHandler;
import caveworld.handler.CaveEventHooks;
import caveworld.handler.CaveFuelHandler;
import caveworld.handler.CaveGuiHandler;
import caveworld.item.CaveItems;
import caveworld.item.ItemDiggingShovel;
import caveworld.item.ItemLumberingAxe;
import caveworld.item.ItemMiningPickaxe;
import caveworld.network.CaveNetworkRegistry;
import caveworld.plugin.CavePlugins;
import caveworld.recipe.RecipeCavenicBow;
import caveworld.recipe.RecipeCaveniumTool;
import caveworld.recipe.RecipeFarmingHoe;
import caveworld.util.CaveLog;
import caveworld.util.CaveUtils;
import caveworld.util.Version;
import caveworld.world.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mod(modid = Caveworld.MODID, guiFactory = "caveworld.client.config.CaveGuiFactory")
public class Caveworld
{
	public static final String
	MODID = "caveworld",
	CONFIG_LANG = "caveworld.config.";

	@Metadata(MODID)
	public static ModMetadata metadata;

	@Instance(MODID)
	public static Caveworld instance;

	@SidedProxy(modId = MODID, clientSide = "caveworld.client.ClientProxy", serverSide = "caveworld.core.CommonProxy")
	public static CommonProxy proxy;

	public static final CreativeTabCaveworld tabCaveworld = new CreativeTabCaveworld();
	public static final CreativeTabMiningPickaxe tabMiningPickaxe = new CreativeTabMiningPickaxe();
	public static final CreativeTabLumberingAxe tabLumberingAxe = new CreativeTabLumberingAxe();
	public static final CreativeTabDiggingShovel tabDiggingShovel = new CreativeTabDiggingShovel();
	public static final CreativeTabFarmingHoe tabFarmingHoe = new CreativeTabFarmingHoe();

	@EventHandler
	public void construct(FMLConstructionEvent event)
	{
		proxy.initConfigEntries();

		CaveworldAPI.apiHandler = new CaveAPIHandler();
		CaveworldAPI.biomeManager = new CaveBiomeManager();
		CaveworldAPI.veinManager = new CaveVeinManager();
		CaveworldAPI.biomeCavernManager = new CavernBiomeManager();
		CaveworldAPI.veinCavernManager = new CavernVeinManager();
		CaveworldAPI.biomeAquaCavernManager = new AquaCavernBiomeManager();
		CaveworldAPI.veinAquaCavernManager = new AquaCavernVeinManager();
		CaveworldAPI.veinCavelandManager = new CavelandVeinManager();

		CaverAPI.caverManager = new CaverManager();

		Version.versionCheck();

		RecipeSorter.register(MODID + ":cavenium_tool", RecipeCaveniumTool.class, Category.SHAPED, "after:minecraft:shaped");
		RecipeSorter.register(MODID + ":farming_hoe", RecipeFarmingHoe.class, Category.SHAPED, "after:minecraft:shaped");
		RecipeSorter.register(MODID + ":cavenic_bow", RecipeCavenicBow.class, Category.SHAPED, "after:minecraft:shaped");
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		Config.syncGeneralCfg();

		CaveBlocks.registerBlocks();
		CaveItems.registerItems();

		CaveNetworkRegistry.registerMessages();

		if (event.getSide().isServer())
		{
			Config.syncServerCfg();
		}

		GameRegistry.registerFuelHandler(new CaveFuelHandler());

		proxy.registerKeyBindings();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		CaveBlocks.registerRecipes();
		CaveItems.registerRecipes();

		CaveBlocks.addChestContents();
		CaveItems.addChestContents();

		CaveEntityRegistry.registerEntities();
		CaveEntityRegistry.addVallilaSpawns();

		proxy.registerRenderers();

		Config.syncMobsCfg();
		Config.syncDimensionCfg();

		int id = CaveworldAPI.getDimension();
		DimensionManager.registerProviderType(id, WorldProviderCaveworld.class, false);
		DimensionManager.registerDimension(id, id);

		id = CaveworldAPI.getCavernDimension();
		DimensionManager.registerProviderType(id, WorldProviderCavern.class, false);
		DimensionManager.registerDimension(id, id);

		id = CaveworldAPI.getAquaCavernDimension();
		DimensionManager.registerProviderType(id, WorldProviderAquaCavern.class, false);
		DimensionManager.registerDimension(id, id);

		id = CaveworldAPI.getCavelandDimension();
		DimensionManager.registerProviderType(id, WorldProviderCaveland.class, false);
		DimensionManager.registerDimension(id, id);

		id = CaveworldAPI.getCaveniaDimension();
		DimensionManager.registerProviderType(id, WorldProviderCavenia.class, false);
		DimensionManager.registerDimension(id, id);

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new CaveGuiHandler());

		FMLCommonHandler.instance().bus().register(CaveEventHooks.instance);

		MinecraftForge.EVENT_BUS.register(CaveEventHooks.instance);

		CaveAchievementList.registerAchievements();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{

		for (Item item : GameData.getItemRegistry().typeSafeIterable())
		{
			CaveUtils.isItemPickaxe(item);
			CaveUtils.isItemAxe(item);
			CaveUtils.isItemShovel(item);
			CaveUtils.isItemHoe(item);
		}

        if (!Config.disableCaveniumTools) {

            for (Block block : GameData.getBlockRegistry().typeSafeIterable()) {

                Item item = Item.getItemFromBlock(block);
                CreativeTabs tab = CreativeTabs.tabBlock;

                try {

                    List<ItemStack> list = new ArrayList<>();

                    block.getSubBlocks(item, tab, list);

                    if (list.isEmpty()) {
					{
						if (Strings.nullToEmpty(block.getHarvestTool(0)).equalsIgnoreCase("pickaxe") || CaveItems.mining_pickaxe.func_150897_b(block) ||
							block instanceof BlockOre || block instanceof BlockRedstoneOre || block instanceof BlockGlowstone)
						{
							ItemMiningPickaxe.breakableBlocks.addIfAbsent(new BlockEntry(block, 0));

							if (block instanceof BlockRotatedPillar)
							{
								ItemMiningPickaxe.breakableBlocks.addIfAbsent(new BlockEntry(block, 4));
								ItemMiningPickaxe.breakableBlocks.addIfAbsent(new BlockEntry(block, 8));
							}
						}

						if (CaveUtils.isWood(block, 0))
						{
							ItemLumberingAxe.breakableBlocks.addIfAbsent(new BlockEntry(block, 0));

							if (block instanceof BlockRotatedPillar)
							{
								ItemLumberingAxe.breakableBlocks.addIfAbsent(new BlockEntry(block, 4));
								ItemLumberingAxe.breakableBlocks.addIfAbsent(new BlockEntry(block, 8));
							}
						}

						if (Strings.nullToEmpty(block.getHarvestTool(0)).equalsIgnoreCase("shovel") || CaveItems.digging_shovel.func_150897_b(block) || block instanceof BlockClay ||
							block.getMaterial() == Material.ground || block.getMaterial() == Material.grass || block.getMaterial() == Material.sand || block.getMaterial() == Material.snow || block.getMaterial() == Material.craftedSnow)
						{
							ItemDiggingShovel.breakableBlocks.addIfAbsent(new BlockEntry(block, 0));

							if (block instanceof BlockRotatedPillar)
							{
								ItemDiggingShovel.breakableBlocks.addIfAbsent(new BlockEntry(block, 4));
								ItemDiggingShovel.breakableBlocks.addIfAbsent(new BlockEntry(block, 8));
							}
					}
					else for (ItemStack itemstack : list)
					{
						if (itemstack != null && itemstack.getItem() != null)
						{
							Block sub = Block.getBlockFromItem(itemstack.getItem());

							if (sub == Blocks.air)
							{
								continue;
							}

							int meta = itemstack.getItemDamage();

							if (Strings.nullToEmpty(sub.getHarvestTool(meta)).equalsIgnoreCase("pickaxe") ||
								CaveItems.mining_pickaxe.func_150897_b(sub) || sub instanceof BlockOre || sub instanceof BlockRedstoneOre || block instanceof BlockGlowstone)
							{
								ItemMiningPickaxe.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta));

								if (sub instanceof BlockRotatedPillar)
								{
									ItemMiningPickaxe.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta + 4));
									ItemMiningPickaxe.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta + 8));
								}
							}

							if (CaveUtils.isWood(sub, meta))
							{
								ItemLumberingAxe.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta));

								if (sub instanceof BlockRotatedPillar)
								{
									ItemLumberingAxe.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta + 4));
									ItemLumberingAxe.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta + 8));
								}
							}

							if (Strings.nullToEmpty(sub.getHarvestTool(meta)).equalsIgnoreCase("shovel") || CaveItems.digging_shovel.func_150897_b(sub) || block instanceof BlockClay ||
								sub.getMaterial() == Material.ground || sub.getMaterial() == Material.grass || sub.getMaterial() == Material.sand || sub.getMaterial() == Material.snow || sub.getMaterial() == Material.craftedSnow)
							{
								ItemDiggingShovel.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta));

								if (sub instanceof BlockRotatedPillar)
								{
									ItemDiggingShovel.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta + 4));
									ItemDiggingShovel.breakableBlocks.addIfAbsent(new BlockEntry(sub, meta + 8));
								}
							}
						}
					}		}
                    }
				}
				catch (Throwable e) {}
			}
		}

		CaverAPI.setMiningPointAmount("oreCoal", 1);
		CaverAPI.setMiningPointAmount("oreIron", 1);
		CaverAPI.setMiningPointAmount("oreGold", 1);
		CaverAPI.setMiningPointAmount("oreRedstone", 2);
		CaverAPI.setMiningPointAmount(Blocks.lit_redstone_ore, 0, 2);
		CaverAPI.setMiningPointAmount("oreLapis", 3);
		CaverAPI.setMiningPointAmount("oreEmerald", 3);
		CaverAPI.setMiningPointAmount("oreDiamond", 5);
		CaverAPI.setMiningPointAmount("oreQuartz", 2);
		CaverAPI.setMiningPointAmount("oreCopper", 1);
		CaverAPI.setMiningPointAmount("copperOre", 1);
		CaverAPI.setMiningPointAmount("oreTin", 1);
		CaverAPI.setMiningPointAmount("tinOre", 1);
		CaverAPI.setMiningPointAmount("oreLead", 1);
		CaverAPI.setMiningPointAmount("leadOre", 1);
		CaverAPI.setMiningPointAmount("oreSilver", 1);
		CaverAPI.setMiningPointAmount("silverOre", 1);
		CaverAPI.setMiningPointAmount("oreAdamantium", 1);
		CaverAPI.setMiningPointAmount("adamantiumOre", 1);
		CaverAPI.setMiningPointAmount("oreAluminum", 1);
		CaverAPI.setMiningPointAmount("aluminumOre", 1);
		CaverAPI.setMiningPointAmount("oreApatite", 1);
		CaverAPI.setMiningPointAmount("apatiteOre", 1);
		CaverAPI.setMiningPointAmount("oreMythril", 1);
		CaverAPI.setMiningPointAmount("mythrilOre", 1);
		CaverAPI.setMiningPointAmount("oreOnyx", 1);
		CaverAPI.setMiningPointAmount("onyxOre", 1);
		CaverAPI.setMiningPointAmount("oreUranium", 2);
		CaverAPI.setMiningPointAmount("uraniumOre", 2);
		CaverAPI.setMiningPointAmount("oreSapphire", 3);
		CaverAPI.setMiningPointAmount("sapphireOre", 3);
		CaverAPI.setMiningPointAmount("oreRuby", 3);
		CaverAPI.setMiningPointAmount("rubyOre", 3);
		CaverAPI.setMiningPointAmount("oreTopaz", 2);
		CaverAPI.setMiningPointAmount("topazOre", 2);
		CaverAPI.setMiningPointAmount("oreChrome", 1);
		CaverAPI.setMiningPointAmount("chromeOre", 1);
		CaverAPI.setMiningPointAmount("orePlatinum", 1);
		CaverAPI.setMiningPointAmount("platinumOre", 1);
		CaverAPI.setMiningPointAmount("oreTitanium", 1);
		CaverAPI.setMiningPointAmount("titaniumOre", 1);
		CaverAPI.setMiningPointAmount("oreTofu", 1);
		CaverAPI.setMiningPointAmount("tofuOre", 1);
		CaverAPI.setMiningPointAmount("oreTofuDiamond", 4);
		CaverAPI.setMiningPointAmount("tofuDiamondOre", 4);
		CaverAPI.setMiningPointAmount("oreSulfur", 1);
		CaverAPI.setMiningPointAmount("sulfurOre", 1);
		CaverAPI.setMiningPointAmount("oreSaltpeter", 1);
		CaverAPI.setMiningPointAmount("saltpeterOre", 1);
		CaverAPI.setMiningPointAmount("oreFirestone", 2);
		CaverAPI.setMiningPointAmount("firestoneOre", 2);
		CaverAPI.setMiningPointAmount("oreSalt", 1);
		CaverAPI.setMiningPointAmount("saltOre", 1);
		CaverAPI.setMiningPointAmount("oreJade", 1);
		CaverAPI.setMiningPointAmount("jadeOre", 1);
		CaverAPI.setMiningPointAmount("oreManganese", 1);
		CaverAPI.setMiningPointAmount("manganeseOre", 1);
		CaverAPI.setMiningPointAmount("oreLanite", 1);
		CaverAPI.setMiningPointAmount("laniteOre", 1);
		CaverAPI.setMiningPointAmount("oreMeurodite", 1);
		CaverAPI.setMiningPointAmount("meuroditeOre", 1);
		CaverAPI.setMiningPointAmount("oreSoul", 1);
		CaverAPI.setMiningPointAmount("soulOre", 1);
		CaverAPI.setMiningPointAmount("oreSunstone", 1);
		CaverAPI.setMiningPointAmount("sunstoneOre", 1);
		CaverAPI.setMiningPointAmount("oreZinc", 1);
		CaverAPI.setMiningPointAmount("zincOre", 1);
		CaverAPI.setMiningPointAmount("oreCrocoite", 3);
		CaverAPI.setMiningPointAmount("crocoiteOre", 3);
		CaverAPI.setMiningPointAmount("oreAquamarine", 2);
		CaverAPI.setMiningPointAmount("aquamarineOre", 2);
		CaverAPI.setMiningPointAmount("glowstone", 2);

		if (Config.initCavebornItems)
		{
			ConfigCategory category = Config.generalCfg.getCategory("options");
			Property prop = category.get("cavebornItems");

			if (prop != null)
			{
				List<ItemStack> items = Lists.newArrayList();

				items.add(new ItemStack(Items.stone_pickaxe));
				items.add(new ItemStack(Items.stone_sword));
				items.add(new ItemStack(Blocks.torch));
				items.add(new ItemStack(Items.bread));

				for (int i = 0; i < 3; ++i)
				{
					items.add(new ItemStack(CaveBlocks.perverted_sapling, 1, i));
				}

				items.add(new ItemStack(Blocks.crafting_table));
				items.add(new ItemStack(Blocks.dirt));
				items.add(new ItemStack(CaveItems.acresia));

				Config.cavebornItems = ConfigHelper.getStringsFromItems(items);

				prop.set(Config.cavebornItems);
			}
		}

		Config.syncBiomesCfg();
		Config.syncBiomesCavernCfg();
		Config.syncBiomesAquaCavernCfg();
		Config.syncVeinsCfg();
		Config.syncVeinsCavernCfg();
		Config.syncVeinsAquaCavernCfg();
		Config.syncVeinsCavelandCfg();

		CavePlugins.registerPlugins();
		Config.syncPluginsCfg();

		CavePlugins.invokePlugins();

		if (event.getSide().isClient())
		{
			CavePlugins.invokeClientPlugins();
		}
	}

	@EventHandler
	public void loaded(FMLLoadCompleteEvent event)
	{
		Set<String> entries = Sets.newTreeSet();

		if (!Config.disableCaveniumTools)
		{
			for (Block block : GameData.getBlockRegistry().typeSafeIterable())
			{
				for (int i = 0; i < 16; ++i)
				{
					int point = CaverAPI.getMiningPointAmount(block, i);

					if (point > 0)
					{
						entries.add(GameData.getBlockRegistry().getNameForObject(block) + ":" + i + "," + point);

						ItemMiningPickaxe.defaultBreakables.add(CaveUtils.toStringHelper(block, i));
					}
				}
			}
		}

		ConfigCategory category = Config.generalCfg.getCategory(Configuration.CATEGORY_GENERAL);
		Property prop = category.get("miningPoints");

		if (prop.getStringList() == null || prop.getStringList().length <= 0)
		{
			prop.set(entries.toArray(new String[entries.size()]));
		}

		prop = category.get("miningPointValidItems");

		if (prop.getStringList() == null || prop.getStringList().length <= 0)
		{
			entries.clear();

			for (Item item : CaveUtils.pickaxeItems)
			{
				entries.add(GameData.getItemRegistry().getNameForObject(item));
			}

			prop.set(entries.toArray(new String[entries.size()]));
		}

		Config.saveConfig(Config.generalCfg);
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		ConfigHelper.refreshMiningPoints();
		ConfigHelper.refreshCavebornItems();

		event.registerServerCommand(new CommandCaveworld());

		if (event.getSide().isServer() && (Version.DEV_DEBUG || Config.versionNotify && Version.isOutdated()))
		{
			event.getServer().logInfo(StatCollector.translateToLocalFormatted("caveworld.version.message", metadata.name) + ": " + Version.getLatest());
		}
	}

	@EventHandler
	public void serverStopping(FMLServerStoppedEvent event)
	{
		CaveEventHooks.firstJoinPlayers.clear();

		ItemMiningPickaxe.BreakMode.executors.clear();
		ItemLumberingAxe.BreakMode.executors.clear();
		ItemDiggingShovel.BreakMode.executors.clear();

		File dir = Config.getConfigDir();

		if (dir != null && dir.exists())
		{
			try (FileOutputStream output = new FileOutputStream(new File(dir, "UniversalChest.dat")))
			{
				NBTTagCompound data = CaveBlocks.universal_chest.getData();

				data.setTag("ChestItems", CaveBlocks.universal_chest.inventory.saveInventoryToNBT());

				CompressedStreamTools.writeCompressed(data, output);
			}
			catch (Exception e)
			{
				CaveLog.log(Level.ERROR, e, "An error occurred trying to writing Universal Chest data");
			}
		}
	}

	@EventHandler
	public void missingMappings(FMLMissingMappingsEvent event)
	{
		event.applyModContainer(new DummyModContainer()
		{
			@Override
			public String getModId()
			{
				return "kegare.caveworld";
			}
		});

		for (MissingMapping missing : event.get())
		{
			String name = missing.name;
			Object obj = missing.type.getRegistry().getObject(MODID + name.substring(name.indexOf(':')));

			if (obj != null)
			{
				if (obj instanceof Block)
				{
					missing.remap((Block)obj);
				}
				else if (obj instanceof Item)
				{
					missing.remap((Item)obj);
				}
			}
		}
	}
}
