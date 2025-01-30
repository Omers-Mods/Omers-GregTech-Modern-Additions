package com.oe.ogtma.client.model.pipe;

import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.client.model.ItemBakedModel;
import com.gregtechceu.gtceu.client.model.PipeModel;

import com.lowdragmc.lowdraglib.client.renderer.IItemRendererProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class QuarryPipeModel extends PipeModel {

    public static final int Y_AXIS_CONNECTIONS = Node.ALL_CLOSED | 1 | 2;
    private final Map<Optional<Direction>, List<BakedQuad>> itemModelCache = new ConcurrentHashMap<>();

    public QuarryPipeModel(float thickness, Supplier<ResourceLocation> sideTexture,
                           Supplier<ResourceLocation> endTexture,
                           @Nullable Supplier<@Nullable ResourceLocation> secondarySideTexture,
                           @Nullable Supplier<@Nullable ResourceLocation> secondaryEndTexture) {
        super(thickness, sideTexture, endTexture, secondarySideTexture, secondaryEndTexture);
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, boolean leftHand, PoseStack matrixStack,
                           MultiBufferSource buffer, int combinedLight, int combinedOverlay, BakedModel model) {
        IItemRendererProvider.disabled.set(true);
        Minecraft.getInstance().getItemRenderer().render(stack, transformType, leftHand, matrixStack, buffer,
                combinedLight, combinedOverlay,
                (ItemBakedModel) (state, direction, random) -> itemModelCache.computeIfAbsent(
                        Optional.ofNullable(direction),
                        direction1 -> bakeQuads(direction1.orElse(null), Y_AXIS_CONNECTIONS, 0)));
        IItemRendererProvider.disabled.set(false);
    }
}
