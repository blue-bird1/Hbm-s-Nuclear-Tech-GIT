package com.hbm.entity.mob.siege;

import com.hbm.handler.SiegeOrchestrator;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.SoundEvent;

public class EntitySiegeZombie extends EntitySiegeBase implements IRadiationImmune {

	public static final DataParameter<Byte> HaveTarget = EntityDataManager.createKey(EntitySiegeZombie.class, DataSerializers.BYTE);

	public EntitySiegeZombie(World world) {
		super(world);
		// this.getNavigator().set(true);
		this.tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
		this.tasks.addTask(3, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(4, new EntityAIWander(this, 1.0D));
		this.setSize(0.6F, 1.8F);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(HaveTarget, (byte)0);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
	}
	


	@Override
	public void onUpdate() {
		super.onUpdate();
		if(!world.isRemote) {
			this.getDataManager().set(HaveTarget, (byte)1);
		}
	}

//	@Override
//	public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {
//		this.setTier(SiegeTier.tiers[rand.nextInt(SiegeTier.getLength())]);
//		return super.onSpawnWithEgg(data);
//	}
}