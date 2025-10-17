package net.ansol.tipsycraft.datagen

import net.ansol.tipsycraft.block.Blocks
import net.ansol.tipsycraft.block.HopsVineBlockBase
import net.ansol.tipsycraft.item.Items
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.condition.BlockStatePropertyLootCondition
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.SetCountLootFunction
import net.minecraft.loot.provider.number.UniformLootNumberProvider
import net.minecraft.predicate.StatePredicate
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

class TipsyCraftLootTableProvider(dataOutput: FabricDataOutput,
                                  registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricBlockLootTableProvider(dataOutput, registryLookup) {
    override fun generate() {
        addDrop(Blocks.WILD_HOPS_VINE_BLOCK,
            hopsLootTableBuilder(
                Blocks.WILD_HOPS_VINE_BLOCK,
                Items.WILD_HOPS_FLOWERS,
                Items.WILD_HOPS_SEEDS
            )
        )
    }

    fun hopsLootTableBuilder(crop: Block, flowers: Item, seeds: Item): LootTable.Builder {
        return applyExplosionDecay(crop, LootTable.builder()
            .pool(LootPool.builder().with(
                ItemEntry.builder(flowers)
                    .conditionally(
                    BlockStatePropertyLootCondition.builder(crop)
                        .properties(StatePredicate.Builder.create().exactMatch(HopsVineBlockBase.AGE, 3))
                    )
                    .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 3.0F)))
            ))
            .pool(LootPool.builder().with(
                ItemEntry.builder(seeds).conditionally(RandomChanceLootCondition.builder(0.3F))
            ))
        )
    }
}