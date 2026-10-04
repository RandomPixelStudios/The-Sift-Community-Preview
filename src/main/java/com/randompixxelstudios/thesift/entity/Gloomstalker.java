package com.randompixxelstudios.thesift.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.level.Level;

/** The Gloomstalker: a glowing-eyed cave spider of the Sift whose bite blinds with darkness. */
public class Gloomstalker extends Spider {
	public Gloomstalker(final EntityType<? extends Gloomstalker> type, final Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Spider.createAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.ATTACK_DAMAGE, 4.0);
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit && target instanceof LivingEntity living) {
			living.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0), this);
		}

		return hit;
	}
}
