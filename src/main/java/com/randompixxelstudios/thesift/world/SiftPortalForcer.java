package com.randompixxelstudios.thesift.world;

import com.randompixxelstudios.thesift.block.SiftPortalBlock;
import com.randompixxelstudios.thesift.registry.ModBlocks;
import com.randompixxelstudios.thesift.registry.ModPoi;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.BlockUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Finds the nearest Sift portal in the destination dimension, or builds a small reinforced deepslate
 * gateway on the surface if none exists yet. Coordinates map 1:1 between the Overworld and the Sift.
 */
public final class SiftPortalForcer {
	private static final int SEARCH_RADIUS = 128;

	private SiftPortalForcer() {
	}

	public static @Nullable TeleportTransition findOrCreateDestination(
		final ServerLevel from, final ServerLevel to, final Entity entity, final BlockPos entryPos
	) {
		WorldBorder border = to.getWorldBorder();
		BlockPos approx = border.clampToBounds(entity.getX(), entity.getY(), entity.getZ());

		// leaving the Sift always brings you back out of an Ancient City's portal
		Optional<BlockPos> existing = to.dimension() == net.minecraft.world.level.Level.OVERWORLD
			? findAncientCityPortal(to, approx, border).or(() -> findClosestPortal(to, approx, border))
			: findClosestPortal(to, approx, border);
		BlockPos portalPos;
		if (existing.isPresent()) {
			portalPos = existing.get();
		} else {
			if (entity.isSpectator()) {
				return null;
			}

			Direction.Axis axis = from.getBlockState(entryPos).getOptionalValue(SiftPortalBlock.AXIS).orElse(Direction.Axis.X);
			portalPos = buildPortal(to, approx, axis);
		}

		BlockState portalState = to.getBlockState(portalPos);
		Direction.Axis axis = portalState.getOptionalValue(SiftPortalBlock.AXIS).orElse(Direction.Axis.X);
		BlockUtil.FoundRectangle rect = BlockUtil.getLargestRectangleAround(
			portalPos, axis, 21, Direction.Axis.Y, 21, pos -> to.getBlockState(pos) == portalState
		);

		// stand in the middle of the bottom row of the portal
		BlockPos corner = rect.minCorner;
		double half = rect.axis1Size / 2.0;
		Vec3 target = axis == Direction.Axis.X
			? new Vec3(corner.getX() + half, corner.getY(), corner.getZ() + 0.5)
			: new Vec3(corner.getX() + 0.5, corner.getY(), corner.getZ() + half);
		Vec3 safe = PortalShape.findCollisionFreePosition(target, to, entity, entity.getDimensions(entity.getPose()));
		float yRot = axis == Direction.Axis.X ? 0.0F : 90.0F;
		return new TeleportTransition(
			to, safe, Vec3.ZERO, yRot, entity.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND.then(e -> e.placePortalTicket(portalPos))
		);
	}

	private static final net.minecraft.tags.TagKey<net.minecraft.world.level.levelgen.structure.Structure> ANCIENT_CITIES = net.minecraft.tags.TagKey.create(
		net.minecraft.core.registries.Registries.STRUCTURE, com.randompixxelstudios.thesift.TheSift.id("ancient_cities")
	);

