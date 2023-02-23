package com.hbm.blocks.bomb;



import java.util.List;
import java.util.Random;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.bomb.TileEntityCharge;

import api.hbm.block.IToolable;
import javafx.geometry.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockChargeBase extends BlockContainerBase implements IBomb, IToolable, ITooltipProvider {
	
	public static boolean safe = false;

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	static float f = 0.0625F;
	protected static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(0.0F, 10 * f, 0.0F, 1.0F, 1.0F, 1.0F);
	protected static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 6 * f, 1.0F);
	protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 10 * f, 1.0F, 1.0F, 1.0F);
	protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 6 * f);
	protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(10 * f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 6 * f, 1.0F, 1.0F);
	public BlockChargeBase(Material m, String s) {
		super(m, s);
	}



	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCharge();
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}


//	@Override
//	public boolean renderAsNormalBlock(IBlockState state) {
//
//		return false;
//	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
		if(this != ModBlocks.safe)
			super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		else
			worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand){
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune){
		return null;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {

		return world.isSideSolid(pos,side);
	}


	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else {

			TileEntityCharge charge = (TileEntityCharge) world.getTileEntity(pos);

			if(!charge.started) {

				if(player.isSneaking()) {

					if(charge.timer > 0) {
						charge.started = true;
						world.playSound(player, hitX,hitY,hitZ, HBMSoundHandler.fstbmbStart, SoundCategory.BLOCKS, 1.0F, 1.0F);

					}
				} else {

					if(charge.timer == 0) { charge.timer = 100; }
					else if(charge.timer == 100) { charge.timer = 200; }
					else if(charge.timer == 200) { charge.timer = 300; }
					else if(charge.timer == 300) { charge.timer = 600; }
					else if(charge.timer == 600) { charge.timer = 1200; }
					else if(charge.timer == 1200) { charge.timer = 3600; }
					else if(charge.timer == 3600) { charge.timer = 6000; }
					else { charge.timer = 0; }
					world.playSound(player, hitX,hitY,hitZ, HBMSoundHandler.techBoop, SoundCategory.BLOCKS, 1.0F, 1.0F);

				}

				charge.markDirty();
			}

			return false;
		}
	}


	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		ForgeDirection dir = ForgeDirection.getOrientation(state.getValue(FACING).ordinal());
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if(!world.isSideSolid(new BlockPos(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ), state.getValue(FACING))) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}




	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = state.getActualState(source, pos);
		EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);

		switch (enumfacing) {
			case UP:
				return UP_AABB;
			case DOWN:
				return DOWN_AABB;
			case EAST:
				return EAST_AABB;
			case SOUTH:
				return SOUTH_AABB;
			case WEST:
				return WEST_AABB;
			case NORTH:
				return NORTH_AABB;
			default:
				return UP_AABB;
		}
	}
	


	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		if(!safe){
			this.explode(worldIn,pos);
		}
	}



	@Override
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn) {
		this.explode(worldIn, pos);
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.YELLOW + "Right-click to change timer.");
		list.add(TextFormatting.YELLOW + "Sneak-click to arm.");
		list.add(TextFormatting.RED + "Can only be disarmed and removed with defuser.");
	}


	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
		if(tool != ToolType.DEFUSER)
			return false;

		TileEntityCharge charge = (TileEntityCharge) world.getTileEntity(new BlockPos(x, y,z));

		if(charge.started) {
			charge.started = !charge.started;
			world.playSound(player, fX,fY,fZ, HBMSoundHandler.fstbmbStart, SoundCategory.BLOCKS, 1.0F, 1.0F);
			charge.markDirty();
		} else {
			safe = true;
			this.dismantle(world, x, y, z);
			safe = false;
		}

		return true;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
