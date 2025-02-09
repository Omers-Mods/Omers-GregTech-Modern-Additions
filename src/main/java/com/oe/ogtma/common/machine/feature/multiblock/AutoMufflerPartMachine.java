package com.oe.ogtma.common.machine.feature.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.misc.IOFilteredInvWrapper;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutoMufflerPartMachine extends TieredPartMachine implements IMufflerMachine, IUIMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AutoMufflerPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Getter
    @Persisted
    protected final NotifiableItemStackHandler inventory;
    @Getter
    protected final int recoveryChance;

    @Nullable
    protected TickableSubscription autoIOSubs;
    @Nullable
    protected ISubscription inventorySubs;

    public AutoMufflerPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
        recoveryChance = Math.max(1, tier * 10);
        inventory = new NotifiableItemStackHandler(this, (int) Math.pow(tier + 1, 2), IO.OUT, IO.BOTH);
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(0, this::updateInventorySubscription));
        }
        inventorySubs = getInventory().addChangedListener(this::updateInventorySubscription);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (inventorySubs != null) {
            inventorySubs.unsubscribe();
            inventorySubs = null;
        }
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        return new IOFilteredInvWrapper(List.of(), IO.OUT, stack -> false, stack -> true);
    }

    //////////////////////////////////////
    // ******** Muffler *********//
    //////////////////////////////////////

    protected boolean calculateChance() {
        return getRecoveryChance() >= 100 || getRecoveryChance() >= GTValues.RNG.nextInt(100);
    }

    @Override
    public void recoverItemsTable(ItemStack... recoveryItems) {
        int numRolls = Math.min(recoveryItems.length, inventory.getSlots());
        IntStream.range(0, numRolls).forEach(slot -> {
            if (calculateChance()) {
                ItemHandlerHelper.insertItemStacked(inventory, recoveryItems[slot].copy(), false);
            }
        });
    }

    @Override
    public boolean isFrontFaceFree() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        super.clientTick();
        for (IMultiController controller : getControllers()) {
            if (controller instanceof IRecipeLogicMachine recipeLogicMachine &&
                    recipeLogicMachine.getRecipeLogic().isWorking()) {
                emitPollutionParticles();
                break;
            }
        }
    }

    //////////////////////////////////////
    // ******** Auto IO *********//
    //////////////////////////////////////

    protected void updateInventorySubscription() {
        if (!getInventory().isEmpty() &&
                GTTransferUtils.hasAdjacentItemHandler(getLevel(), getPos(), getFrontFacing())) {
            autoIOSubs = subscribeServerTick(autoIOSubs, this::autoIO);
        } else if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    protected void autoIO() {
        if (getOffsetTimer() % 5 == 0) {
            getInventory().exportToNearby(getFrontFacing());
            updateInventorySubscription();
        }
    }

    //////////////////////////////////////
    // ********** GUI ***********//
    //////////////////////////////////////
    @Override
    public ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventory.getSlots());
        int xOffset = rowSize == 10 ? 9 : 0;
        var modular = new ModularUI(176 + xOffset * 2, 18 + 18 * rowSize + 94, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(new LabelWidget(10, 5, getBlockState().getBlock().getDescriptionId()))
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7 + xOffset,
                        18 + 18 * rowSize + 12, true));
        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                modular.widget(new SlotWidget(inventory, index, (88 - rowSize * 9 + x * 18) + xOffset, 18 + y * 18,
                        true, false).setBackgroundTexture(GuiTextures.SLOT));
            }
        }
        return modular;
    }
}
