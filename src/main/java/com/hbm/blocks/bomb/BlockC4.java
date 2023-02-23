package com.hbm.blocks.bomb;

import com.hbm.entity.item.EntityTNTPrimedBase;

import com.hbm.interfaces.IBomb;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockC4 extends BlockTNTBase {

	public BlockC4(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		world.createExplosion(entity, x, y, z, 26F, true);
	}

}
