package com.oe.ogtma.integration.jade.provider;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.machine.quarry.QuarryMachine;
import com.oe.ogtma.common.machine.quarry.def.QuarryFluidMode;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public class QuarryBlockProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        if (tag.contains("FluidMode")) {
            var fluidMode = tag.getInt("FluidMode");
            tooltip.add(Component.translatable("ogtma.option.quarry_fluid_mode").append(": ")
                    .append(Component.translatable(QuarryFluidMode.get(fluidMode).getTooltip())));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof MetaMachineBlockEntity mmbe &&
                mmbe.getMetaMachine() instanceof QuarryMachine quarry) {
            tag.putInt("FluidMode", quarry.getQuarryFluidMode().ordinal());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return OGTMA.id("quarry");
    }
}
