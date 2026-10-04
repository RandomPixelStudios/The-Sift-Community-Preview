package com.randompixxelstudios.thesift.registry;

import com.randompixxelstudios.thesift.TheSift;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;

public final class ModItems {
	public static final List<Item> ITEMS = new ArrayList<>();

	public static final Item ICHOR_BUCKET = register(
		"ichor_bucket", p -> new BucketItem(ModFluids.ICHOR, p), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)
	);
	public static final Item ICHOR_BOTTLE = register(
		"ichor_bottle",
		Item::new,
		new Item.Properties()
			.stacksTo(16)
			.craftRemainder(Items.GLASS_BOTTLE)
			.usingConvertsTo(Items.GLASS_BOTTLE)
			.food(
				new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F).alwaysEdible().build(),
				Consumables.defaultDrink()
					.onConsume(
						new ApplyStatusEffectsConsumeEffect(
							List.of(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0))
						)
					)
					.build()
			)
	);
	public static final Item SOUL_SHARD = register("soul_shard", Item::new, new Item.Properties());
	public static final Item RAW_SIFTITE = register("raw_siftite", Item::new, new Item.Properties());
	public static final Item SIFTITE_INGOT = register("siftite_ingot", Item::new, new Item.Properties().fireResistant());
	public static final Item CARAPACE_SCALE = register("carapace_scale", Item::new, new Item.Properties());
	public static final Item HARMONIC_CORE = register("harmonic_core", Item::new, new Item.Properties().stacksTo(16).rarity(Rarity.EPIC).fireResistant());

	public static final Item SIFTITE_SWORD = register("siftite_sword", Item::new, new Item.Properties().sword(ModMaterials.SIFTITE, 3.0F, -2.4F).fireResistant());
	public static final Item SIFTITE_SHOVEL = register("siftite_shovel", Item::new, new Item.Properties().shovel(ModMaterials.SIFTITE, 1.5F, -3.0F).fireResistant());
	public static final Item SIFTITE_PICKAXE = register("siftite_pickaxe", Item::new, new Item.Properties().pickaxe(ModMaterials.SIFTITE, 1.0F, -2.8F).fireResistant());
	public static final Item SIFTITE_AXE = register("siftite_axe", Item::new, new Item.Properties().axe(ModMaterials.SIFTITE, 5.0F, -3.0F).fireResistant());
	public static final Item SIFTITE_HOE = register("siftite_hoe", Item::new, new Item.Properties().hoe(ModMaterials.SIFTITE, -4.0F, 0.0F).fireResistant());
	public static final Item RESONANT_BLADE = register(
		"resonant_blade", Item::new, new Item.Properties().sword(ModMaterials.RESONANT, 3.0F, -2.2F).fireResistant().rarity(Rarity.EPIC)
	);

	public static final Item SIFTITE_HELMET = armor("siftite_helmet", ModMaterials.SIFTITE_ARMOR, ArmorType.HELMET);
	public static final Item SIFTITE_CHESTPLATE = armor("siftite_chestplate", ModMaterials.SIFTITE_ARMOR, ArmorType.CHESTPLATE);
	public static final Item SIFTITE_LEGGINGS = armor("siftite_leggings", ModMaterials.SIFTITE_ARMOR, ArmorType.LEGGINGS);
	public static final Item SIFTITE_BOOTS = armor("siftite_boots", ModMaterials.SIFTITE_ARMOR, ArmorType.BOOTS);
	public static final Item CARAPACE_HELMET = armor("carapace_helmet", ModMaterials.CARAPACE_ARMOR, ArmorType.HELMET);
	public static final Item CARAPACE_CHESTPLATE = armor("carapace_chestplate", ModMaterials.CARAPACE_ARMOR, ArmorType.CHESTPLATE);
	public static final Item CARAPACE_LEGGINGS = armor("carapace_leggings", ModMaterials.CARAPACE_ARMOR, ArmorType.LEGGINGS);
	public static final Item CARAPACE_BOOTS = armor("carapace_boots", ModMaterials.CARAPACE_ARMOR, ArmorType.BOOTS);

	public static final Item LUMENITE_SHARD = register("lumenite_shard", Item::new, new Item.Properties());
	public static final Item JADE = register("jade", Item::new, new Item.Properties());
	public static final Item WARDEN_HIDE = register("warden_hide", Item::new, new Item.Properties().fireResistant().rarity(Rarity.RARE));
	public static final Item WARDEN_HEART = register(
		"warden_heart",
		Item::new,
		new Item.Properties()
			.stacksTo(16)
			.rarity(Rarity.EPIC)
			.food(
				new FoodProperties.Builder().nutrition(6).saturationModifier(1.0F).alwaysEdible().build(),
				Consumables.defaultFood()
					.onConsume(
						new ApplyStatusEffectsConsumeEffect(
							List.of(
								new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3),
								new MobEffectInstance(MobEffects.REGENERATION, 400, 1),
								new MobEffectInstance(MobEffects.STRENGTH, 2400, 0)
							)
						)
					)
					.build()
			)
	);
	public static final Item WARDEN_CLEAVER = register(
		"warden_cleaver", Item::new, new Item.Properties().sword(ModMaterials.ANCIENT, 3.0F, -2.9F).fireResistant().rarity(Rarity.EPIC)
	);

	public static final Item ECHO_EYE = register("echo_eye", com.randompixxelstudios.thesift.item.EchoEyeItem::new, new Item.Properties().rarity(Rarity.UNCOMMON));
	public static final Item BUMBLEHOG_CHOP = food("bumblehog_chop", 3, 0.3F);
	public static final Item COOKED_BUMBLEHOG_CHOP = food("cooked_bumblehog_chop", 8, 0.8F);
	public static final Item DUSKFRUIT = food("duskfruit", 4, 0.3F);
	public static final Item ROASTED_DUSKFRUIT = food("roasted_duskfruit", 6, 0.6F);
	public static final Item VENISON = food("venison", 3, 0.3F);
	public static final Item COOKED_VENISON = food("cooked_venison", 8, 0.8F);
	public static final Item RAY_FILLET = food("ray_fillet", 2, 0.2F);
	public static final Item COOKED_RAY_FILLET = food("cooked_ray_fillet", 6, 0.7F);
	public static final Item BLUB_JELLY = register(
		"blub_jelly",
		Item::new,
		new Item.Properties()
			.food(
				new FoodProperties.Builder().nutrition(2).saturationModifier(0.2F).build(),
				Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 600, 1))).build()
			)
	);

	public static final Item LUMENITE_HELMET = armor("lumenite_helmet", ModMaterials.LUMENITE_ARMOR, ArmorType.HELMET);
	public static final Item LUMENITE_CHESTPLATE = armor("lumenite_chestplate", ModMaterials.LUMENITE_ARMOR, ArmorType.CHESTPLATE);
	public static final Item LUMENITE_LEGGINGS = armor("lumenite_leggings", ModMaterials.LUMENITE_ARMOR, ArmorType.LEGGINGS);
	public static final Item LUMENITE_BOOTS = armor("lumenite_boots", ModMaterials.LUMENITE_ARMOR, ArmorType.BOOTS);
	public static final Item JADE_HELMET = armor("jade_helmet", ModMaterials.JADE_ARMOR, ArmorType.HELMET);
	public static final Item JADE_CHESTPLATE = armor("jade_chestplate", ModMaterials.JADE_ARMOR, ArmorType.CHESTPLATE);
	public static final Item JADE_LEGGINGS = armor("jade_leggings", ModMaterials.JADE_ARMOR, ArmorType.LEGGINGS);
	public static final Item JADE_BOOTS = armor("jade_boots", ModMaterials.JADE_ARMOR, ArmorType.BOOTS);
	public static final Item WARDEN_HELMET = armor("warden_helmet", ModMaterials.WARDEN_ARMOR, ArmorType.HELMET);
	public static final Item WARDEN_CHESTPLATE = armor("warden_chestplate", ModMaterials.WARDEN_ARMOR, ArmorType.CHESTPLATE);
	public static final Item WARDEN_LEGGINGS = armor("warden_leggings", ModMaterials.WARDEN_ARMOR, ArmorType.LEGGINGS);
	public static final Item WARDEN_BOOTS = armor("warden_boots", ModMaterials.WARDEN_ARMOR, ArmorType.BOOTS);

	public static final Item SIFT_WILLOW_BOAT = boat("sift_willow_boat", ModEntities.SIFT_WILLOW_BOAT);
	public static final Item SIFT_WILLOW_CHEST_BOAT = boat("sift_willow_chest_boat", ModEntities.SIFT_WILLOW_CHEST_BOAT);
	public static final Item DUSKWOOD_BOAT = boat("duskwood_boat", ModEntities.DUSKWOOD_BOAT);
	public static final Item DUSKWOOD_CHEST_BOAT = boat("duskwood_chest_boat", ModEntities.DUSKWOOD_CHEST_BOAT);
	public static final Item PEARLBARK_BOAT = boat("pearlbark_boat", ModEntities.PEARLBARK_BOAT);
	public static final Item PEARLBARK_CHEST_BOAT = boat("pearlbark_chest_boat", ModEntities.PEARLBARK_CHEST_BOAT);

	public static final Item BLUB_SPAWN_EGG = register("blub_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.BLUB));
	public static final Item SIFTER_SPAWN_EGG = register("sifter_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SIFTER));
	public static final Item NUZZLE_SPAWN_EGG = register("nuzzle_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.NUZZLE));
	public static final Item SINGER_SPAWN_EGG = register("singer_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SINGER));
	public static final Item SHELLBACK_SPAWN_EGG = register("shellback_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SHELLBACK));
	public static final Item LUMOTH_SPAWN_EGG = register("lumoth_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.LUMOTH));
	public static final Item PEARL_STAG_SPAWN_EGG = register("pearl_stag_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.PEARL_STAG));
	public static final Item ICHOR_RAY_SPAWN_EGG = register("ichor_ray_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.ICHOR_RAY));
	public static final Item SHADE_SPAWN_EGG = register("shade_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.SHADE));
	public static final Item GLOOMSTALKER_SPAWN_EGG = register("gloomstalker_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.GLOOMSTALKER));
	public static final Item BUMBLEHOG_SPAWN_EGG = register("bumblehog_spawn_egg", SpawnEggItem::new, new Item.Properties().spawnEgg(ModEntities.BUMBLEHOG));

	private ModItems() {
	}

	private static Item food(final String name, final int nutrition, final float saturation) {
		return register(name, Item::new, new Item.Properties().food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build()));
	}

	private static Item boat(final String name, final net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.vehicle.boat.AbstractBoat> type) {
		return register(name, p -> new BoatItem(type, p), new Item.Properties().stacksTo(1));
	}

	private static Item armor(final String name, final ArmorMaterial material, final ArmorType type) {
		Item.Properties properties = new Item.Properties().humanoidArmor(material, type);
		if (material == ModMaterials.SIFTITE_ARMOR || material == ModMaterials.WARDEN_ARMOR) {
			properties.fireResistant();
		}

		return register(name, Item::new, properties);
	}

	private static Item register(final String name, final Function<Item.Properties, Item> factory, final Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, TheSift.id(name));
		Item item = factory.apply(properties.setId(key));
		if (item instanceof BlockItem blockItem) {
			blockItem.registerBlocks(Item.BY_BLOCK, item);
		}

		Registry.register(BuiltInRegistries.ITEM, key, item);
		ITEMS.add(item);
		return item;
	}

	private static void registerBlockItem(final Block block) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, BuiltInRegistries.BLOCK.getKey(block));
		Function<Item.Properties, Item> factory = block instanceof DoublePlantBlock || block instanceof DoorBlock
			? p -> new DoubleHighBlockItem(block, p)
			: p -> new BlockItem(block, p);
		Item item = factory.apply(new Item.Properties().useBlockDescriptionPrefix().setId(key));
		if (item instanceof BlockItem blockItem) {
			blockItem.registerBlocks(Item.BY_BLOCK, item);
		}

		Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void init() {
		ModBlocks.BLOCKS_WITH_ITEMS.forEach(ModItems::registerBlockItem);
	}
}
