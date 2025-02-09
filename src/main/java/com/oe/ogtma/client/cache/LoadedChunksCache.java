package com.oe.ogtma.client.cache;

import com.lowdragmc.lowdraglib.utils.ColorUtils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.network.OANetwork;
import com.oe.ogtma.common.network.packets.dev.ChunkLoadedPacket;
import com.oe.ogtma.config.OAConfig;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = OGTMA.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class LoadedChunksCache {

    private static long ticks = 0;
    public static final Set<Long> LOADED = new HashSet<>();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (OAConfig.INSTANCE.dev.loadedChunkOverlay) {
            if (event.phase == TickEvent.Phase.START && ticks++ % 20 == 0 &&
                    Minecraft.getInstance().cameraEntity != null) {
                OANetwork.NETWORK.sendToServer(
                        new ChunkLoadedPacket(Minecraft.getInstance().cameraEntity.chunkPosition().toLong()));
            }
        } else {
            LOADED.clear();
        }
    }

    @SubscribeEvent
    public static void onRenderLast(RenderGuiOverlayEvent.Post event) {
        if (OAConfig.INSTANCE.dev.loadedChunkOverlay &&
                LoadedChunksCache.LOADED.contains(Minecraft.getInstance().player.chunkPosition().toLong())) {
            event.getGuiGraphics().drawString(Minecraft.getInstance().font, "Loaded", 0, 0,
                    ColorUtils.color(255, 255, 0, 0));
        }
    }
}
