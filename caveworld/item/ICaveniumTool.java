/*
 * Caveworld
 *
 * Copyright (c) 2016 kegare
 * https://github.com/kegare
 *
 * This mod is distributed under the terms of the Minecraft Mod Public License Japanese Translation, or MMPL_J.
 */

package caveworld.item;

import java.util.List;
import java.util.Set;

import caveworld.api.BlockEntry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface ICaveniumTool extends IModeItem
{
	public String getToolClass();

	public List<BlockEntry> getBreakableBlocks();

	public int getRefined(ItemStack itemstack);

	public boolean canBreak(ItemStack itemstack, Block block, int metadata);

	public Item getBase(ItemStack itemstack);

	public IBreakMode getMode(ItemStack itemstack);

	public Set<Item> getBaseableItems();
}