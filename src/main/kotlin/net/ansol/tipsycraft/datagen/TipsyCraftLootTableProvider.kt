package net.ansol.tipsycraft.datagen

import net.ansol.tipsycraft.block.Blocks
import net.ansol.tipsycraft.block.HopsVineBlockBase
import net.ansol.tipsycraft.item.Items
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.block.CropBlock
import net.minecraft.loot.condition.BlockStatePropertyLootCondition
import net.minecraft.loot.condition.LootCondition
import net.minecraft.predicate.StatePredicate
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class TipsyCraftLootTableProvider(dataOutput: FabricDataOutput,
                                  registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricBlockLootTableProvider(dataOutput, registryLookup) {
    override fun generate() {
        addDrop(Blocks.WILD_HOPS_VINE_BLOCK, cropDrops(
            Blocks.WILD_HOPS_VINE_BLOCK,
            Items.WILD_HOPS_FLOWERS,
            Items.WILD_HOPS_SEEDS,
            BlockStatePropertyLootCondition.builder(Blocks.WILD_HOPS_VINE_BLOCK)
                .properties(StatePredicate.Builder.create().exactMatch(HopsVineBlockBase.AGE, 3))
        ))
    }
}