package caveworld.client.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class RenderCavenicSkeleton extends RenderSkeleton
{
	private static final ResourceLocation cavenicSkeletonTexture = new ResourceLocation("caveworld", "textures/entity/cavenic_skeleton.png");

	@Override
	protected void preRenderCallback(EntitySkeleton entity, float ticks)
	{
		GL11.glScalef(1.1F, 1.1F, 1.1F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySkeleton entity)
	{
		return cavenicSkeletonTexture;
	}
}
