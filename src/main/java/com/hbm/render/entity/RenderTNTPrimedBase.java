package com.hbm.render.entity;

import com.hbm.entity.projectile.EntityLN2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.lwjgl.opengl.GL11;

import com.hbm.entity.item.EntityTNTPrimedBase;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderTNTPrimedBase extends Render<EntityTNTPrimedBase> {
	
	// private RenderBlocks blockRenderer = new RenderBlocks();
	private BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
	public static final IRenderFactory<EntityTNTPrimedBase> FACTORY = RenderTNTPrimedBase::new;
	protected RenderTNTPrimedBase(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.5F;
	}


	public void doRender(EntityTNTPrimedBase tnt, double x, double y, double z, float f0, float f1) {
		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glRotatef(-90F, 0F, 1F, 0F);

		float f2;

		if((float) tnt.fuse - f1 + 1.0F < 10.0F) {
			f2 = 1.0F - ((float) tnt.fuse - f1 + 1.0F) / 10.0F;

			if(f2 < 0.0F) {
				f2 = 0.0F;
			}

			if(f2 > 1.0F) {
				f2 = 1.0F;
			}

			f2 *= f2;
			f2 *= f2;
			float scale = 1.0F + f2 * 0.3F;
			GL11.glScalef(scale, scale, scale);
		}

		f2 = (1.0F - ((float) tnt.fuse - f1 + 1.0F) / 100.0F) * 0.8F;
		this.bindEntityTexture(tnt);
		this.blockRenderer.renderBlockBrightness(tnt.getBomb().getDefaultState(), tnt.getBrightness());

		if(tnt.fuse / 5 % 2 == 0) {

			GL11.glScaled(1.01, 1.01, 1.01);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_DST_ALPHA);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, f2);
			// boolean prev = this.blockRenderer.;
			// this.blockRenderer.useInventoryTint = false;
			this.blockRenderer.renderBlockBrightness(tnt.getBomb().getDefaultState(), 1.0F);
			// this.blockRenderer.useInventoryTint = prev;
			
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
		}

		GL11.glPopMatrix();
	}

	/**
	 * @param entity 
	 * @return
	 */
	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityTNTPrimedBase entity) {
		return this.getEntityTexture((EntityTNTPrimedBase) entity);
	}

	//protected ResourceLocation getEntityTexture(EntityTNTPrimedBase tnt) {
	//	return TextureMap.locationBlocksTexture;
	// }
	
//	protected ResourceLocation getEntityTexture(Entity entity) {
//		return this.getEntityTexture((EntityTNTPrimedBase) entity);
//	}
	
}
