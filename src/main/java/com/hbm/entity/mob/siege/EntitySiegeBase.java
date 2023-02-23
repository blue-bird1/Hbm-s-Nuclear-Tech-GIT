package com.hbm.entity.mob.siege;

import com.hbm.handler.SiegeOrchestrator;
import com.hbm.lib.HBMSoundHandler;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntitySiegeBase extends EntityMob {

    public static final DataParameter<Integer> Tier = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.VARINT);
    public EntitySiegeBase(World worldIn) {
        super(worldIn);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(10, new EntityAILookIdle(this));;
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true, true));

    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(Tier, 0);
    }


    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
    }

    public void setEntityAttributeByTier(SiegeTier tier){


        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier("Tier Speed Mod", tier.speedMod, 1));
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(new AttributeModifier("Tier Damage Mod", tier.damageMod, 1));
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(tier.health);
        this.setHealth(this.getMaxHealth());
    }

    public void setTier(SiegeTier tier) {
        this.getDataManager().set(Tier, tier.id);

        this.setEntityAttributeByTier(tier);
        this.setHealth(this.getMaxHealth());
    }

    public SiegeTier getTier() {
        SiegeTier tier = SiegeTier.tiers[this.getDataManager().get(Tier)];
        return tier != null ? tier : SiegeTier.CLAY;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt) {
        super.writeEntityToNBT(nbt);
        nbt.setInteger("siegeTier", this.getTier().id);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt) {
        super.readEntityFromNBT(nbt);
        this.setTier(SiegeTier.tiers[nbt.getInteger("siegeTier")]);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return HBMSoundHandler.siegeIdle;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return HBMSoundHandler.siegeHurt;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return HBMSoundHandler.siegeDeath;
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
        this.setTier(SiegeTier.tiers[rand.nextInt(SiegeTier.getLength())]);
        return super.onInitialSpawn(difficulty, livingdata);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {

        if(this.isEntityInvulnerable(source))
            return false;

        if(SiegeOrchestrator.isSiegeMob(source.getTrueSource()))
            return false;

        SiegeTier tier = this.getTier();

        if(tier.fireProof && source.isFireDamage()) {
            this.extinguish();
            return false;
        }

        //noFF can't be harmed by other mobs
        if(tier.noFriendlyFire && source instanceof EntityDamageSource && !(((EntityDamageSource) source).getTrueSource() instanceof EntityPlayer))
            return false;

        damage -= tier.dt;

        EntityPlayer player = (EntityPlayer) ((EntityDamageSource) source).getTrueSource();

        if(damage < 0) {

            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.PLAYERS, 1.0F, 1.0F);

            return false;
        }

        damage *= (1F - tier.dr);

        return super.attackEntityFrom(source, damage);
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
