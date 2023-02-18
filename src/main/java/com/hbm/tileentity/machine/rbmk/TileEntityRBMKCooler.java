package com.hbm.tileentity.machine.rbmk;

import java.util.List;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.packet.FluidTankPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKConsole.ColumnType;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import javax.annotation.Nullable;

public class TileEntityRBMKCooler extends TileEntityRBMKSlottedBase implements IFluidHandler,ITankPacketAcceptor, IControlReceiver {

    private FluidTank tank;
    private int lastCooled;

    public TileEntityRBMKCooler() {
        super(0);

        this.tank = new FluidTank(8000);
    }

    @Override
    public void update() {
        PacketDispatcher.wrapper.sendToAllAround(new FluidTankPacket(pos,   tank), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));

        double xCoord = pos.getX();
        double yCoord = pos.getY();
        double zCoord = pos.getZ();

        if (!world.isRemote) {

            if ((int) (this.heat) > 750) {

                int heatProvided = (int) (this.heat - 750D);
                int cooling = Math.min(heatProvided, tank.getFluidAmount());

                this.heat -= cooling;
                this.tank.drain(cooling,true);

                this.lastCooled = cooling;

                if (lastCooled > 0) {
                    List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(xCoord, yCoord + 4, zCoord, xCoord + 1, yCoord + 8, zCoord + 1));

                    for (Entity e : entities) {
                        e.setFire(5);
                        e.attackEntityFrom(DamageSource.IN_FIRE, 10);
                    }
                }
            } else {
                this.lastCooled = 0;
            }

        } else {

            if (this.lastCooled > 100) {
                for (int i = 0; i < 2; i++) {
                    world.spawnParticle(EnumParticleTypes.FLAME, xCoord + 0.25 + world.rand.nextDouble() * 0.5, yCoord + 4.5, zCoord + 0.25 + world.rand.nextDouble() * 0.5, 0, 0.2, 0);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xCoord + 0.25 + world.rand.nextDouble() * 0.5, yCoord + 4.5, zCoord + 0.25 + world.rand.nextDouble() * 0.5, 0, 0.2, 0);
                }

                if (world.rand.nextInt(20) == 0)
                    world.spawnParticle(EnumParticleTypes.LAVA, xCoord + 0.25 + world.rand.nextDouble() * 0.5, yCoord + 4.5, zCoord + 0.25 + world.rand.nextDouble() * 0.5, 0, 0.0, 0);
            } else if (this.lastCooled > 50) {
                for (int i = 0; i < 2; i++) {
                    world.spawnParticle(EnumParticleTypes.CLOUD, xCoord + 0.25 + world.rand.nextDouble() * 0.5, yCoord + 4.5, zCoord + 0.25 + world.rand.nextDouble() * 0.5, world.rand.nextGaussian() * 0.05, 0.2, world.rand.nextGaussian() * 0.05);
                }
            } else if (this.lastCooled > 0) {

                if (world.getTotalWorldTime() % 2 == 0)
                    world.spawnParticle(EnumParticleTypes.CLOUD, xCoord + 0.25 + world.rand.nextDouble() * 0.5, yCoord + 4.5, zCoord + 0.25 + world.rand.nextDouble() * 0.5, 0, 0.2, 0);

            }
        }

        super.update();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        tank.readFromNBT(nbt.getCompoundTag("cryo"));
        this.lastCooled = nbt.getInteger("cooled");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        tank.writeToNBT(nbt.getCompoundTag("cryo"));
        nbt.setInteger("cooled", this.lastCooled);
        nbt.setTag("cryo",tank.writeToNBT(new NBTTagCompound()));
        return nbt;
    }

    @Override
    public ColumnType getConsoleType() {
        return ColumnType.COOLER;
    }

    @Override
    public IFluidTankProperties[] getTankProperties(){
        return new IFluidTankProperties[]{tank.getTankProperties()[0]};
    }

    @Override
    public int fill(FluidStack resource, boolean doFill){
        if(resource != null && resource.getFluid() == ModForgeFluids.cryogel){
            return tank.fill(resource, doFill);
        }
        return 0;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public void recievePacket(NBTTagCompound[] tags){
        if(tags.length == 1){
            tank.readFromNBT(tags[0]);
        }
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return Vec3.createVectorHelper(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).lengthVector() < 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        return;
    }

    @Override
    public String getName() {
        return "container.rbmkCooler";
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

    @Override
    public NBTTagCompound getNBTForConsole() {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("cryogel", this.tank.getFluidAmount());
        data.setInteger("maxCryogel", this.tank.getCapacity());
        return data;
    }

}
