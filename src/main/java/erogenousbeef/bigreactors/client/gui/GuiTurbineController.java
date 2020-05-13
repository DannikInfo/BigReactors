package erogenousbeef.bigreactors.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import java.math.BigDecimal;

import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.MultiblockReactor;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine;
import erogenousbeef.bigreactors.common.multiblock.MultiblockTurbine.VentStatus;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityTurbinePartBase;
import erogenousbeef.bigreactors.gui.BeefGuiIconManager;
import erogenousbeef.bigreactors.gui.controls.BeefGuiFluidBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiIcon;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.BeefGuiPowerBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiRpmBar;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.MachineCommandActivateMessage;
import erogenousbeef.bigreactors.net.message.multiblock.TurbineChangeInductorMessage;
import erogenousbeef.bigreactors.net.message.multiblock.TurbineChangeMaxIntakeMessage;
import erogenousbeef.bigreactors.net.message.multiblock.TurbineChangeVentMessage;
import erogenousbeef.core.common.CoordTriplet;

public class GuiTurbineController extends BeefGuiBase {

	TileEntityTurbinePartBase part;
	MultiblockTurbine turbine;
	
	private BeefGuiLabel titleString;
	private BeefGuiLabel statusString;
	
	private BeefGuiIcon speedIcon;
	private BeefGuiLabel speedString;
	
	private BeefGuiIcon energyGeneratedIcon;
	private BeefGuiLabel energyGeneratedString;
	private BeefGuiIcon energyGeneratedIcon2;
	private BeefGuiLabel energyGeneratedString2;
	
	private BeefGuiIcon rotorEfficiencyIcon;
	private BeefGuiLabel rotorEfficiencyString;

	private BeefGuiIcon steamIcon;
	private BeefGuiFluidBar steamBar;
	private BeefGuiIcon waterIcon;
	private BeefGuiFluidBar waterBar;
	
	private BeefGuiIcon rpmIcon;
	private BeefGuiRpmBar rpmBar;

	private BeefGuiIcon governorIcon;
	private BeefGuiLabel governorString;
	private GuiIconButton btnGovernorUp;
	private GuiIconButton btnGovernorDown;
	
	private GuiIconButton btnActivate;
	private GuiIconButton btnDeactivate;
	
	private GuiIconButton btnVentAll;
	private GuiIconButton btnVentOverflow;
	private GuiIconButton btnVentNone;
	
	private BeefGuiIcon inductorIcon;
	private GuiIconButton btnInductorOn;
	private GuiIconButton btnInductorOff;
	
