package com.hbm.blocks.siege;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SiegeCircuit extends SiegeBase {


	public SiegeCircuit(Material m, String s) {
		super(m, s);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!this.solidNeighbors(world, pos)) {
			world.setBlockState(pos, ModBlocks.siege_emergency.getDefaultState());
		}
	}

}
