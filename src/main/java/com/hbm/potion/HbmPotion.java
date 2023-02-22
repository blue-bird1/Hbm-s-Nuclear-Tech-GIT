package com.hbm.potion;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.BlockTaint;
import com.hbm.capability.HbmLivingCapability;
import com.hbm.config.GeneralConfig;
import com.hbm.config.PotionConfig;
import com.hbm.entity.mob.EntityTaintedCreeper;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.RefStrings;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class HbmPotion extends Potion {
	
	public static HbmPotion taint;
	public static HbmPotion radiation;
	public static HbmPotion bang;
	public static HbmPotion mutation;
	public static HbmPotion radx;
	public static HbmPotion lead;
	public static HbmPotion radaway;
	public static HbmPotion telekinesis;
	public static HbmPotion phosphorus;
	public static HbmPotion stability;
	public static HbmPotion potionsickness;
	
	public HbmPotion(boolean isBad, int color, String name, int x, int y){
		super(isBad, color);
		this.setPotionName(name);
		this.setRegistryName(RefStrings.MODID, name);
		this.setIconIndex(x, y);
	}

	public static void init() {
		taint = registerPotion(true, 8388736, "potion.hbm_taint", 0, 0);
		radiation = registerPotion(true, 8700200, "potion.hbm_radiation", 1, 0);
		bang = registerPotion(true, 1118481, "potion.hbm_bang", 3, 0);
		mutation = registerPotion(false, 8388736, "potion.hbm_mutation", 2, 0);
		radx = registerPotion(false, 0xBB4B00, "potion.hbm_radx", 5, 0);
		lead = registerPotion(false, 0x767682, "potion.hbm_lead", 6, 0);
		radaway = registerPotion(false, 0xBB4B00, "potion.hbm_radaway", 7, 0);
		telekinesis = registerPotion(true, 0x00F3FF, "potion.hbm_telekinesis", 0, 1);
		phosphorus = registerPotion(true, 0xFFFF00, "potion.hbm_phosphorus", 1, 1);
		stability = registerPotion(false, 0xD0D0D0, "potion.hbm_stability", 2, 1);
		potionsickness = registerPotion(false, 0xff8080, "potion.hbm_potionsickness", 3, 1);
	}

	public static HbmPotion registerPotion(boolean isBad, int color, String name, int x, int y) {

	/*	if (id >= Potion.potionTypes.length) {

			Potion[] newArray = new Potion[Math.max(256, id)];
			System.arraycopy(Potion.potionTypes, 0, newArray, 0, Potion.potionTypes.length);
			
			Field field = ReflectionHelper.findField(Potion.class, new String[] { "field_76425_a", "potionTypes" });
			field.setAccessible(true);
			
			try {
				
				Field modfield = Field.class.getDeclaredField("modifiers");
				modfield.setAccessible(true);
				modfield.setInt(field, field.getModifiers() & 0xFFFFFFEF);
				field.set(null, newArray);
				
			} catch (Exception e) {
				
			}
		}*/
		
		HbmPotion effect = new HbmPotion(isBad, color, name, x, y);
		ForgeRegistries.POTIONS.register(effect);
		
		return effect;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getStatusIconIndex() {
		ResourceLocation loc = new ResourceLocation(RefStrings.MODID, "textures/gui/potions.png");
		Minecraft.getMinecraft().renderEngine.bindTexture(loc);
		return super.getStatusIconIndex();
	}

	public void performEffect(EntityLivingBase entity, int level) {

		if(this == taint) {
			if(!(entity instanceof EntityTaintedCreeper) && entity.world.rand.nextInt(80) == 0)
				entity.attackEntityFrom(ModDamageSource.taint, (level + 1));
			
			if(GeneralConfig.enableHardcoreTaint && !entity.world.isRemote) {
				
				int x = (int)(entity.posX - 1);
				int y = (int)entity.posY;
				int z = (int)(entity.posZ);
				BlockPos pos = new BlockPos(x, y, z);
				
				if(entity.world.getBlockState(pos).getBlock()
						.isReplaceable(entity.world, pos) && 
						BlockTaint.hasPosNeightbour(entity.world, pos)) {
					
					entity.world.setBlockState(pos, ModBlocks.taint.getBlockState().getBaseState().withProperty(BlockTaint.TEXTURE, 14), 2);
				}
			} 
		}
		if(this == radiation) {
			ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, (float)(level + 1F) * 0.05F);
		}
		if(this == radaway) {
			if(entity.hasCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null))
				entity.getCapability(HbmLivingCapability.EntityHbmPropsProvider.ENT_HBM_PROPS_CAP, null).decreaseRads(level+1);
		}
		if(this == bang) {
			
			entity.attackEntityFrom(ModDamageSource.bang, 1000);
			entity.setHealth(0.0F);

			if (!(entity instanceof EntityPlayer))
				entity.setDead();

			entity.world.playSound(null, new BlockPos(entity), HBMSoundHandler.laserBang, SoundCategory.AMBIENT, 100.0F, 1.0F);
			ExplosionLarge.spawnParticles(entity.world, entity.posX, entity.posY, entity.posZ, 10);
		}
		if(this == lead) {
			
			entity.attackEntityFrom(ModDamageSource.lead, (level + 1));
		}
		if(this == telekinesis) {
			
			int remaining = entity.getActivePotionEffect(this).getDuration();
			
			if(remaining > 1) {
				entity.motionY = 0.5;
			} else {
				entity.motionY = -2;
				entity.fallDistance = 50;
			}
		}
		if(this == phosphorus && !entity.world.isRemote) {
			
			entity.setFire(1);
		}
	}

	public boolean isReady(int par1, int par2) {

		if(this == taint) {

	        return par1 % 2 == 0;
		}
		if(this == radiation || this == radaway || this == telekinesis || this == phosphorus) {
			
			return true;
		}
		if(this == bang) {

			return par1 <= 10;
		}
		if(this == lead) {

			int k = 60;
	        return par1 % k == 0;
		}
		
		return false;
	}
	
}
