package caveworld.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.Random;

public interface ICaveVein
{
	public BlockEntry setBlock(BlockEntry entry);

	public BlockEntry getBlock();

	public int setGenBlockCount(int count);

	public int getGenBlockCount();

	public int setGenWeight(int weight);

	public int getGenWeight();

	public int setGenRate(int rate);

	public int getGenRate();

	public int setGenMinHeight(int height);

	public int getGenMinHeight();

	public int setGenMaxHeight(int height);

	public int getGenMaxHeight();

	public BlockEntry setGenTargetBlock(BlockEntry entry);

	public BlockEntry getGenTargetBlock();

	public int[] setGenBiomes(int[] biomes);

	public int[] getGenBiomes();

	public void generateVeins(World world, Random random, int chunkX, int chunkZ);

	public void loadFromNBT(NBTTagCompound nbt);

	public NBTTagCompound saveToNBT();
}
