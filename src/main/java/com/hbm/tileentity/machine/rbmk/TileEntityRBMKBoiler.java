package com.hbm.tileentity.machine.rbmk;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityRBMKDebris.DebrisType;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityRBMKBoiler extends TileEntityRBMKSlottedBase implements IFluidHandler, ITankPacketAcceptor, IControlReceiver,SimpleComponent  {
	
	public FluidTank feed;
	public FluidTank steam;
	public Fluid steamType;
	
	public TileEntityRBMKBoiler() {
		super(0);
		feed = new FluidTank(10000);
		steam = new FluidTank(1000000);
		steamType = ModForgeFluids.steam;
	}

	@Override
	public String getName() {
		return "container.rbmkBoiler";
	}
	
	@Override
	public void update() {
		
		if(!world.isRemote) {
			PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos, feed, steam), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));
			NBTTagCompound type = new NBTTagCompound();
			type.setString("steamType2", steamType.getName());
			networkPack(type, 50);
			
			double heatCap = this.getHeatFromSteam(steamType);
			double heatProvided = this.heat - heatCap;
			
			if(heatProvided > 0) {
				int waterUsed = (int)Math.floor(heatProvided / RBMKDials.getBoilerHeatConsumption(world));
				waterUsed = Math.min(waterUsed, feed.getFluidAmount());
				int steamProduced = (int)Math.round((waterUsed * 100F) / getFactorFromSteam(steamType));

				feed.drain(waterUsed, true);
				steam.fill(new FluidStack(steamType, steamProduced), true); 
				
				this.heat -= waterUsed * RBMKDials.getBoilerHeatConsumption(world);
			}
			
			fillFluidInit(steam);
		}
		
		super.update();
	}
	
	public double getHeatFromSteam(Fluid type) {
		if(type == ModForgeFluids.steam){
			return 100D;
		} else if(type == ModForgeFluids.hotsteam){
			return 300D;
		} else if(type == ModForgeFluids.superhotsteam){
			return 450D;
		} else if(type == ModForgeFluids.ultrahotsteam){
			return 600D;
		} else {
			return 0D;
		}
	}
	
	public double getFactorFromSteam(Fluid type) {
		if(type == ModForgeFluids.steam){
			return 1D;
		} else if(type == ModForgeFluids.hotsteam){
			return 10D;
		} else if(type == ModForgeFluids.superhotsteam){
			return 100D;
		} else if(type == ModForgeFluids.ultrahotsteam){
			return 1000D;
		} else {
			return 0D;
		}
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
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		feed.readFromNBT(nbt.getCompoundTag("feed"));
		steam.readFromNBT(nbt.getCompoundTag("steam"));
		steamType = FluidRegistry.getFluid(nbt.getString("steamType"));
		if (this.steamType == null) {
			this.steamType = ModForgeFluids.steam;
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

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return Vec3.createVectorHelper(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).lengthVector() < 20;
	}

	@Override
	public void receiveControl(NBTTagCompound data) {
		
		if(data.hasKey("compression")) {
			int newAmount = 0;
			if (this.steamType == null) {
				this.steamType = ModForgeFluids.steam;
			}
			if(steamType == ModForgeFluids.steam){
				steamType = ModForgeFluids.hotsteam;
				newAmount = steam.getFluidAmount()/10;
			} else if(steamType == ModForgeFluids.hotsteam){
				steamType = ModForgeFluids.superhotsteam;
				newAmount = steam.getFluidAmount()/10;
			} else if(steamType == ModForgeFluids.superhotsteam){
				steamType = ModForgeFluids.ultrahotsteam;
				newAmount = steam.getFluidAmount()/10;
			} else if(steamType == ModForgeFluids.ultrahotsteam){
				steamType = ModForgeFluids.steam;
				newAmount = steam.getFluidAmount()*1000;
			}
			if(newAmount > 0){
				steam.setFluid(new FluidStack(steamType, Math.min(newAmount, steam.getCapacity())));
			} else {
				steam.setFluid(null);
			}
			
			this.markDirty();
		}
	}
	
	@Override
	public void networkUnpack(NBTTagCompound nbt){
		if(nbt.hasKey("steamType2")){
			this.steamType = FluidRegistry.getFluid(nbt.getString("steamType2"));
			if (this.steamType == null) {
				this.steamType = ModForgeFluids.steam;
			}
		} else {
			super.networkUnpack(nbt);
		}
	}
	
	@Override
	public void onMelt(int reduce) {
		
		int count = 1 + world.rand.nextInt(2);
		
		for(int i = 0; i < count; i++) {
			spawnDebris(DebrisType.BLANK);
		}
		
		super.onMelt(reduce);
	}

	@Override
	public ColumnType getConsoleType() {
		return ColumnType.BOILER;
	}

	@Override
	public NBTTagCompound getNBTForConsole() {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("water", this.feed.getFluidAmount());
		data.setInteger("maxWater", this.feed.getCapacity());
		data.setInteger("steam", this.steam.getFluidAmount());
		data.setInteger("maxSteam", this.steam.getCapacity());
		data.setString("type", steamType.getName());
		return data;
	}
	
	@Override
	public void recievePacket(NBTTagCompound[] tags){
		if(tags.length == 2){
			feed.readFromNBT(tags[0]);
			steam.readFromNBT(tags[1]);
		}
	}

	@Override
	public IFluidTankProperties[] getTankProperties(){
		return new IFluidTankProperties[]{feed.getTankProperties()[0], steam.getTankProperties()[0]};
	}

	@Override
	public int fill(FluidStack resource, boolean doFill){
		if(resource != null && resource.getFluid() == FluidRegistry.WATER){
			return feed.fill(resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain){
		if(resource != null && resource.getFluid() == steamType){
			return steam.drain(resource, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain){
		return steam.drain(maxDrain, doDrain);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing){
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing){
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
		return super.getCapability(capability, facing);
	}

	// do some opencomputer stuff
	@Override
	public String getComponentName() {
		return "rbmk_boiler";
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getHeat(Context context, Arguments args) {
		return new Object[] {heat};
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getSteam(Context context, Arguments args) {
		return new Object[] {steam.getFluidAmount()};
	}
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getSteamMax(Context context, Arguments args) {
		return new Object[] {steam.getCapacity()};
	}

	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getWater(Context context, Arguments args) {
		return new Object[] {feed.getFluidAmount()};
	}
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getWaterMax(Context context, Arguments args) {
		return new Object[] {feed.getCapacity()};
	}
}