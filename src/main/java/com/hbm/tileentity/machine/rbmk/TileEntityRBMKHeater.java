package com.hbm.tileentity.machine.rbmk;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.lib.Library;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;

public class TileEntityRBMKHeater extends TileEntityRBMKSlottedBase implements IFluidHandler, ITickable {

	public FluidTank feed;
	public FluidTank steam;
	public Fluid steamType;
	
	public TileEntityRBMKHeater() {
		super(1);
	}

	@Override
	public String getName() {
		return "container.rbmkHeater";
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.HEATEX;
	}
	
	@Override
	public void update() {
		
		if(!world.isRemote) {
			PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos, feed, steam), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));

			
			double heatCap = getConversionHeat(steamType);
			Fluid type = getConversion(steamType);
			double heatProvided = this.heat - heatCap;
			
			if(heatProvided > 0) {
				
				int converted = (int)Math.floor(heatProvided / RBMKDials.getBoilerHeatConsumption(world));
				converted = Math.min(converted, feed.getFluidAmount());
				converted = Math.min(converted, steam.getCapacity()- steam.getFluidAmount());
				feed.drain(converted, true);
				steam.fill(new FluidStack(type, converted), true);
				this.heat -= converted * RBMKDials.getBoilerHeatConsumption(world);
			}
			
			fillFluidInit(steam);
		}
		
		super.update();
	}
	
	public static double getConversionHeat(Fluid type) {

		Fluid f =  getConversion(type) ;
		if(f!=null){
			return f.getTemperature();
		}
	    return 0;
	}
	
	public static Fluid getConversion(Fluid type) {
		if(type == ModForgeFluids.mug)		return ModForgeFluids.mug_hot;
		if(type == ModForgeFluids.coolant)	return ModForgeFluids.coolant_hot;
		return null;
	}


	public void fillFluidInit(FluidTank tank) {

		fillFluid(this.pos.getX(), this.pos.getY() + RBMKDials.getColumnHeight(world) + 1, this.pos.getZ(), tank);

		if(world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock() == ModBlocks.rbmk_loader) {

			fillFluid(this.pos.getX() + 1, this.pos.getY() - 1, this.pos.getZ(), tank);
			fillFluid(this.pos.getX() - 1, this.pos.getY() - 1, this.pos.getZ(), tank);
			fillFluid(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() + 1, tank);
			fillFluid(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ() - 1, tank);
			fillFluid(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ(), tank);
		}

		if(world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 2, pos.getZ())).getBlock() == ModBlocks.rbmk_loader) {

			fillFluid(this.pos.getX() + 1, this.pos.getY() - 2, this.pos.getZ(), tank);
			fillFluid(this.pos.getX() - 1, this.pos.getY() - 2, this.pos.getZ(), tank);
			fillFluid(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() + 1, tank);
			fillFluid(this.pos.getX(), this.pos.getY() - 2, this.pos.getZ() - 1, tank);
			fillFluid(this.pos.getX(), this.pos.getY() - 1, this.pos.getZ(), tank);
			fillFluid(this.pos.getX(), this.pos.getY() - 3, this.pos.getZ(), tank);
		}
	}

	public void fillFluid(int x, int y, int z, FluidTank tank) {
		FFUtils.fillFluid(this, tank, world, new BlockPos(x, y, z), tank.getCapacity());
	}

	public void setTypeForSync(Fluid type, int index) {

		if(index == 0)
			steamType = type;
			// feed.setTankType(type);
		else if(index == 1)
			steamType = type;
			// steam.setTankType(type);
	}
	


	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[]{feed.getTankProperties()[0], steam.getTankProperties()[0]};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource.getFluid() == steamType){
			feed.fill(resource, doFill);
		}
		return 0;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if(resource.getFluid() == getConversion(steamType)){
			return steam.drain(resource,doDrain);
		}
		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		return steam.drain(maxDrain,doDrain);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		feed.readFromNBT(nbt.getCompoundTag("feed"));
		steam.readFromNBT(nbt.getCompoundTag("steam"));
		steamType = FluidRegistry.getFluid(nbt.getString("steamType"));
		if (this.steamType == null) {
			this.steamType = ModForgeFluids.coolant;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setTag("feed", feed.writeToNBT(new NBTTagCompound()));
		nbt.setTag("steam", steam.writeToNBT(new NBTTagCompound()));
		nbt.setString("steamType", steamType.getName());
		return nbt;
	}
}
