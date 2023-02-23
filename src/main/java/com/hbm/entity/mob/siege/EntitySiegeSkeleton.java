package com.hbm.entity.mob.siege;

import com.hbm.entity.projectile.EntitySiegeLaser;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntitySiegeSkeleton extends EntitySiegeBase implements IRangedAttackMob, IRadiationImmune {

	public EntitySiegeSkeleton(World world) {
		super(world);
		this.tasks.addTask(1, new EntityAISwimming(this));
	//	this.tasks.addTask(2, new EntityAIArrowAttack(this, 1.0D, 20, 60, 15.0F));
		this.tasks.addTask(3, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, true));
	}



	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(ModItems.detonator_laser));
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
		this.setEquipmentBasedOnDifficulty(difficulty);
		return super.onInitialSpawn(difficulty, livingdata);
	}

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float f) {
		
		double x = posX;
		double y = posY + this.getEyeHeight();
		double z = posZ;
		
		Vec3 vec = Vec3.createVectorHelper(target.posX - x, target.posY + target.getYOffset() + target.height * 0.5 - y, target.posZ - z).normalize();
		
		SiegeTier tier = this.getTier();
		
		for(int i = 0; i < 3; i++) {
			EntitySiegeLaser laser = new EntitySiegeLaser(world, this);
			laser.setPosition(x, y, z);
		//	laser.setThrowableHeading(vec.xCoord, vec.yCoord, vec.zCoord, 1F, i == 1 ? 0.15F : 5F);
			laser.setColor(0x808000);
			laser.setDamage(tier.damageMod);
			laser.setExplosive(tier.laserExplosive);
			laser.setBreakChance(tier.laserBreak);
			if(tier.laserIncendiary) laser.setIncendiary();
			world.spawnEntity(laser);
		}
		
		this.playSound(HBMSoundHandler.ballsLaser, 2.0F, 0.9F + rand.nextFloat() * 0.2F);
	}

	/**
	 * @param swingingArms
	 */
	@Override
	public void setSwingingArms(boolean swingingArms) {
		return;

	}
}
