package net.ansol.tipsycraft.block

import net.ansol.tipsycraft.TipsyCraft
import net.ansol.tipsycraft.item.Items
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object Blocks {
    val WILD_HOPS_VINE_BLOCK: Block = registerBlock("wild_hops_vine_block",
        object : HopsVineBlockBase(AbstractBlock.Settings.create()) {
            override val flowerItem: Item
                get() = Items.WILD_HOPS_FLOWERS
            override val seedItem: Item
                get() = Items.WILD_HOPS_SEEDS
            override val spec: HopsVineVarietalSpec = HopsVineVarietalSpec()
        }
    )

    private fun registerBlock(name: String, block: Block): Block {
        val id = Identifier.of(TipsyCraft.MOD_ID, name)
        return Registry.register(Registries.BLOCK, id, block)
    }

    fun init() {
        TipsyCraft.logger.info("Blocks init")

        BlockRenderLayerMap.INSTANCE.putBlock(WILD_HOPS_VINE_BLOCK, RenderLayer.getCutout())
    }
}