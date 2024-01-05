package caveworld.item;

import caveworld.core.Caveworld;
import caveworld.entity.EntityTorchArrow;
import caveworld.plugin.moreinventory.MIMPlugin;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;

public class ItemCavenicBow extends ItemBow implements IModeItem
{
	public enum BowMode
	{
		NORMAL,
		RAPID,
		SNIPE,
		TORCH
	}

	public long highlightStart;

	public ItemCavenicBow(String name)
	{
		super();
		this.setUnlocalizedName(name);
		this.setTextureName("caveworld:cavenic_bow");
		this.setMaxDamage(768);
		this.setCreativeTab(Caveworld.tabCaveworld);
	}

	public BowMode getMode(ItemStack itemstack)
	{
		if (itemstack.getTagCompound() == null)
		{
			return BowMode.NORMAL;
		}

		BowMode[] modes = BowMode.values();
		int mode = MathHelper.clamp_int(itemstack.getTagCompound().getInteger("Mode"), 0, modes.length - 1);

		return modes[mode];
	}

	@Override
	public long getHighlightStart()
	{
		return highlightStart;
	}

	@Override
	public void setHighlightStart(long time)
	{
		highlightStart = time;
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entity, ItemStack itemstack)
	{
		if (entity.isSneaking())
		{
			NBTTagCompound nbt = itemstack.getTagCompound();

			if (nbt == null)
			{
				itemstack.setTagCompound(new NBTTagCompound());

				nbt = itemstack.getTagCompound();
			}

			int i = nbt.getInteger("Mode");

			if (++i > BowMode.values().length - 1)
			{
				i = 0;
			}

			nbt.setInteger("Mode", i);

			if (entity.worldObj.isRemote && entity instanceof EntityPlayer)
			{
				highlightStart = System.currentTimeMillis();
			}

			entity.worldObj.playSoundAtEntity(entity, "random.click", 0.6F, 1.7F);
		}

		return super.onEntitySwing(entity, itemstack);
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer player, int useRemaining)
	{
		if (world.isRemote)
		{
			Caveworld.proxy.setDebugBoundingBox(false);
		}

		BowMode mode = getMode(itemstack);
		int charge = getMaxItemUseDuration(itemstack) - useRemaining;
		float power = charge / (mode == BowMode.SNIPE ? 100.0F : 20.0F);

		power = (power * power + power * 2.0F) / 3.0F;

		if (power < 0.1D)
		{
			return;
		}

		if (power > 1.0F)
		{
			power = 1.0F;
		}

		boolean flag = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, itemstack) > 0;
		boolean holder = MIMPlugin.hasArrowHolder(player);

		switch (mode)
		{
			case RAPID:
				if (flag || player.inventory.hasItem(Items.arrow) || holder)
				{
					EntityArrow arrow = createEntityArrow(world, player, power * 1.7F);

					int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemstack);

					if (j > 0)
					{
						arrow.setDamage(arrow.getDamage() + j * 0.5D + 0.5D);
					}

					j = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemstack);

					if (j > 0)
					{
						arrow.setKnockbackStrength(j);
					}

