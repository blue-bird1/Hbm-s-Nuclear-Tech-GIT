package com.hbm.blocks.bomb;

import com.hbm.entity.item.EntityTNTPrimedBase;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class BlockTNT extends BlockTNTBase {

	public BlockTNT(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		world.createExplosion(entity, x, y, z, 12F, true);
	}
}
