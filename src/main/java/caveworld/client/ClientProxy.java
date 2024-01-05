package caveworld.client;

import caveworld.client.config.*;
import caveworld.client.gui.GuiIngameCaveMenu;
import caveworld.client.gui.MenuType;
import caveworld.client.renderer.*;
import caveworld.config.Config;
import caveworld.core.CommonProxy;
import caveworld.entity.*;
import caveworld.item.CaveItems;
import caveworld.util.breaker.MultiBreakExecutor;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	@Override
	public void initConfigEntries()
	{
		Config.selectItems = SelectItemEntry.class;
		Config.selectItemsWithBlocks = SelectItemWithBlockEntry.class;
		Config.selectBiomes = SelectBiomeEntry.class;
		Config.selectMobs = SelectMobEntry.class;
		Config.selectPotions = SelectPotionEntry.class;
		Config.cycleInteger = CycleIntegerEntry.class;
		Config.pointsEntry = MiningPointsEntry.class;
	}

	@Override
	public void registerKeyBindings()
	{
		Config.keyBindAtCommand = new KeyBinding("key.atCommand", Keyboard.KEY_GRAVE, "key.categories.caveworld");

		ClientRegistry.registerKeyBinding(Config.keyBindAtCommand);
	}

	@Override
	public void registerRenderers()
	{
		RenderingRegistry.registerBlockHandler(new RenderCavePortal());
		RenderingRegistry.registerBlockHandler(new RenderBlockOverlay());

		TileEntityUniversalChestRenderer chestRenderer = new TileEntityUniversalChestRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUniversalChest.class, chestRenderer);
		RenderingRegistry.registerBlockHandler(Config.RENDER_TYPE_CHEST, chestRenderer);

		IItemRenderer itemRenderer = new RenderCaveniumTool();
		MinecraftForgeClient.registerItemRenderer(CaveItems.mining_pickaxe, itemRenderer);
		MinecraftForgeClient.registerItemRenderer(CaveItems.lumbering_axe, itemRenderer);
		MinecraftForgeClient.registerItemRenderer(CaveItems.digging_shovel, itemRenderer);
		MinecraftForgeClient.registerItemRenderer(CaveItems.farming_hoe, new RenderFarmingHoe());
		MinecraftForgeClient.registerItemRenderer(CaveItems.cavenic_bow, new RenderCavenicBow());

		RenderingRegistry.registerEntityRenderingHandler(EntityCaveman.class, new RenderCaveman());
		RenderingRegistry.registerEntityRenderingHandler(EntityArcherZombie.class, new RenderZombie());
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicSkeleton.class, new RenderCavenicSkeleton());
		RenderingRegistry.registerEntityRenderingHandler(EntityMasterCavenicSkeleton.class, new RenderMasterCavenicSkeleton());
		RenderingRegistry.registerEntityRenderingHandler(EntityCrazyCavenicSkeleton.class, new RenderCrazyCavenicSkeleton());
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicCreeper.class, new RenderCavenicCreeper());
		RenderingRegistry.registerEntityRenderingHandler(EntityMasterCavenicCreeper.class, new RenderMasterCavenicCreeper());
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicZombie.class, new RenderCavenicZombie());
		RenderingRegistry.registerEntityRenderingHandler(EntityCavenicSpider.class, new RenderCavenicSpider());
	}

	@Override
	public int getUniqueRenderType()
	{
		return RenderingRegistry.getNextAvailableRenderId();
	}

	@Override
	public EntityPlayer getClientPlayer()
	{
		return FMLClientHandler.instance().getClientPlayerEntity();
	}

	@Override
	public void displayMenu(MenuType type)
	{
		FMLClientHandler.instance().showGuiScreen(new GuiIngameCaveMenu().setMenuType(type));
	}

	@Override
	public void displayPortalMenu(MenuType type, int x, int y, int z)
	{
		FMLClientHandler.instance().showGuiScreen(new GuiIngameCaveMenu().setMenuType(type).setPortalCoord(x, y, z));
	}

	@Override
	public int getMultiBreakCount(EntityPlayer player)
	{
		return MultiBreakExecutor.positionsCount.get();
	}

	@Override
	public void setDebugBoundingBox(boolean flag)
	{
		RenderManager.debugBoundingBox = flag;
	}
}
