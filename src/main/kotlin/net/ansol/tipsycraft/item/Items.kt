package net.ansol.tipsycraft.item

import net.ansol.tipsycraft.TipsyCraft
import net.ansol.tipsycraft.block.Blocks
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry
import net.minecraft.component.type.FoodComponent
import net.minecraft.item.AliasedBlockItem
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object Items {
    val WILD_HOPS_FLOWERS: Item = registerItem("wild_hops_flowers",
            Item(
                Item.Settings().food(
                    FoodComponent.Builder()
                        .nutrition(1)
                        .saturationModifier(0.1f)
                        .alwaysEdible()
                        .build()
                )
            )
        )

    val WILD_HOPS_SEEDS: Item = registerItem("wild_hops_seeds",
        AliasedBlockItem(Blocks.WILD_HOPS_VINE_BLOCK, Item.Settings())
        )

    private fun registerItem(name: String, item: Item): Item {
        val id = Identifier.of(TipsyCraft.MOD_ID, name)
        return Registry.register(Registries.ITEM, id, item)
    }

    fun init() {
        TipsyCraft.logger.info("Items init")

        CompostingChanceRegistry.INSTANCE.add(WILD_HOPS_FLOWERS, 0.3f)
    }
}