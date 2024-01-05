package caveworld.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderCavenicZombie extends RenderZombie
{
	private static final ResourceLocation cavenicZombieTexture = new ResourceLocation("caveworld", "textures/entity/cavenic_zombie.png");

	@Override
	protected ResourceLocation getEntityTexture(EntityZombie entity)
	{
		return cavenicZombieTexture;
	}
}