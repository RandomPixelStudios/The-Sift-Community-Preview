package com.randompixxelstudios.thesift.registry;

import com.randompixxelstudios.thesift.TheSift;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.resources.ResourceKey;

public final class ModMaterials {
	public static final TagKey<Item> SIFTITE_TOOL_MATERIALS = TagKey.create(Registries.ITEM, TheSift.id("siftite_tool_materials"));
	public static final TagKey<Item> REPAIRS_SIFTITE_ARMOR = TagKey.create(Registries.ITEM, TheSift.id("repairs_siftite_armor"));
	public static final TagKey<Item> REPAIRS_CARAPACE_ARMOR = TagKey.create(Registries.ITEM, TheSift.id("repairs_carapace_armor"));
	public static final TagKey<Item> REPAIRS_LUMENITE_ARMOR = TagKey.create(Registries.ITEM, TheSift.id("repairs_lumenite_armor"));
	public static final TagKey<Item> REPAIRS_JADE_ARMOR = TagKey.create(Registries.ITEM, TheSift.id("repairs_jade_armor"));
	public static final TagKey<Item> REPAIRS_WARDEN_ARMOR = TagKey.create(Registries.ITEM, TheSift.id("repairs_warden_armor"));
	public static final TagKey<Item> SIFTITE_EQUIPMENT = TagKey.create(Registries.ITEM, TheSift.id("siftite_equipment"));

	/** Siftite: a touch better than netherite and noticeably faster to mine with. */
	public static final ToolMaterial SIFTITE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2400, 11.0F, 4.5F, 18, SIFTITE_TOOL_MATERIALS);
	/** The Harmonizer's core forged into a blade. */
	public static final ToolMaterial RESONANT = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 3000, 11.0F, 6.0F, 22, SIFTITE_TOOL_MATERIALS);

	/** The Ancient Warden's cleaver: slow, brutal and nearly unbreakable. */
	public static final ToolMaterial ANCIENT = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4000, 9.0F, 8.0F, 15, REPAIRS_WARDEN_ARMOR);

	public static final ResourceKey<EquipmentAsset> SIFTITE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, TheSift.id("siftite"));
	public static final ResourceKey<EquipmentAsset> CARAPACE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, TheSift.id("carapace"));
	public static final ResourceKey<EquipmentAsset> LUMENITE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, TheSift.id("lumenite"));
	public static final ResourceKey<EquipmentAsset> JADE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, TheSift.id("jade"));
	public static final ResourceKey<EquipmentAsset> WARDEN_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, TheSift.id("warden"));

	public static final ArmorMaterial SIFTITE_ARMOR = new ArmorMaterial(
		40, defense(3, 6, 8, 3), 18, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.5F, 0.1F, REPAIRS_SIFTITE_ARMOR, SIFTITE_ASSET
	);
	/** Carapace: light shell armour, iron-grade but with a little toughness. */
	public static final ArmorMaterial CARAPACE_ARMOR = new ArmorMaterial(
		20, defense(2, 5, 6, 2), 12, SoundEvents.ARMOR_EQUIP_TURTLE, 1.0F, 0.0F, REPAIRS_CARAPACE_ARMOR, CARAPACE_ASSET
	);

	/** Lumenite: glowing gem armour, light and gold-grade. Full set: night vision and reveals nearby creatures. */
	public static final ArmorMaterial LUMENITE_ARMOR = new ArmorMaterial(
		18, defense(2, 5, 6, 2), 25, SoundEvents.ARMOR_EQUIP_GOLD, 0.0F, 0.0F, REPAIRS_LUMENITE_ARMOR, LUMENITE_ASSET
	);
	/** Jade: diamond-grade stone armour. Full set: swiftness and higher jumps. */
	public static final ArmorMaterial JADE_ARMOR = new ArmorMaterial(
		33, defense(3, 6, 8, 3), 14, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, REPAIRS_JADE_ARMOR, JADE_ASSET
	);
	/** Warden hide: the best armour in the Sift. Full set: strength, and immunity to darkness. */
	public static final ArmorMaterial WARDEN_ARMOR = new ArmorMaterial(
		45, defense(4, 7, 9, 4), 15, SoundEvents.ARMOR_EQUIP_NETHERITE, 4.0F, 0.15F, REPAIRS_WARDEN_ARMOR, WARDEN_ASSET
	);

	private ModMaterials() {
	}

	private static Map<ArmorType, Integer> defense(final int boots, final int legs, final int chest, final int helmet) {
		return Map.of(ArmorType.BOOTS, boots, ArmorType.LEGGINGS, legs, ArmorType.CHESTPLATE, chest, ArmorType.HELMET, helmet, ArmorType.BODY, chest);
	}
}
