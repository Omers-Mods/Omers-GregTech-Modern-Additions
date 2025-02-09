package com.oe.ogtma.common.data;

import com.gregtechceu.gtceu.api.block.MaterialBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterialBlocks;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Blocks;

import com.oe.ogtma.common.block.pipe.quarry.QuarryPipeBlock;
import com.oe.ogtma.config.OAConfig;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import static com.oe.ogtma.OGTMA.REGISTRATE;

@SuppressWarnings("removal")
public class OAMaterialBlocks {

    public static final BlockEntry<? extends MaterialBlock>[] QUARRY_BLOCKS;
    public static final BlockEntry<QuarryPipeBlock>[] QUARRY_PIPE_BLOCKS;
    static {
        if (OAConfig.INSTANCE.features.enableAutoMufflers) {
            QUARRY_BLOCKS = new BlockEntry[] {
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.WroughtIron),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.Steel),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.Aluminium),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.StainlessSteel),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.Titanium),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.TungstenSteel),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.RhodiumPlatedPalladium),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.NaquadahAlloy),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.Darmstadtium),
                    GTMaterialBlocks.MATERIAL_BLOCKS.get(TagPrefix.block, GTMaterials.Neutronium)
            };
            QUARRY_PIPE_BLOCKS = new BlockEntry[] {
                    registerQuarryPipe(GTMaterials.WroughtIron),
                    registerQuarryPipe(GTMaterials.Steel),
                    registerQuarryPipe(GTMaterials.Aluminium),
                    registerQuarryPipe(GTMaterials.StainlessSteel),
                    registerQuarryPipe(GTMaterials.Titanium),
                    registerQuarryPipe(GTMaterials.TungstenSteel),
                    registerQuarryPipe(GTMaterials.RhodiumPlatedPalladium),
                    registerQuarryPipe(GTMaterials.NaquadahAlloy),
                    registerQuarryPipe(GTMaterials.Darmstadtium),
                    registerQuarryPipe(GTMaterials.Neutronium)
            };
        } else {
            QUARRY_BLOCKS = new BlockEntry[0];
            QUARRY_PIPE_BLOCKS = new BlockEntry[0];
        }
    }

    public static void init() {}

    public static BlockEntry<QuarryPipeBlock> registerQuarryPipe(Material material) {
        return REGISTRATE
                .block("%s_quarry_pipe".formatted(material.getName()), QuarryPipeBlock::new)
                .lang("Quarry Pipe")
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(p -> p.dynamicShape().noOcclusion().noLootTable().forceSolidOn())
                .blockstate(NonNullBiConsumer.noop())
                .setData(ProviderType.LOOT, NonNullBiConsumer.noop())
                .addLayer(() -> RenderType::cutoutMipped)
                .color(() -> () -> (blockState, level, blockPos, index) -> material.getMaterialRGB())
                .register();
    }
}
