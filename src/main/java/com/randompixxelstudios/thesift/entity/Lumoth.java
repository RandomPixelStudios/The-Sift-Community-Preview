package com.randompixxelstudios.thesift.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** The Lumoth: a small glowing moth that flutters around in swarms and lights up the Sift's evenings. */
public class Lumoth extends PathfinderMob {
	public Lumoth(final EntityType<? extends Lumoth> type, final Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl<>(this, 10, true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 4.0).add(Attributes.FLYING_SPEED, 0.3).add(Attributes.MOVEMENT_SPEED, 0.2);
	}

	@Override
	protected PathNavigation createNavigation(final Level level) {
		FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
		navigation.setCanOpenDoors(false);
		navigation.setCanFloat(true);
		return navigation;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomFlyingGoal(this, 1.0));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
	}

	public static boolean checkLumothSpawnRules(
		final EntityType<Lumoth> type, final LevelAccessor level, final EntitySpawnReason reason, final BlockPos pos, final RandomSource random
	) {
		return level.getBlockState(pos).isAir();
	}

	@Override
	public void travel(final Vec3 input) {
		this.travelFlying(input, this.getSpeed());
	}

	@Override
	public void tick() {
		super.tick();
		// erratic fluttering
		if (this.random.nextInt(10) == 0) {
			this.setDeltaMovement(this.getDeltaMovement().add((this.random.nextDouble() - 0.5) * 0.1, (this.random.nextDouble() - 0.45) * 0.08, (this.random.nextDouble() - 0.5) * 0.1));
		}

		if (this.level().isClientSide() && this.random.nextInt(12) == 0) {
			this.level().addParticle(ParticleTypes.GLOW, this.getX(), this.getY() + 0.2, this.getZ(), 0.0, -0.02, 0.0);
		}
	}

	@Override
	protected void checkFallDamage(final double ya, final boolean onGround, final BlockState onState, final BlockPos pos) {
	}

	@Override
	protected void playStepSound(final BlockPos pos, final BlockState blockState) {
	}

	@Override
	public boolean removeWhenFarAway(final double distSqr) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return null;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.ALLAY_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ALLAY_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 1.8F;
	}
}
