package com.hbm.blocks.siege;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModBlocks;


import com.hbm.interfaces.IHasCustomModel;
import com.hbm.lib.ForgeDirection;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;


public abstract class SiegeBase extends BlockBase  {

	public static final PropertyEnum<EnumColor> meta = PropertyEnum.create("meta", EnumColor.class);
	public static enum EnumColor  implements IStringSerializable  {
		BLACK,
		RED,
		GREEN,
		BROWN,
		BLUE,
		PURPLE,
		CYAN,
		SILVER,
		GRAY,
		PINK,
		LIME,
		YELLOW,
		LIGHTBLUE,
		MAGENTA,
		ORANGE,
		WHITE;


		@Override
		public String getName() {
			return "color";
		}
	}

	public SiegeBase(Material m, String s) {
		super(m, s);
		this.setTickRandomly(true);
	}
	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, meta);
	}

	protected boolean solidNeighbors(World world, BlockPos pos) {
		
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if(block.isAir(state, world, pos) || !block.isNormalCube(state, world, pos))
			  return false;
		}
		return true;
	}
	
	protected boolean shouldReplace(Block b) {
		return b != ModBlocks.siege_circuit && b != ModBlocks.siege_internal && b != ModBlocks.siege_shield;
	}
}