					if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemstack) > 0)
					{
						arrow.setFire(100);
					}

					itemstack.damageItem(1, player);
					world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + power * 0.5F);

					if (flag)
					{
						arrow.canBePickedUp = 2;
					}
					else
					{
						if (holder)
						{
							for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
							{
								ItemStack item = player.inventory.getStackInSlot(i);

								if (item != null && item.getItem() == MIMPlugin.arrowHolder && item.getMaxDamage() - item.getItemDamage() - 2 > 0)
								{
									item.damageItem(1, player);

									break;
								}
							}
						}
						else
						{
							player.inventory.consumeInventoryItem(Items.arrow);
						}
					}

					if (!world.isRemote)
					{
						world.spawnEntityInWorld(arrow);
					}
				}

				break;
			case SNIPE:
				if (flag || player.inventory.hasItem(Items.arrow) || holder)
				{
					EntityArrow arrow = createEntityArrow(world, player, power > 0.75F ? power * 10.0F : power * 2.0F);

					arrow.setDamage(3.0D);
					arrow.setIsCritical(true);

					int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemstack);

					if (j > 0)
					{
						arrow.setDamage(arrow.getDamage() + j + 0.5D);
					}

					arrow.setKnockbackStrength(10 * EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemstack));

					if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemstack) > 0)
					{
						arrow.setFire(100);
					}

					itemstack.damageItem(2, player);
					world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + power * 0.5F);

					if (flag)
					{
						arrow.canBePickedUp = 2;
					}
					else
					{
						if (holder)
						{
							for (int k = 0; k < player.inventory.getSizeInventory(); ++k)
							{
								ItemStack item = player.inventory.getStackInSlot(k);

								if (item != null && item.getItem() == MIMPlugin.arrowHolder && item.getMaxDamage() - item.getItemDamage() - 2 > 0)
								{
									item.damageItem(1, player);

									break;
								}
							}
						}
						else
						{
							player.inventory.consumeInventoryItem(Items.arrow);
						}
					}

					if (!world.isRemote)
					{
						world.spawnEntityInWorld(arrow);
					}
				}

				break;
			case TORCH:
				if (flag || player.inventory.hasItem(Items.arrow) || holder)
				{
					boolean holder2 = MIMPlugin.hasTorchHolder(player);
					boolean torch = player.capabilities.isCreativeMode || player.inventory.hasItem(Item.getItemFromBlock(Blocks.torch)) || holder2;
					EntityArrow arrow;

					if (torch)
					{
						arrow = new EntityTorchArrow(world, player, power * 1.5F);
					}
					else
					{
						arrow = createEntityArrow(world, player, power * 1.85F);
					}

					int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemstack);

					if (j > 0)
					{
						arrow.setDamage(arrow.getDamage() + j * 0.5D + 0.25D);
					}

					j = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemstack);

					if (j > 0)
					{
						arrow.setKnockbackStrength(j);
					}

					if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemstack) > 0)
					{
						arrow.setFire(100);
					}

					itemstack.damageItem(1, player);
					world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + power * 0.5F);

					if (flag)
					{
						arrow.canBePickedUp = 2;
					}
					else
					{
						if (holder)
						{
							for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
							{
								ItemStack item = player.inventory.getStackInSlot(i);

								if (item != null && item.getItem() == MIMPlugin.arrowHolder && item.getMaxDamage() - item.getItemDamage() - 2 > 0)
								{
									item.damageItem(1, player);

									break;
								}
							}
						}
						else
						{
							player.inventory.consumeInventoryItem(Items.arrow);
						}

						if (torch)
						{
							if (holder2)
							{
								for (int i = 0; i < player.inventory.getSizeInventory(); ++i)
								{
									ItemStack item = player.inventory.getStackInSlot(i);

									if (item != null && item.getItem() == MIMPlugin.torchHolder && item.getMaxDamage() - item.getItemDamage() - 2 > 0)
									{
										item.damageItem(1, player);

										break;
									}
								}
							}
							else
							{
								player.inventory.consumeInventoryItem(Item.getItemFromBlock(Blocks.torch));
							}
						}
					}

					if (!world.isRemote)
					{
						world.spawnEntityInWorld(arrow);
					}
				}

				break;
			default:
				if (flag || player.inventory.hasItem(Items.arrow) || holder)
				{
					EntityArrow arrow = createEntityArrow(world, player, power * 2.0F);

					if (power == 1.0F)
					{
						arrow.setIsCritical(true);
					}

					int j = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, itemstack);

					if (j > 0)
					{
						arrow.setDamage(arrow.getDamage() + j * 0.5D + 0.5D);
					}

					j = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, itemstack);

					if (j > 0)
					{
						arrow.setKnockbackStrength(j);
					}

					if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, itemstack) > 0)
					{
						arrow.setFire(100);
					}

					itemstack.damageItem(1, player);
					world.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + power * 0.5F);

					if (flag)
					{
						arrow.canBePickedUp = 2;
					}
					else
					{
						if (holder)
						{
							for (int k = 0; k < player.inventory.getSizeInventory(); ++k)
							{
								ItemStack item = player.inventory.getStackInSlot(k);

								if (item != null && item.getItem() == MIMPlugin.arrowHolder && item.getMaxDamage() - item.getItemDamage() - 2 > 0)
								{
									item.damageItem(1, player);

									break;
								}
							}
						}
						else
						{
							player.inventory.consumeInventoryItem(Items.arrow);
						}
					}

					if (!world.isRemote)
					{
						world.spawnEntityInWorld(arrow);
					}
				}

				break;
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
	{
		BowMode mode = getMode(itemstack);

		switch (mode)
		{
			case RAPID:
				if (player.capabilities.isCreativeMode || player.inventory.hasItem(Items.arrow) || MIMPlugin.hasArrowHolder(player))
				{
					player.setItemInUse(itemstack, 2);
					itemstack.onPlayerStoppedUsing(world, player, getMaxItemUseDuration(itemstack) / 2);
				}

				return itemstack;
			default:
				if (player.capabilities.isCreativeMode || player.inventory.hasItem(Items.arrow) || MIMPlugin.hasArrowHolder(player))
				{
					player.setItemInUse(itemstack, getMaxItemUseDuration(itemstack));
				}

				return itemstack;
		}
	}

	private EntityArrow createEntityArrow(World world, EntityLivingBase living, float power)
	{
		if (MIMPlugin.enabled())
		{
			try
			{
				return (EntityArrow)Class.forName("moreinventory.entity.EntityMIMArrow").getConstructor(World.class, EntityLivingBase.class, float.class).newInstance(world, living, power);
			}
			catch (Throwable e) {}
		}

		return new EntityArrow(world, living, power);
	}

	@Override
	public String getModeName(ItemStack itemstack)
	{
		return getMode(itemstack).name();
	}

	@Override
	public String getModeDisplayName(ItemStack itemstack)
	{
		return StatCollector.translateToLocal("caveworld.bowmode." + getModeName(itemstack).toLowerCase(Locale.ENGLISH));
	}

	@Override
	public String getModeInfomation(ItemStack itemstack)
	{
		return StatCollector.translateToLocal("caveworld.bowmode") + ": " + getModeDisplayName(itemstack);
	}

	@Override
	public EnumRarity getRarity(ItemStack itemstack)
	{
		return ItemCavenium.cavenium;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean advanced)
	{
		list.add(getModeInfomation(itemstack));

		super.addInformation(itemstack, player, list, advanced);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(ItemStack itemstack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
	{
		if (usingItem != null && usingItem.getItem() == this)
		{
			BowMode mode = getMode(itemstack);
			int i = usingItem.getMaxItemUseDuration() - useRemaining;
			boolean snipe = mode == BowMode.SNIPE;

			if (i >= (snipe ? 100 : 20))
			{
				return getItemIconForUseDuration(2);
			}

			if (i >= (snipe ? 50 : 10))
			{
				return getItemIconForUseDuration(1);
			}

			if (i > 0)
			{
				return getItemIconForUseDuration(0);
			}
		}

		return super.getIcon(itemstack, renderPass, player, usingItem, useRemaining);
	}
}
