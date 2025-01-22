package com.oe.ogtma.common.machine.storage;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomFluidWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputFluid;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.oe.ogtma.common.cache.fluid.FluidChannelCache;
import com.oe.ogtma.common.cache.fluid.FluidChannelHandler;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EntangledQuantumTankMachine extends MetaMachine implements IAutoOutputFluid, IInteractedMachine,
                                         IControllable, IDropSaveMachine, IFancyUIMachine {

    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected Direction outputFacingFluids;
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected boolean autoOutputFluids;
    @Getter
    @Setter
    @Persisted
    protected boolean allowInputFromOutputSideFluids;
    @Persisted
    protected boolean isVoiding;
    @Persisted
    @DescSynced
    @RequireRerender
    protected int channel;
    @Nullable
    protected TickableSubscription autoOutputOwnSubs;
    @Nullable
    protected TickableSubscription autoOutputHandlerSubs;
    @DescSynced
    @RequireRerender
    protected FluidChannelHandler handler;
    protected final FluidChannelWrapper wrapper;

    public EntangledQuantumTankMachine(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.outputFacingFluids = getFrontFacing().getOpposite();
        this.channel = 0;
        this.wrapper = new FluidChannelWrapper(this);
        setHandler(getHandler());
    }

    protected FluidChannelHandler getHandler() {
        if (isRemote()) {
            return new FluidChannelHandler();
        }
        return FluidChannelCache.getInstance().getOrCreateHandler(getHolder().getOwner(), channel);
    }

    protected void setHandler(FluidChannelHandler handler) {
        this.handler = handler;
        updateAutoOutputSubscription();
    }

    protected void setChannel(int channel) {
        this.channel = channel;
        setHandler(getHandler());
    }

    // initializing

    @Override
    public void onLoad() {
        super.onLoad();
        setHandler(getHandler());
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateAutoOutputSubscription));
        }
    }
    
    public void onFluidChanged() {
        if (!isRemote()) {
            updateAutoOutputSubscription();
        }
    }

    // interaction

    @Override
    public boolean isFacingValid(Direction facing) {
        if (facing == outputFacingFluids) {
            return false;
        }
        return super.isFacingValid(facing);
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (hit.getDirection() == getFrontFacing() && !isRemote()) {
            if (FluidUtil.interactWithFluidHandler(player, hand, handler)) {
                return InteractionResult.SUCCESS;
            }
        }
        return IInteractedMachine.super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult onWrenchClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                              BlockHitResult hitResult) {
        if (!playerIn.isShiftKeyDown() && !isRemote()) {
            var tool = playerIn.getItemInHand(hand);
            if (tool.getDamageValue() >= tool.getMaxDamage()) return InteractionResult.PASS;
            if (hasFrontFacing() && gridSide == getFrontFacing()) return InteractionResult.PASS;
            if (gridSide != getOutputFacingFluids()) {
                setOutputFacingFluids(gridSide);
            } else {
                setOutputFacingFluids(null);
            }
            return InteractionResult.CONSUME;
        }

        return super.onWrenchClick(playerIn, hand, gridSide, hitResult);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        if (!isRemote()) {
            if (gridSide == getOutputFacingFluids()) {
                if (isAllowInputFromOutputSideFluids()) {
                    setAllowInputFromOutputSideFluids(false);
                    playerIn.sendSystemMessage(
                            Component.translatable("gtceu.machine.basic.input_from_output_side.disallow")
                                    .append(Component.translatable("gtceu.creative.tank.fluid")));
                } else {
                    setAllowInputFromOutputSideFluids(true);
                    playerIn.sendSystemMessage(
                            Component.translatable("gtceu.machine.basic.input_from_output_side.allow")
                                    .append(Component.translatable("gtceu.creative.tank.fluid")));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.onScrewdriverClick(playerIn, hand, gridSide, hitResult);
    }

    // auto output fluids

    @Override
    public void setAutoOutputFluids(boolean allow) {
        autoOutputFluids = allow;
        updateAutoOutputSubscription();
    }

    @Override
    public void setOutputFacingFluids(@Nullable Direction outputFacing) {
        outputFacingFluids = outputFacing;
        updateAutoOutputSubscription();
    }

    @Override
    public boolean isWorkingEnabled() {
        return isAutoOutputFluids();
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        setAutoOutputFluids(isWorkingAllowed);
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);
        updateAutoOutputSubscription();
    }

    protected void updateAutoOutputSubscription() {
        var outputFacing = getOutputFacingFluids();
        if ((isAutoOutputFluids() && !handler.isEmpty()) && outputFacing != null &&
                GTTransferUtils.hasAdjacentFluidHandler(getLevel(), getPos(), outputFacing)) {
            autoOutputOwnSubs = subscribeServerTick(autoOutputOwnSubs, this::checkAutoOutput);
            autoOutputHandlerSubs = handler.subscribe(autoOutputHandlerSubs, this::checkAutoOutput);
        } else {
            if (autoOutputOwnSubs != null) {
                autoOutputOwnSubs.unsubscribe();
                autoOutputOwnSubs = null;
            }
            if (autoOutputHandlerSubs != null) {
                autoOutputHandlerSubs.unsubscribe();
                autoOutputHandlerSubs = null;
            }
        }
    }

    protected void checkAutoOutput() {
        if (getOffsetTimer() % 5 == 0) {
            if (isAutoOutputFluids() && getOutputFacingFluids() != null) {
                wrapper.exportToNearby(getOutputFacingFluids());
                scheduleRenderUpdate();
            }
//            updateAutoOutputSubscription();
        }
    }

    // save and load data

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putInt("Channel", channel);
        tag.putBoolean("IsVoiding", isVoiding);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        setChannel(tag.getInt("Channel"));
        isVoiding = tag.getBoolean("IsVoiding");
    }

    // gui

    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 90, 63);
        group.addWidget(new ImageWidget(4, 4, 82, 55, GuiTextures.DISPLAY))
                .addWidget(new LabelWidget(8, 8, "gtceu.gui.fluid_amount"))
                .addWidget(new LabelWidget(8, 18, () -> FormattingUtil.formatBuckets(handler.getStoredAmount()))
                        .setTextColor(-1)
                        .setDropShadow(false))
                .addWidget(new TankWidget(handler, 0, 68, 23, true, true)
                        .setShowAmount(false)
                        .setBackground(GuiTextures.FLUID_SLOT))
                .addWidget(new PhantomFluidWidget(handler, 1, 68, 41, 18, 18,
                        handler::getLockedFluid, handler::setLocked)
                        .setShowAmount(false)
                        .setBackground(ColorPattern.T_GRAY.rectTexture()))
                .addWidget(new ToggleButtonWidget(4, 41, 18, 18,
                        GuiTextures.BUTTON_FLUID_OUTPUT, this::isAutoOutputFluids, this::setAutoOutputFluids)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_auto_output.tooltip"))
                .addWidget(new ToggleButtonWidget(22, 41, 18, 18,
                        GuiTextures.BUTTON_LOCK, handler::isLocked, handler::setLocked)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_lock.tooltip"))
                .addWidget(new ToggleButtonWidget(40, 41, 18, 18,
                        GuiTextures.BUTTON_VOID, () -> isVoiding, (b) -> isVoiding = b)
                        .setShouldUseBaseBackground()
                        .setTooltipText("gtceu.gui.fluid_voiding_partial.tooltip"));
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    // rendering

    @Override
    public ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                    Direction side) {
        if (toolTypes.contains(GTToolType.WRENCH)) {
            if (!player.isShiftKeyDown()) {
                if (!hasFrontFacing() || side != getFrontFacing()) {
                    return GuiTextures.TOOL_IO_FACING_ROTATION;
                }
            }
        } else if (toolTypes.contains(GTToolType.SCREWDRIVER)) {
            if (side == getOutputFacingFluids()) {
                return GuiTextures.TOOL_ALLOW_INPUT;
            }
        } else if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            if (side == getFrontFacing()) return null;
        }
        return super.sideTips(player, pos, state, toolTypes, side);
    }

    // boilerplate syncing
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            EntangledQuantumTankMachine.class, MetaMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public class FluidChannelWrapper extends MachineTrait implements IFluidHandler {

        public FluidChannelWrapper(MetaMachine holder) {
            super(holder);
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return handler.getFluidInTank(tank);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return handler.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return handler.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return handler.drain(maxDrain, action);
        }

        @Override
        public int getTankCapacity(int tank) {
            return handler.getTankCapacity(tank);
        }

        @Override
        public int getTanks() {
            return handler.getTanks();
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return handler.isFluidValid(tank, stack);
        }

        public void exportToNearby(@NotNull Direction... facings) {
            if (handler.isEmpty()) {
                return;
            }
            for (var facing : facings) {
                var filter = getFluidCapFilter(facing, IO.OUT);
                GTTransferUtils.getAdjacentFluidHandler(getLevel(), getPos(), facing)
                        .ifPresent(adj -> GTTransferUtils.transferFluidsFiltered(handler, adj, filter));
            }
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }
}
