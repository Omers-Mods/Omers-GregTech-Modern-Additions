package com.oe.ogtma.common.cache.fluid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import com.oe.ogtma.api.channel.cache.ChannelCache;
import com.oe.ogtma.api.channel.handler.IChannelHandler;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class FluidChannelHandler implements IFluidHandler, IChannelHandler {

    @Getter
    protected long capacity;
    @Getter
    protected long storedAmount;
    @Getter
    protected FluidStack fluid = FluidStack.EMPTY;
    @Getter
    protected FluidStack lockedFluid = FluidStack.EMPTY;
    protected List<TickableSubscription> subscribers = new ArrayList<>();

    public FluidChannelHandler(long capacity) {
        this.capacity = capacity;
    }

    public TickableSubscription subscribe(@NotNull Runnable runnable) {
        if (GTCEu.isClientThread()) {
            return null;
        }
        var listener = new TickableSubscription(runnable);
        subscribers.add(listener);
        return listener;
    }

    public TickableSubscription subscribe(@Nullable TickableSubscription last, Runnable runnable) {
        if (last == null || !last.isStillSubscribed()) {
            return subscribe(runnable);
        }
        return last;
    }

    protected void notifySubscribers() {
        var iterator = subscribers.iterator();
        while (iterator.hasNext()) {
            var sub = iterator.next();
            if (!sub.isStillSubscribed()) {
                iterator.remove();
            } else {
                sub.run();
            }
        }
    }

    public void setLocked(boolean locked) {
        if (!fluid.isEmpty() && locked) {
            lockedFluid = new FluidStack(fluid, 1000);
        } else if (!locked) {
            lockedFluid = FluidStack.EMPTY;
        }
    }

    public void setLocked(FluidStack fluid) {
        if (fluid.isEmpty()) {
            setLocked(false);
        } else if (this.fluid.isEmpty()) {
            lockedFluid = fluid;
        } else if (this.fluid.isFluidEqual(fluid)) {
            setLocked(true);
        }
    }

    public boolean isLocked() {
        return !lockedFluid.isEmpty();
    }

    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    @Override
    public boolean shouldSave() {
        return isLocked() || storedAmount > 0;
    }

    @Override
    public ChannelCache getCache() {
        return FluidChannelCache.getInstance();
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return switch (tank) {
            case 0 -> new FluidStack(fluid, GTMath.saturatedCast(storedAmount));
            case 1 -> lockedFluid.copy();
            default -> throw new IndexOutOfBoundsException("Unexpected value: " + tank);
        };
    }

    @Override
    public int getTankCapacity(int tank) {
        return GTMath.saturatedCast(capacity);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return !isLocked() || lockedFluid.isFluidEqual(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        long free = capacity - storedAmount;
        long canFill = 0;
        if ((fluid.isEmpty() || fluid.isFluidEqual(resource)) && isFluidValid(0, resource)) {
            canFill = Math.min(resource.getAmount(), free);
        }
        if (action.execute() && canFill > 0) {
            if (fluid.isEmpty()) fluid = new FluidStack(resource, 1000);
            storedAmount = Math.min(capacity, storedAmount + canFill);
        }
        if (canFill > 0 && !getCache().isDirty()) {
            notifySubscribers();
            getCache().setDirty();
        }
        return (int) canFill;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        if (fluid.isEmpty()) return FluidStack.EMPTY;
        long toDrain = Math.min(storedAmount, maxDrain);
        var copy = new FluidStack(fluid, (int) toDrain);
        if (action.execute() && toDrain > 0) {
            storedAmount -= toDrain;
            if (storedAmount == 0) fluid = FluidStack.EMPTY;
        }
        if (copy.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (!getCache().isDirty()) {
            notifySubscribers();
            getCache().setDirty();
        }
        return copy;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        if (!resource.isFluidEqual(fluid)) return FluidStack.EMPTY;
        return drain(resource.getAmount(), action);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putLong("Capacity", capacity);
        tag.putLong("StoredAmount", storedAmount);
        if (storedAmount > 0) {
            var fluidTag = new CompoundTag();
            fluid.writeToNBT(fluidTag);
            tag.put("Fluid", fluidTag);
        }
        tag.putBoolean("Locked", isLocked());
        if (isLocked()) {
            var lockedFluidTag = new CompoundTag();
            lockedFluid.writeToNBT(lockedFluidTag);
            tag.put("LockedFluid", lockedFluidTag);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        capacity = tag.getLong("Capacity");
        storedAmount = tag.getLong("StoredAmount");
        if (storedAmount > 0) {
            fluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("Fluid"));
        } else {
            fluid = FluidStack.EMPTY;
        }
        var locked = tag.getBoolean("Locked");
        if (locked) {
            lockedFluid = FluidStack.loadFluidStackFromNBT(tag.getCompound("LockedFluid"));
        } else {
            lockedFluid = FluidStack.EMPTY;
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FluidChannelHandler that)) return false;

        return getCapacity() == that.getCapacity() && getStoredAmount() == that.getStoredAmount() &&
                getFluid().equals(that.getFluid()) && getLockedFluid().equals(that.getLockedFluid());
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(getCapacity());
        result = 31 * result + Long.hashCode(getStoredAmount());
        result = 31 * result + getFluid().hashCode();
        result = 31 * result + getLockedFluid().hashCode();
        return result;
    }
}
