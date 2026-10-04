package com.randompixxelstudios.thesift.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A moss-topped ground block (Blushing Moss, Glowmoss). Like grass it dies back to its base rock when
 * covered, and slowly creeps over neighbouring base rock that is open to the air.
 */
public class SiftSoilBlock extends Block {
	private final Block base;

	public SiftSoilBlock(final Block base, final Properties properties) {
		super(properties);
		this.base = base;
	}

	public Block getBase() {
		return this.base;
	}

	private static boolean isCovered(final LevelReader level, final BlockPos pos) {
		BlockPos above = pos.above();
		BlockState aboveState = level.getBlockState(above);
		return aboveState.getFluidState().isFull() || aboveState.isSolidRender();
	}

	@Override
	protected void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
		if (isCovered(level, pos)) {
			level.setBlockAndUpdate(pos, this.base.defaultBlockState());
			return;
		}

		for (int i = 0; i < 3; i++) {
			BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
			if (level.getBlockState(target).is(this.base) && !isCovered(level, target)) {
				level.setBlockAndUpdate(target, this.defaultBlockState());
			}
		}
	}
}
