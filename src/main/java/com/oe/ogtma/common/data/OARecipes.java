package com.oe.ogtma.common.data;

import net.minecraft.data.recipes.FinishedRecipe;

import com.oe.ogtma.common.data.recipe.MachineRecipes;

import java.util.function.Consumer;

public class OARecipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        MachineRecipes.init(provider);
    }
}
