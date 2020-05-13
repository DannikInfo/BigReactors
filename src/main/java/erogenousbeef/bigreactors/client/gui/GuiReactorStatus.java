package erogenousbeef.bigreactors.client.gui;

import java.math.BigDecimal;

import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor.WasteEjectionSetting;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorPart;
import erogenousbeef.bigreactors.gui.BeefGuiIconManager;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFuelMixBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiHeatBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiIcon;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.MachineCommandActivateMessage;
import erogenousbeef.bigreactors.net.message.multiblock.ReactorChangeWasteEjectionMessage;
import erogenousbeef.bigreactors.net.message.multiblock.ReactorCommandEjectMessage;
import erogenousbeef.bigreactors.utils.StaticUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GuiReactorStatus extends BeefGuiBase {

	private GuiIconButton btnReactorOn;
	private GuiIconButton btnReactorOff;
	private GuiIconButton btnWasteAutoEject;
	private GuiIconButton btnWasteManual;
	
	private GuiIconButton btnWasteEject;
	
	private TileEntityReactorPart part;
	private MultiblockReactor reactor;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	
	private BeefGuiIcon  heatIcon;
	private BeefGuiLabel heatString;
	private BeefGuiIcon outputIcon;
	private BeefGuiLabel outputString;
	private BeefGuiIcon output2Icon;
	private BeefGuiLabel output2String;
	private BeefGuiIcon fuelConsumedIcon;
	private BeefGuiLabel fuelConsumedString;
	private BeefGuiIcon reactivityIcon;
	private BeefGuiLabel reactivityString;

	private BeefGuiHeatBar coreHeatBar;
	private BeefGuiHeatBar caseHeatBar;
	private BeefGuiFuelMixBar fuelMixBar;
	
	private BeefGuiIcon coolantIcon;
	private BeefGuiFluidBar coolantBar;
	private BeefGuiIcon hotFluidIcon;
	private BeefGuiFluidBar hotFluidBar;
	
	public GuiReactorStatus(Container container, TileEntityReactorPart tileEntityReactorPart) {
		super(container);
		
		ySize = 186;
		
		this.part = tileEntityReactorPart;
		this.reactor = part.getReactorController();

	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();

		btnReactorOn = new GuiIconButton(0, guiLeft + 4, guiTop + 164, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"));
		btnReactorOff = new GuiIconButton(1, guiLeft + 22, guiTop + 164, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"));
		
		btnReactorOn.setTooltip(new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Activate Reactor") });
		btnReactorOff.setTooltip(new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Deactivate Reactor"), StatCollector.translateToLocal("Residual heat will still"), StatCollector.translateToLocal("generate power/consume coolant,"), StatCollector.translateToLocal("until the reactor cools.") });
		
		btnWasteAutoEject = new GuiIconButton(2, guiLeft + 4, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject_off"));
		btnWasteManual = new GuiIconButton(4, guiLeft + 22, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("wasteManual_off"));
		btnWasteEject = new GuiIconButton(5, guiLeft + 50, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("wasteEject"));

		btnWasteEject.visible = false;

		btnWasteAutoEject.setTooltip(new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Auto-Eject Waste"), StatCollector.translateToLocal("Waste in the core will be ejected"), StatCollector.translateToLocal("as soon as possible") });
		btnWasteManual.setTooltip(new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Do Not Auto-Eject Waste"), EnumChatFormatting.LIGHT_PURPLE + StatCollector.translateToLocal("Waste must be manually ejected."), "", StatCollector.translateToLocal("Ejection can be done from this"), StatCollector.translateToLocal("screen, or via rednet,"), StatCollector.translateToLocal("redstone or computer port signals.")});
		btnWasteEject.setTooltip(new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Eject Waste Now"), StatCollector.translateToLocal("Ejects waste from the core"), StatCollector.translateToLocal("into access ports."), StatCollector.translateToLocal("Each 1000mB waste 1 ingot"), "", StatCollector.translateToLocal("SHIFT: Dump excess waste, if any")});
		
		registerControl(btnReactorOn);
		registerControl(btnReactorOff);
		registerControl(btnWasteAutoEject);
		registerControl(btnWasteManual);
		registerControl(btnWasteEject);
		
		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		titleString = new BeefGuiLabel(this, StatCollector.translateToLocal("Reactor Control"), leftX, topY);
		topY += titleString.getHeight() + 4;
		
		heatIcon = new BeefGuiIcon(this, leftX - 2, topY, 16, 16, ClientProxy.GuiIcons.getIcon("temperature"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Core Temperature"), "", StatCollector.translateToLocal("Temperature inside the reactor core."), StatCollector.translateToLocal("Higher temperatures increase fuel burnup.") });
		heatString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += heatIcon.getHeight() + 5;

		outputIcon = new BeefGuiIcon(this, leftX + 1, topY);
		outputString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		if(reactor.getPTEUCount() > 0 || (reactor.getPTRFCount() == 0 && reactor.getPTEUCount() == 0))
			topY += outputIcon.getHeight() + 5;

		output2Icon = new BeefGuiIcon(this, leftX + 1, topY);
		output2String = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		if(reactor.getPTRFCount() > 0) {
			topY += output2Icon.getHeight() + 5;
		}		
		
		fuelConsumedIcon = new BeefGuiIcon(this, leftX + 1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("fuelUsageRate"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Fuel Burnup Rate"), "", StatCollector.translateToLocal("The rate at which fuel is"), StatCollector.translateToLocal("fissioned into waste in the core.")});
		fuelConsumedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += fuelConsumedIcon.getHeight() + 5;

		reactivityIcon = new BeefGuiIcon(this, leftX, topY, 16, 16, ClientProxy.GuiIcons.getIcon("reactivity"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Fuel Reactivity"), "", StatCollector.translateToLocal("How heavily irradiated the core is."), StatCollector.translateToLocal("Higher levels of radiation"), StatCollector.translateToLocal("reduce fuel burnup.")});
		reactivityString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += reactivityIcon.getHeight() + 6;

		statusString = new BeefGuiLabel(this, "", leftX+1, topY);
		topY += statusString.getHeight() + 4;
		
		
		coreHeatBar = new BeefGuiHeatBar(this, guiLeft + 130, guiTop + 22, EnumChatFormatting.AQUA + StatCollector.translateToLocal("Core Heat"), new String[] { StatCollector.translateToLocal("Heat of the reactor's fuel."), StatCollector.translateToLocal("High heat raises fuel usage."), "", StatCollector.translateToLocal("Core heat is transferred to"), StatCollector.translateToLocal("the casing. Transfer rate"), StatCollector.translateToLocal("is based on the design of"), StatCollector.translateToLocal("the reactor's interior.")});
		caseHeatBar = new BeefGuiHeatBar(this, guiLeft + 108, guiTop + 22, EnumChatFormatting.AQUA + StatCollector.translateToLocal("Casing Heat"), new String[] { StatCollector.translateToLocal("Heat of the reactor's casing."), StatCollector.translateToLocal("High heat raises energy output"), StatCollector.translateToLocal("and coolant conversion.")});
		fuelMixBar = new BeefGuiFuelMixBar(this, guiLeft + 86, guiTop + 22, this.reactor);

		coolantIcon = new BeefGuiIcon(this, guiLeft + 132, guiTop + 91, 16, 16, ClientProxy.GuiIcons.getIcon("coolantIn"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Coolant Fluid Tank"), "", StatCollector.translateToLocal("Casing heat will superheat"), StatCollector.translateToLocal("coolant in this tank.") });
		coolantBar = new BeefGuiFluidBar(this, guiLeft + 131, guiTop + 108, this.reactor, MultiblockReactor.FLUID_COOLANT);
		
		hotFluidIcon = new BeefGuiIcon(this, guiLeft + 154, guiTop + 91, 16, 16, ClientProxy.GuiIcons.getIcon("hotFluidOut"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Hot Fluid Tank"), "", StatCollector.translateToLocal("Superheated coolant"), StatCollector.translateToLocal("will pump into this tank,"), StatCollector.translateToLocal("and must be piped out"), StatCollector.translateToLocal("via coolant ports") });
		hotFluidBar = new BeefGuiFluidBar(this, guiLeft + 153, guiTop + 108, this.reactor, MultiblockReactor.FLUID_SUPERHEATED);
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(heatIcon);
		registerControl(heatString);
		registerControl(output2Icon);
		registerControl(output2String);
		registerControl(outputIcon);
		registerControl(outputString);
		registerControl(fuelConsumedIcon);
		registerControl(fuelConsumedString);
		registerControl(reactivityIcon);
		registerControl(reactivityString);
		registerControl(coreHeatBar);
		registerControl(caseHeatBar);
		registerControl(fuelMixBar);
		registerControl(coolantBar);
		registerControl(hotFluidBar);
		registerControl(coolantIcon);
		registerControl(hotFluidIcon);
		updateIcons();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "ReactorController.png");
	}

	@Override
	public void updateScreen() {
		super.updateScreen();

		if(reactor.getPTRFCount() == 0 && reactor.getPTEUCount() == 0) {
			outputIcon.visible = true;
			outputString.visible = true;
		}else {
			outputIcon.visible = false;
			outputString.visible = false;
		}
		if(reactor.getPTRFCount() == 0){
			output2Icon.visible = false;
			output2String.visible = false;
		}
		if(reactor.getPTRFCount() > 0) {
			output2Icon.visible = true;
			output2String.visible = true;
		}
		if(reactor.getPTEUCount() > 0) {
			outputIcon.visible = true;
			outputString.visible = true;
		}
		updateIcons();
		
		if(reactor.getActive()) {
			statusString.setLabelText(StatCollector.translateToLocal("Status: ") + EnumChatFormatting.DARK_GREEN + StatCollector.translateToLocal("Online"));
		}
		else {
			statusString.setLabelText(StatCollector.translateToLocal("Status: ") + EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("Offline"));
		}
		if(reactor.getPTRFCount() > 0)
			output2String.setLabelText(getFormattedOutputString(1));
		if(reactor.getPTEUCount() > 0)
			outputString.setLabelText(getFormattedOutputString(0));
		if(reactor.getPTEUCount() == 0 && reactor.getPTRFCount() == 0)
			outputString.setLabelText(getFormattedOutputString(0));
		if(reactor.isPassivelyCooled()) {
			if(reactor.getPTEUCount() > 0)
				outputString.setLabelTooltip(String.format("%.2f " + StatCollector.translateToLocal("EU per tick"), BigDecimal.valueOf(((reactor.getEnergyGeneratedLastTick()*BigReactors.RFtoEU)/reactor.getAttachedPowerTapsCount())*reactor.getPTEUCount()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue()));
			else if(reactor.getPTRFCount() == 0 && reactor.getPTEUCount() == 0)
				outputString.setLabelTooltip(String.format("%.2f " + StatCollector.translateToLocal("EU lost per tick"), reactor.getEnergyGeneratedLastTick()*BigReactors.RFtoEU));
			
			output2String.setLabelTooltip(String.format("%.2f " + StatCollector.translateToLocal("RF per tick"), (reactor.getEnergyGeneratedLastTick()/reactor.getAttachedPowerTapsCount())*reactor.getPTRFCount()));
			
		}else {
			outputString.setLabelTooltip(String.format("%.0f " + StatCollector.translateToLocal("millibuckets per tick"), reactor.getEnergyGeneratedLastTick()));
		}

		heatString.setLabelText(Integer.toString((int)reactor.getFuelHeat()) + " C");
		coreHeatBar.setHeat(reactor.getFuelHeat());
		caseHeatBar.setHeat(reactor.getReactorHeat());

		float fuelConsumption = reactor.getFuelConsumedLastTick();
		fuelConsumedString.setLabelText(StaticUtils.Strings.formatMillibuckets(fuelConsumption) + "/t");
		fuelConsumedString.setLabelTooltip(getFuelConsumptionTooltip(fuelConsumption));

		reactivityString.setLabelText(String.format("%2.0f%%", reactor.getFuelFertility() * 100f));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0 || button.id == 1) {
			boolean newSetting = button.id == 0;
			if(newSetting != reactor.getActive()) {
                CommonPacketHandler.INSTANCE.sendToServer(new MachineCommandActivateMessage(reactor, newSetting));
			}
		}
		else if(button.id >= 2 && button.id <= 4) {
			WasteEjectionSetting newEjectionSetting;
			switch(button.id) {
			case 4:
				newEjectionSetting = WasteEjectionSetting.kManual;
				break;
			default:
				newEjectionSetting = WasteEjectionSetting.kAutomatic;
				break;
			}
			
			if(reactor.getWasteEjection() != newEjectionSetting) {
                CommonPacketHandler.INSTANCE.sendToServer(new ReactorChangeWasteEjectionMessage(reactor, newEjectionSetting));
			}
		}else if(button.id == 5) {
            CommonPacketHandler.INSTANCE.sendToServer(new ReactorCommandEjectMessage(reactor, false, isShiftKeyDown()));
		}
	}
	
	protected void updateIcons() {
		if(reactor.getActive()) {
			btnReactorOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_ON));
			btnReactorOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
		}
		else {
			btnReactorOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_OFF));
			btnReactorOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
		}
		
		if(reactor.isPassivelyCooled()) {
			outputIcon.setIcon(ClientProxy.GuiIcons.getIcon("energyOutput"));
			outputIcon.setTooltip(passivelyCooledTooltip);
			output2Icon.setIcon(ClientProxy.GuiIcons.getIcon("energyOutput"));
			output2Icon.setTooltip(passivelyCooledTooltip);
			coolantIcon.visible = false;
			coolantBar.visible = false;
			hotFluidIcon.visible = false;
			hotFluidBar.visible = false;
		}
		else {
			outputIcon.setIcon(ClientProxy.GuiIcons.getIcon("hotFluidOut"));
			outputIcon.setTooltip(activelyCooledTooltip);
			
			coolantIcon.visible = true;
			coolantBar.visible = true;
			hotFluidIcon.visible = true;
			hotFluidBar.visible = true;
		}

		
		switch(reactor.getWasteEjection()) {
		case kAutomatic:
			btnWasteAutoEject.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_EJECT_ON));
			btnWasteManual.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_MANUAL_OFF));
			btnWasteEject.visible = false;
			break;
		case kManual:
		default:
			btnWasteAutoEject.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_EJECT_OFF));
			btnWasteManual.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.WASTE_MANUAL_ON));
			btnWasteEject.visible = true;
			break;
		}
	}
	
	private static final String[] passivelyCooledTooltip = new String[] {
		EnumChatFormatting.AQUA + StatCollector.translateToLocal("Energy Output"),
		"",
		StatCollector.translateToLocal("This reactor is passively cooled"),
		StatCollector.translateToLocal("and generates energy directly from"),
		StatCollector.translateToLocal("the heat of its core.")
	};
	
	private static final String[] activelyCooledTooltip = new String[] {
		EnumChatFormatting.AQUA + StatCollector.translateToLocal("Hot Fluid Output"),
		"",
		StatCollector.translateToLocal("This reactor is actively cooled"),
		StatCollector.translateToLocal("by a fluid, such as water, which"),
		StatCollector.translateToLocal("is superheated by the core.")
	};

	private String getFormattedOutputString(int type) {
		float number = 0;
		if(reactor.getAttachedPowerTapsCount() != 0)
			number = reactor.getEnergyGeneratedLastTick()/reactor.getAttachedPowerTapsCount(); // Also doubles as fluid vaporized last tick
		else
			number = reactor.getEnergyGeneratedLastTick(); // Also doubles as fluid vaporized last tick
		
		if(reactor.isPassivelyCooled()) {
			if(type == 1)
				return StaticUtils.Strings.formatRF(number*reactor.getPTRFCount()) + "/t";
			else 
				if(reactor.getPTEUCount() != 0)
					return StaticUtils.Strings.formatEU((float)(number * BigReactors.RFtoEU)*reactor.getPTEUCount()) + "/t";
				else
					return StaticUtils.Strings.formatEU((float)(number * BigReactors.RFtoEU)) + "/t";
		}
		else {
			return StaticUtils.Strings.formatMillibuckets(number) + "/t";			
		}
	}
	
	private String getFuelConsumptionTooltip(float fuelConsumption) {
		if(fuelConsumption <= 0.000001f) { return "0 " + StatCollector.translateToLocal("millibuckets per tick"); }
		
		int exp = (int)Math.log10(fuelConsumption);
		
		int decimalPlaces = 0;
		if(exp < 1) {
			decimalPlaces = Math.abs(exp) + 2;
			return String.format("%." + Integer.toString(decimalPlaces) + "f " + StatCollector.translateToLocal("millibuckets per tick"), fuelConsumption);
		}
		else {
			return String.format("%.0f" + StatCollector.translateToLocal("millibuckets per tick"), fuelConsumption);
		}
	}
}
