package com.randompixxelstudios.thesift.block;

import com.randompixxelstudios.thesift.world.SiftDimension;
import com.randompixxelstudios.thesift.world.SiftPortalForcer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * The shimmering blue membrane that fills every Ancient City portal frame. Unlike a nether portal it is
 * always active and never needs a frame check - the Sift is already pressing through.
 */
public class SiftPortalBlock extends Block implements Portal {
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
	private static final Map<Direction.Axis, VoxelShape> SHAPES = Shapes.rotateHorizontalAxis(Block.column(4.0, 16.0, 0.0, 16.0));

	public SiftPortalBlock(final Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
	}

	@Override
	protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
		return SHAPES.get(state.getValue(AXIS));
	}

	@Override
	protected void entityInside(
		final BlockState state, final Level level, final BlockPos pos, final Entity entity, final InsideBlockEffectApplier effectApplier, final boolean isPrecise
	) {
		if (entity.canUsePortal(false)) {
			entity.setAsInsidePortal(this, pos);
		}
	}

	@Override
	public int getPortalTransitionTime(final ServerLevel level, final Entity entity) {
		// a little quicker than a nether portal; creative players pass through instantly
		return entity instanceof Player player ? (player.getAbilities().invulnerable ? 1 : 60) : 0;
	}

	@Override
	public @Nullable TeleportTransition getPortalDestination(final ServerLevel currentLevel, final Entity entity, final BlockPos portalEntryPos) {
		ServerLevel target = currentLevel.getServer().getLevel(SiftDimension.isSift(currentLevel) ? Level.OVERWORLD : SiftDimension.LEVEL);
		if (target == null) {
			return null;
		}

		return SiftPortalForcer.findOrCreateDestination(currentLevel, target, entity, portalEntryPos);
	}

	@Override
	public Portal.Transition getLocalTransition() {
		return Portal.Transition.CONFUSION;
	}

	@Override
	public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
		if (random.nextInt(120) == 0) {
			level.playLocalSound(
				pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.4F, random.nextFloat() * 0.3F + 1.3F, false
			);
		}

		boolean alongX = state.getValue(AXIS) == Direction.Axis.X;
		for (int i = 0; i < 2; i++) {
			double flip = random.nextInt(2) * 2 - 1;
			double x = pos.getX() + (alongX ? random.nextDouble() : 0.5 + 0.25 * flip);
			double y = pos.getY() + random.nextDouble();
			double z = pos.getZ() + (alongX ? 0.5 + 0.25 * flip : random.nextDouble());
			level.addParticle(random.nextInt(3) == 0 ? ParticleTypes.SCULK_SOUL : ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0, 0.02, 0.0);
		}
	}

	@Override
	protected ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state, final boolean includeData) {
		return ItemStack.EMPTY;
	}

	@Override
	protected BlockState rotate(final BlockState state, final Rotation rotation) {
		return switch (rotation) {
			case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
			default -> state;
		};
	}

	@Override
	protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}
}
