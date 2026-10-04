package com.randompixxelstudios.thesift.entity;

import com.randompixxelstudios.thesift.world.SiftTides;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * The Sifter: a tall, lanky hunter of the Sift with a sieve for a face. Its strikes siphon off a bit
 * of the victim's soul (slowness + weakness). During the Endure tide Sifters grow stronger.
 */
public class Sifter extends Monster {
	public Sifter(final EntityType<? extends Sifter> type, final Level level) {
		super(type, level);
		this.xpReward = 8;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 30.0)
			.add(Attributes.MOVEMENT_SPEED, 0.27)
			.add(Attributes.ATTACK_DAMAGE, 5.0)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.ARMOR, 4.0)
			.add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	public static boolean checkSifterSpawnRules(
		final EntityType<Sifter> type, final LevelAccessor level, final EntitySpawnReason reason, final BlockPos pos, final RandomSource random
	) {
		return level.getDifficulty() != Difficulty.PEACEFUL
			&& Mob.checkMobSpawnRules(type, level, reason, pos, random)
			&& (level.getRawBrightness(pos, 0) < 10 || random.nextInt(4) == 0);
	}

	@Override
	public boolean doHurtTarget(final ServerLevel level, final Entity target) {
		boolean hit = super.doHurtTarget(level, target);
		if (hit && target instanceof LivingEntity living) {
			int seconds = SiftTides.current(level) == SiftTides.Tide.ENDURE ? 6 : 3;
			living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, seconds * 20, 0), this);
			living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, seconds * 20, 0), this);
		}

		return hit;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WARDEN_AMBIENT;
	}

	@Override
	public float getVoicePitch() {
		return 1.5F + this.random.nextFloat() * 0.2F;
	}

	@Override
	protected float getSoundVolume() {
		return 0.6F;
	}

	@Override
	protected SoundEvent getHurtSound(final DamageSource source) {
		return SoundEvents.SCULK_SHRIEKER_BREAK;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SCULK_CATALYST_BLOOM;
	}
}
