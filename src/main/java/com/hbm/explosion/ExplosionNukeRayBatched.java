package com.hbm.explosion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class ExplosionNukeRayBatched {

	public HashMap<ChunkPos, List<FloatTriplet>> perChunk = new HashMap(); //for future: optimize blockmap further by using sub-chunks instead of chunks
	public List<ChunkPos> orderedChunks = new ArrayList();
	private CoordComparator comparator = new CoordComparator();
	int posX;
	int posY;
	int posZ;
	World world;

	int strength;
	int length;

	int gspNumMax;
	int gspNum;
	double gspX;
	double gspY;

	public boolean isAusf3Complete = false;

	public ExplosionNukeRayBatched(World world, int x, int y, int z, int strength, int speed, int length) {
		this.world = world;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.strength = strength;
		this.length = length;

		// Total number of points
		this.gspNumMax = (int)(2.5 * Math.PI * Math.pow(this.strength,2));
		this.gspNum = 1;

		// The beginning of the generalized spiral points
		this.gspX = Math.PI;
		this.gspY = 0.0;
	}

	private void generateGspUp(){
		if (this.gspNum < this.gspNumMax) {
			int k = this.gspNum + 1;
			double hk = -1.0 + 2.0 * (k - 1.0) / (this.gspNumMax - 1.0);
			this.gspX = Math.acos(hk);

			double prev_lon = this.gspY;
			double lon = prev_lon + 3.6 / Math.sqrt(this.gspNumMax) / Math.sqrt(1.0 - hk * hk);
			this.gspY = lon % (Math.PI * 2);
		} else {
			this.gspX = 0.0;
			this.gspY = 0.0;
		}
		this.gspNum++;
	}

	// Get Cartesian coordinates for spherical coordinates
	private Vec3 getSpherical2cartesian(){
		double dx = Math.sin(this.gspX) * Math.cos(this.gspY);
		double dz = Math.sin(this.gspX) * Math.sin(this.gspY);
		double dy = Math.cos(this.gspX);
		return Vec3.createVectorHelper(dx, dy, dz);
	}

	public void collectTip(int count) {

		int amountProcessed = 0;

		while (this.gspNumMax >= this.gspNum){
			// Get Cartesian coordinates for spherical coordinates
			Vec3 vec = this.getSpherical2cartesian();

			int length = (int)Math.ceil(strength);
			float res = strength;

			FloatTriplet lastPos = null;
			HashSet<ChunkPos> chunkCoords = new HashSet();

			for(int i = 0; i < length; i ++) {

				if(i > this.length)
					break;

				float x0 = (float) (posX + (vec.xCoord * i));
				float y0 = (float) (posY + (vec.yCoord * i));
				float z0 = (float) (posZ + (vec.zCoord * i));

				int iX = (int) Math.floor(x0);
				int iY = (int) Math.floor(y0);
				int iZ = (int) Math.floor(z0);

				double fac = 100 - ((double) i) / ((double) length) * 100;
				fac *= 0.07D;
				BlockPos pos = new BlockPos(iX, iY, iZ);
				if(!world.getBlockState(new BlockPos(iX, iY, iZ)).getMaterial().isLiquid())
					res -= Math.pow(world.getBlockState(new BlockPos(iX, iY, iZ)).getBlock().getExplosionResistance(null), 7.5D - fac);
				//else
				//	res -= Math.pow(Blocks.air.getExplosionResistance(null), 7.5D - fac); // air is 0, might want to raise that is necessary

				if(res > 0 && world.getBlockState(pos).getBlock() != Blocks.AIR) {
					lastPos = new FloatTriplet(x0, y0, z0);
					//all-air chunks don't need to be buffered at all
					ChunkPos chunkPos = new ChunkPos(iX >> 4, iZ >> 4);
					chunkCoords.add(chunkPos);
				}

				if(res <= 0 || i + 1 >= this.length) {
					break;
				}
			}
			
			for(ChunkPos pos : chunkCoords) {
				List<FloatTriplet> triplets = perChunk.get(pos);
				
				if(triplets == null) {
					triplets = new ArrayList();
					perChunk.put(pos, triplets); //we re-use the same pos instead of using individualized per-chunk ones to save on RAM
				}
				
				triplets.add(lastPos);
			}
			
			// Raise one generalized spiral points
			this.generateGspUp();

			amountProcessed++;
			if(amountProcessed >= count) {
				return;
			}
		}
		
		orderedChunks.addAll(perChunk.keySet());
		orderedChunks.sort(comparator);
		
		isAusf3Complete = true;
	}
	
	/** little comparator for roughly sorting chunks by distance to the center */
	public class CoordComparator implements Comparator<ChunkPos> {

		@Override
		public int compare(ChunkPos o1, ChunkPos o2) {

			int chunkX = ExplosionNukeRayBatched.this.posX >> 4;
			int chunkZ = ExplosionNukeRayBatched.this.posZ >> 4;

			int diff1 = Math.abs((chunkX - o1.x)) + Math.abs((chunkZ - o1.x));
			int diff2 = Math.abs((chunkX - o2.z)) + Math.abs((chunkZ - o2.z));
			
			return Integer.compare(diff1, diff2);
		}
	}

	public void processChunk() {
		
		if(this.perChunk.isEmpty()) return;
		
		ChunkPos coord = orderedChunks.get(0);
		List<FloatTriplet> list = perChunk.get(coord);
		HashSet<BlockPos> toRem = new HashSet();
		//List<BlockPos> toRem = new ArrayList();
		int chunkX = coord.x;
		int chunkZ = coord.z;
		
		int enter = (int) (Math.min(
				Math.abs(posX - (chunkX << 4)),
				Math.abs(posZ - (chunkZ << 4)))) - 16; //jump ahead to cut back on NOPs
		
		for(FloatTriplet triplet : list) {
			float x = triplet.xCoord;
			float y = triplet.yCoord;
			float z = triplet.zCoord;
			Vec3 vec = Vec3.createVectorHelper(x - this.posX, y - this.posY, z - this.posZ);
			double pX = vec.xCoord / vec.lengthVector();
			double pY = vec.yCoord / vec.lengthVector();
			double pZ = vec.zCoord / vec.lengthVector();
			
			boolean inChunk = false;
			for(int i = enter; i < vec.lengthVector(); i++) {
				int x0 = (int) Math.floor(posX + pX * i);
				int y0 = (int) Math.floor(posY + pY * i);
				int z0 = (int) Math.floor(posZ + pZ * i);
				
				if(x0 >> 4 != chunkX || z0 >> 4 != chunkZ) {
					if(inChunk) {
						break;
					} else {
						continue;
					}
				}
				
				inChunk = true;

				if(!world.isAirBlock(new BlockPos(x0, y0, z0))) {
					toRem.add(new BlockPos(x0, y0, z0));
				}
			}
		}
		
		for(BlockPos pos : toRem) {
			world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
		
		perChunk.remove(coord);
		orderedChunks.remove(0);
	}
	
	public class FloatTriplet {
		public float xCoord;
		public float yCoord;
		public float zCoord;
		
		public FloatTriplet(float x, float y, float z) {
			xCoord = x;
			yCoord = y;
			zCoord = z;
		}
	}
}
