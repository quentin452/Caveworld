/*
 * Caveworld
 *
 * Copyright (c) 2016 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package caveworld.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class SubItemHelper
{
	public static final Map<Block, List<ItemStack>> cachedSubBlocks = Maps.newHashMap();
	public static final Map<Item, List<ItemStack>> cachedSubItems = Maps.newHashMap();

	public static void cacheSubBlocks()
	{
		cachedSubBlocks.clear();

		for (Block block : GameData.getBlockRegistry().typeSafeIterable())
		{
			List<ItemStack> list = Lists.newArrayList();

			if (Item.getItemFromBlock(block) == null)
			{
				cachedSubBlocks.put(block, list);
				continue;
			}

			ItemStack stack;
			String last = null;
			String name = null;

			for (int i = 0; i < 16; ++i)
			{
				stack = new ItemStack(block, 1, i);
				name = stack.getDisplayName();

				if (Strings.isNullOrEmpty(last))
				{
					list.add(stack);
				}
				else if (!last.equals(name))
				{
					list.add(stack);
				}

				last = name;
			}

			if (list.size() > 1)
			{
				list.remove(list.size() - 1);
			}

			cachedSubBlocks.put(block, list);

			if (list.size() > 1)
			{
				CaveLog.fine("Cached - SubBlocks: " + GameData.getBlockRegistry().getNameForObject(block) + " has " + list.size() + " blocks");
			}
		}
	}

	public static void cacheSubItems()
	{
		cachedSubItems.clear();

		for (Item item : GameData.getItemRegistry().typeSafeIterable())
		{
			List<ItemStack> list = Lists.newArrayList();

			if (item instanceof ItemBlock)
			{
				cachedSubItems.put(item, list);
				continue;
			}
			else if (item.isDamageable())
			{
				list.add(new ItemStack(item));
				cachedSubItems.put(item, list);
				continue;
			}

			ItemStack stack;
			String last = null;
			String name = null;

			for (int i = 0; i < 32767; ++i)
			{
				stack = new ItemStack(item, 1, i);
				name = stack.getDisplayName();

				if (Strings.isNullOrEmpty(last))
				{
					list.add(stack);
				}
				else if (!last.equals(name))
				{
					list.add(stack);
				}

				last = name;
			}

			if (list.size() > 1)
			{
				list.remove(list.size() - 1);
			}

			cachedSubItems.put(item, list);

			if (list.size() > 1)
			{
				CaveLog.fine("Cached - SubItems: " + GameData.getItemRegistry().getNameForObject(item) + " has " + list.size() + " items");
			}
		}
	}

	public static List<ItemStack> getSubBlocks(Block block)
	{
		return block == null || !cachedSubBlocks.containsKey(block) ? new ArrayList<ItemStack>() : cachedSubBlocks.get(block);
	}

	public static List<ItemStack> getSubItems(Item item)
	{
		return item == null || !cachedSubItems.containsKey(item) ? new ArrayList<ItemStack>() : cachedSubItems.get(item);
	}
}