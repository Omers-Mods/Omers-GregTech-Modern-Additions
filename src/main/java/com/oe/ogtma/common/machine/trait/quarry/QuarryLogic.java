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
import net.minecraft.world.level.block.Block;
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
import com.mojang.datafixers.util.Pair;
import com.oe.ogtma.api.area.QuarryArea;
import com.oe.ogtma.common.data.OAMaterialBlocks;
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
    protected final IgnoreEnergyRecipeHandler inputEnergyHandler;

    @Persisted
    protected QuarryArea.QuarryAreaIterator areaIterator;
    @Persisted
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
        this.last = quarry.getPos();
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

    protected @Nullable QuarryArea.QuarryAreaIterator getAreaIterator() {
        if (quarry.getArea() == null || quarry.getQuarryStage() == QuarryMachine.INITIAL) {
            areaIterator = null;
        } else if (areaIterator == null) {
            areaIterator = quarry.getArea().iterator();
        }
        return areaIterator;
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
                if (isWorking()) {
                    setWaiting(Component.translatable("gtceu.recipe_logic.insufficient_out").append(": ")
                            .append(ItemRecipeCapability.CAP.getName()));
                }
            }

            // if there are blocks to mine and the correct amount of time has passed, do the mining
            if (getMachine().getOffsetTimer() %
                    (quarry.getQuarryStage() == QuarryMachine.CLEARING ? speed / 3 : speed) == 0) {
                for (int i = 0; i < blocksToMine.length; i++) {
                    var pos = blocksToMine[i];
                    var blockState = serverLevel.getBlockState(pos);
                    if (skipBlock(pos, blockState)) {
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
                if (inventoryFull) {
                    return;
                }
                var result = getBlocksToMine();
                if (result.getSecond() == 0 && (areaIterator == null || !areaIterator.hasNext())) {
                    if (quarry.getQuarryStage() == QuarryMachine.CLEARING) {
                        quarry.getDrill().setTargetAir(false);
                        quarry.setQuarryStage(QuarryMachine.QUARRYING);
                        areaIterator = null;
                    } else if (quarry.getQuarryStage() == QuarryMachine.QUARRYING) {
                        done = true;
                        setStatus(Status.IDLE);
                    }
                }
                if (quarry.getQuarryStage() == QuarryMachine.QUARRYING) {
                    var drill = quarry.getDrill();
                    if (drill != null) {
                        drill.setTargets(blocksToMine);
                        drill.setMoveTarget(result.getFirst());
                    }
                }
            }
        } else {
            // machine isn't working enabled
            setStatus(Status.IDLE);
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
        }
    }

    protected boolean skipBlock(BlockPos pos, BlockState blockState) {
        return pos.equals(quarry.getPos()) || switch (quarry.getQuarryStage()) {
            case QuarryMachine.CLEARING -> blockState.isAir() && (areaIterator == null || !areaIterator.isEdge(pos));
            case QuarryMachine.QUARRYING -> blockState.isAir() ||
                    (blockState.getFluidState() != Fluids.EMPTY.defaultFluidState() &&
                            !blockState.getFluidState().isSource());
            default -> true;
        } || (!blockState.isAir() && blockState.getDestroySpeed(quarry.getLevel(), pos) <= 0);
    }

    protected Pair<BlockPos, Integer> getBlocksToMine() {
        var level = quarry.getLevel();
        var quarryPos = quarry.getPos();
        var iterator = getAreaIterator();
        var exists = iterator != null;
        int x = 0, y = 0, z = 0, count = 0;
        for (int i = 0, chances = 0; i < blocksToMine.length; i++) {
            var pos = quarryPos;
            if (exists && iterator.hasNext()) {
                pos = iterator.next();
                x += pos.getX();
                y = Math.max(y, pos.getY());
                z += pos.getZ();
                last = pos;
                count++;
                var state = level.getBlockState(pos);
                if (skipBlock(pos, state)) {
                    if (chances < blocksToMine.length * quarry.getVoltageTier() - 1) {
                        chances++;
                        i--;
                        continue;
                    }
                    pos = quarryPos;
                }
            }
            blocksToMine[i] = pos;
        }
        if (count == 0) {
            return Pair.of(last, 0);
        }
        return Pair.of(new BlockPos(x / count, y, z / count), count);
    }

    protected boolean checkCanMine() {
        return !done && quarry.getQuarryStage() != QuarryMachine.INITIAL;
    }

    public void resetArea(boolean checkToMine) {
        if (isDone()) {
            setWorkingEnabled(false);
        }
        done = false;
        if (checkToMine) {
            areaIterator = null;
        }
    }

    @Override
    public void resetRecipeLogic() {
        super.resetRecipeLogic();
        areaIterator = null;
        cachedItemHandler = null;
    }

    @Override
    public void inValid() {
        super.inValid();
        cachedItemHandler = null;
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
                if (match.handleRecipeIO(IO.OUT, this, chanceCaches)) {
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
        if (quarry.getQuarryStage() == QuarryMachine.CLEARING) {
            var iterator = getAreaIterator();
            if (iterator != null && iterator.isEdge(pos)) {
                quarry.getLevel().setBlock(pos,
                        OAMaterialBlocks.QUARRY_PIPE_BLOCKS[quarry.getVoltageTier()].getDefaultState(),
                        Block.UPDATE_ALL);
            } else {
                quarry.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        } else if (quarry.getQuarryStage() == QuarryMachine.QUARRYING) {
            var handler = getCachedItemHandler();
            if (GTTransferUtils.addItemsToItemHandler(handler, true, blockDrops)) {
                GTTransferUtils.addItemsToItemHandler(handler, false, blockDrops);
                quarry.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

                // if the inventory was previously considered full, mark it as not since an item was able to fit
                inventoryFull = false;
            } else {
                // the ore block was not able to fit, so the inventory is considered full
                inventoryFull = true;
            }
        }
    }
}
