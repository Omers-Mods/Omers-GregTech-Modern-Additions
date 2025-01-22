package com.oe.ogtma.common.data;

import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Items;

import com.oe.ogtma.OGTMA;

import java.util.function.Consumer;

public class Recipes {

    public static void init(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapelessRecipe(provider, OGTMA.id("entangled_tank"), Machines.ENTANGLED_TANK.asStack(),
                "s", 's', Items.STICK);
    }
}
