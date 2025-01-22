package com.oe.ogtma.api.channel.handler;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import com.oe.ogtma.api.channel.cache.ChannelCache;

public interface IChannelHandler extends INBTSerializable<CompoundTag> {

    boolean shouldSave();

    <T extends IChannelHandler> ChannelCache<T> getCache();
}
