package caveworld.entity;

import org.apache.commons.lang3.ArrayUtils;

import caveworld.api.CaveworldAPI;
import caveworld.api.ICavenicMob;
import caveworld.core.CaveAchievementList;
import caveworld.item.CaveItems;
import caveworld.util.CaveUtils;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class EntityCavenicCreeper extends EntityCreeper implements ICavenicMob
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

		CaveEntityRegistry.removeSpawn(EntityCavenicCreeper.class, def);

		if (spawnWeight > 0)
		{
			CaveEntityRegistry.addSpawn(EntityCavenicCreeper.class, spawnWeight, 1, 1, biomes);
		}
	}

	protected int fuseTime = 15;
	protected int explosionRadius = 5;

	public EntityCavenicCreeper(World world)
	{
		super(world);
		this.experienceValue = 10;
		this.applyCustomValues();
	}

	protected void applyCustomValues()
	{
		ObfuscationReflectionHelper.setPrivateValue(EntityCreeper.class, this, fuseTime, "fuseTime", "field_82225_f");
		ObfuscationReflectionHelper.setPrivateValue(EntityCreeper.class, this, explosionRadius, "explosionRadius", "field_82226_g");
	}

	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();

		getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30.0D + 10.0D * rand.nextInt(3));
		getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(2.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2D);
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data)
	{
		if (!worldObj.isRemote && rand.nextInt(50) == 0)
		{
			EntityMasterCavenicCreeper master = new EntityMasterCavenicCreeper(worldObj);
			master.setLocationAndAngles(posX, posY, posZ, rotationYaw, rotationPitch);
			master.onSpawnWithEgg(null);

			worldObj.spawnEntityInWorld(master);
			setDead();

			return data;
		}

		return super.onSpawnWithEgg(data);
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
			((EntityPlayer)entity).triggerAchievement(CaveAchievementList.cavenicCreeperSlayer);
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
}