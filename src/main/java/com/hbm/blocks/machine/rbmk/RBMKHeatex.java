package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.machine.ItemForgeFluidIdentifier;
import com.hbm.tileentity.machine.rbmk.TileEntityHeatex;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKHeater;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class RBMKHeatex extends BlockContainer{

	public RBMKHeatex(Material mat, String s) {

		super(mat);
		this.setUnlocalizedName(s);
		this.setRegistryName(s);
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityHeatex();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(world.isRemote)
			return true;
		if(player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemForgeFluidIdentifier) {
			ItemForgeFluidIdentifier id = (ItemForgeFluidIdentifier) player.getHeldItem(EnumHand.MAIN_HAND).getItem();
			Fluid type = ItemForgeFluidIdentifier.getType(player.getHeldItem(EnumHand.MAIN_HAND));
			Fluid convert = TileEntityRBMKHeater.getConversion(type);
			
			if(!player.isSneaking() && convert != null) {
				
				TileEntity te = world.getTileEntity(pos);
				
				if(te instanceof TileEntityHeatex) {
					TileEntityHeatex heatex = (TileEntityHeatex) te;
					// heatex.coolantIn.setFluid(type);
					// heatex.coolantOut.setTankType(convert);
					heatex.markDirty();
					// player.sendMessage(new TextComponentString("Changed type to ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendSibling(new ChatComponentTranslation("hbmfluid." + type.getName().toLowerCase())).appendSibling(new ChatComponentText("!")));
					// player.addChatComponentMessage(new ChatComponentText("Changed type to ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)).appendSibling(new ChatComponentTranslation("hbmfluid." + type.getName().toLowerCase())).appendSibling(new ChatComponentText("!")));
				}
			}
		}
		
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
}
