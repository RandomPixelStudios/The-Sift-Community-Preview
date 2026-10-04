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

/**
 * The Singer: a gentle creature that drifts through the air of the Sift, humming. Its song soothes
 * nearby players, slowly healing them.
 */
public class Singer extends PathfinderMob {
	public Singer(final EntityType<? extends Singer> type, final Level level) {
		super(type, level);
		this.moveControl = new FlyingMoveControl<>(this, 10, true);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.FLYING_SPEED, 0.08).add(Attributes.MOVEMENT_SPEED, 0.08);
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

	public static boolean checkSingerSpawnRules(
		final EntityType<Singer> type, final LevelAccessor level, final EntitySpawnReason reason, final BlockPos pos, final RandomSource random
	) {
		return level.getRawBrightness(pos, 0) > 8;
	}

	@Override
	public void travel(final Vec3 input) {
		this.travelFlying(input, this.getSpeed());
	}

	@Override
	public void tick() {
		super.tick();
		// gentle bobbing
		this.setDeltaMovement(this.getDeltaMovement().multiply(1.0, 0.6, 1.0).add(0.0, Math.sin(this.tickCount * 0.08) * 0.006, 0.0));
		if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 80 == 0) {
			this.sing(serverLevel);
		}
	}

	private void sing(final ServerLevel level) {
		this.playSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, 1.2F, 0.5F + this.random.nextFloat() * 0.3F);
		level.sendParticles(ParticleTypes.NOTE, this.getX(), this.getY() + 1.2, this.getZ(), 3, 0.6, 0.3, 0.6, 1.0);
		for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, this.getBoundingBox().inflate(8.0))) {
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0, true, true), this);
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
		return SoundEvents.AMETHYST_BLOCK_CHIME;
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
		return 0.6F;
	}
}
