package com.randompixxelstudios.thesift;

import com.randompixxelstudios.thesift.registry.ModBlocks;
import com.randompixxelstudios.thesift.registry.ModEffects;
import com.randompixxelstudios.thesift.registry.ModCreativeTab;
import com.randompixxelstudios.thesift.registry.ModEntities;
import com.randompixxelstudios.thesift.registry.ModFluids;
import com.randompixxelstudios.thesift.registry.ModItems;
import com.randompixxelstudios.thesift.registry.ModPoi;
import com.randompixxelstudios.thesift.world.SetBonuses;
import com.randompixxelstudios.thesift.world.SiftTides;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TheSift implements ModInitializer {
	public static final String MOD_ID = "thesift";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(final String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModEffects.init();
		ModFluids.init();
		ModBlocks.init();
		ModItems.init();
		ModEntities.init();
		ModPoi.init();
		ModCreativeTab.init();
		SiftTides.init();
		SetBonuses.init();
		com.randompixxelstudios.thesift.command.SiftCommands.init();
		LOGGER.info("The Sift is open. Mind the tides.");
	}
}
