package com.oe.ogtma;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.oe.ogtma.api.data.accessor.OAEntityDataSerializers;
import com.oe.ogtma.common.data.*;
import com.oe.ogtma.common.network.OANetwork;
import com.oe.ogtma.config.OAConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.oe.ogtma.OGTMA.MOD_ID;

@Mod(MOD_ID)
public class OGTMA {

    public static final String MOD_ID = "ogtma";
    public static final Logger LOGGER = LogManager.getLogger();
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(MOD_ID);
    public static final String NAME = "Omer's GregTech Modern Additions";

    public OGTMA() {
        init();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addGenericListener(MachineDefinition.class, this::registerMachines);
    }

    private void init() {
        OAConfig.init();
        OAEntityDataSerializers.init();
        OACreativeModeTabs.init();
        REGISTRATE.creativeModeTab(OACreativeModeTabs.GENERAL);
        OABlocks.init();
        OAItems.init();
        OAEntities.init();
        OANetwork.init();

        REGISTRATE.registerRegistrate();

        OADatagen.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    private void registerMachines(final GTCEuAPI.RegisterEvent<ResourceLocation, MachineDefinition> event) {
        OAMaterialBlocks.init();
        OABlockEntities.init();
        OAMachines.init();
    }
}
