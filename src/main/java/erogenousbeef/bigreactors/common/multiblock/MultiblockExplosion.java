package erogenousbeef.bigreactors.common.multiblock;

import java.util.List;
import java.util.Random;
import java.util.Set;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.ParticleExplodeMessage;
import erogenousbeef.core.common.CoordTriplet;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class MultiblockExplosion {

	//Part of code used from WorldEdit sk89q for generation spherical explosion
	public static void reactorExplosion(World world, int x, int y, int z, int energy, int type, double fuel) {
		int px = x;
		int py = y;
		int pz = z;
		int maxY = y+20;

		int lastY = py;
		Random rand = new Random(energy);
		int size = (int)(fuel / 1000 * rand.nextDouble());
		if(size < 10)
			size = 10;
		if(size > BigReactors.reactorExplosionMaxRadius)
			size = BigReactors.reactorExplosionMaxRadius;
		if(type == 1)
			size += 4;
		int radius = (int) Math.round(size);
		int radiusSqr = (int) Math.round(size * size);
		for (int k = -radius; k <= radius; k++) {
		    int zz = k * k;
		    int az = pz + k;

		    int remaining = radiusSqr - zz;
		    int xRadius = usqrt(remaining);

		    for (int i = -xRadius; i <= xRadius; i++) {
		        int xx = i * i;
		        int ax = px + i;

		        int remainingY = remaining - xx;
		        if (remainingY < 0) continue;

		        int yRadius = usqrt(remainingY);
		        int startY = Math.max(0, py - yRadius);
		        int endY = Math.min(maxY, py + yRadius);

		        int heightY =  100;
		        if (heightY < startY) {
		            int diff = startY - heightY;
		            startY -= diff;
		            endY -= diff;
		        }
		        
		        for (int j = startY; j <= heightY; j++) {
		        	if(world.getBlock(ax, j, az) != Blocks.bedrock && type == 1 && !world.isRemote) {
		        		world.setBlockToAir(ax + (int)rand.nextGaussian(), j, az);
		        	}
		        	if(world.getBlock(ax, j, az) != Blocks.bedrock && type == 0 && !world.isRemote) {
		        		world.setBlockToAir(ax, j, az);
		        	}
		        	if((int)rand.nextGaussian() < 0 && world.isRemote) {
		        		randomDisplayTick(world, ax, j, az, rand);
		        	}
		        }
		        for (int j = heightY + 1;  j<= endY; j++) {
		        	if(world.getBlock(ax, j, az) != Blocks.bedrock && type == 1 && !world.isRemote)
		        		world.setBlockToAir(ax, j, az + (int)rand.nextGaussian());
		        	if(world.getBlock(ax, j, az) != Blocks.bedrock && type == 0 && !world.isRemote)
		        		world.setBlockToAir(ax, j, az);
		        	if((int)rand.nextGaussian() < 0 && world.isRemote) {
		        		randomDisplayTick(world, ax, j, az, rand);
		        	}
		        }
			}
		}
    	
		world.playSoundEffect(x, y, z, "random.explode", 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.6F) * 0.4F);
		
		if(!world.isRemote) {
			CommonPacketHandler.INSTANCE.sendToAllAround(new ParticleExplodeMessage(x,y,z,energy,type,(int)fuel,0), new TargetPoint(world.getWorldInfo().getVanillaDimension(), (double)x, (double)y, (double)z, radius*radius));
		}
		if(type != 1 && !world.isRemote) {
	    	reactorExplosion(world, x, y, z, energy, 1, fuel);
			List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(x-radius-10, y-radius-10, z-radius-10, x+radius+10, y+radius+100, z+radius+10));
			for(int i = 0; i < entities.size(); i++) {
				entities.get(i).attackEntityFrom(new DamageSource("BRReactorExplosion"), rand.nextFloat() + 50);
			}
		}	
	}
	
	@SideOnly(Side.CLIENT)
    private static void randomDisplayTick(World p_149734_1_, int p_149734_2_, int p_149734_3_, int p_149734_4_, Random p_149734_5_){
		for (int l = 0; l < 4; ++l){
            double d0 = (double)((float)p_149734_2_ + p_149734_5_.nextFloat());
            double d1 = (double)((float)p_149734_3_ + p_149734_5_.nextFloat());
            double d2 = (double)((float)p_149734_4_ + p_149734_5_.nextFloat());
            double d3 = 0.0D;
            double d4 = 0.0D;
            double d5 = 0.0D;
            int i1 = p_149734_5_.nextInt(2) * 2 - 1;
            d3 = ((double)p_149734_5_.nextFloat() - 0.5D) * 0.5D;
            d4 = ((double)p_149734_5_.nextFloat() - 0.5D) * 0.5D;
            d5 = ((double)p_149734_5_.nextFloat() - 0.5D) * 0.5D;
  
            d2 = (double)p_149734_4_ + 0.5D + 0.25D * (double)i1;
            d5 = (double)(p_149734_5_.nextFloat() * 2.0F * (float)i1);
            

            p_149734_1_.spawnParticle("smoke", d0, d1, d2, d3, d4, d5);
            p_149734_1_.spawnParticle("explode", d0, d1, d2, d3, d4, d5);
        }
    }
	
	 private static char[] SQRT = new char[65536];

	 static {
		 for (int i = 0; i < SQRT.length; i++) {
			 SQRT[i] = (char) Math.round(Math.sqrt(i));
	     }
	 }
	
	 private static int usqrt(int i) {
		 if (i < 65536) {
			 return SQRT[i];
	     }
		 return (int) Math.round(Math.sqrt(i));
	 }
	
	 
	 public static void turbineExplosion(World world, CoordTriplet max, CoordTriplet min, CoordTriplet reference, int energy, int rotorMass) {
			Random rand = new Random(rotorMass);
			int size = (int)(rotorMass / 100 * rand.nextDouble());
			if(size < 10)
				size = 10;
			if(size > BigReactors.turbineMaxAttackRadius)
				size = BigReactors.turbineMaxAttackRadius;
			int radius = (int) Math.round(size);
			for(int i = min.x; i <= max.x; i++) {
				for(int j = min.y; j <= max.y; j++) {
					for(int k = min.z; k <= max.z; k++) {
						if(energy == 0) {
							if(world.getBlock(i, j, k) == BigReactors.blockTurbineRotorPart||
									world.getBlock(i, j, k) == BigReactors.blockMultiblockGlass||
									world.getBlock(i, j, k) == BigReactors.blockTurbinePart){
								if(rand.nextGaussian() < 0) {
									if(!world.isRemote)
										world.setBlockToAir(i, j, k);
									if(world.isRemote)
										randomDisplayTick(world, i, j, k, rand);
								}
							}
						}else {
							if(rand.nextGaussian() < 0) {
								if(!world.isRemote)
									world.setBlockToAir(i, j, k);
								if(world.isRemote)
									randomDisplayTick(world, i, j, k, rand);
							}
						}
					}
				}
			}
	    	
			world.playSoundEffect((int)reference.x, (int)reference.y, (int)reference.z, "random.explode", 4.0F, (1.0F + (rand.nextFloat() - rand.nextFloat()) * 0.6F) * 0.4F);
			
			if(!world.isRemote) {
				CommonPacketHandler.INSTANCE.sendToAllAround(new ParticleExplodeMessage(max, min, reference, energy, rotorMass,1), new TargetPoint(world.getWorldInfo().getVanillaDimension(), (double)reference.x, (double)reference.y, (double)reference.z, radius*radius));
				List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(reference.x-radius-10, reference.y-radius-10, reference.z-radius-10, reference.x+radius+10, reference.y+radius+10, reference.z+radius+10));
				for(int i = 0; i < entities.size(); i++) {
					entities.get(i).attackEntityFrom(new DamageSource("BRTurbineExplosion"), rand.nextFloat() + 10);
				}
			}	
			
	}

}
