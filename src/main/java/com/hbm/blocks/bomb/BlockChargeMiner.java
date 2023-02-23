package com.hbm.blocks.bomb;

import java.util.*;

import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.ExplosionNT.ExAttrib;

import com.hbm.interfaces.IBomb;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockChargeMiner extends BlockChargeBase implements IBomb {
	public BlockChargeMiner(Material m, String s) {
		super(m, s);
	}

//
//	@Override
//	public int getRenderType() {
//		return BlockChargeDynamite.renderID;
//	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
		list.add(TextFormatting.BLUE + "Will drop all blocks.");
		list.add(TextFormatting.BLUE + "Does not do damage.");
	}

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
			List<ExAttrib> attribs = Arrays.asList(ExAttrib.NOHURT, ExAttrib.ALLDROP);

			exp.addAllAttrib(attribs);
			exp.explode();
			ExplosionLarge.spawnParticles(world, x + 0.5, y + 0.5, z + 0.5, 20);

		}

	}
}
