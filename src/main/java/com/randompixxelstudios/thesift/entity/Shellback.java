package com.randompixxelstudios.thesift.entity;

import com.randompixxelstudios.thesift.registry.ModBlocks;
import com.randompixxelstudios.thesift.registry.ModEntities;
import com.randompixxelstudios.thesift.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.Nullable;

/**
 * The Shellback: a big, slow tortoise of the Carapace with a shell of layered orange stone. It is
 * peaceful until struck - then it bites back hard. Every few minutes it sheds a carapace scale.
 */
public class Shellback extends Animal {
	private int shedTime = this.random.nextInt(6000) + 6000;

	public Shellback(final EntityType<? extends Shellback> type, final Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Animal.createAnimalAttributes()
			.add(Attributes.MAX_HEALTH, 30.0)
			.add(Attributes.MOVEMENT_SPEED, 0.16)
			.add(Attributes.ARMOR, 10.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
			.add(Attributes.ATTACK_DAMAGE, 6.0)
			.add(Attributes.TEMPT_RANGE, 10.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, this::isFood, false));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
	}

	public static boolean checkGroundSpawnRules(
		final EntityType<? extends Mob> type, final LevelAccessor level, final EntitySpawnReason reason, final BlockPos pos, final RandomSource random
	) {
		return level.getBlockState(pos.below()).isSolid() && level.getRawBrightness(pos, 0) > 6;
	}

	@Override
	public boolean isFood(final ItemStack stack) {
		return stack.is(ModBlocks.SIFT_THORNS.asItem());
	}

	@Override
	public @Nullable AgeableMob getBreedOffspring(final ServerLevel level, final AgeableMob partner) {
		return ModEntities.SHELLBACK.create(level, EntitySpawnReason.BREEDING);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level() instanceof ServerLevel level && this.isAlive() && !this.isBaby() && --this.shedTime <= 0) {
			this.spawnAtLocation(level, new ItemStack(ModItems.CARAPACE_SCALE));
			this.playSound(SoundEvents.ARMADILLO_SCUTE_DROP, 1.0F, 0.8F);
			this.shedTime = this.random.nextInt(6000) + 6000;
		}
	}

	@Override
	public boolean removeWhenFarAway(final double distSqr) {
		return false;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.TURTLE_AMBIENT_LAND;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.TURTLE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.TURTLE_DEATH;
	}

	@Override
	public float getVoicePitch() {
		return 0.6F;
	}
}
