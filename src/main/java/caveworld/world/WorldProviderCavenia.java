package caveworld.world;

import caveworld.api.CaveworldAPI;
import caveworld.network.CaveNetworkRegistry;
import caveworld.network.client.CaveAdjustMessage;
import caveworld.network.client.CaveMusicMessage;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldProviderCavenia extends WorldProviderCaveworld
{
	public static final String NAME = "Cavenia";
	public static final int TYPE = 4;
	public static final CaveniaSaveHandler saveHandler = new CaveniaSaveHandler(NAME);

	public WorldProviderCavenia()
	{
		this.dimensionId = CaveworldAPI.getCaveniaDimension();
		this.hasNoSky = true;

		saveHandler.setDimension(dimensionId);
	}

	@Override
	public float getBrightness()
	{
		return ChunkProviderCavenia.caveBrightness;
	}

	@Override
	protected void registerWorldChunkManager()
	{
		worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.deepOcean, 0.0F);
	}

	@Override
	public IChunkProvider createChunkGenerator()
	{
		return new ChunkProviderCavenia(worldObj, saveHandler.getBossType());
	}

	@Override
	public String getDimensionName()
	{
		return NAME;
	}

	@Override
	public void adjustData()
	{
		if (!worldObj.isRemote && saveHandler.getRawData() == null)
		{
			saveHandler.getData();

			CaveNetworkRegistry.sendToAll(new CaveAdjustMessage(TYPE, saveHandler));
		}
	}

	@Override
	public long getSeed()
	{
		adjustData();

		return saveHandler.getWorldSeed();
	}

	@Override
	public int getActualHeight()
	{
		return 100;
	}

	@Override
	public void calculateInitialWeather()
	{
		if (!worldObj.isRemote)
		{
			if (saveHandler.getBossAlive())
			{
				musicTime = 300;
			}
		}

		worldObj.prevRainingStrength = 0.0F;
		worldObj.rainingStrength = 0.0F;
		worldObj.prevThunderingStrength = 0.0F;
		worldObj.thunderingStrength = 0.0F;
	}

	@Override
	public void updateWeather()
	{
		if (!worldObj.isRemote)
		{
			if (saveHandler.getBossAlive() && --musicTime <= 0)
			{
				musicTime = 300;

				CaveNetworkRegistry.sendToDimension(new CaveMusicMessage("cavemusic.battle" + (worldObj.rand.nextInt(2) + 1), false), dimensionId);
			}
		}

		worldObj.prevRainingStrength = 0.0F;
		worldObj.rainingStrength = 0.0F;
		worldObj.prevThunderingStrength = 0.0F;
		worldObj.thunderingStrength = 0.0F;
	}
}