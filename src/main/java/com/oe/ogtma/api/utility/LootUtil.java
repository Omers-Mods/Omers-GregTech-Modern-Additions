package com.oe.ogtma.api.utility;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;

public class LootUtil {

    public static boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }

    public static boolean isFluid(BlockState state) {
        return !state.getFluidState().isEmpty() && state.getFluidState().isSource();
    }

    public static void getRegularBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                            LootParams.Builder builder) {
        blockDrops.addAll(blockState.getDrops(builder));
    }

    public static void getSilkTouchBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState) {
        blockDrops.add(new ItemStack(blockState.getBlock()));
    }

    public static void getSmallOreBlockDrops(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                             LootParams.Builder builder) {
        // this is a noop :shrug:
    }

    public static FluidIngredient getFluidBlockDrops(BlockState blockState) {
        var state = blockState.getFluidState();
        return FluidIngredient
                .of(state.isEmpty() || !state.isSource() ? FluidStack.EMPTY : new FluidStack(state.getType(), 1000));
    }
}
