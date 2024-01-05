package caveworld.item;

import caveworld.api.CaverAPI;
import caveworld.core.CaveAchievementList;
import caveworld.core.Caveworld;
import caveworld.util.breaker.BreakPos;
import caveworld.util.breaker.BreakPos.NearestBreakPosComparator;
import com.google.common.collect.Lists;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class ItemOreCompass extends Item
{
	@SideOnly(Side.CLIENT)
	private static OreFinder oreFinder;
	@SideOnly(Side.CLIENT)
	public static BreakPos nearestOrePos;

	@SideOnly(Side.CLIENT)
	protected IIcon[] compassIcons;

	private static final BreakPos failedPos = new BreakPos(null, 0, 0, 0)
	{
		@Override
		public void refresh(World world, int x, int y, int z) {}

		@Override
		public boolean isPlaced()
		{
			return false;
		}

		@Override
		public Block getCurrentBlock()
		{
			return Blocks.air;
		}

		@Override
		public int getCurrentMetadata()
		{
			return 0;
		}
	};

	public ItemOreCompass(String name)
	{
		this.setUnlocalizedName(name);
		this.setTextureName("caveworld:ore_compass");
		this.setMaxStackSize(1);
		this.setCreativeTab(Caveworld.tabCaveworld);
	}

	@Override
	public void onCreated(ItemStack itemstack, World world, EntityPlayer player)
	{
		super.onCreated(itemstack, world, player);

		player.triggerAchievement(CaveAchievementList.oreFinder);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player)
	{
		resetFinder();

		return super.onItemRightClick(itemstack, world, player);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		compassIcons = new IIcon[32];

		for (int i = 0; i < compassIcons.length; ++i)
		{
			compassIcons[i] = iconRegister.registerIcon(getIconString() + "_" + i);
		}

		itemIcon = compassIcons[0];
	}

	@SideOnly(Side.CLIENT)
	protected int getCompassIconIndex(ItemStack itemstack)
	{
		Minecraft mc = FMLClientHandler.instance().getClient();

		if (mc.theWorld == null || mc.thePlayer == null)
		{
			return -1;
		}

		initFinder();

		if (nearestOrePos != null && nearestOrePos.world != null && !nearestOrePos.isPlaced())
		{
			NBTTagCompound nbt = itemstack.getTagCompound();

			if (nbt == null)
			{
				nbt = new NBTTagCompound();

				itemstack.setTagCompound(nbt);
			}

			int max = compassIcons.length;
			double angle = nbt.getDouble("angle");
			double delta = nbt.getDouble("delta");

			if (nbt.getLong("time") != mc.theWorld.getWorldTime())
			{
				nbt.setLong("time", mc.theWorld.getWorldTime());

				double dir;
				double vec;

				if (mc.theWorld.provider.dimensionId != nearestOrePos.world.provider.dimensionId)
				{
					dir = Math.random() * 360.0D;
				}
				else
				{
					if (itemstack.isOnItemFrame())
					{
						dir = Math.atan2(nearestOrePos.z + 0.5D - itemstack.getItemFrame().posZ, nearestOrePos.x + 0.5D - itemstack.getItemFrame().posX) * 180.0D / Math.PI + itemstack.getItemFrame().rotationYaw + 90.0D;
					}
					else
					{
						dir = Math.atan2(nearestOrePos.z + 0.5D - mc.thePlayer.posZ, nearestOrePos.x + 0.5D - mc.thePlayer.posX) * 180.0D / Math.PI - mc.thePlayer.rotationYaw + 90.0D;
					}
				}

				vec = dir - angle;

				while (vec < -180.0D)
				{
					vec += 360.0D;
				}

				while (vec >= 180.0)
				{
					vec -= 360.0;
				}

				if (vec >  6.0)
				{
					vec =  6.0;
				}

				if (vec < -6.0)
				{
					vec = -6.0;
				}

				delta = (delta + vec) * 0.8;
				angle += delta;

				while (angle < 0.0)
				{
					angle += 360.0;
				}

				while (angle >= 360.0)
				{
					angle -= 360.0;
				}

				nbt.setDouble("angle", angle);
				nbt.setDouble("delta", delta);
			}

			return MathHelper.floor_double(angle * max / 360.0) % max;
		}

		return -1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(ItemStack itemstack, int pass)
	{
		if (pass == 0)
		{
			int i = getCompassIconIndex(itemstack);

			if (i >= 0 && i < compassIcons.length)
			{
				return compassIcons[i];
			}
		}

		return super.getIcon(itemstack, pass);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	public int getRenderPasses(int metadata)
	{
		return 1;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean advanced)
	{
		if (player.capabilities.isCreativeMode && nearestOrePos != null)
		{
			Block block = nearestOrePos.getCurrentBlock();
			int meta = nearestOrePos.getCurrentMetadata();
			ItemStack nearest = new ItemStack(block, 1, meta);

			if (nearest != null && nearest.getItem() != null)
			{
				int x = MathHelper.floor_double(player.posX);
				int y = MathHelper.floor_double(player.posY);
				int z = MathHelper.floor_double(player.posZ);

				list.add(nearest.getDisplayName() + ": " + (int)(Math.ceil(Math.abs(nearestOrePos.getDistance(x, y, z))) - 1));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public static void initFinder()
	{
		if (oreFinder == null || !oreFinder.isAlive())
		{
			oreFinder = new OreFinder();
			oreFinder.setName("OreFinder");
			oreFinder.setDaemon(true);
			oreFinder.setPriority(Thread.MIN_PRIORITY);
			oreFinder.start();
		}
	}

	@SideOnly(Side.CLIENT)
	public static void resetFinder()
	{
		if (oreFinder != null)
		{
			oreFinder.setFinding(false);
			oreFinder = null;
		}

		nearestOrePos = null;
	}

	@SideOnly(Side.CLIENT)
	private static class OreFinder extends Thread
	{
		private final List<BreakPos> result = Lists.newArrayList();

		private long prevFindTime;
		private int findFailedCount;

		private boolean finding = true;

		public void setFinding(boolean flag)
		{
			finding = flag;
		}

		@Override
		public void run()
		{
			Minecraft mc = FMLClientHandler.instance().getClient();

			while (finding && mc != null && mc.isIntegratedServerRunning())
			{
				World world = mc.theWorld;
				EntityPlayer player = mc.thePlayer;

				if (world == null || player == null || findFailedCount > 10 && Minecraft.getSystemTime() - prevFindTime < 10000L)
				{
					break;
				}

				boolean first = false;

				if (prevFindTime <= 0)
				{
					prevFindTime = Minecraft.getSystemTime();
					first = true;
				}

				if (first || nearestOrePos != null && nearestOrePos.isPlaced() || Minecraft.getSystemTime() - prevFindTime >= 1500L)
				{
					prevFindTime = Minecraft.getSystemTime();
					result.clear();

					int findDistance = 1;
					int originX = MathHelper.floor_double(player.posX);
					int originY = MathHelper.floor_double(player.posY);
					int originZ = MathHelper.floor_double(player.posZ);

					do
					{
						if (++findDistance > 50)
						{
							break;
						}

						for (int x = originX - findDistance - 1; x <= originX + findDistance; ++x)
						{
							for (int z = originZ - findDistance - 1; z <= originZ + findDistance; ++z)
							{
								for (int y = originY - 3; y <= originY + 3; ++y)
								{
									Block block = world.getBlock(x, y, z);
									int meta = world.getBlockMetadata(x, y, z);

									if (block instanceof BlockOre || block instanceof BlockRedstoneOre || CaverAPI.getMiningPointAmount(block, meta) > 0)
									{
										result.add(new BreakPos(world, x, y, z));
									}
								}
							}
						}
					}
					while (result.isEmpty());

					if (!result.isEmpty())
					{
						Collections.sort(result, new NearestBreakPosComparator(new BreakPos(mc.theWorld, originX, originY, originZ)));

						nearestOrePos = result.get(0);
					}
				}

				if (nearestOrePos == null)
				{
					nearestOrePos = failedPos;

					++findFailedCount;
				}
				else
				{
					findFailedCount = 0;
				}
			}
		}
	}
}
