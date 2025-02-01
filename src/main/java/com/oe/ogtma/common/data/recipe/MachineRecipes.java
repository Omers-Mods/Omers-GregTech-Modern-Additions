package com.oe.ogtma.common.data.recipe;

import com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader;

import net.minecraft.data.recipes.FinishedRecipe;

import com.oe.ogtma.common.data.OAMachines;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.*;

public class MachineRecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        MetaTileEntityLoader.registerMachineRecipe(provider, OAMachines.QUARRY, "CPC", "MHM", "BEB", 'C', CIRCUIT, 'P',
                PISTON, 'M', MOTOR, 'H', HULL, 'B', CABLE, 'E', EMITTER);
    }
}
