package com.randompixxelstudios.thesift.registry;

import com.randompixxelstudios.thesift.TheSift;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {
	public static final CreativeModeTab THE_SIFT = Registry.register(
		BuiltInRegistries.CREATIVE_MODE_TAB,
		TheSift.id("the_sift"),
		FabricCreativeModeTab.builder()
			.title(Component.translatable("itemGroup.thesift.the_sift"))
			.icon(() -> new ItemStack(ModItems.BLUB_SPAWN_EGG))
			.displayItems((parameters, output) -> {
				ModBlocks.BLOCKS_WITH_ITEMS.forEach(output::accept);
				ModItems.ITEMS.forEach(output::accept);
			})
			.build()
	);

	private ModCreativeTab() {
	}

	public static void init() {
	}
}
