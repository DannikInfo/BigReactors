package erogenousbeef.bigreactors.net.message;

import java.util.HashSet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import erogenousbeef.bigreactors.common.multiblock.MultiblockExplosion;
import erogenousbeef.bigreactors.net.message.base.WorldMessageClient;
import erogenousbeef.core.common.CoordTriplet;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;

public class ParticleExplodeMessage extends WorldMessageClient {
    private int energy, type, fuel, who, rotorMass;
    private CoordTriplet max, min, reference;
    //who = 0 - reactor 1 - turbine

    public ParticleExplodeMessage() { super(); energy = 0; type = 0; fuel = 0; who = 0;}
    
    public ParticleExplodeMessage(int x, int y, int z, int energy, int type, int fuel, int who) {
    	super(x, y, z);
        this.energy = energy;
        this.type = type;
        this.fuel = fuel;
        this.who = who;
    }
    
    public ParticleExplodeMessage(CoordTriplet max, CoordTriplet min, CoordTriplet reference, int energy, int rotorMass, int who) {
    	super(reference.x, reference.y, reference.z);
        this.energy = energy;
        this.who = who;
        this.max = max;
        this.min = min;
        this.reference = reference;
        this.rotorMass = rotorMass;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    	super.fromBytes(buf);
        energy = buf.readInt();
        who = buf.readInt();
        if(who == 0) {
	        type = buf.readInt();
	        fuel = buf.readInt();
        }else if(who == 1){
        	max = new CoordTriplet(0, 0, 0);
        	min = new CoordTriplet(0, 0, 0);
        	reference = new CoordTriplet(0, 0, 0);
        	rotorMass = buf.readInt();
        	max.x = buf.readInt();
        	max.y = buf.readInt();
        	max.z = buf.readInt();

        	min.x = buf.readInt();
        	min.y = buf.readInt();
        	min.z = buf.readInt();

        	reference.x = buf.readInt();
        	reference.y = buf.readInt();
        	reference.z = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	super.toBytes(buf);
        buf.writeInt(energy);
        buf.writeInt(who);
        if(who == 0) {
	        buf.writeInt(type);
	        buf.writeInt(fuel);
        }else if(who == 1) {
        	buf.writeInt(rotorMass);
            buf.writeInt(max.x);
            buf.writeInt(max.y);
            buf.writeInt(max.z);

            buf.writeInt(min.x);
            buf.writeInt(min.y);
            buf.writeInt(min.z);

            buf.writeInt(reference.x);
            buf.writeInt(reference.y);
            buf.writeInt(reference.z);
            
        }
    }

    public static class Handler extends WorldMessageClient.Handler<ParticleExplodeMessage>{
        @Override
        protected IMessage handleMessage(ParticleExplodeMessage message, MessageContext ctx, TileEntity te) {
        	
        	if(message.who == 0)
        		MultiblockExplosion.reactorExplosion(getWorld(ctx), message.x, message.y, message.z, message.energy, message.type, message.fuel);
        	if(message.who == 1)
        		MultiblockExplosion.turbineExplosion(getWorld(ctx), message.max, message.min, message.reference, message.rotorMass, message.energy);
            
        	return null;
        }	
    }

}
