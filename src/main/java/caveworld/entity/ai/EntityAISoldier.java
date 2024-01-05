package caveworld.entity.ai;

import java.util.List;

import caveworld.api.CaveworldAPI;
import caveworld.entity.EntityCaveman;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

public class EntityAISoldier extends EntityAIBase implements IEntitySelector
{
	public enum CombatType
	{
		SWORD,
		TOOL,
		NONE
	}

	private final EntityCaveman theSoldier;
	private final World theWorld;

	private EntityLivingBase theTarget;
	private ItemStack theWeapon;

	private int tickCounter;

	private CombatType currentType = CombatType.NONE;

	public EntityAISoldier(EntityCaveman entity)
	{
		this.theSoldier = entity;
		this.theWorld = entity.worldObj;
		this.setMutexBits(3);
	}

	public CombatType getCurrentType()
	{
		return currentType == null ? CombatType.NONE : currentType;
	}

	@Override
	public boolean shouldExecute()
	{
		ItemStack itemstack = getHeldItem();

		if (itemstack == null)
		{
			currentType = CombatType.NONE;

			return false;
		}

		theWeapon = itemstack.copy();

		if (itemstack.getItem() instanceof ItemSword)
		{
			currentType = CombatType.SWORD;
		}
		else if (itemstack.getItem() instanceof ItemTool)
		{
			currentType = CombatType.TOOL;
		}
		else
		{
			currentType = CombatType.NONE;
		}

		switch (getCurrentType())
		{
			case SWORD:
			case TOOL:
				List<EntityLivingBase> list = theWorld.selectEntitiesWithinAABB(EntityLivingBase.class, theSoldier.boundingBox.expand(20.0D, 4.0D, 20.0D), this);
				EntityLivingBase target = null;

				for (EntityLivingBase entity : list)
				{
					if (target == null)
					{
						target = entity;
					}
					else if (entity.getDistanceSqToEntity(theSoldier) < target.getDistanceSqToEntity(theSoldier))
					{
						target = entity;
					}
				}

				if (!canMoveToEntity(target))
				{
					return false;
				}

				theTarget = target;

				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean continueExecuting()
	{
		if (tickCounter > 60 || getHeldItem() == null || theSoldier.isStopped() || theWorld.difficultySetting == EnumDifficulty.PEACEFUL)
		{
			return false;
		}

		return getCurrentType() != CombatType.NONE && canMoveToEntity(theTarget);
	}

	@Override
	public void startExecuting()
	{
		theSoldier.getNavigator().clearPathEntity();
		tickCounter = 0;
	}

	@Override
	public void resetTask()
	{
		theTarget = null;
		theWeapon = null;
		theSoldier.getNavigator().clearPathEntity();
		tickCounter = 0;
	}

	@Override
	public void updateTask()
	{
		theSoldier.getLookHelper().setLookPositionWithEntity(theTarget, 10.0F, theSoldier.getVerticalFaceSpeed());

		ItemStack current = getHeldItem();

		switch (getCurrentType())
		{
			case SWORD:
			case TOOL:
				if (theSoldier.getDistanceSqToEntity(theTarget) > 3.0D)
				{
					if (theSoldier.getEntitySenses().canSee(theTarget))
					{
						theSoldier.getMoveHelper().setMoveTo(theTarget.posX, theTarget.posY, theTarget.posZ, 0.85D);
						theSoldier.getNavigator().tryMoveToEntityLiving(theTarget, 0.85D);
					}
				}
				else if (theSoldier.ticksExisted % 10 == 0)
				{
					Item item = theWeapon.getItem();
					float damage;

					if (item instanceof ItemSword)
					{
						damage = ((ItemSword)item).func_150931_i();
					}
					else if (item instanceof ItemTool)
					{
						damage = ((ItemTool)item).func_150913_i().getDamageVsEntity();
					}
					else break;

					theSoldier.swingItem();

					if (theSoldier.getCavemanType() > 0)
					{
						theTarget.hurtResistantTime = 0;

						damage *= 3.0F;
					}

					theTarget.attackEntityFrom(DamageSource.causeMobDamage(theSoldier), damage);

					theWeapon.getItem().hitEntity(current, theTarget, theSoldier);
				}

				break;
			default:
				break;
		}

		++tickCounter;
	}

	public ItemStack getHeldItem()
	{
		ItemStack item = theSoldier.getHeldItem();

		if (item == null || item.getItem() == null || item.stackSize <= 0 ||
			item.isItemStackDamageable() && item.getItemDamage() >= item.getMaxDamage())
		{
			return null;
		}

		return item;
	}

	public boolean canMoveToEntity(Entity entity)
	{
		if (entity == null || entity.isDead)
		{
			return false;
		}

		if (!theWorld.isDaytime() || CaveworldAPI.isEntityInCaves(entity))
		{
			return true;
		}

		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.boundingBox.minY);
		int z = MathHelper.floor_double(entity.posZ);

		return !theWorld.canBlockSeeTheSky(x, y, z);
	}

	@Override
	public boolean isEntityApplicable(Entity entity)
	{
		if (theWorld.difficultySetting == EnumDifficulty.PEACEFUL || !canMoveToEntity(entity) || entity instanceof EntityCaveman)
		{
			return false;
		}

		switch (theSoldier.getCavemanType())
		{
			case 1:
			case 2:
			case 3:
				return entity instanceof IMob;
			default:
				return entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode;
		}
	}
}