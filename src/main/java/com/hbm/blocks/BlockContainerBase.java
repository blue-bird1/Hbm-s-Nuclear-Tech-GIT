package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockContainerBase extends BlockBase implements ITileEntityProvider {


    public BlockContainerBase(Material m, String s) {
        super(m, s);
        // this.isBlockContainer = true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        worldIn.removeTileEntity(pos);
    }

//    public boolean onBlockEventReceived(World world, int x, int y, int z, int eventNo, int eventArg) {
//
//        super.onBlockEventReceived(world, x, y, z, eventNo, eventArg);
//        TileEntity tileentity = world.getTileEntity(x, y, z);
//        return tileentity != null && tileentity.receiveClientEvent(eventNo, eventArg);
//    }
}