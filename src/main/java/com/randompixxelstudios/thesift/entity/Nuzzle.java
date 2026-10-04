package com.randompixxelstudios.thesift.entity;

import com.randompixxelstudios.thesift.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * The Nuzzle: a furry, frog-like critter of the Sift. It hops around flower fields and, when given a
 * flower, nuzzles up to you and leaves behind a soul shard as thanks.
 */
public class Nuzzle extends PathfinderMob {
	private int giftCooldown;

	public Nuzzle(final EntityType<? extends Nuzzle> type, final Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.TEMPT_RANGE, 10.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.8));
		this.goalSelector.addGoal(2, new TemptGoal(this, 1.1, Nuzzle::isFlower, false));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	private static boolean isFlower(final ItemStack stack) {
		return stack.is(ModBlocks.LULLABY_BLOOM.asItem()) || stack.is(ModBlocks.BLUEBELL.asItem()) || stack.is(ModBlocks.GLIMMER_PETALS.asItem());
	}

	public static boolean checkNuzzleSpawnRules(
		final EntityType<Nuzzle> type, final LevelAccessor level, final EntitySpawnReason reason, final BlockPos pos, final RandomSource random
	) {
		return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 6;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		// frogs hop: give a little bounce whenever walking on the ground
		if (!this.level().isClientSide() && this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 0.002 && this.random.nextInt(8) == 0) {
			this.setDeltaMovement(this.getDeltaMovement().add(new Vec3(0.0, 0.42, 0.0)));
		}

		if (this.giftCooldown > 0) {
			this.giftCooldown--;
		}
	}

	@Override
	public InteractionResult mobInteract(final Player player, final InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isFlower(stack) && this.giftCooldown == 0) {
			if (this.level() instanceof ServerLevel serverLevel) {
				stack.consume(1, player);
				this.giftCooldown = 2400;
				this.spawnAtLocation(serverLevel, new ItemStack(com.randompixxelstudios.thesift.registry.ModItems.SOUL_SHARD));
				serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.6, this.getZ(), 4, 0.3, 0.2, 0.3, 0.0);
				this.playSound(SoundEvents.FROG_AMBIENT, 1.0F, 1.5F);
			}

			return InteractionResult.SUCCESS;
		}

		return super.mobInteract(player, hand);
	}

	@Override
	public boolean removeWhenFarAway(final double distSqr) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.FROG_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.FROG_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.FROG_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 1.3F + this.random.nextFloat() * 0.2F;
	}
}
