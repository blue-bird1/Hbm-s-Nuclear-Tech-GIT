package com.hbm.entity.item;

import com.hbm.blocks.bomb.BlockTNTBase;

import com.hbm.interfaces.IBomb;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityTNTPrimedBase extends Entity implements IBomb {

	public int fuse;
	private EntityLivingBase tntPlacedBy;

	public static final DataParameter<Integer> id = EntityDataManager.createKey(EntityTNTPrimedBase.class, DataSerializers.VARINT);

	public EntityTNTPrimedBase(World world) {
		super(world);
		this.preventEntitySpawning = true;
		this.setSize(0.98F, 0.98F);

		// this.getYOffset() = this.height / 2.0F;
		this.fuse = 80;
	}

	public EntityTNTPrimedBase(World world, double x, double y, double z, EntityLivingBase entity, BlockTNTBase bomb) {
		this(world);
		this.setPosition(x, y, z);
		float f = (float) (Math.random() * Math.PI * 2.0D);
		this.motionX = (double) (-((float) Math.sin((double) f)) * 0.02F);
		this.motionY = 0.2D;
		this.motionZ = (double) (-((float) Math.cos((double) f)) * 0.02F);
		this.prevPosX = x;
		this.prevPosY = y;
		this.prevPosZ = z;
		this.tntPlacedBy = entity;
		this.getDataManager().set(id,Block.getIdFromBlock(bomb));
	}

	@Override
	protected void entityInit() {

		this.getDataManager().register(id, 0);
	}

	@Override
	public double getYOffset() {
		return this.height/2.0F;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead;
	}

	@Override
	public void onUpdate() {
		
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.04D;
		this.move(MoverType.SELF,this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98D;
		this.motionY *= 0.98D;
		this.motionZ *= 0.98D;

		if(this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
			this.motionY *= -0.5D;
		}

		if(this.fuse-- <= 0) {
			this.setDead();

			if(!this.world.isRemote) {
				this.explode(world,this.getPosition());
			}
		} else {
			this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
		}
	}



	@Override
	public void explode(World world, BlockPos pos) {
		this.getBomb().explodeEntity(world, posX, posY, posZ, this);
		// world.createExplosion(entity, x, y, z, 26F, true);
	}
	
	public BlockTNTBase getBomb() {
		return (BlockTNTBase) Block.getBlockById(this.getDataManager().get(id));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setByte("Fuse", (byte) this.fuse);
		nbt.setInteger("Tile", this.getDataManager().get(id));
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		this.fuse = nbt.getByte("Fuse");
		this.getDataManager().set(id,nbt.getInteger("Tile"));
	}

//	@Override
//	@SideOnly(Side.CLIENT)
//	public float getShadowSize() {
//		return 0.0F;
//	}
//
	public EntityLivingBase getTntPlacedBy() {
		return this.tntPlacedBy;
	}
}