	public GuiTurbineController(Container container, TileEntityTurbinePartBase part) {
		super(container);
		
		this.part = part;
		turbine = part.getTurbine();
	}

	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation(BigReactors.GUI_DIRECTORY + "TurbineController.png");
	}
	
	// Add controls, etc.
	@Override
	public void initGui() {
		super.initGui();
		
		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		titleString = new BeefGuiLabel(this, StatCollector.translateToLocal("Turbine Control"), leftX, topY);
		topY += titleString.getHeight() + 4;
		
		speedIcon = new BeefGuiIcon(this, leftX + 1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("rpm"), new String[] { 
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Rotor Speed"),
				"",
				StatCollector.translateToLocal("Speed of the rotor in"),
				StatCollector.translateToLocal("revolutions per minute."),
				"",
				StatCollector.translateToLocal("Rotors perform best at 900"),
				StatCollector.translateToLocal("or 1800 RPM."),
				"",
				StatCollector.translateToLocal("Speeds over 2000PM are overspeed"),
				StatCollector.translateToLocal("and may cause a turbine to"),
				StatCollector.translateToLocal("fail catastrophically.") });
		speedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += speedIcon.getHeight() + 4;

		energyGeneratedIcon = new BeefGuiIcon(this, leftX+1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("energyOutput"), new String[] {
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Energy Output"),
				"", 
				StatCollector.translateToLocal("Turbines generate energy via"),
				StatCollector.translateToLocal("metal induction coils placed"),
				StatCollector.translateToLocal("around a spinning rotor."), 
				StatCollector.translateToLocal("More, or higher-quality, coils"),
				StatCollector.translateToLocal("generate energy faster.")});
		energyGeneratedString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		if(turbine.getPTEUCount() > 0 || (turbine.getPTRFCount() == 0 && turbine.getPTEUCount() == 0))
			topY += energyGeneratedIcon.getHeight() + 4;
		energyGeneratedIcon2 = new BeefGuiIcon(this, leftX+1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("energyOutput"), new String[] {
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Energy Output"),
				"", 
				StatCollector.translateToLocal("Turbines generate energy via"),
				StatCollector.translateToLocal("metal induction coils placed"),
				StatCollector.translateToLocal("around a spinning rotor."), 
				StatCollector.translateToLocal("More, or higher-quality, coils"),
				StatCollector.translateToLocal("generate energy faster.")});
		energyGeneratedString2 = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		if(turbine.getPTRFCount() > 0)
			topY += energyGeneratedIcon.getHeight() + 4;
		
		rotorEfficiencyIcon = new BeefGuiIcon(this, leftX + 1, topY, 16, 16, ClientProxy.GuiIcons.getIcon("rotorEfficiency"), new String[] {
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Rotor Efficiency"),
				"",
				StatCollector.translateToLocal("Rotor blades can only fully"),
				String.format(StatCollector.translateToLocal("capture energy from %d mB of"),MultiblockTurbine.inputFluidPerBlade), 
				StatCollector.translateToLocal("fluid per blade."),
				"",
				StatCollector.translateToLocal("Efficiency drops if the flow"),
				StatCollector.translateToLocal("of input fluid rises past"),
				StatCollector.translateToLocal("capacity.")});
		rotorEfficiencyString = new BeefGuiLabel(this, "", leftX + 22, topY + 4);
		topY += rotorEfficiencyIcon.getHeight() + 4;

		statusString = new BeefGuiLabel(this, "", leftX, guiTop + 94);
		topY += statusString.getHeight() + 4;
		
		steamIcon = new BeefGuiIcon(this, guiLeft + 113, guiTop + 4, 16, 16, ClientProxy.GuiIcons.getIcon("hotFluidIn"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Intake Fluid Tank") });
		steamBar = new BeefGuiFluidBar(this, guiLeft + 112, guiTop + 22, turbine, MultiblockTurbine.TANK_INPUT);

		waterIcon = new BeefGuiIcon(this, guiLeft + 133, guiTop + 4, 16, 16, ClientProxy.GuiIcons.getIcon("coolantOut"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Exhaust Fluid Tank") });
		waterBar = new BeefGuiFluidBar(this, guiLeft + 132, guiTop + 22, turbine, MultiblockTurbine.TANK_OUTPUT);

		rpmIcon = new BeefGuiIcon(this, guiLeft + 93, guiTop + 4, 16, 16, ClientProxy.GuiIcons.getIcon("rpm"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Rotor Speed") });
		rpmBar = new BeefGuiRpmBar(this, guiLeft + 92, guiTop + 22, turbine, StatCollector.translateToLocal("Rotor Speed"), 
				new String[] {
						StatCollector.translateToLocal("Rotors perform best at"),
						StatCollector.translateToLocal("900 or 1800 RPM."),
						"",
						StatCollector.translateToLocal("Rotors kept overspeed for too"),
						StatCollector.translateToLocal("long may fail."),
						"",
						StatCollector.translateToLocal("Catastrophically.")});
	
		governorIcon = new BeefGuiIcon(this, guiLeft + 102, guiTop + 107, 16, 16, ClientProxy.GuiIcons.getIcon("flowRate"), new String[] { 
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Flow Rate Governor"),
				"",
				StatCollector.translateToLocal("Controls the maximum rate at"),
				StatCollector.translateToLocal("which hot fluids are drawn"),
				StatCollector.translateToLocal("from the turbine's intake tank"),
				StatCollector.translateToLocal("and passed over the turbines."),
				"",
				StatCollector.translateToLocal("Effectively, the max rate at which"),
				StatCollector.translateToLocal("the turbine will process fluids.")});
		governorString = new BeefGuiLabel(this, "", guiLeft + 122, guiTop + 112);
		btnGovernorUp   = new GuiIconButton(2, guiLeft + 120, guiTop + 125, 18, 18, ClientProxy.GuiIcons.getIcon("upArrow"),   new String[] { 
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Increase Max Flow Rate"),
				"",
				StatCollector.translateToLocal("Higher flow rates will"),
				StatCollector.translateToLocal("increase rotor speed."),
				"",
				StatCollector.translateToLocal("SHIFT: +10 mB"),
				StatCollector.translateToLocal("CTRL: +100mB"),
				StatCollector.translateToLocal("CTRL+SHIFT: +1000mB")});
		btnGovernorDown = new GuiIconButton(3, guiLeft + 140, guiTop + 125, 18, 18, ClientProxy.GuiIcons.getIcon("downArrow"), new String[] { 
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Decrease Max Flow Rate"),
				"",
				StatCollector.translateToLocal("Lower flow rates will"),
				StatCollector.translateToLocal("decrease rotor speed."),
				"",
				StatCollector.translateToLocal("SHIFT: -10 mB"),
				StatCollector.translateToLocal("CTRL: -100mB"),
				StatCollector.translateToLocal("CTRL+SHIFT: -1000mB")});

		inductorIcon = new BeefGuiIcon(this, leftX, guiTop + 105, 16, 16, ClientProxy.GuiIcons.getIcon("coil"), new String[] { 
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Induction Coils"),
				"",
				StatCollector.translateToLocal("Metal coils inside the turbine"),
				StatCollector.translateToLocal("extract energy from the rotor"),
				StatCollector.translateToLocal("and convert it into EU/RF."),
				"",
				StatCollector.translateToLocal("These controls engage/disengage"),
				StatCollector.translateToLocal("the coils.")});
		btnInductorOn = new GuiIconButton(7, guiLeft + 24, guiTop + 104, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"), new String[] { 
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Engage Coils"),
				"",
				StatCollector.translateToLocal("Engages the induction coils."),
				StatCollector.translateToLocal("Energy will be extracted from"),
				StatCollector.translateToLocal("the rotor and converted to RF."),
				"",
				StatCollector.translateToLocal("Energy extraction exerts drag"),
				StatCollector.translateToLocal("on the rotor, slowing it down.") });
		btnInductorOff = new GuiIconButton(8, guiLeft + 44, guiTop + 104, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"), new String[] { 
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Disengage Coils"),
				"",
				StatCollector.translateToLocal("Disengages the induction coils."),
				StatCollector.translateToLocal("Energy will NOT be extracted from"),
				StatCollector.translateToLocal("the rotor, allowing it to"),
				StatCollector.translateToLocal("spin faster.") });
		
		btnActivate = new GuiIconButton(0, guiLeft + 4, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("On_off"), new String[] {
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Activate Turbine"),
				"", 
				StatCollector.translateToLocal("Enables flow of intake fluid to rotor."),
				StatCollector.translateToLocal("Fluid flow will spin up the rotor.") });
		btnDeactivate = new GuiIconButton(1, guiLeft + 24, guiTop + 144, 18, 18, ClientProxy.GuiIcons.getIcon("Off_off"), new String[] { 
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Deactivate Turbine"),
				"",
				StatCollector.translateToLocal("Disables flow of intake fluid to rotor."),
				StatCollector.translateToLocal("The rotor will spin down.") });
		
		btnVentAll = new GuiIconButton(4, guiLeft + 4, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventAllOff"), new String[] {
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Vent: All Exhaust"),
				"",
				StatCollector.translateToLocal("Dump all exhaust fluids."),
				StatCollector.translateToLocal("The exhaust fluid tank"),
				StatCollector.translateToLocal("will not fill.")});
		btnVentOverflow = new GuiIconButton(5, guiLeft + 24, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventOverflowOff"), new String[] {
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Vent: Overflow Only"),
				"",
				StatCollector.translateToLocal("Dump excess exhaust fluids."),
				StatCollector.translateToLocal("Excess fluids will be lost"),
				StatCollector.translateToLocal("if exhaust fluid tank is full.")});
		btnVentNone = new GuiIconButton(6, guiLeft + 44, guiTop + 124, 18, 18, ClientProxy.GuiIcons.getIcon("ventNoneOff"), new String[] {
				EnumChatFormatting.AQUA + 
				StatCollector.translateToLocal("Vent: Closed"),
				"",
				StatCollector.translateToLocal("Preserve all exhaust fluids."),
				StatCollector.translateToLocal("Turbine will slow or halt"),
				StatCollector.translateToLocal("fluid intake if exhaust"),
				StatCollector.translateToLocal("fluid tank is full.")});
		
		registerControl(titleString);
		registerControl(statusString);
		registerControl(speedIcon);
		registerControl(speedString);
		registerControl(energyGeneratedIcon);
		registerControl(energyGeneratedString);
		registerControl(energyGeneratedIcon2);
		registerControl(energyGeneratedString2);
		registerControl(rotorEfficiencyIcon);
		registerControl(rotorEfficiencyString);
		registerControl(steamBar);
		registerControl(waterBar);
		registerControl(steamIcon);
		registerControl(waterIcon);
		registerControl(rpmIcon);
		registerControl(rpmBar);
		registerControl(governorIcon);
		registerControl(governorString);
		registerControl(btnGovernorUp);
		registerControl(btnGovernorDown);
		registerControl(btnActivate);
		registerControl(btnDeactivate);
		registerControl(btnVentAll);
		registerControl(btnVentOverflow);
		registerControl(btnVentNone);
		registerControl(inductorIcon);
		registerControl(btnInductorOn);
		registerControl(btnInductorOff);

		updateStrings();
		updateTooltips();
	}

	private void updateStrings() {
		if(turbine.getPTRFCount() == 0 && turbine.getPTEUCount() == 0) {
			energyGeneratedIcon.visible = true;
			energyGeneratedString.visible = true;
		}else {
			energyGeneratedIcon.visible = false;
			energyGeneratedString.visible = false;
		}
		if(turbine.getPTRFCount() == 0){
			energyGeneratedIcon2.visible = false;
			energyGeneratedString2.visible = false;
		}
		if(turbine.getPTRFCount() > 0) {
			energyGeneratedIcon2.visible = true;
			energyGeneratedString2.visible = true;
		}
		if(turbine.getPTEUCount() > 0) {
			energyGeneratedIcon.visible = true;
			energyGeneratedString.visible = true;
		}
		
		if(turbine.getActive()) {
			statusString.setLabelText(StatCollector.translateToLocal("Status: ") + EnumChatFormatting.DARK_GREEN + StatCollector.translateToLocal("Active"));
			btnActivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_ON));
			btnDeactivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
		}
		else {
			statusString.setLabelText(StatCollector.translateToLocal("Status: ") + EnumChatFormatting.DARK_RED + StatCollector.translateToLocal("Inactive"));
			btnActivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_OFF));
			btnDeactivate.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
		}
		
		speedString.setLabelText(String.format("%.1f RPM", turbine.getRotorSpeed()));
		
		if(turbine.getPTRFCount() == 0 && turbine.getPTEUCount() == 0)
			energyGeneratedString.setLabelText(String.format("%.0f EU/t", turbine.getEnergyGeneratedLastTick()*BigReactors.RFtoEU));
		if(turbine.getPTEUCount() > 0)
			energyGeneratedString.setLabelText(String.format("%.0f EU/t", (turbine.getEnergyGeneratedLastTick()*BigReactors.RFtoEU)/turbine.getAttachedPowerTapsCount()*turbine.getPTEUCount()));
		if(turbine.getPTRFCount() > 0)
			energyGeneratedString2.setLabelText(String.format("%.0f RF/t", turbine.getEnergyGeneratedLastTick()/turbine.getAttachedPowerTapsCount()*turbine.getPTRFCount()));
		
		if(turbine.getPTEUCount() > 0)
			energyGeneratedString.setLabelTooltip(String.format("%.2f " + StatCollector.translateToLocal("EU per tick"), ((turbine.getEnergyGeneratedLastTick()*BigReactors.RFtoEU)/turbine.getAttachedPowerTapsCount())*turbine.getPTEUCount()));
		else if(turbine.getPTRFCount() == 0 && turbine.getPTEUCount() == 0)
			energyGeneratedString.setLabelTooltip(String.format("%.2f " + StatCollector.translateToLocal("EU lost per tick"), turbine.getEnergyGeneratedLastTick()*BigReactors.RFtoEU));
		
		energyGeneratedString2.setLabelTooltip(String.format("%.2f " + StatCollector.translateToLocal("RF per tick"), (turbine.getEnergyGeneratedLastTick()/turbine.getAttachedPowerTapsCount())*turbine.getPTRFCount()));
		
		
		governorString.setLabelText(String.format("%d mB/t", turbine.getMaxIntakeRate()));
		
		if(turbine.getActive()) {
			if(turbine.getRotorEfficiencyLastTick() < 1f) {
				rotorEfficiencyString.setLabelText(String.format("%.1f%%", turbine.getRotorEfficiencyLastTick() * 100f));
			}
			else {
				rotorEfficiencyString.setLabelText("100%");
			}

			int numBlades = turbine.getNumRotorBlades();
			int fluidLastTick = turbine.getFluidConsumedLastTick();
			int neededBlades = fluidLastTick / MultiblockTurbine.inputFluidPerBlade;
			
			rotorEfficiencyString.setLabelTooltip(String.format(StatCollector.translateToLocal("%d / %d blades"), numBlades, neededBlades));
		}
		else {
			rotorEfficiencyString.setLabelText(StatCollector.translateToLocal("Unknown"));
		}
		
		switch(turbine.getVentSetting()) {
		case DoNotVent:
			btnVentNone.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_NONE_ON));
			btnVentOverflow.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_OVERFLOW_OFF));
			btnVentAll.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_ALL_OFF));
			break;
		case VentOverflow:
			btnVentNone.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_NONE_OFF));
			btnVentOverflow.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_OVERFLOW_ON));
			btnVentAll.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_ALL_OFF));
			break;
		default:
			// Vent all
			btnVentNone.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_NONE_OFF));
			btnVentOverflow.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_OVERFLOW_OFF));
			btnVentAll.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.VENT_ALL_ON));
		}
		
		if(turbine.getInductorEngaged())
		{
			btnInductorOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_ON));
			btnInductorOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_OFF));
		}
		else
		{
			btnInductorOn.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.ON_OFF));
			btnInductorOff.setIcon(ClientProxy.GuiIcons.getIcon(BeefGuiIconManager.OFF_ON));
		}
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		
		updateStrings();
	}
	
	protected void updateTooltips() {
		
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0 || button.id == 1) {
			boolean setActive = button.id == 0;
			if(setActive != turbine.getActive()) {
                CommonPacketHandler.INSTANCE.sendToServer(new MachineCommandActivateMessage(turbine, setActive));
            }
		}
		
		if(button.id == 2 || button.id == 3) {
			int exponent = 0;

			if(isShiftKeyDown()) {
				exponent += 1;
			}
			if(isCtrlKeyDown()) {
				exponent += 2;
			}

			int newMax = (int) Math.round(Math.pow(10, exponent));

			if(button.id == 3) { newMax *= -1; }
			
			newMax = Math.max(0, Math.min(turbine.getMaxIntakeRateMax(), turbine.getMaxIntakeRate() + newMax));

			if(newMax != turbine.getMaxIntakeRate()) {
                CommonPacketHandler.INSTANCE.sendToServer(new TurbineChangeMaxIntakeMessage(turbine, newMax));
			}
		}
		
		if(button.id >= 4 && button.id <= 6) {
			VentStatus newStatus;
			switch(button.id) {
			case 5:
				newStatus = VentStatus.VentOverflow;
				break;
			case 6:
				newStatus = VentStatus.DoNotVent;
				break;
			default:
				newStatus = VentStatus.VentAll;
				break;
			}
			
			if(newStatus != turbine.getVentSetting()) {
                CommonPacketHandler.INSTANCE.sendToServer(new TurbineChangeVentMessage(turbine, newStatus));
			}
		}
		
		if(button.id == 7 || button.id == 8)
		{
			boolean newStatus = button.id == 7;
			if(newStatus != turbine.getInductorEngaged())
			{
                CommonPacketHandler.INSTANCE.sendToServer(new TurbineChangeInductorMessage(turbine, newStatus));
			}
		}
	}
}
