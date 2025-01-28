package com.oe.ogtma.common.machine.trait.quarry;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.IgnoreEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.misc.ItemRecipeHandler;
import com.gregtechceu.gtceu.api.transfer.item.NotifiableAccountedInvWrapper;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.oe.ogtma.api.area.QuarryArea;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;
import com.oe.ogtma.common.machine.quarry.def.IQuarry;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuarryLogic extends RecipeLogic implements IRecipeCapabilityHolder {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuarryLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);
    public static final ChunkPos EMPTY = new ChunkPos(ChunkPos.INVALID_CHUNK_POS);

    @Nullable
    protected NotifiableAccountedInvWrapper cachedItemHandler = null;
    protected final IQuarry quarry;
    @Persisted
    @Getter
    protected int speed;
    @Persisted
    @Getter
    protected int fortune;
    @Persisted
    @Getter
    @Setter
    protected boolean silkTouchMode;
    @Getter
    protected ItemStack pickaxeTool;
    protected final Set<Long> blocksToMine = new LongLinkedOpenHashSet();
    @Nullable
    @Persisted
    @Getter
    protected ChunkPos chunk;
    @Nullable
    @Persisted
    @Getter
    protected BlockPos.MutableBlockPos lastMiningPos;
    @Nullable
    @Persisted
    @Getter
    protected QuarryArea area;
    @Getter
    @Setter
    protected QuarryDrillEntity drillEntity;
    @Persisted
    @Getter
    protected boolean done;
    @Getter
    protected boolean inventoryFull;
    @Getter
    protected final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy;
    protected final ItemRecipeHandler inputItemHandler, outputItemHandler;
    protected final IgnoreEnergyRecipeHandler inputEnergyHandler;

    @SuppressWarnings("UnstableApiUsage")
    public QuarryLogic(IRecipeLogicMachine machine, int speed, int fortune, Object... args) {
        super(machine);
        this.quarry = (IQuarry) machine;
        this.capabilitiesProxy = Tables.newCustomTable(new EnumMap<>(IO.class), IdentityHashMap::new);
        this.speed = speed;
        this.fortune = fortune;
        this.done = false;
        this.pickaxeTool = GTMaterialItems.TOOL_ITEMS.get(GTMaterials.Neutronium, GTToolType.PICKAXE).get().get();
        this.pickaxeTool.enchant(Enchantments.BLOCK_FORTUNE, fortune);
        this.inputItemHandler = createInputItemHandler(args);
        this.outputItemHandler = createOutputItemHandler(args);
        this.inputEnergyHandler = new IgnoreEnergyRecipeHandler();
        this.capabilitiesProxy.put(IO.IN, inputItemHandler.getCapability(), List.of(inputItemHandler));
        this.capabilitiesProxy.put(IO.IN, inputEnergyHandler.getCapability(), List.of(inputEnergyHandler));
        this.capabilitiesProxy.put(IO.OUT, outputItemHandler.getCapability(), List.of(outputItemHandler));
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    protected ItemRecipeHandler createInputItemHandler(Object... args) {
        return new ItemRecipeHandler(IO.IN, machine.getRecipeType().getMaxInputs(ItemRecipeCapability.CAP));
    }

    protected ItemRecipeHandler createOutputItemHandler(Object... args) {
        return new ItemRecipeHandler(IO.OUT, machine.getRecipeType().getMaxOutputs(ItemRecipeCapability.CAP));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    //////////////////////////////////////
    // ******** FACTS & LOGIC **********//
    //////////////////////////////////////

    @Override
    public void serverTick() {
        // todo: implement quarry logic
    }

    protected boolean checkCanMine() {
        // todo: check if iterator has next block
        return !done;
    }

    public void resetArea(boolean checkToMine) {
        // todo: reset the area iterator
    }

    @Override
    public void resetRecipeLogic() {
        super.resetRecipeLogic();
        lastMiningPos = null;
        chunk = null;
        cachedItemHandler = null;
    }

    @Override
    public void inValid() {
        super.inValid();
        this.cachedItemHandler = null;
        resetArea(false);
    }

    public void onRemove() {
        drillEntity.discard();
    }
}
