package com.oe.ogtma.common.machine.trait.quarry;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.oe.ogtma.common.machine.quarry.def.IQuarry;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;

public class QuarryLogic extends RecipeLogic implements IRecipeCapabilityHolder {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuarryLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);
    protected final IQuarry quarry;
    protected final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy;

    @SuppressWarnings("UnstableApiUsage")
    public QuarryLogic(IRecipeLogicMachine machine) {
        super(machine);
        this.quarry = (IQuarry) machine;
        this.capabilitiesProxy = Tables.newCustomTable(new EnumMap<>(IO.class), IdentityHashMap::new);
    }

    @Override
    public @NotNull Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy() {
        return null;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
