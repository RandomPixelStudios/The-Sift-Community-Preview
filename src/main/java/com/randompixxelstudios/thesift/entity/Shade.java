package com.randompixxelstudios.thesift.entity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;

/**
 * The Shade: a hollow soul wandering the dark caves of the Sift. It shambles like a zombie and its touch
 * leaves victims weakened and slowed.
 */
public class Shade extends Zombie {
	public Shade(final EntityType<? extends Shade> type, final Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 24.0).add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ATTACK_DAMAGE, 4.0);
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit && target instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), this);
			living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0), this);
		}

		return hit;
	}

	@Override
	public void setBaby(final boolean baby) {
		// shades are never children
	}

	@Override
	protected boolean convertsInWater() {
		return false;
	}

	@Override
	protected boolean isSunSensitive() {
		return false;
	}
}
