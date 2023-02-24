package com.hbm.entity.mob.siege;

import java.util.List;

import com.hbm.entity.mob.EntityUFO;
import com.hbm.entity.projectile.EntitySiegeLaser;
import com.hbm.handler.SiegeOrchestrator;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class EntitySiegeCraft extends EntitySiegeUFOBase {
	
	private int attackCooldown;
	private int beamCountdown;

	private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS));


	public static final DataParameter<Byte> Beam = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.BYTE);
	public static final DataParameter<Float> LockIn_X = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.FLOAT);
	public static final DataParameter<Float> LockIn_Y = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.FLOAT);
	public static final DataParameter<Float> LockIn_Z = EntityDataManager.createKey(EntitySiegeCraft.class, DataSerializers.FLOAT);
	public EntitySiegeCraft(World world) {
		super(world);
		this.setSize(7F, 1F);
		this.isImmuneToFire = true;
		this.ignoreFrustumCheck = true;
	}
	

	@Override
	protected void onDeathUpdate() {
		
		this.beamCountdown = 200;
		this.setBeam(false);
		
		this.motionY -= 0.05D;
		
		if(this.deathTime == 19 && !world.isRemote) {
			
			NBTTagCompound data = new NBTTagCompound();
			data.setString("type", "tinytot");
			PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, posX, posY + 0.5, posZ), new NetworkRegistry.TargetPoint(this.dimension, posX, posY, posZ, 250));
			world.playSound(null,posX, posY, posZ, HBMSoundHandler.mukeExplosion, SoundCategory.PLAYERS, 15.0F, 1.0F);
		}
		
		super.onDeathUpdate();
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(Beam, (byte)0);
		this.getDataManager().register(LockIn_X, 0f);
		this.getDataManager().register(LockIn_Y, 0f);
		this.getDataManager().register(LockIn_Z, 0f);
	}

	
	public void setBeam(boolean beam) {
		this.getDataManager().set(Beam, beam ? (byte) 1 : (byte) 0);
	}
	
	public boolean getBeam() {
		return this.getDataManager().get(Beam) == 1;
	}


	public void setLockOn(float x, float y, float z) {

		this.dataManager.set(LockIn_X,x);
		this.dataManager.set(LockIn_Y,y);
		this.dataManager.set(LockIn_Z,z);
	}
	public Vec3 getLockon() {
		return Vec3.createVectorHelper(
				this.dataManager.get(LockIn_X),
				this.dataManager.get(LockIn_Y),
                this.dataManager.get(LockIn_Z)

		);
	}
	public BlockPos getLockOn(){
		return this.dataManager.get(WAYPOINT);
	}

	@Override
	protected int getScanRange() {
		return 100;
	}

	@Override
	protected int targetHeightOffset() {
		return 7 + rand.nextInt(5);
	}

	@Override
	protected int wanderHeightOffset() {
		return 10 + rand.nextInt(2);
	}

	@Override
	protected void updateAITasks() {
		super.updateAITasks();
		if(world.isRemote){
			if(this.attackCooldown > 0) {
				this.attackCooldown--;
			}
			if(this.beamCountdown > 0) {
				this.beamCountdown--;
			}

			if(rand.nextInt(50) == 0) {

				NBTTagCompound dPart = new NBTTagCompound();
				dPart.setString("type", "tau");
				dPart.setByte("count", (byte)(2 + rand.nextInt(3)));
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(dPart, posX + rand.nextGaussian() * 2, posY + rand.nextGaussian(), posZ + rand.nextGaussian() * 2), new NetworkRegistry.TargetPoint(world.provider.getDimension(), posX, posY, posZ, 50));
			}

			boolean beam = false;

			if(this.target == null || this.beamCountdown <= 0) {
				this.beamCountdown = 300; //200 - 100: nothing, 100 - 40: update lockon, 40 - 20: fix lockon, 20 - 0: beam
			} else {

				if(this.beamCountdown >= 60 && this.beamCountdown < 120) {
					double x =  this.target.posX;
					double y = this.target.posY + this.target.height * 0.5;
					double z =  this.target.posZ;
					this.setLockOn((float) x, (float) y, (float) z);

					if(this.beamCountdown == 110) {
						world.playSound(null, this.getPosition(), HBMSoundHandler.stingerLockon,SoundCategory.HOSTILE, 2F, 0.75F);
					}
				}

				if(this.beamCountdown >= 40 && this.beamCountdown < 100) {

					Vec3 lockon = this.getLockon();
					NBTTagCompound fx = new NBTTagCompound();
					fx.setString("type", "vanillaburst");
					fx.setString("mode", "reddust");
					fx.setDouble("motion", 0.2D);
					fx.setInteger("count", 5);
					PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(fx, lockon.xCoord, lockon.yCoord, lockon.zCoord), new NetworkRegistry.TargetPoint(this.dimension, lockon.xCoord, lockon.yCoord, lockon.zCoord, 100));
				}

				if(this.beamCountdown < 40) {

					Vec3 lockon = this.getLockon();

					if(this.beamCountdown == 39) {
						world.playSound(null, lockon.xCoord, lockon.yCoord, lockon.zCoord, HBMSoundHandler.ufoBlast,SoundCategory.HOSTILE, 5.0F, 0.9F + world.rand.nextFloat() * 0.2F);
					}

					List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(lockon.xCoord, lockon.yCoord, lockon.zCoord, lockon.xCoord, lockon.yCoord, lockon.zCoord).expand(2, 2, 2));

					for(Entity e : entities) {
						if(e instanceof EntityLivingBase && (canAttackClass(((EntityLivingBase)e).getClass()))) {
							e.attackEntityFrom(ModDamageSource.causeCombineDamage(this, e), 1000F);
							e.setFire(5);

							ContaminationUtil.contaminate((EntityLivingBase)e, HazardType.RADIATION, ContaminationType.CREATIVE, 5F);
						}
					}


					NBTTagCompound data = new NBTTagCompound();
					data.setString("type", "plasmablast");
					data.setFloat("r", 0.0F);
					data.setFloat("g", 0.75F);
					data.setFloat("b", 1.0F);
					data.setFloat("pitch", -90 + rand.nextFloat() * 180);
					data.setFloat("yaw", rand.nextFloat() * 180F);
					data.setFloat("scale", 5F);
					PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, lockon.xCoord, lockon.yCoord, lockon.zCoord),  new NetworkRegistry.TargetPoint(dimension, lockon.xCoord, lockon.yCoord, lockon.zCoord, 150));
					beam = true;
				}
				this.setBeam(beam);

				if(this.attackCooldown == 0 && this.target != null) {
					this.attackCooldown = 30 + rand.nextInt(10);

					double x = posX;
					double y = posY;
					double z = posZ;

					Vec3 vec = Vec3.createVectorHelper(target.posX - x, target.posY + target.height * 0.5 - y, target.posZ - z).normalize();
					SiegeTier tier = this.getTier();

					float health = getHealth() / getMaxHealth();

					int r = (int)(0xff * (1 - health));
					int g = (int)(0xff * health);
					int b = 0;
					int color = (r << 16) | (g << 8) | b;

					for(int i = 0; i < 7; i++) {

						Vec3 copy = Vec3.createVectorHelper(vec.xCoord, vec.yCoord, vec.zCoord);

						copy.rotateAroundY((float)Math.PI / 180F * (i - 3) * 5F);

						EntitySiegeLaser laser = new EntitySiegeLaser(world, this);
						laser.setPosition(x, y, z);
					//	laser.setThrowableHeading(copy.xCoord, copy.yCoord, copy.zCoord, 1F, 0.0F);
						laser.setColor(color);
						laser.setDamage(tier.damageMod);
						laser.setBreakChance(tier.laserBreak * 2);
						if(tier.laserIncendiary) laser.setIncendiary();
						world.spawnEntity(laser);
					}

					this.playSound(HBMSoundHandler.ballsLaser, 2.0F, 1.0F);
				}
			}

			if(this.courseChangeCooldown > 0) {
				approachPosition(this.target == null ? 0.25D : 0.5D + this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 1);
			}
		}

	}


	@Override
	protected void setCourseWithoutTaget() {
		int x = (int) Math.floor(posX + rand.nextGaussian() * 15);
		int z = (int) Math.floor(posZ + rand.nextGaussian() * 15);
		this.setWaypoint(x, this.world.getHeight(x, z) + 5 + rand.nextInt(6),  z);
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
