package net.ansol.tipsycraft

import net.ansol.tipsycraft.block.Blocks
import net.ansol.tipsycraft.item.ItemGroups
import net.ansol.tipsycraft.item.Items
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object TipsyCraft : ModInitializer {
    const val MOD_ID = "tipsycraft"
    val logger: Logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
        Blocks.init()
        Items.init()
        ItemGroups.init()
		logger.info("Hello Fabric world!")
	}
}