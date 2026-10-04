package com.randompixxelstudios.thesift.registry;

import com.randompixxelstudios.thesift.TheSift;
import com.randompixxelstudios.thesift.block.IchorFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;

public final class ModFluids {
	public static final FlowingFluid FLOWING_ICHOR = Registry.register(BuiltInRegistries.FLUID, TheSift.id("flowing_ichor"), new IchorFluid.Flowing());
	public static final FlowingFluid ICHOR = Registry.register(BuiltInRegistries.FLUID, TheSift.id("ichor"), new IchorFluid.Source());

	private ModFluids() {
	}

	public static void init() {
	}
}
