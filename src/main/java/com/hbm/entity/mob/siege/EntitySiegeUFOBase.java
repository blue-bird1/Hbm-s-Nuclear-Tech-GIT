package com.hbm.entity.mob.siege;

import com.hbm.entity.mob.EntityUFO;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.ContaminationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.List;

public abstract class EntitySiegeUFOBase extends EntitySiegeBase implements IMob {

	protected int scanCooldown;
	protected int courseChangeCooldown;
	public int hurtCooldown;
	protected Entity target;

	public static final DataParameter<BlockPos> WAYPOINT = EntityDataManager.createKey(EntitySiegeUFOBase.class, DataSerializers.BLOCK_POS);
	public EntitySiegeUFOBase(World worldIn) {
		super(worldIn);
	}


	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(WAYPOINT, new BlockPos(0,0,0));

	}

	@Override
	protected void updateAITasks() {
		if(!this.world.isRemote) {

			if(this.world.getDifficulty() == EnumDifficulty.PEACEFUL) {
				this.setDead();
				return;
			}

			if(this.hurtCooldown > 0) {
				this.hurtCooldown--;
			}
		}

		if(this.courseChangeCooldown > 0) {
			this.courseChangeCooldown--;
		}
		if(this.scanCooldown > 0) {
			this.scanCooldown--;
		}

		if(this.target != null && !this.target.isEntityAlive()) {
			this.target = null;
		}
		scanForTarget();


		if(this.courseChangeCooldown <= 0) {
			this.setCourse();
		}


		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;

		if(this.courseChangeCooldown > 0) {

			double deltaX = this.getX() - this.posX;
			double deltaY = this.getY() - this.posY;
			double deltaZ = this.getZ() - this.posZ;
			Vec3 delta = Vec3.createVectorHelper(deltaX, deltaY, deltaZ);
			double len = delta.lengthVector();
			double speed = this.target instanceof EntityPlayer ? 5D : 2D;

			if(len > 5) {
				if(isCourseTraversable(this.getX(), this.getY(), this.getZ(), len)) {
					this.motionX = delta.xCoord * speed / len;
					this.motionY = delta.yCoord * speed / len;
					this.motionZ = delta.zCoord * speed / len;
				} else {
					this.courseChangeCooldown = 0;
				}
			}
		}
		super.updateAITasks();
	}

	
	/**
	 * Standard implementation for choosing single player targets
	 * Keeps the check delay in mind and resets it too, simply call this every update
	 */
	protected void scanForTarget() {
		
		int range = getScanRange();
		
		if(this.scanCooldown <= 0) {
			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox().expand(range, range / 2, range));
			this.target = null;
			
			for(Entity entity : entities) {
				
				if(!entity.isEntityAlive() || !canAttackClass((Class<? extends EntityLivingBase>) entity.getClass()))
					continue;
				
				if(entity instanceof EntityPlayer) {
					
					if(((EntityPlayer)entity).capabilities.isCreativeMode)
						continue;
					
					if(((EntityPlayer)entity).isPotionActive(MobEffects.INVISIBILITY))
						continue;
					if(this.target == null) {
						this.target = entity;
					} else {
						if(this.getDistanceSq(entity) < this.getDistanceSq(this.target)) {
							this.target = entity;
						}
					}
				}
			}
			
			this.scanCooldown = getScanDelay();
		}
	}
	
	protected int getScanRange() {
		return 50;
	}
	
	protected int getScanDelay() {
		return 100;
	}
	
	protected boolean isCourseTraversable(double p_70790_1_, double p_70790_3_, double p_70790_5_, double p_70790_7_) {
		
		double d4 = (this.getX() - this.posX) / p_70790_7_;
		double d5 = (this.getY() - this.posY) / p_70790_7_;
		double d6 = (this.getZ() - this.posZ) / p_70790_7_;
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();

		for(int i = 1; i < p_70790_7_; ++i) {
			axisalignedbb.offset(d4, d5, d6);

			if(!this.world.getCollisionBoxes(this, axisalignedbb).isEmpty()) {
				return false;
			}
		}

		return true;
	}
	
	protected void approachPosition(double speed) {
		
		double deltaX = this.getX() - this.posX;
		double deltaY = this.getY() - this.posY;
		double deltaZ = this.getZ() - this.posZ;
		Vec3 delta = Vec3.createVectorHelper(deltaX, deltaY, deltaZ);
		double len = delta.lengthVector();
		
		if(len > 5) {
			if(this.isCourseTraversable(this.getX(), this.getY(), this.getZ(), len)) {
				this.motionX = delta.xCoord * speed / len;
				this.motionY = delta.yCoord * speed / len;
				this.motionZ = delta.zCoord * speed / len;
			} else {
				this.courseChangeCooldown = 0;
			}
		}
	}
	
	protected void setCourse() {
		
		if(this.target != null) {
			this.setCourseForTaget();
			this.courseChangeCooldown = 20 + rand.nextInt(20);
		} else {
			this.setCourseWithoutTaget();
			this.courseChangeCooldown = 60 + rand.nextInt(20);
		}
	}
	
	protected void setCourseForTaget() {

		Vec3 vec = Vec3.createVectorHelper(this.posX - this.target.posX, 0, this.posZ - this.target.posZ);

		if(rand.nextInt(3) > 0)
			vec.rotateAroundY((float)Math.PI * 2 * rand.nextFloat());

		double length = vec.lengthVector();
		double overshoot = 35;

		int wX = (int)Math.floor(this.target.posX - vec.xCoord / length * overshoot);
		int wZ = (int)Math.floor(this.target.posZ - vec.zCoord / length * overshoot);

		this.setWaypoint(wX, Math.max(this.world.getHeight(wX, wZ) + 20 + rand.nextInt(15), (int) this.target.posY + 15),  wZ);
	}
	
	protected int targetHeightOffset() {
		return 2 + rand.nextInt(2);
	}
	
	protected int wanderHeightOffset() {
		return 2 + rand.nextInt(3);
	}
	
	protected void setCourseWithoutTaget() {

		int x = (int) Math.floor(posX + rand.nextGaussian() * 5);
		int z = (int) Math.floor(posZ + rand.nextGaussian() * 5);
		this.setWaypoint(x, this.world.getHeight(x, z) + wanderHeightOffset(),  z);
	}

	public void setWaypoint(int x, int y, int z) {
		this.dataManager.set(WAYPOINT, new BlockPos(x, y, z));
	}

	public BlockPos getWaypoint(){
		return this.dataManager.get(WAYPOINT);
	}

	public int getX() {
		return this.dataManager.get(WAYPOINT).getX();
	}

	public int getY() {
		return this.dataManager.get(WAYPOINT).getY();
	}

	public int getZ() {
		return this.dataManager.get(WAYPOINT).getZ();
	}
}
