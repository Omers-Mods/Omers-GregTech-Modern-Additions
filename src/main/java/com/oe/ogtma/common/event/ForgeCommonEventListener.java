package com.oe.ogtma.common.event;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.command.OACommands;

@Mod.EventBusSubscriber(modid = OGTMA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeCommonEventListener {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        OACommands.register(event.getDispatcher(), event.getBuildContext());
    }
}
