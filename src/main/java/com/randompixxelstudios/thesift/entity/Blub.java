package com.randompixxelstudios.thesift.entity;

import com.randompixxelstudios.thesift.registry.ModBlocks;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * The Blub: a small, friendly, bouncy blue critter of the Sift. Blubs love company so much that they
 * climb on top of each other into wobbly towers - until the tower inevitably topples over.
 */
public class Blub extends PathfinderMob {
	public static final int MAX_STACK = 5;
	private int stackCooldown;

	public Blub(final EntityType<? extends Blub> type, final Level level) {
		super(type, level);
		this.stackCooldown = 200 + this.random.nextInt(400);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.TEMPT_RANGE, 10.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.6));
		this.goalSelector.addGoal(2, new TemptGoal(this, 1.1, stack -> stack.is(ModBlocks.LULLABY_BLOOM.asItem()) || stack.is(ModBlocks.ECHO_BLOOM.asItem()), false));
		this.goalSelector.addGoal(3, new StackUpGoal(this));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.9));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	public static boolean checkBlubSpawnRules(
		final EntityType<Blub> type, final LevelAccessor level, final EntitySpawnReason reason, final BlockPos pos, final RandomSource random
	) {
		return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 6;
	}

	/** Number of Blubs in the tower this one belongs to. */
	public int stackHeight() {
		int height = 1;
		Entity e = this.getRootVehicle();
		while (e != this && e instanceof Blub) {
			height++;
			e = e.getFirstPassenger();
		}

		Entity top = this;
		while (top.getFirstPassenger() instanceof Blub above) {
			height++;
			top = above;
		}

		return height;
	}

	public Blub topOfStack() {
		Entity top = this.getRootVehicle();
		while (top.getFirstPassenger() instanceof Blub above) {
			top = above;
		}

		return top instanceof Blub blub ? blub : this;
	}

	@Override
	protected boolean canAddPassenger(final Entity passenger) {
		return passenger instanceof Blub && !this.isVehicle();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.stackCooldown > 0) {
			this.stackCooldown--;
		}

		if (this.level() instanceof ServerLevel serverLevel && this.isVehicle() && !this.isPassenger()) {
			int height = this.stackHeight();
			// the taller the tower, the sooner it falls
			if (height >= 2 && this.random.nextInt(Math.max(20, 900 / height)) == 0) {
				this.topple(serverLevel);
			}
		}
	}

	private void topple(final ServerLevel level) {
		List<Entity> tower = new ArrayList<>();
		this.getIndirectPassengers().forEach(tower::add);
		this.ejectPassengers();
		for (Entity e : tower) {
			e.stopRiding();
			e.setDeltaMovement((this.random.nextDouble() - 0.5) * 0.6, 0.35, (this.random.nextDouble() - 0.5) * 0.6);
			if (e instanceof Blub blub) {
				blub.stackCooldown = 600 + this.random.nextInt(600);
			}
		}

		this.stackCooldown = 600 + this.random.nextInt(600);
		this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 1.0F, 1.4F);
		level.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.0, this.getZ(), 12, 0.3, 0.6, 0.3, 0.1);
	}

	@Override
	public boolean removeWhenFarAway(final double distSqr) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SLIME_SQUISH_SMALL;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.SLIME_HURT_SMALL;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SLIME_DEATH_SMALL;
	}

	@Override
	public float getVoicePitch() {
		return 1.6F + this.random.nextFloat() * 0.3F;
	}

	/** Hop over to a nearby Blub and climb on top of its tower. */
	static class StackUpGoal extends Goal {
		private final Blub blub;
		private Blub target;
		private int timeout;

		StackUpGoal(final Blub blub) {
			this.blub = blub;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (this.blub.stackCooldown > 0 || this.blub.isPassenger() || this.blub.isVehicle() || this.blub.getRandom().nextInt(60) != 0) {
				return false;
			}

			List<Blub> nearby = this.blub.level().getEntitiesOfClass(Blub.class, this.blub.getBoundingBox().inflate(8.0), b -> b != this.blub && !b.isPassenger());
			for (Blub other : nearby) {
				if (other.stackHeight() < MAX_STACK) {
					this.target = other;
					return true;
				}
			}

			return false;
		}

		@Override
		public boolean canContinueToUse() {
			return this.target != null && this.target.isAlive() && this.timeout > 0 && !this.blub.isPassenger() && this.target.stackHeight() < MAX_STACK;
		}

		@Override
		public void start() {
			this.timeout = 200;
		}

		@Override
		public void stop() {
			this.target = null;
			this.blub.stackCooldown = 300 + this.blub.getRandom().nextInt(300);
		}

		@Override
		public void tick() {
			this.timeout--;
			this.blub.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
			if (this.blub.distanceToSqr(this.target) < 2.0) {
				Blub top = this.target.topOfStack();
				if (top != this.blub && this.blub.startRiding(top, true, true)) {
					this.blub.playSound(SoundEvents.SLIME_JUMP_SMALL, 1.0F, 1.6F);
				}

				this.timeout = 0;
			} else {
				this.blub.getNavigation().moveTo(this.target, 1.1);
				if (this.blub.onGround() && this.blub.getRandom().nextInt(10) == 0) {
					this.blub.setDeltaMovement(this.blub.getDeltaMovement().add(new Vec3(0.0, 0.3, 0.0)));
				}
			}
		}
	}
}
