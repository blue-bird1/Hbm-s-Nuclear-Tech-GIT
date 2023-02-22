package com.hbm.blocks.siege;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SiegeInternal extends SiegeBase {


	public SiegeInternal(Material m, String s) {
		super(m, s);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		
		//if exposed to air, harden
		if(!this.solidNeighbors(world, pos)) {
			world.setBlockState(pos, ModBlocks.siege_emergency.getDefaultState());
			return;
		}

		
		int succ = 0;
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			
			Block b = world.getBlockState(new BlockPos(pos.getX()+dir.offsetX,pos.getY()+dir.offsetY, pos.getZ()+dir.offsetZ)).getBlock();
			//if the bordering block is either an internal or a circuit, increment
			if(b == this || b == ModBlocks.siege_circuit) {
				succ++;
			}
		}
		
		//all neighbors are internals or circuits? turn into a circuit
		if(succ == 6)
			world.setBlockState(pos, ModBlocks.siege_circuit.getDefaultState());
	}
}