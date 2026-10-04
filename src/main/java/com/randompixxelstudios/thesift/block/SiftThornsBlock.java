package com.randompixxelstudios.thesift.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Sift Thorns: one of the Sift's hazardous blocks. Entities wading through get snagged, scratched
 * and briefly drained of strength.
 */
public class SiftThornsBlock extends VegetationBlock {
	private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 13.0);

	public SiftThornsBlock(final Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void entityInside(
		final BlockState state, final Level level, final BlockPos pos, final Entity entity, final InsideBlockEffectApplier effectApplier, final boolean isPrecise
	) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}

		entity.makeStuckInBlock(state, new Vec3(0.7F, 0.75, 0.7F));
		if (level instanceof ServerLevel serverLevel) {
			Vec3 movement = entity.isClientAuthoritative() ? entity.getKnownMovement() : entity.oldPosition().subtract(entity.position());
			if (movement.horizontalDistanceSqr() > 0.0) {
				living.hurtServer(serverLevel, level.damageSources().sweetBerryBush(), 1.5F);
				living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
			}
		}
	}
}
