package caveworld.entity;

import org.apache.commons.lang3.ArrayUtils;

import caveworld.api.CaveworldAPI;
import caveworld.api.ICavenicMob;
import caveworld.core.CaveAchievementList;
import caveworld.item.CaveItems;
import caveworld.util.CaveUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class EntityCavenicZombie extends EntityZombie implements ICavenicMob
{
	public static int spawnWeight;
	public static int spawnMinHeight;
	public static int spawnMaxHeight;
	public static int spawnInChunks;
	public static int[] spawnBiomes;
	public static boolean despawn;

	public static void refreshSpawn()
	{
		BiomeGenBase[] def = CaveUtils.getBiomes().toArray(new BiomeGenBase[0]);
		BiomeGenBase[] biomes = new BiomeGenBase[0];
		BiomeGenBase biome;

		for (int i : spawnBiomes)
		{
			if (i >= 0 && i < BiomeGenBase.getBiomeGenArray().length)
			{
				biome = BiomeGenBase.getBiome(i);

				if (biome != null)
				{
					biomes = ArrayUtils.add(biomes, biome);
				}
			}
		}

		if (ArrayUtils.isEmpty(biomes))
		{
			biomes = def;
		}

		CaveEntityRegistry.removeSpawn(EntityCavenicZombie.class, def);

		if (spawnWeight > 0)
		{
			CaveEntityRegistry.addSpawn(EntityCavenicZombie.class, spawnWeight, 1, 2, biomes);
		}
	}

	public EntityCavenicZombie(World world)
	{
		super(world);
		this.experienceValue = 10;
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		getEntityAttribute(field_110186_bp).setBaseValue(0.0D);
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(50.0D + 10.0D * rand.nextInt(3));
		getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(50.0D);
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(3.0D);
		getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(6.0D);
	}

	@Override
	protected void dropFewItems(boolean par1, int looting)
	{
		super.dropFewItems(par1, looting);

		entityDropItem(new ItemStack(CaveItems.cavenium, 1, rand.nextInt(2)), 0.5F);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage)
	{
		return !source.isFireDamage() && source != DamageSource.fall && super.attackEntityFrom(source, damage);
	}

	@Override
	public boolean interact(EntityPlayer player)
	{
		return false;
	}

	@Override
	public void onDeath(DamageSource source)
	{
		super.onDeath(source);

		Entity entity = source.getEntity();

		if (entity == null)
		{
			entity = source.getSourceOfDamage();
		}

		if (entity != null && entity instanceof EntityPlayer)
		{
			((EntityPlayer)entity).triggerAchievement(CaveAchievementList.cavenicZombieSlayer);
		}
	}

	@Override
	protected boolean canDespawn()
	{
		return despawn;
	}

	public boolean isValidHeight()
	{
		int y = MathHelper.floor_double(boundingBox.minY);

		return y >= spawnMinHeight && y <= spawnMaxHeight;
	}

	@Override
	public boolean getCanSpawnHere()
	{
		if (CaveworldAPI.isEntityInCaves(this) && !CaveworldAPI.isEntityInCavern(this))
		{
			return isValidHeight() && super.getCanSpawnHere() || CaveworldAPI.isEntityInCavenia(this) && rand.nextInt(10) == 0;
		}

		return false;
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return spawnInChunks;
	}

	@Override
	public boolean isVillager()
	{
		return false;
	}

	@Override
	public void setVillager(boolean flag) {}

	@Override
	protected void startConversion(int time) {}

	@Override
	public boolean isConverting()
	{
		return false;
	}

	@Override
	protected void convertToVillager() {}

	@Override
	protected int getConversionTimeBoost()
	{
		return 0;
	}
}