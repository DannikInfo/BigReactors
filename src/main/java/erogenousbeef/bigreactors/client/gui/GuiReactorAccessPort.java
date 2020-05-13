package erogenousbeef.bigreactors.client.gui;

import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorAccessPort;
import erogenousbeef.bigreactors.gui.BeefGuiIconManager;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.ReactorAccessPortChangeDirectionMessage;
import erogenousbeef.bigreactors.net.message.multiblock.ReactorCommandEjectToPortMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GuiReactorAccessPort extends BeefGuiBase {
	private TileEntityReactorAccessPort _port;
	
	protected BeefGuiLabel inventoryLabel;
	
	protected GuiIconButton ejectFuel;
	protected GuiIconButton ejectWaste;
	
	protected GuiIconButton btnInlet;
	protected GuiIconButton btnOutlet;
	
	public GuiReactorAccessPort(Container container, TileEntityReactorAccessPort accessPort) {
		super(container);
		
		_port = accessPort;
	}
	
	@Override
	public void initGui() {
		super.initGui();

		ejectFuel = new GuiIconButton(2, guiLeft + xSize - 97, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("fuelEject"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Eject_Fuel"), "", StatCollector.translateToLocal("Ejects_fuel_contained_in_the"), StatCollector.translateToLocal("reactor_placing_ingots_in_the"), StatCollector.translateToLocal("reactors_access_ports"), "", StatCollector.translateToLocal("SHIFT_Dump_excess_fuel")});
		ejectWaste = new GuiIconButton(3, guiLeft + xSize - 77, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Eject_Waste"), "", StatCollector.translateToLocal("Ejects_waste_contained_in_the"), StatCollector.translateToLocal("reactor_placing_ingots_in_the"), StatCollector.translateToLocal("reactors_access_ports"), "", StatCollector.translateToLocal("SHIFT_Dump_excess_waste")});
		
		btnInlet = new GuiIconButton(0, guiLeft + xSize - 47, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("inletOn"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Inlet_Mode"), "", StatCollector.translateToLocal("Sets_the_access_port_to"), StatCollector.translateToLocal("inlet_mode"), "", StatCollector.translateToLocal("Port_WILL_accept"), StatCollector.translateToLocal("items_from_pipes/ducts"), StatCollector.translateToLocal("Port_WILL_NOT_eject"), StatCollector.translateToLocal("items_to_pipes/ducts")});
		btnOutlet = new GuiIconButton(1, guiLeft + xSize - 27, guiTop + 53, 18, 18, ClientProxy.GuiIcons.getIcon("outletOn"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Outlet_Mode"), "", StatCollector.translateToLocal("Sets_the_access_port_to"), StatCollector.translateToLocal("outlet_mode"), "", StatCollector.translateToLocal("Port_WILL_NOT_accept"), StatCollector.translateToLocal("items_from_pipes/ducts"), StatCollector.translateToLocal("Port_WILL_eject"), StatCollector.translateToLocal("ingots_to_pipes/ducts")});
		
		inventoryLabel = new BeefGuiLabel(this, "Inventory", guiLeft + 8, guiTop + 64);
		
		registerControl(ejectFuel);
		registerControl(ejectWaste);
		registerControl(btnOutlet);
		registerControl(btnInlet);
		registerControl(inventoryLabel);
		
		updateIcons();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "ReactorAccessPort.png");
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		updateIcons();
	}
	
	protected void updateIcons() {
		if(_port.isInlet()) {
			btnInlet.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.INLET_ON));
			btnOutlet.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OUTLET_OFF));
		}
		else {
			btnInlet.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.INLET_OFF));
			btnOutlet.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OUTLET_ON));
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float gameTicks) {
		super.drawScreen(mouseX, mouseY, gameTicks);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0 || button.id == 1) {
            CommonPacketHandler.INSTANCE.sendToServer(new ReactorAccessPortChangeDirectionMessage(_port, button.id == btnInlet.id));
		}
		
		else if(button.id == 2 || button.id == 3) {
			boolean ejectFuel = button.id == 2;
            CommonPacketHandler.INSTANCE.sendToServer(new ReactorCommandEjectToPortMessage(_port, ejectFuel, isShiftKeyDown()));
		}
	}
}
