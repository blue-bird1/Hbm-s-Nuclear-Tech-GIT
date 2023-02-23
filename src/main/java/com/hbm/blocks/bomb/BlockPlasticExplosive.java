package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockBase;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.interfaces.IBomb;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockPlasticExplosive extends BlockBase implements IBomb {


	public BlockPlasticExplosive(Material m, String s) {
		super(m, s);
	}



	public static int getPistonOrientation(int meta) {
		return meta & 7;
	}



	public static int determineOrientation(World world, int x, int y, int z, EntityLivingBase player) {

		if(MathHelper.abs((float) player.posX - (float) x) < 2.0F && MathHelper.abs((float) player.posZ - (float) z) < 2.0F) {
			double d0 = player.posY + 1.82D - (double) player.getYOffset();
			
			if(d0 - (double) y > 2.0D) {
				return 0;
			}
			
			if((double) y - d0 > 0.0D) {
				return 1;
			}
		}
		
		int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		
		return l == 0 ? 3 : l == 1 ? 4 : l == 2 ? 2 : 5;
	}


	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if(worldIn.isBlockIndirectlyGettingPowered(pos) > 0){
			this.explode(worldIn, pos);
		}
	}

	/**
	 * @param worldIn 
	 * @param pos
	 * @param state
	 * @param placer
	 * @param stack
	 */
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		int l = determineOrientation(worldIn, pos.getX(),pos.getY(),pos.getZ(), placer);
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}


	@Override
	public void explode(World world, BlockPos pos) {
		if(!world.isRemote) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			new ExplosionNT(world, null, x + 0.5, y + 0.5, z + 0.5, 50).overrideResolution(64).explode();
			ExplosionLarge.spawnParticles(world, x, y, z, ExplosionLarge.cloudFunction(15));
		}

	}
}
