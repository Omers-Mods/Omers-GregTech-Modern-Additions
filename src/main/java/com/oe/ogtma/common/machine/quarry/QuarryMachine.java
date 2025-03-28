package com.oe.ogtma.common.machine.quarry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.WidgetUtils;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.gui.editor.EditableUI;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputBoth;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.*;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.oe.ogtma.OGTMA;
import com.oe.ogtma.api.area.QuarryArea;
import com.oe.ogtma.api.gui.configurator.EnumSelectorConfigurator;
import com.oe.ogtma.api.gui.tab.SlotsTab;
import com.oe.ogtma.api.utility.OAMachineUtils;
import com.oe.ogtma.common.blockentity.marker.MarkerBlockEntity;
import com.oe.ogtma.common.data.OAEntities;
import com.oe.ogtma.common.data.OAMaterialBlocks;
import com.oe.ogtma.common.entity.quarry.QuarryDrillEntity;
import com.oe.ogtma.common.machine.quarry.def.IQuarry;
import com.oe.ogtma.common.machine.quarry.def.QuarryFluidMode;
import com.oe.ogtma.common.machine.trait.quarry.QuarryLogic;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class QuarryMachine extends WorkableTieredMachine
                           implements IQuarry, IControllable, IFancyUIMachine, IDataInfoProvider, IAutoOutputBoth {

    protected static final int NUM_ARGS = 4;
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(QuarryMachine.class,
            WorkableTieredMachine.MANAGED_FIELD_HOLDER);

    public static final Direction[] HORIZONTAL = { Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST };
    public static final int INITIAL = 0;
    public static final int CLEARING = 1;
    public static final int QUARRYING = 2;

    @Persisted
    @Getter
    protected int quarryStage;
    @Persisted
    @DropSaved
    @Getter
    @Setter
    protected QuarryFluidMode quarryFluidMode = QuarryFluidMode.IGNORE;
    @Nullable
    @Persisted
    @Getter
    protected QuarryArea area;
    @Setter
    protected QuarryDrillEntity drill;
    @Getter
    protected final long euPerTick;
    @Persisted
    @Getter
    protected final CustomItemStackHandler chargerInventory;
    @Nullable
    protected TickableSubscription autoOutputSubs, batterySubs, formingSubs;
    @Nullable
    protected ISubscription exportItemSubs, exportFluidSubs, energySubs;

    @Persisted
    @DescSynced
    @RequireRerender
    protected Direction outputFacingItems;
    @Persisted
    @DescSynced
    @RequireRerender
    protected Direction outputFacingFluids;
    @Persisted
    @DescSynced
    @RequireRerender
    @Getter
    protected boolean autoOutputItems;
    @Persisted
    @DescSynced
    @RequireRerender
    @Getter
    protected boolean autoOutputFluids;
    @Persisted
    @Getter
    @Setter
    protected boolean allowInputFromOutputSideItems;
    @Persisted
    @Getter
    @Setter
    protected boolean allowInputFromOutputSideFluids;

    public QuarryMachine(IMachineBlockEntity holder, int tier, int speed, int fortune,
                         Object... args) {
        super(holder, tier, GTMachineUtils.defaultTankSizeFunction, args,
                OAMachineUtils.tankNumberScaling.applyAsInt(tier), OAMachineUtils.inventorySizeScaling.applyAsInt(tier),
                Math.min(fortune, 3), speed);
        this.euPerTick = GTValues.V[tier] / 4;
        this.chargerInventory = createChargerItemHandler();
        this.outputFacingItems = hasFrontFacing() ? getFrontFacing().getOpposite() : Direction.UP;
        this.outputFacingFluids = outputFacingItems;
    }

    //////////////////////////////////////
    // ********* Initialization ********//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        var len = args.length;
        if (len >= NUM_ARGS && args[len - 2] instanceof Integer fortune && args[len - 1] instanceof Integer speed) {
            return new QuarryLogic(this, speed, fortune);
        }
        throw new IllegalArgumentException(
                "QuarryMachine needs args [tankCount, inventorySize, fortune, speed] for initialization");
    }

    protected CustomItemStackHandler createChargerItemHandler(Object... args) {
        var handler = new CustomItemStackHandler();
        handler.setFilter(item -> GTCapabilityHelper.getElectricItem(item) != null ||
                (ConfigHolder.INSTANCE.compat.energy.nativeEUToFE &&
                        GTCapabilityHelper.getForgeEnergyItem(item) != null));
        return handler;
    }

    @Override
    protected NotifiableItemStackHandler createImportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 0, IO.NONE);
    }

    @Override
    protected NotifiableItemStackHandler createExportItemHandler(Object... args) {
        var len = args.length;
        if (len >= NUM_ARGS && args[len - 3] instanceof Integer invSize) {
            return new NotifiableItemStackHandler(this, invSize, IO.OUT, IO.BOTH);
        }
        throw new IllegalArgumentException(
                "QuarryMachine needs args [tankCount, inventorySize, fortune, speed] for initialization");
    }

    @Override
    protected NotifiableFluidTank createExportFluidHandler(Object... args) {
        var len = args.length;
        if (len >= NUM_ARGS && args[len - 4] instanceof Integer tankCount) {
            return new NotifiableFluidTank(this, tankCount, tankScalingFunction.apply(tier), IO.OUT);
        }
        throw new IllegalArgumentException(
                "QuarryMachine needs args [tankCount, inventorySize, fortune, speed] for initialization");
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return exportFluids;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(exportItems.storage);
        clearInventory(chargerInventory);
    }

    @Override
    public QuarryLogic getRecipeLogic() {
        return (QuarryLogic) super.getRecipeLogic();
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            if (getLevel() instanceof ServerLevel serverLevel) {
                serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
            }
            updateBatterySubscription();
            formingSubs = subscribeServerTick(formingSubs, this::tryFormQuarry);
            exportItemSubs = exportItems.addChangedListener(this::updateAutoOutputSubscription);
            exportFluidSubs = exportFluids.addChangedListener(this::updateAutoOutputSubscription);
            energySubs = energyContainer.addChangedListener(this::updateBatterySubscription);
            chargerInventory.setOnContentsChanged(this::updateBatterySubscription);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (exportItemSubs != null) {
            exportItemSubs.unsubscribe();
            exportItemSubs = null;
        }
        if (exportFluidSubs != null) {
            exportFluidSubs.unsubscribe();
            exportFluidSubs = null;
        }
        if (energySubs != null) {
            energySubs.unsubscribe();
            energySubs = null;
        }
        if (drill != null) {
            drill.discard();
            drill = null;
        }
        getRecipeLogic().getLoadedChunks().forEach(pos -> unloadChunk(pos.x, pos.z));
    }

    @Override
    public int getVoltageTier() {
        return tier;
    }

    @Nullable
    public QuarryDrillEntity getDrill() {
        if (isRemote()) {
            return drill;
        }
        if (getQuarryStage() == INITIAL || getRecipeLogic().isDone()) {
            drill = null;
        } else {
            if (area == null) {
                setWorkingEnabled(false);
                return null;
            }
            area.setFromQuarry(this);
            if (drill == null) {
                drill = OAEntities.QUARRY_DRILL.create(getLevel());
                drill.setTier(tier);
                drill.setPos((double) (area.getMaxX() + area.getMinX()) / 2, area.getMaxY(),
                        (double) (area.getMaxZ() + area.getMinZ()) / 2);
                getLevel().addFreshEntity(drill);
                drill.setQuarryPos(getPos());
            }
            drill.setTargetAir(getQuarryStage() == CLEARING);
            drill.setAirColor(OAMaterialBlocks.QUARRY_BLOCKS[tier].get().material.getMaterialRGB());
            drill.setQuarryBox(area == null ? drill.getBoundingBox() : area.getViewBox());
        }
        return drill;
    }

    protected void tryFormQuarry() {
        if (!isRemote() && getOffsetTimer() % 10 == 0) {
            if (quarryStage != INITIAL && formingSubs != null) {
                formingSubs.unsubscribe();
                formingSubs = null;
                return;
            }
            var pos = getPos();
            for (var direction : HORIZONTAL) {
                // if neighbor isn't a marker skip
                if (!(getLevel().getBlockEntity(pos.relative(direction)) instanceof MarkerBlockEntity marker)) {
                    continue;
                }
                // if marker doesn't have the required connections skip
                if (!marker.hasConnections(Direction.Axis.X, Direction.Axis.Z)) {
                    continue;
                }
                // marker has the required connections form area
                var area = new QuarryArea();
                area.setFromQuarry(this);
                area.setFromMarker(marker, Direction.Axis.X, Direction.Axis.Z);
                if (Mth.clamp(pos.getX(), area.getMinX(), area.getMaxX()) == pos.getX() &&
                        Mth.clamp(pos.getY(), area.getMinY(), area.getMaxY()) == pos.getY() &&
                        Mth.clamp(pos.getZ(), area.getMinZ(), area.getMaxZ()) == pos.getZ()) {
                    continue;
                }
                area.setMaxY(area.getMinY() + 4);
                marker.destroy(Direction.Axis.X, Direction.Axis.Z);
                setArea(area);
                setQuarryStage(CLEARING);
                setWorkingEnabled(true);
            }
        }
    }

    public void setQuarryStage(int stage) {
        if (isRemote()) {
            return;
        }
        if (stage != INITIAL) {
            if (area != null) {
                area.quarrying(stage == QUARRYING);
            }
        }
        this.quarryStage = stage;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        super.setWorkingEnabled(isWorkingAllowed);
        if (getRecipeLogic().isDone() && isWorkingAllowed) {
            getRecipeLogic().resetArea(true);
        }
    }

    //////////////////////////////////////
    // ******** FACTS & LOGIC **********//
    //////////////////////////////////////

    protected void updateAutoOutputSubscription() {
        var outputFacingItems = getOutputFacingItems();
        var outputFacingFluids = getOutputFacingFluids();
        if ((isAutoOutputItems() && !exportItems.isEmpty() && outputFacingItems != null &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getPos(), outputFacingItems)) ||
                (isAutoOutputFluids() && !exportFluids.isEmpty() && outputFacingFluids != null &&
                        GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getPos(), outputFacingFluids))) {
            autoOutputSubs = subscribeServerTick(autoOutputSubs, this::autoOutput);
        } else if (autoOutputSubs != null) {
            autoOutputSubs.unsubscribe();
            autoOutputSubs = null;
        }
    }

    protected void updateBatterySubscription() {
        if (energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, true)) {
            batterySubs = subscribeServerTick(batterySubs, this::chargeBattery);
        } else if (batterySubs != null) {
            batterySubs.unsubscribe();
            batterySubs = null;
        }
    }

    protected void autoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputFluids() && getOutputFacingFluids() != null) {
                exportFluids.exportToNearby(getOutputFacingFluids());
            }
            if (isAutoOutputItems() && getOutputFacingItems() != null) {
                exportItems.exportToNearby(getOutputFacingItems());
            }
        }
        updateAutoOutputSubscription();
    }

    protected void chargeBattery() {
        if (!energyContainer.dischargeOrRechargeEnergyContainers(chargerInventory, 0, false)) {
            updateBatterySubscription();
        }
    }

    protected void setArea(QuarryArea area) {
        this.area = area;
    }

    @Override
    public void loadChunk(int x, int z) {
        if (isRemote()) {
            return;
        }
        ForgeChunkManager.forceChunk((ServerLevel) getLevel(), OGTMA.MOD_ID, getPos(), x, z, true, true);
    }

    @Override
    public void unloadChunk(int x, int z) {
        if (isRemote()) {
            return;
        }
        ForgeChunkManager.forceChunk((ServerLevel) getLevel(), OGTMA.MOD_ID, getPos(), x, z, false, true);
    }

    //////////////////////////////////////
    // ************** GUI **************//
    //////////////////////////////////////

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        IFancyUIMachine.super.attachSideTabs(sideTabs);
        var sqrtSlots = (int) Math.sqrt(exportItems.getSlots());
        sideTabs.attachSubTab(new SlotsTab(exportItems, exportFluids, sqrtSlots));
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        IFancyUIMachine.super.attachConfigurators(configuratorPanel);
        configuratorPanel.attachConfigurators(
                new EnumSelectorConfigurator<>(quarryFluidMode, QuarryFluidMode.values(), this::setQuarryFluidMode));
    }

    public static BiFunction<ResourceLocation, Integer, EditableMachineUI> EDITABLE_UI_CREATOR = Util
            .memoize((path, inventorySize) -> new EditableMachineUI("misc", path, () -> {
                var template = createTemplate().createDefault();
                var batterySlot = createBatterySlot().createDefault();
                batterySlot.setSelfPosition(new Position(150, 10));
                var group = new WidgetGroup(0, 0, 172, template.getSize().height + 8);
                var size = group.getSize();

                template.setSelfPosition(new Position(
                        (size.width - 4 - template.getSize().width) / 2 + 4,
                        (size.height - template.getSize().height) / 2));

                group.addWidget(template);
                group.addWidget(batterySlot);
                return group;
            }, (template, machine) -> {
                if (machine instanceof QuarryMachine quarryMachine) {
                    createTemplate().setupUI(template, quarryMachine);
                    createEnergyBar().setupUI(template, quarryMachine);
                    createBatterySlot().setupUI(template, quarryMachine);
                }
            }));

    protected static EditableUI<WidgetGroup, QuarryMachine> createTemplate() {
        return new EditableUI<>("quarry", WidgetGroup.class, () -> {
            var group = new WidgetGroup(0, 0, 120, 100);

            var componentPanel = new ComponentPanelWidget(0, 0, list -> {});
            componentPanel.setMaxWidthLimit(110);
            componentPanel.setId("component_panel");

            var container = new WidgetGroup(0, 0, 117, 100);
            container.addWidget(new DraggableScrollableWidgetGroup(4, 4, container.getSize().width - 8,
                    container.getSize().height - 8)
                    .setBackground(GuiTextures.DISPLAY)
                    .addWidget(componentPanel));
            container.setBackground(GuiTextures.BACKGROUND_INVERSE);
            group.addWidget(container);
            return group;
        }, (group, machine) -> {
            WidgetUtils.widgetByIdForEach(group, "^component_panel$", ComponentPanelWidget.class, panel -> {
                panel.textSupplier(machine::addDisplayText);
            });
        });
    }

    protected static EditableUI<SlotWidget, QuarryMachine> createBatterySlot() {
        return new EditableUI<>("battery_slot", SlotWidget.class, () -> {
            var slotWidget = new SlotWidget();
            slotWidget.setBackground(GuiTextures.SLOT, GuiTextures.CHARGER_OVERLAY);
            return slotWidget;
        }, (slotWidget, machine) -> {
            slotWidget.setHandlerSlot(machine.chargerInventory, 0);
            slotWidget.setCanPutItems(true);
            slotWidget.setCanTakeItems(true);
            slotWidget.setHoverTooltips(LangHandler.getMultiLang("gtceu.gui.charger_slot.tooltip",
                    GTValues.VNF[machine.getTier()], GTValues.VNF[machine.getTier()]).toArray(new MutableComponent[0]));
        });
    }

    protected void addDisplayText(@NotNull List<Component> textList) {
        var pos = getRecipeLogic().getLast();
        if (area != null) {
            if (pos != null) {
                textList.add(Component.translatable("gtceu.machine.miner.startx", area.getMinX()).append(" ")
                        .append(Component.translatable("gtceu.machine.miner.minex", pos.getX())));
                textList.add(Component.translatable("gtceu.machine.miner.starty", area.getMaxY()).append(" ")
                        .append(Component.translatable("gtceu.machine.miner.miney", pos.getY())));
                textList.add(Component.translatable("gtceu.machine.miner.startz", area.getMinZ()).append(" ")
                        .append(Component.translatable("gtceu.machine.miner.minez", pos.getZ())));
                textList.add(
                        Component.translatable("gtceu.universal.tooltip.working_area", area.getXSize(),
                                area.getZSize()));
            }
        }
        if (getRecipeLogic().isDone()) {
            textList.add(Component.translatable("gtceu.multiblock.large_miner.done")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        } else if (getRecipeLogic().isWorking()) {
            textList.add(Component.translatable("gtceu.multiblock.large_miner.working")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
        } else if (!this.isWorkingEnabled()) {
            textList.add(Component.translatable("gtceu.multiblock.work_paused"));
        }
        if (getRecipeLogic().isInventoryFull()) {
            textList.add(Component.translatable("gtceu.multiblock.large_miner.invfull")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }
        if (!drainInput(true)) {
            textList.add(Component.translatable("gtceu.multiblock.large_miner.needspower")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }
    }

    @Override
    public boolean drainInput(boolean simulate) {
        var resultEnergy = energyContainer.getEnergyStored() - euPerTick;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.removeEnergy(euPerTick);
            return true;
        }
        return false;
    }

    //////////////////////////////////////
    // ********** Interaction **********//
    //////////////////////////////////////
    @NotNull
    @Override
    public List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        if ((mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) && area != null) {
            return Collections.singletonList(
                    Component.translatable("gtceu.universal.tooltip.working_area", area.getXSize(), area.getZSize()));
        }
        return List.of();
    }

    //////////////////////////////////////
    // ********** Auto Output **********//
    //////////////////////////////////////
    @Override
    public boolean hasAutoOutputFluid() {
        return exportFluids.getTanks() > 0;
    }

    @Override
    public boolean hasAutoOutputItem() {
        return exportItems.getSlots() > 0;
    }

    @Override
    @Nullable
    public Direction getOutputFacingFluids() {
        if (hasAutoOutputFluid()) {
            return outputFacingFluids;
        }
        return null;
    }

    @Override
    @Nullable
    public Direction getOutputFacingItems() {
        if (hasAutoOutputItem()) {
            return outputFacingItems;
        }
        return null;
    }

    @Override
    public void setAutoOutputItems(boolean allow) {
        if (hasAutoOutputItem()) {
            this.autoOutputItems = allow;
            updateAutoOutputSubscription();
        }
    }

    @Override
    public void setAutoOutputFluids(boolean allow) {
        if (hasAutoOutputFluid()) {
            this.autoOutputFluids = allow;
            updateAutoOutputSubscription();
        }
    }

    @Override
    public void setOutputFacingFluids(@org.jetbrains.annotations.Nullable Direction outputFacing) {
        if (hasAutoOutputFluid()) {
            this.outputFacingFluids = outputFacing;
            updateAutoOutputSubscription();
        }
    }

    @Override
    public void setOutputFacingItems(@org.jetbrains.annotations.Nullable Direction outputFacing) {
        if (hasAutoOutputItem()) {
            this.outputFacingItems = outputFacing;
            updateAutoOutputSubscription();
        }
    }
}
