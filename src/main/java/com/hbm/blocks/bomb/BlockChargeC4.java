package com.hbm.blocks.bomb;

import java.util.List;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockChargeC4 extends BlockChargeBase {


    public BlockChargeC4(Material m, String s) {
        super(m, s);
    }

    @Override
    public void explode(World world, BlockPos pos) {

        if (!world.isRemote) {
            safe = true;
            world.setBlockToAir(pos);
            safe = false;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            ExplosionVNT xnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 15F).makeStandard();
            xnt.setBlockAllocator(new BlockAllocatorStandard(32));
            xnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop());
            xnt.explode();
        }
    }


    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
        super.addInformation(stack, player, list, ext);
        list.add("Does not drop blocks.");
    }



}
