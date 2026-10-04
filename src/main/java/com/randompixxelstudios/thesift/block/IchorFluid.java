package com.randompixxelstudios.thesift.block;

import com.randompixxelstudios.thesift.registry.ModBlocks;
import com.randompixxelstudios.thesift.registry.ModEffects;
import com.randompixxelstudios.thesift.registry.ModMaterials;
import com.randompixxelstudios.thesift.world.SetBonuses;
import com.randompixxelstudios.thesift.registry.ModFluids;
import com.randompixxelstudios.thesift.registry.ModItems;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * Ichor: the shimmering liquid of the Sift. It makes swimmers dizzy, and drowns them much faster than water.
 */
public abstract class IchorFluid extends FlowingFluid {
	@Override
	public Fluid getFlowing() {
		return ModFluids.FLOWING_ICHOR;
	}

	@Override
	public Fluid getSource() {
		return ModFluids.ICHOR;
	}

	@Override
	public Item getBucket() {
		return ModItems.ICHOR_BUCKET;
	}

	@Override
	public void animateTick(final Level level, final BlockPos pos, final FluidState fluidState, final RandomSource random) {
		if (fluidState.isSource() && random.nextInt(40) == 0 && level.getBlockState(pos.above()).isAir()) {
			level.addParticle(ParticleTypes.END_ROD, pos.getX() + random.nextDouble(), pos.getY() + 1.0, pos.getZ() + random.nextDouble(), 0.0, 0.03, 0.0);
		}
	}

	@Override
	public @Nullable ParticleOptions getDripParticle() {
		return ParticleTypes.DRIPPING_WATER;
	}

	@Override
	protected boolean canConvertToSource(final ServerLevel level) {
		return false;
	}

	@Override
	protected void beforeDestroyingBlock(final LevelAccessor level, final BlockPos pos, final BlockState state) {
		BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
		Block.dropResources(state, level, pos, blockEntity);
	}

	@Override
	protected void entityInside(final Level level, final BlockPos pos, final Entity entity, final InsideBlockEffectApplier effectApplier) {
		effectApplier.apply(InsideBlockEffectType.EXTINGUISH);
		// ichor makes you dizzy and drowns you fast - unless you wear a full siftite set
		if (!level.isClientSide() && entity instanceof LivingEntity living && !SetBonuses.hasFullSet(living, ModMaterials.SIFTITE_ARMOR)) {
			if (!living.hasEffect(MobEffects.NAUSEA) || living.getEffect(MobEffects.NAUSEA).getDuration() < 60) {
				living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0, true, true));
			}

			if (living.isEyeInFluid(net.minecraft.tags.FluidTags.WATER)) {
				living.addEffect(new MobEffectInstance(ModEffects.ICHOR_DROWNING, 60, 0, true, true));
			}
		}
	}

	@Override
	public int getSlopeFindDistance(final LevelReader level) {
		return 3;
	}

	@Override
	public BlockState createLegacyBlock(final FluidState fluidState) {
		return ModBlocks.ICHOR.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(fluidState));
	}

	@Override
	public boolean isSame(final Fluid other) {
		return other == ModFluids.ICHOR || other == ModFluids.FLOWING_ICHOR;
	}

	@Override
	public int getDropOff(final LevelReader level) {
		return 2;
	}

	@Override
	public int getTickDelay(final LevelReader level) {
		return 10;
	}

	@Override
	public boolean canBeReplacedWith(final FluidState state, final BlockGetter level, final BlockPos pos, final Fluid other, final Direction direction) {
		return direction == Direction.DOWN && !this.isSame(other);
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0F;
	}

	@Override
	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL);
	}

	public static class Flowing extends IchorFluid {
		@Override
		protected void createFluidStateDefinition(final StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getAmount(final FluidState fluidState) {
			return fluidState.getValue(LEVEL);
		}

		@Override
		public boolean isSource(final FluidState fluidState) {
			return false;
		}
	}

	public static class Source extends IchorFluid {
		@Override
		public int getAmount(final FluidState fluidState) {
			return 8;
		}

		@Override
		public boolean isSource(final FluidState fluidState) {
			return true;
		}
	}
}
