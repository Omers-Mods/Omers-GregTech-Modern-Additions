package com.oe.ogtma.common.machine.trait.quarry;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.IgnoreEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.misc.ItemRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.transfer.item.NotifiableAccountedInvWrapper;
import com.gregtechceu.gtceu.common.data.GTMaterialItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.items.IItemHandlerModifiable;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.oe.ogtma.OGTMA;
import com.oe.ogtma.common.machine.quarry.QuarryMachine;
import com.oe.ogtma.common.machine.quarry.def.IQuarry;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.oe.ogtma.api.utility.LootUtil.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class QuarryLogic extends RecipeLogic implements IRecipeCapabilityHolder {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuarryLogic.class,
            RecipeLogic.MANAGED_FIELD_HOLDER);

    protected static final short MAX_SCAN_TICK = 64;
    protected static final short MAX_SPEED = Short.MAX_VALUE;
    protected static final byte POWER = 5;
    protected static final byte TICK_TOLERANCE = 20;
    protected static final double DIVIDEND = MAX_SPEED * Math.pow(TICK_TOLERANCE, POWER);

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
    @Persisted
    protected final BlockPos[] blocksToMine;
    @Persisted
    @Getter
    protected boolean done;
    @Getter
    protected boolean inventoryFull;
    @Getter
    protected final Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> capabilitiesProxy;
    protected final ItemRecipeHandler inputItemHandler, outputItemHandler;
    @Getter
    protected boolean isInventoryFull;
    protected final IgnoreEnergyRecipeHandler inputEnergyHandler;

    protected Iterator<BlockPos> blockIterator;
    @Getter
    protected BlockPos last;

    @SuppressWarnings("UnstableApiUsage")
    public QuarryLogic(IRecipeLogicMachine machine, int speed, int fortune, Object... args) {
        super(machine);
        this.quarry = (IQuarry) machine;
        var temp = new BlockPos[quarry.getVoltageTier() * quarry.getVoltageTier()];
        Arrays.fill(temp, quarry.getPos());
        this.blocksToMine = temp;
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

    protected NotifiableAccountedInvWrapper getCachedItemHandler() {
        if (cachedItemHandler == null) {
            cachedItemHandler = new NotifiableAccountedInvWrapper(machine.getCapabilitiesProxy()
                    .get(IO.OUT, ItemRecipeCapability.CAP).stream()
                    .map(IItemHandlerModifiable.class::cast)
                    .toArray(IItemHandlerModifiable[]::new));
        }
        return cachedItemHandler;
    }

    protected @Nullable Iterator<BlockPos> getBlockIterator() {
        if (quarry.getArea() == null || quarry.getQuarryStage() == QuarryMachine.INITIAL) {
            blockIterator = null;
        } else if (blockIterator == null) {
            blockIterator = quarry.getArea().iterator();
        }
        return blockIterator;
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
        if (!isSuspend() && getMachine().getLevel() instanceof ServerLevel serverLevel && checkCanMine()) {
            // if the inventory is not full, drain energy etc. from the miner
            // the storages have already been checked earlier
            if (!isInventoryFull()) {
                // always drain storages when working, even if blocksToMine ends up being empty
                quarry.drainInput(false);
                // since energy is being consumed the miner is now active
                setStatus(Status.WORKING);
            } else {
                // the miner cannot drain, therefore it is inactive
                if (this.isWorking()) {
                    setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ")
                            .append(ItemRecipeCapability.CAP.getName()));
                }
            }

            // if there are blocks to mine and the correct amount of time has passed, do the mining
            if (getMachine().getOffsetTimer() % this.speed == 0) {
                var quarryPos = quarry.getPos();
                for (int i = 0; i < blocksToMine.length && !blocksToMine[i].equals(quarryPos); i++) {
                    var pos = blocksToMine[i];
                    var blockState = serverLevel.getBlockState(pos);
                    if (skipBlock(pos, blockState)) {
                        OGTMA.LOGGER.debug("Skipping block {}", pos);
                        continue;
                    }
                    var blockDrops = NonNullList.<ItemStack>create();

                    // When we are here we have a block to mine! I'm glad we aren't threaded
                    var builder = new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.BLOCK_STATE, blockState)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(pos))
                            .withParameter(LootContextParams.TOOL, getPickaxeTool());

                    // get the small ore drops, if a small ore
                    getSmallOreBlockDrops(blockDrops, blockState, builder);
                    // get the block's drops.
                    if (isSilkTouchMode()) {
                        getSilkTouchBlockDrops(blockDrops, blockState);
                    } else {
                        getRegularBlockDrops(blockDrops, blockState, builder);
                    }
                    // handle recipe type
                    if (blockState.is(Tags.Blocks.ORES) && !blockDrops.isEmpty() && hasPostProcessing()) {
                        doPostProcessing(blockDrops, blockState, builder);
                    }
                    // try to insert them
                    mineAndInsertItems(blockDrops, pos);
                }
                // get the blocks to mine the next iteration and tell the drill entity
                var moveTarget = getBlocksToMine();
                if (blocksToMine[0].equals(quarryPos)) {
                    this.done = true;
                    this.setStatus(Status.IDLE);
                }
                var drill = quarry.getDrill();
                if (drill != null) {
                    drill.setTargets(blocksToMine);
                    drill.setMoveTarget(moveTarget);
                }
            }
        } else {
            // machine isn't working enabled
            this.setStatus(Status.IDLE);
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    protected boolean skipBlock(BlockPos pos, BlockState blockState) {
        // todo: filter non mine-able blocks
        return blockState.isAir() || (blockState.getFluidState() != Fluids.EMPTY.defaultFluidState() &&
                !blockState.getFluidState().isSource());
    }

    protected BlockPos getBlocksToMine() {
        var iterator = getBlockIterator();
        var exists = iterator != null;
        int x = 0, y = 0, z = 0, count = 0;
        for (int i = 0; i < blocksToMine.length; i++) {
            if (exists && iterator.hasNext()) {
                var pos = iterator.next();
                blocksToMine[i] = pos;
                x += pos.getX();
                y = Math.max(y, pos.getY());
                z += pos.getZ();
                count++;
            } else {
                blocksToMine[i] = quarry.getPos();
            }
        }
        return new BlockPos(x / count, y, z / count);
    }

    protected boolean checkCanMine() {
        return !done && quarry.getQuarryStage() != QuarryMachine.INITIAL;
    }

    public void resetArea(boolean checkToMine) {
        if (isDone()) {
            setWorkingEnabled(false);
        }
        this.done = false;
        if (checkToMine) {
            blockIterator = null;
        }
    }

    @Override
    public void resetRecipeLogic() {
        super.resetRecipeLogic();
        blockIterator = null;
        cachedItemHandler = null;
    }

    @Override
    public void inValid() {
        super.inValid();
        this.cachedItemHandler = null;
        resetArea(false);
    }

    protected boolean hasPostProcessing() {
        return !silkTouchMode;
    }

    protected boolean doPostProcessing(NonNullList<ItemStack> blockDrops, BlockState blockState,
                                       LootParams.Builder builder) {
        var oreDrop = blockDrops.get(0);

        // create dummy recipe handler
        inputItemHandler.storage.setStackInSlot(0, oreDrop);
        outputItemHandler.storage.clear();

        var matches = machine.getRecipeType().searchRecipe(this);

        while (matches != null && matches.hasNext()) {
            var match = matches.next();
            if (match == null) continue;

            var eut = RecipeHelper.getInputEUt(match);
            if (GTUtil.getTierByVoltage(eut) <= quarry.getVoltageTier()) {
                if (match.handleRecipeIO(IO.OUT, this, this.chanceCaches)) {
                    blockDrops.clear();
                    var result = new ArrayList<ItemStack>();
                    for (int i = 0; i < outputItemHandler.storage.getSlots(); ++i) {
                        var stack = outputItemHandler.storage.getStackInSlot(i);
                        if (stack.isEmpty()) continue;
                        result.add(stack);
                    }
                    dropPostProcessing(blockDrops, result, blockState, builder);
                    return true;
                }
            }
        }
        return false;
    }

    protected void dropPostProcessing(NonNullList<ItemStack> blockDrops, List<ItemStack> outputs, BlockState blockState,
                                      LootParams.Builder builder) {
        blockDrops.addAll(outputs);
    }

    protected void mineAndInsertItems(NonNullList<ItemStack> blockDrops, BlockPos pos) {
        // If the block's drops can fit in the inventory, move the previously mined position to the block
        // remove the ore block's position from the mining queue
        var handler = getCachedItemHandler();
        if (GTTransferUtils.addItemsToItemHandler(handler, true, blockDrops)) {
            GTTransferUtils.addItemsToItemHandler(handler, false, blockDrops);
            quarry.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

            // if the inventory was previously considered full, mark it as not since an item was able to fit
            isInventoryFull = false;
        } else {
            // the ore block was not able to fit, so the inventory is considered full
            isInventoryFull = true;
        }
    }
}
