package erogenousbeef.bigreactors.common.multiblock.tileentity;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.interfaces.INeighborUpdatableEntity;
import erogenousbeef.core.multiblock.MultiblockControllerBase;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.core.IC2;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityReactorPowerTap extends TileEntityReactorPart implements IEnergyProvider, IEnergyConnection, IEnergySource, INeighborUpdatableEntity {
	public IEnergyAcceptor euNetwork;
	public IEnergyReceiver rfNetwork;
	
	boolean addedEnergyNet = false;
	
	public TileEntityReactorPowerTap() {
		super();
		euNetwork = null;
		rfNetwork = null;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighborBlock) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	@Override
	public void onNeighborTileChange(IBlockAccess world, int x, int y, int z, int neighborX, int neighborY, int neighborZ) {
		if(isConnected()) {
			checkForConnections(world, x, y, z);
		}
	}

	// IMultiblockPart
	@Override
	public void onAttached(MultiblockControllerBase newController) {
		super.onAttached(newController);

		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public void onMachineAssembled(MultiblockControllerBase multiblockControllerBase) {
		super.onMachineAssembled(multiblockControllerBase);

		checkForConnections(this.worldObj, xCoord, yCoord, zCoord);
		
		// Force a connection to the power taps
		this.notifyNeighborsOfTileChange();
	}
		
	// Custom PowerTap methods
	/**
	 * Check for a world connection, if we're assembled.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	protected void checkForConnections(IBlockAccess world, int x, int y, int z) {
			boolean wasConnected = (euNetwork != null || rfNetwork != null);
			ForgeDirection out = getOutwardsDir();
			if(out == ForgeDirection.UNKNOWN) {
				wasConnected = false;
				euNetwork = null;
				rfNetwork = null;
				getReactorController().updateClient();
			}
			else {
				// See if our adjacent non-reactor coordinate has a TE
				getReactorController().updateClient();
				euNetwork = null;
				rfNetwork = null;
				TileEntity te = world.getTileEntity(x + out.offsetX, y + out.offsetY, z + out.offsetZ);
				if(!(te instanceof TileEntityReactorPowerTap)) {
					// Skip power taps, as they implement these APIs and we don't want to shit energy back and forth
					//EU
					if(te instanceof IEnergyAcceptor) {
						IEnergyAcceptor handler = (IEnergyAcceptor)te;
						if(handler.acceptsEnergyFrom(this, out.getOpposite())) {
							euNetwork = handler;
						}
					}
					//RF
					if(te instanceof IEnergyReceiver) {
						rfNetwork = (IEnergyReceiver)te;	
					}
				}
			}
		boolean isConnected = (euNetwork != null || rfNetwork != null);
		if(wasConnected != isConnected) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		getReactorController().updateClient();
	}

	@Override
	public void onChunkUnload() { //When chunk unload Tile remove from energy net.
		if (!worldObj.isRemote){
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			addedEnergyNet = false;
		}
	}
	
	@Override
	public void markDirty() { //I think this is not necessary, but just leave it here.
		super.markDirty();
		if (worldObj != null && !worldObj.isRemote) {
			if (addedEnergyNet) {
				EnergyTileUnloadEvent event = new EnergyTileUnloadEvent(this);
				MinecraftForge.EVENT_BUS.post(event);
			}
			addedEnergyNet = false;
			EnergyTileLoadEvent event = new EnergyTileLoadEvent(this);
			MinecraftForge.EVENT_BUS.post(event);
			addedEnergyNet = true;
		}
	}
	
	/** This will be called by the Reactor Controller when this tap should be providing power.
	 * @return Power units remaining after consumption.
	 */
	public int onProvidePower(int units) {
		//EU
		if (!worldObj.isRemote){
			if(!addedEnergyNet && euNetwork != null){
				MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
				addedEnergyNet = true;
			}
		}
		//RF
		
		if(rfNetwork == null) {
			return units;
		}
		
		ForgeDirection approachDirection = getOutwardsDir().getOpposite();
		int energyConsumed = rfNetwork.receiveEnergy(approachDirection, (int)units / this.getReactorController().getAttachedPowerTapsCount(), false);
		
		return units;
	}

	public boolean hasEnergyConnection() {
		return (euNetwork != null || rfNetwork != null); 
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
		if(!this.isConnected())
			return false;
		if(direction == getOutwardsDir()) {
			if(receiver instanceof IEnergyAcceptor) {
				IEnergyAcceptor handler = (IEnergyAcceptor)receiver;
				handler.acceptsEnergyFrom(receiver, direction);
				return true;
			}
		}
		return false;
	}

	@Override
	public double getOfferedEnergy() {
		if(!this.isConnected())
			return 0;
		return (this.getReactorController().getEnergyGeneratedLastTick() * BigReactors.RFtoEU) / this.getReactorController().getAttachedPowerTapsCount();
	}

	@Override
	public void drawEnergy(double amount) {}

	@Override
	public int getSourceTier() {
		return 4;
	}

	//IEnergyConnection
	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return from == getOutwardsDir();
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		if(!this.isConnected())
			return 0;
	
		if(from == getOutwardsDir()) {
			return (int) this.getReactorController().getEnergyGeneratedLastTick();
		}
	
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection arg0) {
		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection arg0) {
		return 0;
	}

}
