package com.hbm.blocks.bomb;

import java.util.List;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class BlockChargeSemtex extends BlockChargeBase {


	public BlockChargeSemtex(Material m, String s) {
		super(m, s);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		super.addInformation(stack, player, list, ext);
		list.add(TextFormatting.BLUE + "Will drop all blocks.");
		list.add(TextFormatting.BLUE + "Does not do damage.");
		list.add(TextFormatting.BLUE + "");
		list.add(TextFormatting.LIGHT_PURPLE + "Fortune III");
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
			ExplosionVNT xnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 10F);
			xnt.setBlockAllocator(new BlockAllocatorStandard(32));
			xnt.setBlockProcessor(new BlockProcessorStandard()
					.setAllDrop()
					.setFortune(3));
			xnt.setSFX(new ExplosionEffectStandard());
			xnt.explode();

		}

	}
}
