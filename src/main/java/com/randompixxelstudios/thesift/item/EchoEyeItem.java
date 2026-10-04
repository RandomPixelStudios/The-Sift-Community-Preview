package com.randompixxelstudios.thesift.item;

import com.randompixxelstudios.thesift.TheSift;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;

/** The Echo Eye: thrown like an eye of ender, it flies towards the nearest Ancient Temple in the Sift. */
public class EchoEyeItem extends Item {
	public static final TagKey<Structure> LOCATES = TagKey.create(Registries.STRUCTURE, TheSift.id("echo_eye_located"));

	public EchoEyeItem(final Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level instanceof ServerLevel serverLevel) {
			BlockPos target = serverLevel.findNearestMapStructure(LOCATES, player.blockPosition(), 100, false);
			if (target == null) {
				return InteractionResult.CONSUME;
			}

			EyeOfEnder eye = new EyeOfEnder(level, player.getX(), player.getY(0.5), player.getZ());
			eye.setItem(stack);
			eye.signalTo(Vec3.atLowerCornerOf(target));
			level.addFreshEntity(eye);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 1.0F, 0.6F);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.NEUTRAL, 0.4F, 1.6F);
			stack.consume(1, player);
		}

		return InteractionResult.SUCCESS_SERVER;
	}
}
