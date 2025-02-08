package com.oe.ogtma.common.data;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import com.oe.ogtma.common.machine.quarry.QuarryMachine;
import com.oe.ogtma.config.OAConfig;

import java.util.Locale;
import java.util.function.BiFunction;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.MACERATOR_RECIPES;
import static com.oe.ogtma.OGTMA.REGISTRATE;
import static com.oe.ogtma.OGTMA.id;
import static com.oe.ogtma.api.utility.OAMachineUtils.*;

public class OAMachines {

    // todo: rework speed
    public static final MachineDefinition[] QUARRY;
    static {
        if (OAConfig.INSTANCE.features.enableQuarry) {
            QUARRY = registerTieredMachines("quarry", (holder, tier) -> new QuarryMachine(holder, tier,
                    quarrySpeedScaling.applyAsInt(tier), Math.min(tier, 3)),
                    (tier, builder) -> builder
                            .rotationState(RotationState.NON_Y_AXIS)
                            .langValue("%s Quarry %s".formatted(VLVH[tier], VLVT[tier]))
                            .recipeType(MACERATOR_RECIPES)
                            .editableUI(QuarryMachine.EDITABLE_UI_CREATOR.apply(id("quarry"),
                                    inventorySizeScaling.applyAsInt(tier)))
                            .workableTieredHullRenderer(id("block/machines/quarry"))
                            .tooltipBuilder(((stack, tooltip) -> {
                                var euPerTick = V[tier] / 2;
                                var tickSpeed = quarrySpeedScaling.applyAsInt(tier);
                                var perIteration = blocksPerIterationScaling.applyAsInt(tier);
                                tooltip.add(Component.translatable("ogtma.machine.quarry.tooltip"));
                                tooltip.add(Component.translatable("gtceu.universal.tooltip.uses_per_tick", euPerTick)
                                        .append(Component.literal(", ").withStyle(ChatFormatting.GRAY))
                                        .append(Component.translatable("ogtma.machine.quarry.per_blocks",
                                                tickSpeed / 20, perIteration)));
                                tooltip.add(Component.translatable("gtceu.universal.tooltip.voltage_in",
                                        FormattingUtil.formatNumbers(GTValues.V[tier]),
                                        GTValues.VNF[tier]));
                                tooltip.add(Component.translatable("gtceu.universal.tooltip.energy_storage_capacity",
                                        FormattingUtil.formatNumbers(GTValues.V[tier] * 64L)));
                                tooltip.add(Component.translatable("ogtma.machine.quarry.working_area_max"));
                            }))
                            .register(),
                    tiersBetween(LV, IV));
        } else {
            QUARRY = new MachineDefinition[0];
        }
    }

    public static void init() {}

    // utilities
    public static MachineDefinition[] registerTieredMachines(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                             BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                             int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }
}
