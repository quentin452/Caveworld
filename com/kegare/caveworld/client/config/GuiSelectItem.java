/*
 * Caveworld
 *
 * Copyright (c) 2014 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL.
 * Please check the contents of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package com.kegare.caveworld.client.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.kegare.caveworld.core.Caveworld;
import com.kegare.caveworld.core.Config;
import com.kegare.caveworld.util.ArrayListExtended;
import com.kegare.caveworld.util.CaveLog;
import com.kegare.caveworld.util.ItemComparator;

import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.GuiCheckBox;
import cpw.mods.fml.client.config.HoverChecker;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSelectItem extends GuiScreen
{
	protected final GuiScreen parentScreen;
	protected final GuiTextField parentTextField;

	private GuiButtonExt doneButton;
	private GuiCheckBox instantFilter;
	private GuiTextField filterTextField;
	private ItemList itemList;

	private HoverChecker instantHoverChecker;

	private static final Map<String, List<Item>> filterCache = Maps.newHashMap();

	public GuiSelectItem(GuiScreen parent, GuiTextField textField)
	{
		this.parentScreen = parent;
		this.parentTextField = textField;
	}

	@Override
	public void initGui()
	{
		doneButton = new GuiButtonExt(0, width / 2 - 155 + 165, height - 24, 145, 20, I18n.format("gui.done"));
		instantFilter = new GuiCheckBox(1, width / 2 - 155 + 250, 8, I18n.format(Caveworld.CONFIG_LANG + "select.instant"), CaveConfigGui.instantFilter);

		buttonList.clear();
		buttonList.add(doneButton);
		buttonList.add(instantFilter);

		filterTextField = new GuiTextField(fontRendererObj, width / 2 - 155, height - 23, 150, 16);
		filterTextField.setMaxStringLength(100);

		itemList = new ItemList(this);
		itemList.registerScrollButtons(2, 3);

		instantHoverChecker = new HoverChecker(instantFilter, 800);
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button.enabled)
		{
			switch (button.id)
			{
				case 0:
					if (itemList.selected == null)
					{
						parentTextField.setText("");
					}
					else
					{
						parentTextField.setText(GameData.getItemRegistry().getNameForObject(itemList.selected));
					}

					parentTextField.setFocused(true);
					parentTextField.setCursorPositionEnd();

					mc.displayGuiScreen(parentScreen);
					break;
			}
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		filterTextField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float ticks)
	{
		itemList.drawScreen(mouseX, mouseY, ticks);

		drawCenteredString(fontRendererObj, I18n.format(Caveworld.CONFIG_LANG + "select.item"), width / 2, 15, 0xFFFFFF);

		super.drawScreen(mouseX, mouseY, ticks);

		if (instantHoverChecker.checkHover(mouseX, mouseY))
		{
			func_146283_a(fontRendererObj.listFormattedStringToWidth(I18n.format(Caveworld.CONFIG_LANG + "select.instant.hover"), 300), mouseX, mouseY);
		}

		GL11.glDisable(GL11.GL_LIGHTING);

		filterTextField.drawTextBox();
	}

	@Override
	protected void mouseClicked(int x, int y, int code)
	{
		super.mouseClicked(x, y, code);

		filterTextField.mouseClicked(x, y, code);
	}

	@Override
	protected void keyTyped(char c, int code)
	{
		if (filterTextField.isFocused() && code != 1)
		{
			String prev = filterTextField.getText();

			filterTextField.textboxKeyTyped(c, code);

			String text = filterTextField.getText();
			boolean changed = text != prev;

			if (Strings.isNullOrEmpty(text) && changed)
			{
				itemList.setFilter("");
			}
			else if (instantFilter.isChecked() && changed || code == Keyboard.KEY_RETURN)
			{
				itemList.setFilter(text);
			}
		}
		else
		{
			if (code == Keyboard.KEY_ESCAPE)
			{
				mc.displayGuiScreen(parentScreen);
			}
			else if (code == Keyboard.KEY_BACK)
			{
				itemList.selected = null;
			}
			else if (code == Keyboard.KEY_TAB)
			{
				if (++itemList.nameType > 2)
				{
					itemList.nameType = 0;
				}
			}
			else if (code == Keyboard.KEY_UP)
			{
				int i = itemList.getAmountScrolled() % itemList.getSlotHeight();

				if (i == 0)
				{
					itemList.scrollBy(-itemList.getSlotHeight());
				}
				else
				{
					itemList.scrollBy(-i);
				}
			}
			else if (code == Keyboard.KEY_DOWN)
			{
				itemList.scrollBy(itemList.getSlotHeight() - (itemList.getAmountScrolled() % itemList.getSlotHeight()));
			}
			else if (code == Keyboard.KEY_PRIOR)
			{
				itemList.scrollBy(-((itemList.getAmountScrolled() % itemList.getSlotHeight()) + ((itemList.bottom - itemList.top) / itemList.getSlotHeight()) * itemList.getSlotHeight()));
			}
			else if (code == Keyboard.KEY_NEXT)
			{
				itemList.scrollBy((itemList.getAmountScrolled() % itemList.getSlotHeight()) + ((itemList.bottom - itemList.top) / itemList.getSlotHeight()) * itemList.getSlotHeight());
			}
			else if (code == Keyboard.KEY_SPACE)
			{
				itemList.scrollBy(-itemList.getAmountScrolled());

				if (itemList.selected != null)
				{
					itemList.scrollBy(itemList.contents.indexOf(itemList.selected) * itemList.getSlotHeight());
				}
			}
			else if (code == Keyboard.KEY_F || code == mc.gameSettings.keyBindChat.getKeyCode())
			{
				filterTextField.setFocused(true);
			}
		}
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		CaveConfigGui.instantFilter = instantFilter.isChecked();
	}

	private static class ItemList extends GuiSlot
	{
		private static final ArrayListExtended<Item> items = new ArrayListExtended<Item>().addAllObject(GameData.getItemRegistry()).sort(new ItemComparator());

		private final GuiSelectItem parent;

		private final ArrayListExtended<Item> contents = new ArrayListExtended(items);
		private final Set<Item> ignoredRender = Sets.newHashSet();

		private int nameType;
		private Item selected = null;

		public ItemList(GuiSelectItem parent)
		{
			super(parent.mc, parent.width, parent.height, 32, parent.height - 28, 18);
			this.parent = parent;
			this.selected = GameData.getItemRegistry().getObject(parent.parentTextField.getText());

			for (Item item : items)
			{
				UniqueIdentifier unique = GameRegistry.findUniqueIdentifierFor(item);

				if (unique != null && Config.ignoredRenderGuiItems != null && Config.ignoredRenderGuiItems.length > 0 &&
					(ArrayUtils.contains(Config.ignoredRenderGuiItems, unique.modId) ||
					ArrayUtils.contains(Config.ignoredRenderGuiItems, GameData.getItemRegistry().getNameForObject(item))))
				{
					ignoredRender.add(item);
				}
			}
		}

		@Override
		protected int getSize()
		{
			return contents.size();
		}

		@Override
		protected void drawBackground()
		{
			parent.drawDefaultBackground();
		}

		@Override
		protected void drawSlot(int index, int par2, int par3, int par4, Tessellator tessellator, int mouseX, int mouseY)
		{
			Item item = contents.get(index, null);

			if (item == null)
			{
				return;
			}

			if (!ignoredRender.contains(item))
			{
				try
				{
					GL11.glEnable(GL12.GL_RESCALE_NORMAL);
					RenderHelper.enableGUIStandardItemLighting();
					RenderItem.getInstance().renderItemAndEffectIntoGUI(parent.fontRendererObj, parent.mc.getTextureManager(), new ItemStack(item), width / 2 - 100, par3 - 1);
					RenderHelper.disableStandardItemLighting();
					GL11.glDisable(GL12.GL_RESCALE_NORMAL);
				}
				catch (Exception e)
				{
					CaveLog.log(Level.WARN, e, "Failed to trying render item into gui: %s", GameData.getBlockRegistry().getNameForObject(item));

					ignoredRender.add(item);
				}
			}

			String name = null;

			switch (nameType)
			{
				case 1:
					name = GameData.getItemRegistry().getNameForObject(item);
					break;
				case 2:
					name = item.getUnlocalizedName();
					name = name.substring(name.indexOf(".") + 1);
					break;
				default:
					name = item.getItemStackDisplayName(new ItemStack(item));
					break;
			}

			parent.drawCenteredString(parent.fontRendererObj, name, width / 2, par3 + 1, 0xFFFFFF);
		}

		@Override
		protected void elementClicked(int index, boolean flag, int mouseX, int mouseY)
		{
			selected = isSelected(index) ? null : contents.get(index, null);
		}

		@Override
		protected boolean isSelected(int index)
		{
			return selected == contents.get(index, null);
		}

		protected void setFilter(final String filter)
		{
			ForkJoinPool pool = new ForkJoinPool();

			pool.execute(new RecursiveAction()
			{
				@Override
				protected void compute()
				{
					List<Item> result;

					if (Strings.isNullOrEmpty(filter))
					{
						result = items;
					}
					else
					{
						if (!GuiSelectItem.filterCache.containsKey(filter))
						{
							GuiSelectItem.filterCache.put(filter, Lists.newArrayList(Collections2.filter(items, new ItemFilter(filter))));
						}

						result = GuiSelectItem.filterCache.get(filter);
					}

					if (!contents.equals(result))
					{
						contents.clear();
						contents.addAll(result);
					}
				}
			});

			pool.shutdown();
		}
	}

	private static class ItemFilter implements Predicate<Item>
	{
		private final String filter;

		private ItemFilter(String filter)
		{
			this.filter = filter;
		}

		@Override
		public boolean apply(Item item)
		{
			if (GameData.getItemRegistry().getNameForObject(item).toLowerCase().contains(filter.toLowerCase()) ||
				item.getUnlocalizedName().toLowerCase().contains(filter.toLowerCase()) ||
				item.getItemStackDisplayName(new ItemStack(item)).toLowerCase().contains(filter.toLowerCase()) ||
				item.getToolClasses(new ItemStack(item)).contains(filter))
			{
				return true;
			}

			return false;
		}
	}
}