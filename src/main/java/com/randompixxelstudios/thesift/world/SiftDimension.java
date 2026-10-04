package com.randompixxelstudios.thesift.world;

import com.randompixxelstudios.thesift.TheSift;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class SiftDimension {
	public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, TheSift.id("the_sift"));

	private SiftDimension() {
	}

	public static boolean isSift(final Level level) {
		return level.dimension() == LEVEL;
	}
}
