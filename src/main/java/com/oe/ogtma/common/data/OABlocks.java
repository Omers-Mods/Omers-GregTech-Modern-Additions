package com.oe.ogtma.common.data;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;

import com.oe.ogtma.common.block.marker.MarkerBlock;
import com.oe.ogtma.common.block.marker.WallMarkerBlock;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import static com.oe.ogtma.OGTMA.REGISTRATE;

@SuppressWarnings("removal")
public class OABlocks {

    public static final BlockEntry<MarkerBlock> MARKER = REGISTRATE
            .block("marker", MarkerBlock::new)
            .initialProperties(() -> Blocks.TORCH)
            .lang("Marker")
            .addLayer(() -> RenderType::cutout)
            .blockstate((ctx, prov) -> {
                BlockModelBuilder model = prov.models().torch(ctx.getName(), prov.modLoc("block/marker/marker"));
                prov.simpleBlock(ctx.get(), model);
            })
            .item((b, p) -> new StandingAndWallBlockItem(b, OABlocks.WALL_MARKER.get(), p, Direction.DOWN))
            .model((ctx, prov) -> prov.torch(ctx.getName(), prov.modLoc("item/marker/marker")))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .build()
            .register();

    public static final BlockEntry<WallMarkerBlock> WALL_MARKER = REGISTRATE
            .block("wall_marker", WallMarkerBlock::new)
            .initialProperties(() -> Blocks.WALL_TORCH)
            .lang("Wall Marker")
            .addLayer(() -> RenderType::cutout)
            .blockstate((ctx, prov) -> {
                BlockModelBuilder model = prov.models().torchWall(ctx.getName(), prov.modLoc("block/marker/marker"));
                prov.getVariantBuilder(ctx.get())
                        .forAllStates(state -> {
                            var facing = state.getValue(WallMarkerBlock.FACING);
                            var yRot = switch (facing) {
                                case NORTH -> 270;
                                case SOUTH -> 90;
                                case WEST -> 180;
                                default -> 0;
                            };
                            return ConfiguredModel.builder()
                                    .modelFile(model)
                                    .rotationY(yRot)
                                    .build();
                        });
            })
            .loot((table, block) -> table.dropOther(block, MARKER.asItem()))
            .setData(ProviderType.LANG, NonNullBiConsumer.noop())
            .register();

    public static void init() {}
}
