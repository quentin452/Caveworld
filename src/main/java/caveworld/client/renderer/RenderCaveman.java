package caveworld.client.renderer;

import caveworld.entity.EntityCaveman;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class RenderCaveman extends RenderBiped
{
	private static final ResourceLocation cavemanTextures = new ResourceLocation("caveworld", "textures/entity/caveman.png");

	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private final ModelCaveman cavemanModel;

	private double renderHealth = -1.0D;

	public RenderCaveman()
	{
		super(new ModelCaveman(), 0.45F, 1.0F);
		this.cavemanModel = (ModelCaveman)super.mainModel;
		this.setRenderPassModel(cavemanModel);
	}

	@Override
	protected void func_82421_b()
	{
		field_82423_g = new ModelCaveman();
		field_82425_h = new ModelCaveman();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return cavemanTextures;
	}


	@Override
	public void doRender(Entity entity, double par2, double par3, double par4, float par5, float par6)
	{
		super.doRender(entity, par2, par3, par4, par5, par6);

		if (entity instanceof EntityCaveman)
		{
			EntityCaveman caveman = (EntityCaveman)entity;

			if (!mc.gameSettings.hideGUI && caveman.getCavemanType() > 0 && caveman.getDistanceToEntity(mc.thePlayer) <= 20.0F)
			{
				float scale = 0.01666667F * 1.5F;
				int width = 15;
				double top = 5.0D;
				double under = top + 2.0D;

				GL11.glPushMatrix();
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glTranslatef((float)par2, (float)par3 + 2.3F, (float)par4);
				GL11.glNormal3f(0.0F, 1.0F, 0.0F);
				GL11.glRotatef(-RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
				GL11.glRotatef(RenderManager.instance.playerViewX, 1.0F, 0.0F, 0.0F);
				GL11.glScalef(-scale, -scale, scale);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glTranslatef(0.0F, (caveman.isStopped() ? 0.6F : 0.12F) / scale, 0.0F);
				GL11.glDepthMask(false);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(770, 771);

				int x = caveman.getBrightnessForRender((float)par2);
				int y = x % 65536;
				int z = x / 65536;

				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, y / 1.0F, z / 1.0F);
				Tessellator tessellator = Tessellator.instance;
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				tessellator.startDrawingQuads();
				tessellator.setColorRGBA_I(0, 115);
				tessellator.addVertex(-width - 1, top - 0.5D, 0.0D);
				tessellator.addVertex(-width - 1, under + 0.5D, 0.0D);
				tessellator.addVertex(width + 1, under + 0.5D, 0.0D);
				tessellator.addVertex(width + 1, top - 0.5D, 0.0D);
				tessellator.draw();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glDepthMask(true);

				Tessellator tessellator1 = Tessellator.instance;
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				float max = caveman.getMaxHealth();
				int health = (int)(2.0D * width * (Math.min(caveman.getHealth(), max) / max));

				if (renderHealth < 0.0D)
				{
					renderHealth = health;
				}
				else
				{
					double dh = renderHealth - health;
					double distance = Math.abs(Math.sqrt(dh * dh));

					if ((int)renderHealth < health)
					{
						if (distance > 10.0D)
						{
							renderHealth += 0.35D;
						}
						else if (distance < 1.5D)
						{
							renderHealth += 0.01D;
						}
						else
						{
							renderHealth += 0.1D;
						}
					}
					else if ((int)renderHealth > health)
					{
						if (distance > 10.0D)
						{
							renderHealth -= 0.35D;
						}
						else if (distance < 1.5D)
						{
							renderHealth -= 0.01D;
						}
						else
						{
							renderHealth -= 0.1D;
						}
					}
				}

				int color = Color.GREEN.getRGB();

				if (renderHealth < 7.0D)
				{
					color = Color.RED.getRGB();
				}
				else if (renderHealth < 15.0D)
				{
					color = Color.YELLOW.getRGB();
				}

				tessellator1.startDrawingQuads();
				tessellator1.setColorRGBA_I(color, 145);
				tessellator1.addVertex(-width, top, 0.0D);
				tessellator1.addVertex(-width, under, 0.0D);
				tessellator1.addVertex(-width + renderHealth, under, 0.0D);
				tessellator1.addVertex(-width + renderHealth, top, 0.0D);
				tessellator1.draw();
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glPopMatrix();
			}
			else
			{
				renderHealth = -1.0D;
			}
		}
	}
}
