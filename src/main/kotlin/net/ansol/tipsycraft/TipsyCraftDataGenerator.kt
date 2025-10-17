package net.ansol.tipsycraft

import net.ansol.tipsycraft.datagen.TipsyCraftLootTableProvider
import net.ansol.tipsycraft.datagen.TipsyCraftModelProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

object TipsyCraftDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(fabricDataGenerator: FabricDataGenerator) {
        val pack: FabricDataGenerator.Pack = fabricDataGenerator.createPack()

        pack.addProvider(::TipsyCraftLootTableProvider)
        pack.addProvider(::TipsyCraftModelProvider)
	}
}