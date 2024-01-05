package caveworld.util.breaker;

import caveworld.util.CaveUtils;
import com.google.common.base.Objects;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;

import java.util.Comparator;

public class BreakPos implements Comparable
{
	public World world;
	public int x;
	public int y;
	public int z;
	public Block prevBlock;
	public int prevMeta;

	public BreakPos() {}

	public BreakPos(World world, int x, int y, int z)
	{
		this.refresh(world, x, y, z);
	}

	public BreakPos(BreakPos pos)
	{
		this.refresh(pos.world, pos.x, pos.y, pos.z);
	}

	public void refresh(World world, int x, int y, int z)
	{
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.prevBlock = getCurrentBlock();
		this.prevMeta = getCurrentMetadata();
	}

	public void clear()
	{
		world = null;
		x = 0;
		y = 0;
		z = 0;
		prevBlock = null;
		prevMeta = 0;
	}

	public boolean isPlaced()
	{
		return getCurrentBlock() != prevBlock || getCurrentMetadata() != prevMeta;
	}

	public void doBreak(EntityPlayer player)
	{
		Block block = getCurrentBlock();
		int meta = getCurrentMetadata();
		boolean flag = !player.capabilities.isCreativeMode;

		if (flag)
		{
			world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(block) + (meta << 12));
		}

		block.onBlockHarvested(world, x, y, z, meta, player);

		if (block.removedByPlayer(world, player, x, y, z, true))
		{
			block.onBlockDestroyedByPlayer(world, x, y, z, meta);

			if (flag)
			{
				block.harvestBlock(world, player, x, y, z, meta);
			}

			BreakEvent event = new BreakEvent(x, y, z, world, block, meta, player);

			if (!MinecraftForge.EVENT_BUS.post(event) && flag)
			{
				block.dropXpOnBlockBreak(world, x, y, z, event.getExpToDrop());
			}

			if (flag)
			{
				player.getCurrentEquippedItem().damageItem(1, player);
			}
		}

		if (player instanceof EntityPlayerMP)
		{
			((EntityPlayerMP)player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, world));
		}
	}

	public Block getCurrentBlock()
	{
		return world.getBlock(x, y, z);
	}

	public int getCurrentMetadata()
	{
		return world.getBlockMetadata(x, y, z);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj == null || !(obj instanceof BreakPos))
		{
			return false;
		}

		BreakPos pos = (BreakPos)obj;

		return world.provider.dimensionId == pos.world.provider.dimensionId && x == pos.x && y == pos.y && z == pos.z;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(world.provider.dimensionId, x, y, z);
	}

	@Override
	public int compareTo(Object obj)
	{
		BreakPos pos = (BreakPos)obj;
		int i = Integer.compare(world.provider.dimensionId, pos.world.provider.dimensionId);

		if (i == 0)
		{
			ChunkCoordinates coord1 = new ChunkCoordinates(x, y, z);
			ChunkCoordinates coord2 = new ChunkCoordinates(pos.x, pos.y, pos.z);

			i = coord1.compareTo(coord2);

			if (i == 0)
			{
				i = CaveUtils.blockComparator.compare(prevBlock, pos.prevBlock);

				if (i == 0)
				{
					i = Integer.compare(prevMeta, pos.prevMeta);
				}
			}
		}

		return i;
	}

	public int compareWithOrigin(BreakPos pos, BreakPos origin)
	{
		if (pos == null || origin == null)
		{
			return 1;
		}

		return Integer.compare(Math.abs(origin.getDistanceSq(this)), Math.abs(origin.getDistanceSq(pos)));
	}

	public double getDistance(int x, int y, int z)
	{
		return Math.sqrt(getDistanceSq(x, y, z));
	}

	public int getDistanceSq(int x, int y, int z)
	{
		int distX = this.x - x;
		int distY = this.y - y;
		int distZ = this.z - z;

		return distX * distX + distY * distY + distZ * distZ;
	}

	public double getDistance(BreakPos pos)
	{
		return Math.sqrt(getDistanceSq(pos));
	}

	public int getDistanceSq(BreakPos pos)
	{
		return getDistanceSq(pos.x, pos.y, pos.z);
	}

	public static class NearestBreakPosComparator implements Comparator<BreakPos>
	{
		private final BreakPos originPos;

		public NearestBreakPosComparator(BreakPos origin)
		{
			this.originPos = origin;
		}

		@Override
		public int compare(BreakPos o1, BreakPos o2)
		{
			int i = o1.compareWithOrigin(o2, originPos);

			if (i == 0)
			{
				i = o1.compareTo(o2);
			}

			return i;
		}
	}
}
