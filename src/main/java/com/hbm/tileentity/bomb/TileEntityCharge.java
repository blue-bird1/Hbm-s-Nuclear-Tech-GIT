package com.hbm.tileentity.bomb;

import com.hbm.blocks.bomb.BlockChargeBase;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.NBTPacket;
import com.hbm.packet.PacketDispatcher;
import com.hbm.tileentity.INBTPacketReceiver;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;

public class TileEntityCharge extends TileEntity implements INBTPacketReceiver, ITickable {
	
	public boolean started;
	public int timer;

	@Override
	public void update() {
		int xCoord = this.getPos().getX();
		int yCoord = this.getPos().getY();
		int zCoord = this.getPos().getZ();
		if(!world.isRemote) {
			
			if(started) {
				timer--;
				
				if(timer % 20 == 0 && timer > 0)
					world.playSound(null, this.getPos(), HBMSoundHandler.fstbmbPing, SoundCategory.BLOCKS, 1.0F, 1.0F);
				
				if(timer <= 0) {
					((BlockChargeBase)this.getBlockType()).explode(world, this.getPos());
				}
			}
			
			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("timer", timer);
			data.setBoolean("started", started);
			PacketDispatcher.wrapper.sendToAllAround(new NBTPacket(data, this.getPos()), new TargetPoint(this.world.provider.getDimension(), xCoord, yCoord, zCoord, 100));
		}
	}

	@Override
	public void networkUnpack(NBTTagCompound data) {
		timer = data.getInteger("timer");
		started = data.getBoolean("started");
	}
	
	public String getMinutes() {
		
		String mins = "" + (timer / 1200);
		
		if(mins.length() == 1)
			mins = "0" + mins;
		
		return mins;
	}
	
	public String getSeconds() {
		
		String mins = "" + ((timer / 20) % 60);
		
		if(mins.length() == 1)
			mins = "0" + mins;
		
		return mins;
	}
}
