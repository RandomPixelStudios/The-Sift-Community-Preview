package com.randompixxelstudios.thesift.registry;

import com.randompixxelstudios.thesift.TheSift;
import com.randompixxelstudios.thesift.entity.Blub;
import com.randompixxelstudios.thesift.entity.Bumblehog;
import com.randompixxelstudios.thesift.entity.Gloomstalker;
import com.randompixxelstudios.thesift.entity.Shade;
import com.randompixxelstudios.thesift.entity.IchorRay;
import com.randompixxelstudios.thesift.entity.Lumoth;
import com.randompixxelstudios.thesift.entity.Nuzzle;
import com.randompixxelstudios.thesift.entity.PearlStag;
import com.randompixxelstudios.thesift.entity.Shellback;
import com.randompixxelstudios.thesift.entity.Singer;
import com.randompixxelstudios.thesift.entity.Sifter;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntities {
	public static final EntityType<Blub> BLUB = register(
		"blub", EntityType.Builder.of(Blub::new, MobCategory.CREATURE).sized(0.6F, 0.55F).eyeHeight(0.4F).passengerAttachments(0.5F).clientTrackingRange(8)
	);
	public static final EntityType<Sifter> SIFTER = register(
		"sifter", EntityType.Builder.of(Sifter::new, MobCategory.MONSTER).sized(0.7F, 2.3F).eyeHeight(2.0F).clientTrackingRange(8).notInPeaceful()
	);
	public static final EntityType<Nuzzle> NUZZLE = register(
		"nuzzle", EntityType.Builder.of(Nuzzle::new, MobCategory.CREATURE).sized(0.65F, 0.45F).eyeHeight(0.35F).clientTrackingRange(8)
	);
	public static final EntityType<Singer> SINGER = register(
		"singer", EntityType.Builder.of(Singer::new, MobCategory.CREATURE).sized(0.9F, 0.8F).eyeHeight(0.5F).clientTrackingRange(10)
	);
	public static final EntityType<Shellback> SHELLBACK = register(
		"shellback", EntityType.Builder.of(Shellback::new, MobCategory.CREATURE).sized(1.2F, 0.9F).eyeHeight(0.6F).clientTrackingRange(10)
	);
	public static final EntityType<Lumoth> LUMOTH = register(
		"lumoth", EntityType.Builder.of(Lumoth::new, MobCategory.AMBIENT).sized(0.5F, 0.4F).eyeHeight(0.2F).clientTrackingRange(8)
	);
	public static final EntityType<PearlStag> PEARL_STAG = register(
		"pearl_stag", EntityType.Builder.of(PearlStag::new, MobCategory.CREATURE).sized(0.9F, 1.6F).eyeHeight(1.45F).clientTrackingRange(10)
	);
	public static final EntityType<IchorRay> ICHOR_RAY = register(
		"ichor_ray", EntityType.Builder.of(IchorRay::new, MobCategory.WATER_CREATURE).sized(1.2F, 0.35F).eyeHeight(0.2F).clientTrackingRange(8)
	);
	public static final EntityType<Shade> SHADE = register(
		"shade", EntityType.Builder.of(Shade::new, MobCategory.MONSTER).sized(0.6F, 1.95F).eyeHeight(1.74F).clientTrackingRange(8).notInPeaceful()
	);
	public static final EntityType<Gloomstalker> GLOOMSTALKER = register(
		"gloomstalker", EntityType.Builder.of(Gloomstalker::new, MobCategory.MONSTER).sized(1.4F, 0.9F).eyeHeight(0.65F).clientTrackingRange(8).notInPeaceful()
	);
	public static final EntityType<Bumblehog> BUMBLEHOG = register(
		"bumblehog", EntityType.Builder.of(Bumblehog::new, MobCategory.CREATURE).sized(0.9F, 0.8F).eyeHeight(0.6F).clientTrackingRange(10)
	);

	public static final EntityType<Boat> SIFT_WILLOW_BOAT = boat("sift_willow_boat", () -> ModItems.SIFT_WILLOW_BOAT);
	public static final EntityType<ChestBoat> SIFT_WILLOW_CHEST_BOAT = chestBoat("sift_willow_chest_boat", () -> ModItems.SIFT_WILLOW_CHEST_BOAT);
	public static final EntityType<Boat> DUSKWOOD_BOAT = boat("duskwood_boat", () -> ModItems.DUSKWOOD_BOAT);
	public static final EntityType<ChestBoat> DUSKWOOD_CHEST_BOAT = chestBoat("duskwood_chest_boat", () -> ModItems.DUSKWOOD_CHEST_BOAT);
	public static final EntityType<Boat> PEARLBARK_BOAT = boat("pearlbark_boat", () -> ModItems.PEARLBARK_BOAT);
	public static final EntityType<ChestBoat> PEARLBARK_CHEST_BOAT = chestBoat("pearlbark_chest_boat", () -> ModItems.PEARLBARK_CHEST_BOAT);

	private ModEntities() {
	}

	private static EntityType<Boat> boat(final String name, final Supplier<Item> item) {
		return register(
			name,
			EntityType.Builder.<Boat>of((type, level) -> new Boat(type, level, item), MobCategory.MISC)
				.noLootTable()
				.sized(1.375F, 0.5625F)
				.eyeHeight(0.5625F)
				.clientTrackingRange(10)
		);
	}

	private static EntityType<ChestBoat> chestBoat(final String name, final Supplier<Item> item) {
		return register(
			name,
			EntityType.Builder.<ChestBoat>of((type, level) -> new ChestBoat(type, level, item), MobCategory.MISC)
				.noLootTable()
				.sized(1.375F, 0.5625F)
				.eyeHeight(0.5625F)
				.clientTrackingRange(10)
		);
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(final String name, final EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, TheSift.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	public static void init() {
		FabricDefaultAttributeRegistry.register(BLUB, Blub.createAttributes());
		FabricDefaultAttributeRegistry.register(SIFTER, Sifter.createAttributes());
		FabricDefaultAttributeRegistry.register(NUZZLE, Nuzzle.createAttributes());
		FabricDefaultAttributeRegistry.register(SINGER, Singer.createAttributes());
		FabricDefaultAttributeRegistry.register(SHELLBACK, Shellback.createAttributes());
		FabricDefaultAttributeRegistry.register(LUMOTH, Lumoth.createAttributes());
		FabricDefaultAttributeRegistry.register(PEARL_STAG, PearlStag.createAttributes());
		FabricDefaultAttributeRegistry.register(ICHOR_RAY, IchorRay.createAttributes());
		FabricDefaultAttributeRegistry.register(SHADE, Shade.createAttributes());
		FabricDefaultAttributeRegistry.register(GLOOMSTALKER, Gloomstalker.createAttributes());
		FabricDefaultAttributeRegistry.register(BUMBLEHOG, Bumblehog.createAttributes());
		SpawnPlacements.register(SHADE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules);
		SpawnPlacements.register(GLOOMSTALKER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules);
		SpawnPlacements.register(BUMBLEHOG, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Bumblehog::checkGroundSpawnRules);
		SpawnPlacements.register(BLUB, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Blub::checkBlubSpawnRules);
		SpawnPlacements.register(NUZZLE, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Nuzzle::checkNuzzleSpawnRules);
		SpawnPlacements.register(SINGER, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Singer::checkSingerSpawnRules);
		SpawnPlacements.register(SIFTER, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Sifter::checkSifterSpawnRules);
		SpawnPlacements.register(SHELLBACK, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Shellback::checkGroundSpawnRules);
		SpawnPlacements.register(PEARL_STAG, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PearlStag::checkGroundSpawnRules);
		SpawnPlacements.register(LUMOTH, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Lumoth::checkLumothSpawnRules);
		SpawnPlacements.register(ICHOR_RAY, SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, IchorRay::checkRaySpawnRules);
	}
}
