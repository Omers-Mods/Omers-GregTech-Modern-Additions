package com.oe.ogtma;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.FinishedRecipe;

import com.oe.ogtma.common.data.*;

import java.util.function.Consumer;

import static com.oe.ogtma.OGTMA.REGISTRATE;

@GTAddon
public class OAGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return OGTMA.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        OARecipes.init(provider);
    }

    @Override
    public void registerCovers() {
        OACovers.init();
    }
}
