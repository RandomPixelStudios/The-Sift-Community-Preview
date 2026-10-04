package com.randompixxelstudios.thesift.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

/** The Ichor Ray: a flat, shimmering ray that glides through the ichor seas, slowly flapping its wings. */
public class IchorRay extends WaterAnimal {
	public IchorRay(final EntityType<? extends IchorRay> type, final Level level) {
		super(type, level);
		this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 1.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
		this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
	}

	@Override
	protected PathNavigation createNavigation(final Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	public static boolean checkRaySpawnRules(
		final EntityType<? extends Mob> type, final LevelAccessor level, final EntitySpawnReason reason, final BlockPos pos, final RandomSource random
	) {
		return level.getFluidState(pos).isSource() && level.getFluidState(pos.above()).isSource();
	}

	@Override
	public void travel(final Vec3 input) {
		if (this.isInWater()) {
			this.moveRelative(this.getSpeed() * 0.02F, input);
			this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
		} else {
			super.travel(input);
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.DOLPHIN_AMBIENT_WATER;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.TROPICAL_FISH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.TROPICAL_FISH_DEATH;
	}
}
