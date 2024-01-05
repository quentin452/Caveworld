package caveworld.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.biome.BiomeGenBase;

public interface ICaveBiome
{
	public BiomeGenBase getBiome();

	public int setGenWeight(int weight);

	public int getGenWeight();

	public BlockEntry setTerrainBlock(BlockEntry entry);

	public BlockEntry getTerrainBlock();

	public BlockEntry setTopBlock(BlockEntry entry);

	public BlockEntry getTopBlock();

	public void loadFromNBT(NBTTagCompound nbt);

	public NBTTagCompound saveToNBT();
}