	private static Optional<BlockPos> findAncientCityPortal(final ServerLevel level, final BlockPos approx, final WorldBorder border) {
		BlockPos city = level.findNearestMapStructure(ANCIENT_CITIES, approx, 100, false);
		if (city == null) {
			return Optional.empty();
		}

		// make sure the city centre (and its portal) has been generated before looking for it
		int cx = city.getX() >> 4;
		int cz = city.getZ() >> 4;
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				level.getChunk(cx + dx, cz + dz);
			}
		}

		BlockPos centre = new BlockPos(city.getX(), -27, city.getZ());
		Optional<BlockPos> found = findClosestPortal(level, centre, border, 64);
		return found;
	}

	private static Optional<BlockPos> findClosestPortal(final ServerLevel level, final BlockPos approx, final WorldBorder border) {
		return findClosestPortal(level, approx, border, SEARCH_RADIUS);
	}

	private static Optional<BlockPos> findClosestPortal(final ServerLevel level, final BlockPos approx, final WorldBorder border, final int radius) {
		PoiManager poiManager = level.getPoiManager();
		poiManager.ensureLoadedAndValid(level, approx, radius);
		return poiManager.getInSquare(type -> type.is(ModPoi.SIFT_PORTAL), approx, radius, PoiManager.Occupancy.ANY)
			.map(PoiRecord::getPos)
			.filter(border::isWithinBounds)
			.filter(pos -> level.getBlockState(pos).is(ModBlocks.SIFT_PORTAL))
			.min(Comparator.<BlockPos>comparingDouble(p -> p.distSqr(approx)).thenComparingInt(Vec3i::getY));
	}

	/**
	 * Builds a decorated gateway on the surface: a 3x4 portal in a reinforced deepslate frame, flanked by
	 * sift-brick pillars with lanterns, standing on a stepped polished platform. Returns the bottom-left
	 * portal block.
	 */
	private static BlockPos buildPortal(final ServerLevel level, final BlockPos approx, final Direction.Axis axis) {
		BlockPos ground = findGround(level, approx);
		int maxY = level.getMinY() + level.getLogicalHeight() - 10;
		BlockPos base = ground != null
			? ground.above()
			: new BlockPos(approx.getX(), Mth.clamp(level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, approx.getX(), approx.getZ()) + 1, 70, maxY), approx.getZ());

		Direction along = Direction.get(Direction.AxisDirection.POSITIVE, axis);
		Direction across = along.getClockWise();
		BlockState polished = ModBlocks.POLISHED_SIFT_STONE.defaultBlockState();
		BlockState bricks = ModBlocks.SIFT_STONE_BRICKS.defaultBlockState();
		BlockState chiseled = ModBlocks.CHISELED_SIFT_STONE_BRICKS.defaultBlockState();
		BlockState frame = Blocks.REINFORCED_DEEPSLATE.defaultBlockState();
		BlockState portal = ModBlocks.SIFT_PORTAL.defaultBlockState().setValue(SiftPortalBlock.AXIS, axis);
		BlockState lantern = ModBlocks.SIFT_LANTERN.defaultBlockState();
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

		// clear the air above and lay a two-step platform with foundations reaching down to the ground
		for (int w = -4; w <= 6; w++) {
			for (int d = -3; d <= 3; d++) {
				boolean outer = w == -4 || w == 6 || d == -3 || d == 3;
				for (int h = 0; h <= 7; h++) {
					set(level, m, base, along, across, w, h, d, Blocks.AIR.defaultBlockState());
				}

				set(level, m, base, along, across, w, -1, d, outer ? bricks : polished);
				for (int h = -2; h >= -30; h--) {
					m.setWithOffset(base, along.getStepX() * w + across.getStepX() * d, h, along.getStepZ() * w + across.getStepZ() * d);
					BlockState below = level.getBlockState(m);
					if (!below.isAir() && below.getFluidState().isEmpty()) {
						break;
					}

					level.setBlockAndUpdate(m, bricks);
				}
			}
		}

		// frame and membrane
		for (int w = -1; w <= 3; w++) {
			for (int h = -1; h <= 4; h++) {
				boolean edge = w == -1 || w == 3 || h == -1 || h == 4;
				m.setWithOffset(base, along.getStepX() * w, h, along.getStepZ() * w);
				level.setBlock(m, edge ? frame : portal, 18);
			}
		}

		// pillars with lanterns either side, and a chiseled crown
		for (int w : new int[] {-3, 5}) {
			for (int h = 0; h <= 4; h++) {
				set(level, m, base, along, across, w, h, 0, h == 4 ? chiseled : bricks);
			}

			set(level, m, base, along, across, w, 5, 0, lantern);
		}

		for (int w = -1; w <= 3; w++) {
			set(level, m, base, along, across, w, 5, 0, w == 1 ? chiseled : ModBlocks.SIFT_STONE_BRICK_SLAB.defaultBlockState());
		}

		for (int d : new int[] {-2, 2}) {
			set(level, m, base, along, across, -3, 0, d, lantern);
			set(level, m, base, along, across, 5, 0, d, lantern);
		}

		return base;
	}

	public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> GROUND = net.minecraft.tags.TagKey.create(
		net.minecraft.core.registries.Registries.BLOCK, com.randompixxelstudios.thesift.TheSift.id("arrival_ground")
	);

	/**
	 * Searches outwards from {@code approx} for real, flat, dry ground (grass, moss, sand - not rock arches,
	 * tree tops or ichor) and returns the top ground block, or null if there is none nearby.
	 */
	public static @Nullable BlockPos findGround(final ServerLevel level, final BlockPos approx) {
		for (int r = 0; r <= 48; r += 4) {
			for (int dx = -r; dx <= r; dx += 4) {
				for (int dz = -r; dz <= r; dz += 4) {
					if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
						continue;
					}

					int x = approx.getX() + dx;
					int z = approx.getZ() + dz;
					int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
					BlockPos top = new BlockPos(x, y, z);
					if (isGoodGround(level, top)) {
						return top;
					}
				}
			}
		}

		return null;
	}

	private static boolean isGoodGround(final ServerLevel level, final BlockPos top) {
		BlockState state = level.getBlockState(top);
		if (!state.is(GROUND) || !level.getFluidState(top.above()).isEmpty()) {
			return false;
		}

		for (int i = 1; i <= 4; i++) {
			if (level.getBlockState(top.below(i)).isAir()) {
				return false;
			}
		}

		// roughly flat around it
		for (Direction d : Direction.Plane.HORIZONTAL) {
			int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, top.getX() + d.getStepX() * 3, top.getZ() + d.getStepZ() * 3) - 1;
			if (Math.abs(h - top.getY()) > 2) {
				return false;
			}
		}

		return true;
	}

	private static void set(
		final ServerLevel level, final BlockPos.MutableBlockPos m, final BlockPos base, final Direction along, final Direction across,
		final int w, final int h, final int d, final BlockState state
	) {
		m.setWithOffset(base, along.getStepX() * w + across.getStepX() * d, h, along.getStepZ() * w + across.getStepZ() * d);
		level.setBlockAndUpdate(m, state);
	}
}
