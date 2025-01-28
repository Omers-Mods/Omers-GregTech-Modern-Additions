package com.oe.ogtma.common.command;

import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;

import com.mojang.brigadier.CommandDispatcher;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;

import java.util.Arrays;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class OACommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(
                literal("ogtma")
                        .requires(source -> source.hasPermission(3))
                        .then(literal("quarry_drill_debug")
                                .then(literal("target")
                                        .then(argument("position", BlockPosArgument.blockPos())
                                                .executes(context -> setQuarryDrillDebugPos(
                                                        BlockPosArgument.getBlockPos(context, "position")))))
                                .then(literal("reset")
                                        .executes(context -> setQuarryDrillDebugPos(null)))));
    }

    private static int setQuarryDrillDebugPos(BlockPos pos) {
        QuarryDrillEntity.DRILLS.forEach(entity -> {
            if (pos == null) {
                entity.setTargets(QuarryDrillEntity.NO_TARGET);
                return;
            }
            var targets = entity.getTargets();
            if (!Arrays.asList(targets).contains(pos)) {
                var arr = new BlockPos[targets.length + 1];
                System.arraycopy(targets, 0, arr, 0, targets.length);
                arr[targets.length] = pos;
                entity.setTargets(arr);
            }
        });
        return 1;
    }
}
