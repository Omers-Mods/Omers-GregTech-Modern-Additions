package com.oe.ogtma.client.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.client.layers.OALayers;
import com.oe.ogtma.client.model.entity.quarry.DrillModel;

@Mod.EventBusSubscriber(modid = OGTMA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEventListener {

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(OALayers.QUARRY_LAYER, DrillModel::createBodyLayer);
    }
}
