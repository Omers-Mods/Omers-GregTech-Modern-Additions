package com.oe.ogtma.common.data;

import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.client.renderer.machine.QuantumTankRenderer;

import net.minecraft.network.chat.Component;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.machine.storage.EntangledQuantumTankMachine;

import static com.oe.ogtma.OGTMA.REGISTRATE;

public class Machines {

    public static final MachineDefinition ENTANGLED_TANK = REGISTRATE
            .machine("entangled_tank", EntangledQuantumTankMachine::new)
            .langValue("Entangled Tank")
            .rotationState(RotationState.ALL)
            .tooltipBuilder(((stack, list) -> {
                // todo: add tooltips
                list.add(Component.literal("Hi, remember to add tooltips dumb-dumb"));
            }))
            .renderer(() -> new QuantumTankRenderer(1))
            .hasTESR(true)
            .register();

    public static void init() {
        OGTMA.LOGGER.info("Registering Machines");
    }
}
