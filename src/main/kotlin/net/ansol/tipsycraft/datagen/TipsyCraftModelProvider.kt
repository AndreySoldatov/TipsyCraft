package net.ansol.tipsycraft.datagen

import net.ansol.tipsycraft.item.Items
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.data.client.BlockStateModelGenerator
import net.minecraft.data.client.ItemModelGenerator
import net.minecraft.data.client.Models

class TipsyCraftModelProvider(output: FabricDataOutput) : FabricModelProvider(output) {
    override fun generateBlockStateModels(p0: BlockStateModelGenerator) {
//        TODO("Not yet implemented")
    }

    override fun generateItemModels(p0: ItemModelGenerator) {
        p0.register(Items.WILD_HOPS_FLOWERS, Models.GENERATED)
        p0.register(Items.WILD_HOPS_SEEDS, Models.GENERATED)
    }
}