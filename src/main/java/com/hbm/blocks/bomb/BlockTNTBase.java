package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.blocks.generic.BlockFlammable;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.HBMSoundHandler;

import api.hbm.block.IToolable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.hbm.util.BobMathUtil.rand;

public abstract class BlockTNTBase extends BlockFlammable implements IToolable, IBomb {


	public static final PropertyBool IgniteOnBreak = PropertyBool.create("ignite_on_break");
	public BlockTNTBase(Material materialIn, String s) {
		super(Material.TNT, 15, 100,s);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, IgniteOnBreak);
	}


	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		super.onBlockAdded(worldIn, pos,state);

		if(worldIn.isBlockIndirectlyGettingPowered(pos) > 0) {
			this.onBlockDestroyedByPlayer(worldIn, pos, state);
			worldIn.setBlockToAir(pos);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if(worldIn.isBlockIndirectlyGettingPowered(pos) > 0){
			this.onBlockDestroyedByPlayer(worldIn, pos, state);
			worldIn.setBlockToAir(pos);
		}
	}


	@Override
	public int quantityDropped(Random p_149745_1_) {
		return 1;
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {

		if(!world.isRemote) {
			EntityTNTPrimedBase entity_tnt_primed = new EntityTNTPrimedBase(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, explosion.getExplosivePlacedBy(), this);
			entity_tnt_primed.fuse = world.rand.nextInt(entity_tnt_primed.fuse / 4) + entity_tnt_primed.fuse / 8;
			world.spawnEntity(entity_tnt_primed);
		}
	}


	/**
	 * @param state 
	 * @return
	 */
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {

		this.prime(world, pos, state, (EntityLivingBase) null);
	}

	public void prime(World world, BlockPos pos, IBlockState state, EntityLivingBase living) {
		if(!world.isRemote) {
			if(state.getValue(IgniteOnBreak)) {
				int x = pos.getX();
				int y = pos.getY();
				int z = pos.getZ();
				EntityTNTPrimedBase entitytntprimed = new EntityTNTPrimedBase(world, x + 0.5D, y + 0.5D, z + 0.5D, living, this);
				world.spawnEntity(entitytntprimed);
				world.playSound(null, pos, HBMSoundHandler.osiprShoot, SoundCategory.BLOCKS, 1.0F, 0.8F + (rand.nextFloat() * 0.4F));
				world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 0.8F + (rand.nextFloat() * 0.4F));
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{

		if(player.getActiveItemStack().getItem() == Items.FLINT_AND_STEEL) {
			this.prime(world, pos, state, player);
			world.setBlockToAir(pos);
			player.getActiveItemStack().damageItem(1, player);
			return true;
		} else {
			return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
		}
	}


	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if(entity instanceof EntityArrow && !world.isRemote) {
			EntityArrow entityarrow = (EntityArrow) entity;

			if(entityarrow.isBurning()) {
				this.prime(world, pos, state, entityarrow.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase) entityarrow.shootingEntity : null);
				world.setBlockToAir(pos);
			}
		}
		super.onEntityCollidedWithBlock(world,pos,state,entity);
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}
	
	public abstract void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity);


	@Override
	public IBlockState getStateFromMeta(int meta) {
		boolean on = (meta & 1) == 1;
		return this.getDefaultState().withProperty(IgniteOnBreak, on);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool){
		if(tool == ToolType.DEFUSER) {
			if(!world.isRemote) {
				world.destroyBlock(new BlockPos(x, y, z), true);
				this.dropBlockAsItem(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)), 0);
			}
			return true;
		}
//
		if(tool != ToolType.SCREWDRIVER)
			return false;

		if(!world.isRemote) {
			IBlockState state = world.getBlockState(new BlockPos(x, y, z));
			boolean meta = state.getValue(IgniteOnBreak);
			if(meta) {
				world.setBlockState(new BlockPos(x, y, z), this.getDefaultState().withProperty(IgniteOnBreak, true));
				player.sendMessage(new TextComponentTranslation("bomb.tnt_enabled").setStyle(new Style().setColor(TextFormatting.RED)));
			} else {
				world.setBlockState(new BlockPos(x, y, z), this.getDefaultState().withProperty(IgniteOnBreak, false));
				player.sendMessage(new TextComponentTranslation("bomb.tnt_disabled").setStyle(new Style().setColor(TextFormatting.GOLD)));

			}
		}
		
		return true;
	}

	@Override
	public void explode(World world, BlockPos pos) {
		// this.explodeEntity(world, pos.getX(), pos.getY(), pos.getZ(), this);
		// world.createExplosion(entity, x, y, z, 26F, true);
	}


}
