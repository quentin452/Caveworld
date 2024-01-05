package caveworld.client.gui;

import caveworld.inventory.ContainerCaverBackpack;
import caveworld.inventory.InventoryCaverBackpack;
import caveworld.item.ItemCaverBackpack;
import cpw.mods.fml.common.Optional.Method;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import shift.sextiarysector.api.equipment.EquipmentType;
import shift.sextiarysector.gui.tab.TabManager;

@SideOnly(Side.CLIENT)
public class GuiCaverBackpack extends GuiContainer
{
	private static final ResourceLocation containerTexture = new ResourceLocation("textures/gui/container/generic_54.png");

	private IInventory upperInventory;
	private IInventory lowerInventory;
	private int inventoryRows;

	public GuiCaverBackpack(InventoryPlayer inventory, InventoryCaverBackpack backpackInventory)
	{
		super(new ContainerCaverBackpack(inventory, backpackInventory));
		this.upperInventory = inventory;
		this.lowerInventory = backpackInventory;
		this.allowUserInput = false;
		short s = 222;
		int i = s - 108;
		this.inventoryRows = backpackInventory.getSizeInventory() / 9;
		this.ySize = i + inventoryRows * 18;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();

		super.initGui();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2)
	{
		fontRendererObj.drawString(lowerInventory.hasCustomInventoryName() ? lowerInventory.getInventoryName() : I18n.format(lowerInventory.getInventoryName()), 8, 6, 4210752);
		fontRendererObj.drawString(upperInventory.hasCustomInventoryName() ? upperInventory.getInventoryName() : I18n.format(upperInventory.getInventoryName()), 8, ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(containerTexture);
		int k = (width - xSize) / 2;
		int l = (height - ySize) / 2;
		drawTexturedModalRect(k, l, 0, 0, xSize, inventoryRows * 18 + 17);
		drawTexturedModalRect(k, l + inventoryRows * 18 + 17, 0, 126, xSize, 96);
	}
}
