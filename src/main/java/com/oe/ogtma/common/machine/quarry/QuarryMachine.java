package com.oe.ogtma.common.machine.quarry;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

import com.oe.ogtma.api.area.QuarryArea;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;
import com.oe.ogtma.common.machine.quarry.def.IQuarry;
import com.oe.ogtma.common.machine.quarry.def.QuarryFluidMode;
import com.oe.ogtma.common.machine.quarry.def.QuarryMode;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuarryMachine extends WorkableTieredMachine
                           implements IQuarry, IControllable, IFancyUIMachine, IDataInfoProvider {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuarryMachine.class,
            WorkableTieredMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    @Getter
    @Setter
    protected QuarryMode quarryMode = QuarryMode.SURFACE;
    @Persisted
    @DescSynced
    @Getter
    @Setter
    protected QuarryFluidMode quarryFluidMode;
    @Persisted
    @Getter
    @Setter
    protected ChunkPos miningPos;
    @Persisted
    @Getter
    protected int speed;
    @Persisted
    @Getter
    protected int fortune;
    @Persisted
    @Getter
    protected QuarryArea area;
    @Getter
    @Setter
    protected QuarryDrillEntity drillEntity;

    public QuarryMachine(IMachineBlockEntity holder, int tier, Int2IntFunction tankScalingFunction, Object... args) {
        super(holder, tier, tankScalingFunction, args);
    }

    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        // todo: output status
        return List.of();
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
