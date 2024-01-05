package caveworld.entity;

import org.apache.commons.lang3.ArrayUtils;

import caveworld.api.CaveworldAPI;
import caveworld.util.CaveUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public class EntityArcherZombie extends EntityZombie implements IRangedAttackMob
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

		CaveEntityRegistry.removeSpawn(EntityArcherZombie.class, def);

		if (spawnWeight > 0)
		{
			CaveEntityRegistry.addSpawn(EntityArcherZombie.class, spawnWeight, 4, 4, biomes);
		}
	}

	private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 1.0D, 20, 60, 15.0F);
	private EntityAIAttackOnCollide aiAttackOnCollide = new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false);

	public EntityArcherZombie(World world)
	{
		super(world);
		this.tasks.taskEntries.clear();
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(4, new EntityAIAttackOnCollide(this, EntityVillager.class, 1.0D, true));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
		this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));

		if (world != null && !world.isRemote)
		{
			setCombatTask();
		}
	}

	@Override
	protected void addRandomArmor()
	{
		super.addRandomArmor();

		setCurrentItemOrArmor(0, new ItemStack(Items.bow));
	}

	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data)
	{
		data = super.onSpawnWithEgg(data);

		tasks.addTask(2, aiArrowAttack);

		return data;
	}

	public void setCombatTask()
	{
		tasks.removeTask(aiAttackOnCollide);
		tasks.removeTask(aiArrowAttack);

		ItemStack itemstack = getHeldItem();

		if (itemstack != null && itemstack.getItem() == Items.bow)
		{
			tasks.addTask(2, aiArrowAttack);
		}
		else
		{
			tasks.addTask(2, aiAttackOnCollide);
		}
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase entity, float power)
	{
		EntityArrow arrow = new EntityArrow(worldObj, this, entity, 0.45F + rand.nextFloat() * 2, 14 - worldObj.difficultySetting.getDifficultyId() * 4);

		arrow.setDamage(power * 2.0F + rand.nextGaussian() * 0.25D + worldObj.difficultySetting.getDifficultyId() * 0.11F);

		int i = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, getHeldItem());

		if (i > 0)
		{
			arrow.setDamage(arrow.getDamage() + i * 0.5D + 0.5D);
		}

		i = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, getHeldItem());

		if (i > 0)
		{
			arrow.setKnockbackStrength(i);
		}

		if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, getHeldItem()) > 0)
		{
			arrow.setFire(100);
		}

		playSound("random.bow", 1.0F, 1.0F / (rand.nextFloat() * 0.4F + 0.8F));

		if (!worldObj.isRemote)
		{
			worldObj.spawnEntityInWorld(arrow);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);

		setCombatTask();
	}

	@Override
	public void setCurrentItemOrArmor(int slot, ItemStack itemstack)
	{
		super.setCurrentItemOrArmor(slot, itemstack);

		if (!worldObj.isRemote && slot == 0)
		{
			setCombatTask();
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
		return CaveworldAPI.isEntityInCaves(this) && isValidHeight() && super.getCanSpawnHere();
	}

	@Override
	public int getMaxSpawnedInChunk()
	{
		return spawnInChunks;
	}
}