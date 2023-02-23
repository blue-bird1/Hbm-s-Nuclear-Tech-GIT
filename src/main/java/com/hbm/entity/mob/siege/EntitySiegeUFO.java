package com.hbm.entity.mob.siege;

import com.hbm.entity.projectile.EntitySiegeLaser;
import com.hbm.handler.SiegeOrchestrator;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;

import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;

public class EntitySiegeUFO extends EntitySiegeUFOBase implements IRadiationImmune {

	private int attackCooldown;
	
	public EntitySiegeUFO(World world) {
		super(world);
		this.setSize(1.5F, 1F);
	}



	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		if(!world.isRemote) {
			if(this.attackCooldown > 0) {
				this.attackCooldown--;
			}

			if(this.attackCooldown == 0 && this.target != null) {
				this.attackCooldown = 20 + rand.nextInt(5);

				double x = posX;
				double y = posY;
				double z = posZ;

				Vec3 vec = Vec3.createVectorHelper(target.posX - x, target.posY + target.height * 0.5 - y, target.posZ - z).normalize();
				SiegeTier tier = this.getTier();

				EntitySiegeLaser laser = new EntitySiegeLaser(world, this);
				laser.setPosition(x, y, z);
				//laser.setThrowableHeading(vec.xCoord, vec.yCoord, vec.zCoord, 1F, 0.15F);
				laser.setColor(0x802000);
				laser.setDamage(tier.damageMod);
				laser.setExplosive(tier.laserExplosive);
				laser.setBreakChance(tier.laserBreak);
				if(tier.laserIncendiary) laser.setIncendiary();
				world.spawnEntity(laser);
				this.playSound(HBMSoundHandler.ballsLaser, 2.0F, 1.0F);
			}
		}

		if(this.courseChangeCooldown > 0) {
			approachPosition(this.target == null ? 0.25D : 0.5D + this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 1);
		}
	}




	@Override
	protected void dropFewItems(boolean byPlayer, int fortune) {
		
		if(byPlayer) {
			for(ItemStack drop : this.getTier().dropItem) {
				this.entityDropItem(drop.copy(), 0F);
			}
		}
	}
}
