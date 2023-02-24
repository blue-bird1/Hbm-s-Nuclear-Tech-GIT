package com.hbm.render.entity.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderMovingPackage extends Render {
	
	private ItemStack dummy;

	protected RenderMovingPackage(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f1, float f2) {



		GL11.glPushMatrix();
		GL11.glTranslated(x, y - 0.0125, z);

		if(this.dummy == null) {
			this.dummy = new ItemStack(ModBlocks.crate);
		}

		EntityItem dummy = new EntityItem(entity.world, 0, 0, 0, this.dummy);
		dummy.hoverStart = 0.0F;

		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(this.dummy, dummy.world, null);
		double scale = 2;
		GL11.glScaled(scale, scale, scale);
		model = ForgeHooksClient.handleCameraTransforms(model, ItemCameraTransforms.TransformType.FIXED, false);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getMinecraft().getRenderItem().renderItem(this.dummy, model);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
		return null;
	}
}
