package com.oe.ogtma.api.gui.tab;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class SlotsTab implements IFancyUIProvider {
    public static final Component title = Component.translatable("ogtma.gui.tooltip.inventory");
    public static final IGuiTexture icon = new ItemStackTexture(Items.CHEST);

    protected final NotifiableItemStackHandler inventory;
    protected final NotifiableFluidTank tanks;
    protected final int rows;
    
    public SlotsTab(NotifiableItemStackHandler inventory, NotifiableFluidTank tanks, int rows) {
        this.inventory = inventory;
        this.tanks = tanks;
        this.rows = rows;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return icon;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var group = new WidgetGroup(Position.ORIGIN);
        var io = inventory.capabilityIO;
        var canTake = io == IO.BOTH || io == IO.OUT;
        var canPut = io == IO.BOTH || io == IO.IN;
        var col = 0;
        var ind = 0;
        var inventorySlots = inventory.getSlots();
        while (ind < inventorySlots) {
            for (int row = 0; row < rows && ind < inventorySlots; row++) {
                group.addWidget(new SlotWidget(inventory, ind++, col * 18, row * 18, canTake, canPut));
            }
            col++;
        }
        ind = 0;
        io = tanks.capabilityIO;
        canTake = io == IO.BOTH || io == IO.OUT;
        canPut = io == IO.BOTH || io == IO.IN;
        var tankSlots = tanks.getTanks();
        while (ind < tankSlots) {
            for (int row = 0; row < rows && ind < tankSlots; row++) {
                group.addWidget(new TankWidget(tanks, ind++, col * 18, row * 18, 18, 18, canTake, canPut)
                        .setBackground(GuiTextures.FLUID_SLOT));
            }
            col++;
        }
        
        return group;
    }
}
