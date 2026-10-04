package com.randompixxelstudios.thesift.registry;

import com.google.common.collect.ImmutableSet;
import com.randompixxelstudios.thesift.TheSift;
import com.randompixxelstudios.thesift.mixin.PoiTypesAccessor;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;

public final class ModPoi {
	public static final ResourceKey<PoiType> SIFT_PORTAL = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, TheSift.id("sift_portal"));

	private ModPoi() {
	}

	public static void init() {
		Set<BlockState> states = ImmutableSet.copyOf(ModBlocks.SIFT_PORTAL.getStateDefinition().getPossibleStates());
		Registry.register(BuiltInRegistries.POINT_OF_INTEREST_TYPE, SIFT_PORTAL, new PoiType(states, 0, 1));
		Holder<PoiType> holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getOrThrow(SIFT_PORTAL);
		states.forEach(state -> PoiTypesAccessor.thesift$getTypeByState().put(state, holder));
	}
}
