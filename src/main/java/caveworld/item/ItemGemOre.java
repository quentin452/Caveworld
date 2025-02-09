package caveworld.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

public class ItemGemOre extends ItemBlockWithMetadata
{
	public ItemGemOre(Block block)
	{
		super(block, block);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		switch (itemstack.getItemDamage())
		{
			case 0:
				return "tile.oreAquamarine";
			case 1:
				return "tile.blockAquamarine";
			case 2:
				return "tile.oreMagnite";
			case 3:
				return "tile.blockMagnite";
			case 4:
				return "tile.oreHexcite";
			case 5:
				return "tile.blockHexcite";
			case 6:
				return "tile.oreInfitite";
			case 7:
				return "tile.blockInfitite";
		}

		return super.getUnlocalizedName(itemstack);
	}
}
