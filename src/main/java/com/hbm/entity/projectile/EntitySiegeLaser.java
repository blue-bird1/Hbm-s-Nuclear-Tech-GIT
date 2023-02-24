package com.hbm.entity.projectile;

import com.hbm.lib.ForgeDirection;
import com.hbm.lib.ModDamageSource;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import scala.Int;

public class EntitySiegeLaser extends EntityThrowable {
	
	private float damage = 2;
	private float explosive = 0F;
	private float breakChance = 0F;
	private boolean incendiary = false;

	public static final DataParameter<Integer> Color = EntityDataManager.createKey(EntitySiegeLaser.class, DataSerializers.VARINT);

	public EntitySiegeLaser(World world) {
		super(world);
	}

	public EntitySiegeLaser(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntitySiegeLaser(World world, double x, double y, double z) {
		super(world, x, y, z);
	}
	
	@Override
	protected void entityInit() {
		this.getDataManager().register(Color, (int) 0xffffff);
	}
	
	public EntitySiegeLaser setDamage(float f) {
		this.damage = f;
		return this;
	}
	
	public EntitySiegeLaser setExplosive(float f) {
		this.explosive = f;
		return this;
	}
	
	public EntitySiegeLaser setBreakChance(float f) {
		this.breakChance = f;
		return this;
	}
	
	public EntitySiegeLaser setIncendiary() {
		this.incendiary = true;
		return this;
	}
	
	public EntitySiegeLaser setColor(int color) {
		this.getDataManager().set(Color, color);
		return this;
	}
	
	public int getColor() {
		return this.getDataManager().get(Color);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		
		if(this.ticksExisted > 60)
			this.setDead();
	}

	@Override
	protected void onImpact(RayTraceResult mop) {

		if(mop.typeOfHit == RayTraceResult.Type.ENTITY) {
			DamageSource dmg;

			if(this.getThrower() != null)
				dmg = new EntityDamageSourceIndirect("laser", this, this.getThrower());
			else
				dmg = new DamageSource("laser");
			
			if(mop.entityHit.attackEntityFrom(dmg, this.damage)) {
				this.setDead();
				
				if(this.incendiary)
					mop.entityHit.setFire(3);
				
				if(this.explosive > 0)
					this.world.newExplosion(this, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, this.explosive, this.incendiary, false);
			}
			
			
		} else if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {
			
			if(this.explosive > 0) {
				this.world.newExplosion(this, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, this.explosive, this.incendiary, false);
				
			} else if(this.incendiary) {
				ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit.ordinal());
				int x = mop.getBlockPos().getX() + dir.offsetX;
				int y = mop.getBlockPos().getY() + dir.offsetY;
				int z = mop.getBlockPos().getZ() + dir.offsetZ;
				
				if(this.world.getBlockState(new BlockPos(x, y,z)).getBlock().isReplaceable(this.world, new BlockPos(x, y,z))) {
					this.world.setBlockState(new BlockPos(x, y,z), Blocks.FIRE.getDefaultState());
				}
			}
			
			if(this.rand.nextFloat() < this.breakChance) {
				this.world.destroyBlock(mop.getBlockPos(), false);
			}

			this.setDead();
		}
	}
	
	@Override
	protected float getGravityVelocity() {
		return 0.0F;
	}


	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setFloat("damage", this.damage);
		nbt.setFloat("explosive", this.explosive);
		nbt.setFloat("breakChance", this.breakChance);
		nbt.setBoolean("incendiary", this.incendiary);
		nbt.setInteger("color", this.getColor());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.damage = nbt.getFloat("damage");
		this.explosive = nbt.getFloat("explosive");
		this.breakChance = nbt.getFloat("breakChance");
		this.incendiary = nbt.getBoolean("incendiary");
		this.setColor(nbt.getInteger("color"));
		
	}
}
