package com.oe.ogtma;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import com.oe.ogtma.common.data.Recipes;

import java.util.function.Consumer;

@GTAddon
public class OGTMAGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return OGTMA.REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return OGTMA.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        Recipes.init(provider);
    }
}
