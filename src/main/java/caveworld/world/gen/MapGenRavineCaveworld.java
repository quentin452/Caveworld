package caveworld.world.gen;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenRavine;

import java.util.Random;

public class MapGenRavineCaveworld extends MapGenRavine
{
	protected final Random random = new Random();

	private final float[] parabolicField = new float[1024];

	protected boolean underCaves;

	public void generate(IChunkProvider chunkProvider, World world, int chunkX, int chunkZ, Block[] blocks, boolean flag)
	{
		underCaves = flag;

		super.func_151539_a(chunkProvider, world, chunkX, chunkZ, blocks);
	}

	@Override
	protected void func_151540_a(long ravineSeed, int chunkX, int chunkZ, Block[] blocks, double blockX, double blockY, double blockZ, float scale, float leftRightRadian, float upDownRadian, int currentY, int targetY, double scaleHeight)
	{
		random.setSeed(ravineSeed);

		int worldHeight = worldObj.getActualHeight();
		double centerX = chunkX * 16 + 8;
		double centerZ = chunkZ * 16 + 8;
		float leftRightChange = 0.0F;
		float upDownChange = 0.0F;

		if (targetY <= 0)
		{
			int blockRangeY = range * 16 - 16;
			targetY = blockRangeY - random.nextInt(blockRangeY / 4);
		}

		boolean createFinalRoom = false;

		if (currentY == -1)
		{
			currentY = targetY / 2;
			createFinalRoom = true;
		}

		float nextInterHeight = 1.0F;

		for (int y = 0; y < worldHeight; ++y)
		{
			if (y == 0 || random.nextInt(3) == 0)
			{
				nextInterHeight = 1.0F + random.nextFloat() * random.nextFloat() * 1.0F;
			}

			parabolicField[y] = nextInterHeight * nextInterHeight;
		}

		for (; currentY < targetY; ++currentY)
		{
			double roomWidth = 1.5D + MathHelper.sin(currentY * (float)Math.PI / targetY) * scale * 1.0F;
			double roomHeight = roomWidth * scaleHeight;
			roomWidth *= random.nextFloat() * 0.25D + 0.75D;
			roomHeight *= random.nextFloat() * 0.25D + 0.75D;
			float moveHorizontal = MathHelper.cos(upDownRadian);
			float moveVertical = MathHelper.sin(upDownRadian);
			blockX += MathHelper.cos(leftRightRadian) * moveHorizontal;
			blockY += moveVertical;
			blockZ += MathHelper.sin(leftRightRadian) * moveHorizontal;
			upDownRadian *= 0.7F;
			upDownRadian += upDownChange * 0.05F;
			leftRightRadian += leftRightChange * 0.05F;
			upDownChange *= 0.8F;
			leftRightChange *= 0.5F;
			upDownChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
			leftRightChange += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

			if (createFinalRoom || random.nextInt(4) != 0)
			{
				double distanceX = blockX - centerX;
				double distanceZ = blockZ - centerZ;
				double distanceY = targetY - currentY;
				double maxDistance = scale + 2.0F + 16.0F;

				if (distanceX * distanceX + distanceZ * distanceZ - distanceY * distanceY > maxDistance * maxDistance)
				{
					return;
				}

				if (blockX >= centerX - 16.0D - roomWidth * 2.0D && blockZ >= centerZ - 16.0D - roomWidth * 2.0D && blockX <= centerX + 16.0D + roomWidth * 2.0D && blockZ <= centerZ + 16.0D + roomWidth * 2.0D)
				{
					int xLow = Math.max(MathHelper.floor_double(blockX - roomWidth) - chunkX * 16 - 1, 0);
					int xHigh = Math.min(MathHelper.floor_double(blockX + roomWidth) - chunkX * 16 + 1, 16);
					int yLow = Math.max(MathHelper.floor_double(blockY - roomHeight) - 1, 1);
					int yHigh = Math.min(MathHelper.floor_double(blockY + roomHeight) + 1, worldHeight - 8);
					int zLow = Math.max(MathHelper.floor_double(blockZ - roomWidth) - chunkZ * 16 - 1, 0);
					int zHigh = Math.min(MathHelper.floor_double(blockZ + roomWidth) - chunkZ * 16 + 1, 16);

					for (int x = xLow; x < xHigh; ++x)
					{
						double xScale = (chunkX * 16 + x + 0.5D - blockX) / roomWidth;

						for (int z = zLow; z < zHigh; ++z)
						{
							double zScale = (chunkZ * 16 + z + 0.5D - blockZ) / roomWidth;
							int index = (x * 16 + z) * 256 + yHigh;

							if (xScale * xScale + zScale * zScale < 1.0D)
							{
								for (int y = yHigh - 1; y >= yLow; --y)
								{
									double yScale = (y + 0.5D - blockY) / roomHeight;

									if ((xScale * xScale + zScale * zScale) * parabolicField[y] + yScale * yScale / 6.0D < 1.0D)
									{
										digBlock(blocks, index, x, y, z, chunkX, chunkZ, false);
									}

									--index;
								}
							}
						}
					}

					if (createFinalRoom)
					{
						break;
					}
				}
			}
		}
	}

	@Override
	protected void func_151538_a(World world, int x, int z, int chunkX, int chunkZ, Block[] blocks)
	{
		if (rand.nextInt(45) == 0)
		{
			int worldHeight = world.getActualHeight();
			double blockX = x * 16 + rand.nextInt(16);
			double blockY = rand.nextInt(rand.nextInt(worldHeight / 2) + world.provider.getAverageGroundLevel() + 10);
			double blockZ = z * 16 + rand.nextInt(16);
			float leftRightRadian = rand.nextFloat() * (float)Math.PI * 2.0F;
			float upDownRadian = (rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
			float scale = (rand.nextFloat() * 2.0F + rand.nextFloat()) * 2.0F;

			if (blockY > worldHeight - 40)
			{
				blockY = world.provider.getAverageGroundLevel() + rand.nextInt(10);
			}

			func_151540_a(rand.nextLong(), chunkX, chunkZ, blocks, blockX, blockY, blockZ, scale, leftRightRadian, upDownRadian, 0, 0, 3.0D);
		}
	}

	@Override
	protected void digBlock(Block[] blocks, int index, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop)
	{
		if (underCaves)
		{
			if (y < 16)
			{
				blocks[index] = Blocks.flowing_water;
			}
			else
			{
				blocks[index] = null;
			}
		}
		else
		{
			if (y < 10)
			{
				blocks[index] = Blocks.flowing_lava;
			}
			else
			{
				blocks[index] = null;
			}
		}
	}
}
