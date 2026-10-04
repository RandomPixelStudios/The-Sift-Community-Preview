package com.randompixxelstudios.thesift.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Ichor Drowning: the thick ichor fills the lungs much faster than water. While affected, breath runs
 * out three times as fast and keeps draining for a moment even after surfacing.
 */
public class IchorDrowningEffect extends MobEffect {
	public IchorDrowningEffect() {
		super(MobEffectCategory.HARMFUL, 0xB46CF0);
	}

	@Override
	public boolean applyEffectTick(final ServerLevel level, final LivingEntity mob, final int amplification) {
		if (!mob.canBreatheUnderwater()) {
			int air = mob.getAirSupply() - 2 - amplification;
			if (air <= -20) {
				air = 0;
				mob.hurtServer(level, level.damageSources().drown(), 2.0F);
			}

			mob.setAirSupply(air);
		}

		return true;
	}

	@Override
	public boolean shouldApplyEffectTickThisTick(final int tickCount, final int amplification) {
		return true;
	}
}
