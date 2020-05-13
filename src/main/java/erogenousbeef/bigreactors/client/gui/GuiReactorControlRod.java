package erogenousbeef.bigreactors.client.gui;

import org.lwjgl.input.Keyboard;

import erogenousbeef.bigreactors.client.ClientProxy;
import erogenousbeef.bigreactors.common.BigReactors;
import erogenousbeef.bigreactors.common.multiblock.tileentity.TileEntityReactorControlRod;
import erogenousbeef.bigreactors.gui.controls.BeefGuiIcon;
import erogenousbeef.bigreactors.gui.controls.BeefGuiInsertionProgressBar;
import erogenousbeef.bigreactors.gui.controls.BeefGuiLabel;
import erogenousbeef.bigreactors.gui.controls.GuiIconButton;
import erogenousbeef.bigreactors.net.CommonPacketHandler;
import erogenousbeef.bigreactors.net.message.ControlRodChangeInsertionMessage;
import erogenousbeef.bigreactors.net.message.ControlRodChangeNameMessage;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.inventory.Container;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GuiReactorControlRod extends BeefGuiBase {

	TileEntityReactorControlRod entity;
	
	BeefGuiLabel titleString;
	BeefGuiLabel rodNameLabel;
	BeefGuiLabel insertionLabel;

	GuiButton setNameBtn;
	
	BeefGuiIcon rodInsertIcon;
	GuiIconButton rodInsertBtn;
	GuiIconButton rodRetractBtn;

	BeefGuiInsertionProgressBar insertionBar;
	
    private GuiTextField rodName;

	
	public GuiReactorControlRod(Container c, TileEntityReactorControlRod controlRod) {
		super(c);
		
		entity = controlRod;
	}
	
	@Override
	public ResourceLocation getGuiBackground() {
		return new ResourceLocation (BigReactors.GUI_DIRECTORY + "BasicBackground.png");
	}

	@Override
	public void initGui() {
		super.initGui();

		int leftX = guiLeft + 4;
		int topY = guiTop + 4;
		
		Keyboard.enableRepeatEvents(true);
		
		titleString = new BeefGuiLabel(this, StatCollector.translateToLocal("Reactor_Control_Rod"), leftX, topY);
		topY += titleString.getHeight() + 8;
		
		rodNameLabel = new BeefGuiLabel(this, StatCollector.translateToLocal("Name:"), leftX, topY + 6);
		
		rodName = new GuiTextField(fontRendererObj, leftX + 4 + rodNameLabel.getWidth(), topY, 100, 20);
		rodName.setCanLoseFocus(true);
		rodName.setMaxStringLength(32);
		rodName.setText(entity.getName());
		rodName.setEnabled(true);
		
		setNameBtn = new GuiButton(2, guiLeft + 140, topY, 30, 20, StatCollector.translateToLocal("Set"));
		setNameBtn.enabled = false;
		topY += 28;
		
		rodInsertIcon = new BeefGuiIcon(this, leftX+42, topY, 16, 16, ClientProxy.GuiIcons.getIcon("controlRod"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Rod_Insertion"), "", StatCollector.translateToLocal("Change_the_control_rods_insertion"), StatCollector.translateToLocal("Higher_insertion_slows_reaction_rate"), "", StatCollector.translateToLocal("Lower_reaction_rates_reduce_heat"), StatCollector.translateToLocal("energy_radiation_output_and"), StatCollector.translateToLocal("fuel_consumption") });
		insertionLabel = new BeefGuiLabel(this, "", leftX+62, topY+5);
		topY += 20;
		insertionBar = new BeefGuiInsertionProgressBar(this, leftX+40, topY);

		topY += 12;
		rodRetractBtn = new GuiIconButton(0, leftX+70, topY, 20, 20, ClientProxy.GuiIcons.getIcon("upArrow"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Insert_Rod"), StatCollector.translateToLocal("Increase_insertion_by_10"), "", StatCollector.translateToLocal("Shift_P100"), StatCollector.translateToLocal("Alt_P5"), StatCollector.translateToLocal("ShiftAlt_M1"), "", StatCollector.translateToLocal("Ctrl_Change_ALL_Rods") });
		topY += 20;
		rodInsertBtn = new GuiIconButton(1, leftX+70, topY, 20, 20, ClientProxy.GuiIcons.getIcon("downArrow"), new String[] { EnumChatFormatting.AQUA + StatCollector.translateToLocal("Retract_Rod"), StatCollector.translateToLocal("Reduce_insertion_by_10"), "", StatCollector.translateToLocal("Shift_M100"), StatCollector.translateToLocal("Alt_M5"), StatCollector.translateToLocal("ShiftAlt_M1"), "", StatCollector.translateToLocal("Ctrl_Change_ALL_Rods") });
		topY += 32;

		registerControl(insertionBar);
		registerControl(titleString);
		registerControl(rodNameLabel);
		registerControl(rodInsertIcon);
		registerControl(insertionLabel);

		registerControl(rodName);
		registerControl(rodRetractBtn);
		registerControl(rodInsertBtn);
		registerControl(setNameBtn);
		
		updateStrings();
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}
	
	@Override
	public void updateScreen() {
		super.updateScreen();
		updateStrings();
	}
	
	protected void updateStrings() {
		if(entity.isConnected()) {
			rodInsertBtn.enabled = true;
			rodRetractBtn.enabled = true;
		}
		else {
			rodInsertBtn.enabled = false;
			rodRetractBtn.enabled = false;
		}
		
		insertionLabel.setLabelText(String.format("%d%%", entity.getControlRodInsertion()));
		this.setNameBtn.enabled = !entity.getName().equals(this.rodName.getText());
		insertionBar.setInsertion((float)entity.getControlRodInsertion() / 100f);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		switch(button.id) {
		case 2:
            CommonPacketHandler.INSTANCE.sendToServer(new ControlRodChangeNameMessage(entity.xCoord, entity.yCoord, entity.zCoord, this.rodName.getText()));
			this.rodName.setFocused(false);
			break;
		case 0:
		case 1:
		default:
			int change = 10;
			if(isShiftKeyDown()) {
				if(isAltKeyDown()) {
					change = 1;
				}
				else {
					change = 100;
				}
			}
			else if(isAltKeyDown()) {
				change = 5;
			}
			if(button.id == 1) { change = -change; }
	        CommonPacketHandler.INSTANCE.sendToServer(new ControlRodChangeInsertionMessage(entity.xCoord, entity.yCoord, entity.zCoord, change, isCtrlKeyDown()));
		}
    }
	
	@Override
	protected void keyTyped(char inputChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE ||
        		(!this.rodName.isFocused() && keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())) {
            this.mc.thePlayer.closeScreen();
        }

		this.rodName.textboxKeyTyped(inputChar, keyCode);
		
		if(keyCode == Keyboard.KEY_TAB) {
			// Tab
			if(this.rodName.isFocused()) {
				this.rodName.setFocused(false);
			}
			else {
				this.rodName.setFocused(true);
			}
		}
		
		if(keyCode == Keyboard.KEY_RETURN) {
			// Return/enter
			this.actionPerformed((GuiButton)this.buttonList.get(2));
		}
	}
	
}
