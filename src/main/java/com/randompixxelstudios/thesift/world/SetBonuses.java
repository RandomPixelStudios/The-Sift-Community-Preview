package com.randompixxelstudios.thesift.world;

import com.randompixxelstudios.thesift.registry.ModMaterials;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.ArmorMaterial;

/**
 * Full-set abilities of the Sift's armour:
 * <ul>
 *   <li><b>Siftite</b> - Resistance, and immunity to ichor's nausea and drowning.</li>
 *   <li><b>Carapace</b> - Fire Resistance.</li>
 *   <li><b>Lumenite</b> - Night Vision, and nearby monsters glow.</li>
 *   <li><b>Jade</b> - Speed and Jump Boost.</li>
 *   <li><b>Warden</b> - Strength, and immunity to Darkness.</li>
 * </ul>
 */
public final class SetBonuses {
	private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private SetBonuses() {
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(SetBonuses::tick);
	}

	public static boolean hasFullSet(final LivingEntity entity, final ArmorMaterial material) {
		for (EquipmentSlot slot : ARMOR) {
			ItemStack stack = entity.getItemBySlot(slot);
			Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
			if (equippable == null || equippable.assetId().isEmpty() || equippable.assetId().get() != material.assetId()) {
				return false;
			}
		}

		return true;
	}

	private static void tick(final MinecraftServer server) {
		if (server.getTickCount() % 20 != 0) {
			return;
		}

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (hasFullSet(player, ModMaterials.SIFTITE_ARMOR)) {
				give(player, new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, false, true));
			}

			if (hasFullSet(player, ModMaterials.CARAPACE_ARMOR)) {
				give(player, new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, true));
			}

			if (hasFullSet(player, ModMaterials.LUMENITE_ARMOR)) {
				give(player, new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false, true));
				ServerLevel level = player.level();
				for (LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16.0), e -> e instanceof Enemy)) {
					mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false), player);
				}
			}

			if (hasFullSet(player, ModMaterials.JADE_ARMOR)) {
				give(player, new MobEffectInstance(MobEffects.SPEED, 60, 0, true, false, true));
				give(player, new MobEffectInstance(MobEffects.JUMP_BOOST, 60, 0, true, false, true));
			}

			if (hasFullSet(player, ModMaterials.WARDEN_ARMOR)) {
				give(player, new MobEffectInstance(MobEffects.STRENGTH, 60, 0, true, false, true));
				player.removeEffect(MobEffects.DARKNESS);
			}
		}
	}

	private static void give(final ServerPlayer player, final MobEffectInstance effect) {
		player.addEffect(effect);
	}
}
