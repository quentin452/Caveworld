package caveworld.item;

import net.minecraft.item.ItemStack;

public class ItemInfititeArmor extends ItemCaveArmor
{
	public ItemInfititeArmor(String name, String texture, int armorType)
	{
		super(name, texture, "infitite", CaveItems.INFITITE_ARMOR, armorType);
	}

	@Override
	public int getDamage(ItemStack itemstack)
	{
		return 0;
	}

	@Override
	public void setDamage(ItemStack itemstack, int damage)
	{
		super.setDamage(itemstack, 0);
	}

	@Override
	public boolean isDamaged(ItemStack itemstack)
	{
		return false;
	}
}