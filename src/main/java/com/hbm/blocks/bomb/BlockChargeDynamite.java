package com.hbm.blocks.bomb;

import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockChargeDynamite extends BlockChargeBase {
	public BlockChargeDynamite(Material m, String s) {
		super(m, s);
	}


//	public static int renderID = RenderingRegistry.getNextAvailableRenderId();
//
//	@Override
//	public int getRenderType() {
//		return renderID;
//	}

	/**
	 * @param world
	 * @param pos
	 */
	@Override
	public void explode(World world, BlockPos pos) {
		if(!world.isRemote) {
			safe = true;
			world.setBlockToAir(pos);
			safe = false;
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			ExplosionNT exp = new ExplosionNT(world, null, x + 0.5, y + 0.5, z + 0.5, 4F);
			exp.explode();
			ExplosionLarge.spawnParticles(world, x + 0.5, y + 0.5, z + 0.5, 20);


		}
	}
}
