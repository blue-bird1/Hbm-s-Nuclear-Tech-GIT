package com.hbm.tileentity.machine.rbmk;

import java.util.ArrayList;
import java.util.List;

import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

import static com.hbm.forgefluid.FFUtils.fillFluid;

public class TileEntityHeatex extends TileEntity implements IFluidHandler, ITankPacketAcceptor, ITickable {

	public FluidTank coolantIn;
	public FluidTank coolantOut;
	public FluidTank waterIn;
	public FluidTank waterOut;
	public double heatBuffer;
	public static final double maxHeat = 10_000;
	
	public TileEntityHeatex() {
		coolantIn =		new FluidTank(1000);
		coolantOut =	new FluidTank(1000);
		waterIn =		new FluidTank(1000);
		waterOut =		new FluidTank(1000);
	}
	
	@Override
	public void update() {
		
		if(!world.isRemote) {

			/* Cool input */
			double heatCap = maxHeat - heatBuffer;
			int fillCap = coolantOut.getCapacity() - coolantOut.getFluidAmount();
			double deltaT = ModForgeFluids.coolant_hot.getTemperature() - ModForgeFluids.coolant.getTemperature();
			double heatPot = coolantIn.getFluidAmount() * getCoolantHeatCap() * deltaT;
			double heatEff = Math.min(heatCap, heatPot);
			int convertMax = (int) (heatEff / (getCoolantHeatCap() * deltaT));
			int convertEff = Math.min(convertMax, fillCap);

			coolantIn.drain(convertEff, true);
			coolantOut.fill(new FluidStack(ModForgeFluids.coolant_hot, convertEff),true);
			this.heatBuffer += convertEff * getCoolantHeatCap() * deltaT;
			
			double HEAT_PER_MB_WATER = RBMKDials.getBoilerHeatConsumption(world);
			
			/* Heat water */
			int waterCap = waterOut.getCapacity()- waterOut.getFluidAmount();
			int maxBoil = (int) Math.min(waterIn.getFluidAmount(), heatBuffer / HEAT_PER_MB_WATER);
			int boilEff = Math.min(maxBoil, waterCap);

			waterIn.drain(waterIn.getFluidAmount() - boilEff, true);
			waterOut.fill(new FluidStack(ModForgeFluids.superhotsteam, convertEff),true);
			this.heatBuffer -= boilEff * HEAT_PER_MB_WATER;

			this.fillFluidInit(coolantOut);
			this.fillFluidInit(waterOut);
		}
	}

	private static double getCoolantHeatCap() {
		return 100D;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.coolantIn.readFromNBT(nbt.getCompoundTag( "cI"));
		this.coolantOut.readFromNBT(nbt.getCompoundTag("cO"));
		this.waterIn.readFromNBT(nbt.getCompoundTag( "wI"));
		this.waterOut.readFromNBT(nbt.getCompoundTag( "wO"));
		this.heatBuffer = nbt.getDouble("heat");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setTag("cO",this.coolantIn.writeToNBT(new NBTTagCompound()));
		nbt.setTag("c0",this.coolantOut.writeToNBT(new NBTTagCompound()));
		nbt.setTag("wI",this.waterIn.writeToNBT(new NBTTagCompound()));
		nbt.setTag("w0",this.waterOut.writeToNBT(new NBTTagCompound()));
		nbt.setDouble("heat", this.heatBuffer);
		return nbt;
	}



	public void fillFluidInit(FluidTank  tank) {
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS){
			fillFluid(this.pos.getX() + dir.offsetX, this.pos.getY() + dir.offsetY, this.pos.getZ() + dir.offsetZ, tank);
		}

	}

	public void fillFluid(int x, int y, int z, FluidTank tank) {
		FFUtils.fillFluid(this, tank, world, new BlockPos(x, y, z), tank.getCapacity());
	}




	@Override
	public void recievePacket(NBTTagCompound[] tags) {
		return;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[]{coolantIn.getTankProperties()[0], coolantOut.getTankProperties()[0], waterIn.getTankProperties()[0], waterOut.getTankProperties()[0]};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if(resource != null){
			if (resource.getFluid()  == FluidRegistry.WATER){
				return waterIn.fill(resource, doFill);
			}
			if(resource.getFluid() == ModForgeFluids.coolant_hot){
				return coolantIn.fill(resource,doFill);
			}
		}
		return 0;
	}

	@Nullable
	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if(resource != null ){
			if (resource.getFluid()  == ModForgeFluids.coolant_hot){
				return  coolantOut.drain(resource, doDrain);
			}
			if(resource.getFluid() == ModForgeFluids.superhotsteam){
				return waterOut.drain(resource,doDrain);
			}
		}
		return null;
	}

	@Nullable
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if(waterOut.getFluidAmount() >0){
			return waterOut.drain(maxDrain, doDrain);
		}
		if(coolantOut.getFluidAmount() >0){
			return coolantOut.drain(maxDrain,doDrain);
		}
		return null;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
}
