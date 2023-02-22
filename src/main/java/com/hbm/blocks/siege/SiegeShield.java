package com.hbm.blocks.siege;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.SiegeOrchestrator;

import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SiegeShield extends SiegeBase {


	public SiegeShield(Material m, String s) {
		super(m, s);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if(SiegeOrchestrator.siegeMobCount > SiegeOrchestrator.getExpansionThreshold(world) || !SiegeOrchestrator.enableBaseSpawning(world) || !SiegeOrchestrator.siegeEnabled(world))
			return;
		
		int succ = 0;
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			BlockPos new_pos = new BlockPos(pos.getX()+dir.offsetX,pos.getY()+dir.offsetY, pos.getZ()+dir.offsetZ);
			Block b = world.getBlockState(new_pos).getBlock();

			//if the block is already a siege block, do nothing and increment the success counter
			if(!this.shouldReplace(b)) {
				succ++;
				
			//...if not, check if a new shield can be placed, and try to do so
			} else if(this.solidNeighbors(world, new_pos)) {
				succ++;
				world.setBlockState(new_pos, state);
			}
		}
		
		//if all the blocks are siege blocks now, replace with an internal
		if(succ == 6) {
			world.setBlockState(pos, ModBlocks.siege_internal.getDefaultState());

			if(rand.nextInt(10) == 0) {
				IBlockState  above_state =  world.getBlockState(new BlockPos(pos.getX(),pos.getY()+2, pos.getZ()));
				IBlockState  surface_state =  world.getBlockState(new BlockPos(pos.getX(),pos.getY()+3, pos.getZ()));
				//if the block above the upper shield is solid and *above that* is air, place a hole
				if(above_state.getMaterial() != Material.AIR && above_state.isNormalCube() && (surface_state.getMaterial() == Material.AIR || !surface_state.isNormalCube())) {
					world.setBlockState(new BlockPos(pos.getX(),pos.getY()+2, pos.getZ()),ModBlocks.siege_hole.getDefaultState() );
				}
			}
		}
	}
}
