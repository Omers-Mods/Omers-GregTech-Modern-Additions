package com.oe.ogtma.common.cover.multiblock;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModeSwitcherCover extends CoverBehavior {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(ModeSwitcherCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    @Persisted
    protected int strength = 0;

    public ModeSwitcherCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    protected int getRedstoneStrength() {
        return coverHolder.getLevel().getSignal(coverHolder.getPos().relative(attachedSide), attachedSide);
    }

    protected void cycleMode() {
        var controller = getController();
        assert controller != null;
        controller.setActiveRecipeType((controller.getActiveRecipeType() + 1) % controller.getRecipeTypes().length);
    }

    protected @Nullable WorkableMultiblockMachine getController() {
        var be = coverHolder.getLevel().getBlockEntity(coverHolder.getPos());
        if (be instanceof MetaMachineBlockEntity mmbe &&
                mmbe.getMetaMachine() instanceof WorkableMultiblockMachine controller) {
            return controller;
        }
        return null;
    }

    //////////////////////////////////////
    // ***** Logic ******//
    //////////////////////////////////////
    @Override
    public boolean canAttach() {
        var controller = getController();
        return controller != null && !(controller.hasFrontFacing() && controller.getFrontFacing() == attachedSide) &&
                controller.getRecipeTypes().length > 1;
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        var currStrength = getRedstoneStrength();
        if (strength == 0 && currStrength > 0) {
            cycleMode();
        }
        strength = currStrength;
    }
}
