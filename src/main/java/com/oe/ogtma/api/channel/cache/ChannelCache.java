package com.oe.ogtma.api.channel.cache;

import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;
import com.gregtechceu.gtceu.common.machine.owner.PlayerOwner;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.oe.ogtma.api.channel.handler.IChannelHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

public abstract class ChannelCache<T extends IChannelHandler> extends SavedData {

    protected Supplier<ServerLevel> levelSupplier;
    protected static final IMachineOwner PUBLIC = new PlayerOwner(new UUID(0, 0));
    protected Table<IMachineOwner, Integer, T> channels;

    public ChannelCache(Supplier<ServerLevel> levelSupplier) {
        channels = HashBasedTable.create();
        this.levelSupplier = levelSupplier;
    }

    public ServerLevel getLevel() {
        return levelSupplier.get();
    }

    public abstract T getOrCreateHandler(IMachineOwner owner, int channel);

    public abstract T deserializeNBT(CompoundTag tag);

    public void onCreateHandler(IMachineOwner owner, int channel, T handler) {}

    // handle saving and loading
    public void load(CompoundTag tag) {
        var ownerCount = tag.getInt("OwnerCount");
        for (int i = 0; i < ownerCount; i++) {
            var ownedMapTag = tag.getCompound("" + i);

            var owner = IMachineOwner.create(ownedMapTag.getCompound("Owner"));

            var channelsTag = ownedMapTag.getCompound("Channels");
            var channelCount = channelsTag.getInt("ChannelCount");
            for (int j = 0; j < channelCount; j++) {
                var channel = channelsTag.getCompound("" + j);
                var id = channel.getInt("Id");
                var handler = deserializeNBT(channel.getCompound("Handler"));
                onCreateHandler(owner, id, handler);
                channels.put(owner, id, handler);
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        var root = new CompoundTag();

        var ownerCount = channels.rowKeySet().size();
        root.putInt("OwnerCount", ownerCount);
        var keys = channels.rowKeySet().iterator();
        var rowMap = channels.rowMap();
        for (int i = 0; i < ownerCount; i++) {
            var ownerMapTag = new CompoundTag();
            root.put("" + i, ownerMapTag);

            var ownerTag = new CompoundTag();
            var owner = keys.next();
            owner.save(ownerTag);
            ownerMapTag.put("Owner", ownerTag);

            var channelsTag = new CompoundTag();
            ownerMapTag.put("Channels", channelsTag);
            var ownerChannelsMap = rowMap.get(owner);
            channelsTag.putInt("ChannelCount", ownerChannelsMap.size());
            final int[] j = { 0 };
            ownerChannelsMap.forEach((id, handler) -> {
                if (!handler.shouldSave()) {
                    return;
                }
                var channelTag = new CompoundTag();
                channelsTag.put("" + j[0]++, channelTag);

                channelsTag.putInt("Id", id);
                channelTag.put("Handler", handler.serializeNBT());
            });
        }

        return root;
    }
}
