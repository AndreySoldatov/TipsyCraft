package net.ansol.tipsycraft.item

import net.ansol.tipsycraft.TipsyCraft
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object ItemGroups {
    val TIPSY_CRAFT_ITEM_GROUP: ItemGroup = Registry.register(Registries.ITEM_GROUP,
        Identifier.of(TipsyCraft.MOD_ID, "tipsycraft_items"),
        FabricItemGroup.builder()
            .icon { ItemStack(Items.WILD_HOPS_FLOWERS) }
            .displayName(Text.translatable("item_group.tipsycraft"))
            .entries { displayContext, entries ->
                entries.add(Items.WILD_HOPS_FLOWERS)
                entries.add(Items.WILD_HOPS_SEEDS)
            }
            .build()
    )

    fun init() {
        TipsyCraft.logger.info("Item Group init")
    }
}