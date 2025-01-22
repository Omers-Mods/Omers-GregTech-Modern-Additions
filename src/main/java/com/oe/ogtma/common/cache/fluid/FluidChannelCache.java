package com.oe.ogtma.common.cache.fluid;

import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;

import com.oe.ogtma.api.channel.cache.ChannelCache;

import java.util.Objects;
import java.util.function.Supplier;

public class FluidChannelCache extends ChannelCache<FluidChannelHandler> {

    protected static final Supplier<ServerLevel> LEVEL = () -> ServerLifecycleHooks.getCurrentServer().overworld();
    protected static FluidChannelCache instance;

    public FluidChannelCache(CompoundTag tag) {
        this();
        load(tag);
    }

    public FluidChannelCache() {
        super(LEVEL);
        instance = this;
    }

    public static FluidChannelCache getInstance() {
        if (instance != null) {
            return instance;
        }
        return LEVEL.get()
                .getDataStorage()
                .computeIfAbsent(FluidChannelCache::new, FluidChannelCache::new, "oe_fluid_channel");
    }

    @Override
    public FluidChannelHandler getOrCreateHandler(IMachineOwner owner, int channel) {
        final IMachineOwner machineOwner = Objects.requireNonNullElse(owner, PUBLIC);
        if (channels.contains(machineOwner, channel)) {
            return channels.get(machineOwner, channel);
        }
        var handler = new FluidChannelHandler(1000000);
        channels.put(machineOwner, channel, handler);
        setDirty();
        return handler;
    }

    @Override
    public FluidChannelHandler deserializeNBT(CompoundTag tag) {
        var handler = new FluidChannelHandler();
        handler.deserializeNBT(tag);
        return handler;
    }
}
