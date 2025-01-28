package com.oe.ogtma.api.utility;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;

public class LootUtil {

    public static void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                            LootParams.Builder builder) {
        blockDrops.addAll(blockState.getDrops(builder));
    }

    public static void getSilkTouchBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState) {
        blockDrops.add(new ItemStack(blockState.getBlock()));
    }
}
