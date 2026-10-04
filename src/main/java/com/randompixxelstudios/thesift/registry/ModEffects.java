package com.randompixxelstudios.thesift.registry;

import com.randompixxelstudios.thesift.TheSift;
import com.randompixxelstudios.thesift.effect.IchorDrowningEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class ModEffects {
	public static final Holder<MobEffect> ICHOR_DROWNING = Registry.registerForHolder(
		BuiltInRegistries.MOB_EFFECT, TheSift.id("ichor_drowning"), new IchorDrowningEffect()
	);

	private ModEffects() {
	}

	public static void init() {
	}
}
