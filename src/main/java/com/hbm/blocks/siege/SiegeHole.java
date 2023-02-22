package com.hbm.blocks.siege;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.BlockBase;

import com.hbm.entity.mob.siege.EntitySiegeZombie;
import com.hbm.handler.SiegeOrchestrator;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SiegeHole extends BlockBase {


	public SiegeHole(Material m, String s) {
		super(m, s);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world,pos, state);
		world.scheduleBlockUpdate(pos, this, this.tickRate(world),0);
	}

	@Override
	public int tickRate(World world) {
		return 90 + world.rand.nextInt(20);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		world.scheduleBlockUpdate(pos, this, this.tickRate(world),0);
		
		if(SiegeOrchestrator.spawnThresholdEnabled(world) && SiegeOrchestrator.siegeMobCount > SiegeOrchestrator.getSpawnThreshold(world))
			return;

		List<EntitySiegeZombie> list = world.getEntitiesWithinAABB(EntitySiegeZombie.class, new AxisAlignedBB(pos.getX() - 5, pos.getY() - 2, pos.getZ() - 5, pos.getX() + 6, pos.getY() + 3, pos.getZ() + 6));

		if(list.size() < 2) {
			EntitySiegeZombie zomb = new EntitySiegeZombie(world);
			zomb.setPositionAndRotation(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 0.0F, 0.0F);
			zomb.onSpawnWithEgg(null);
			world.spawnEntity(zomb);
		}
	}
}
