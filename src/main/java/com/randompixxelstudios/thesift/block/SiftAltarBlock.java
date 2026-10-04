package com.randompixxelstudios.thesift.block;

import com.randompixxelstudios.thesift.registry.ModItems;
import com.randompixxelstudios.thesift.registry.ModMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The old altars of the abandoned shrines. Offer siftite gear together with soul shards and some
 * experience, and the souls of the Sift re-roll its enchantments at random - sometimes generously.
 */
public class SiftAltarBlock extends Block {
	public static final int SHARD_COST = 3;
	public static final int LEVEL_COST = 5;

	public SiftAltarBlock(final Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useItemOn(
		final ItemStack stack, final BlockState state, final Level level, final BlockPos pos, final Player player, final InteractionHand hand, final BlockHitResult hit
	) {
		if (!stack.is(ModMaterials.SIFTITE_EQUIPMENT)) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(Component.translatable("block.thesift.sift_altar.only_siftite").withStyle(ChatFormatting.LIGHT_PURPLE));
			}

			return InteractionResult.SUCCESS;
		}

		boolean free = player.getAbilities().instabuild;
		if (!free && (player.experienceLevel < LEVEL_COST || player.getInventory().countItem(ModItems.SOUL_SHARD) < SHARD_COST)) {
			if (!level.isClientSide()) {
				player.sendOverlayMessage(
					Component.translatable("block.thesift.sift_altar.cost", SHARD_COST, LEVEL_COST).withStyle(ChatFormatting.RED)
				);
			}

			return InteractionResult.FAIL;
		}

		if (level instanceof ServerLevel serverLevel) {
			if (!free) {
				removeShards(player, SHARD_COST);
				player.giveExperienceLevels(-LEVEL_COST);
			}

			RandomSource random = serverLevel.getRandom();
			EnchantmentHelper.setEnchantments(stack, ItemEnchantments.EMPTY);
			int power = 15 + random.nextInt(31);
			ItemStack result = EnchantmentHelper.enchantItem(
				random, stack, power, serverLevel.registryAccess(), serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.IN_ENCHANTING_TABLE)
			);
			player.setItemInHand(hand, result);
			serverLevel.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
			serverLevel.playSound(null, pos, SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.5F, 1.0F);
			serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.05);
			serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 60, 0.6, 0.6, 0.6, 0.5);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hit) {
		if (!level.isClientSide()) {
			player.sendOverlayMessage(Component.translatable("block.thesift.sift_altar.only_siftite").withStyle(ChatFormatting.LIGHT_PURPLE));
			level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 0.8F);
		}

		return InteractionResult.SUCCESS;
	}

	private static void removeShards(final Player player, int amount) {
		var inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize() && amount > 0; i++) {
			ItemStack slot = inventory.getItem(i);
			if (slot.is(ModItems.SOUL_SHARD)) {
				int take = Math.min(amount, slot.getCount());
				slot.shrink(take);
				amount -= take;
			}
		}
	}

	@Override
	public void animateTick(final BlockState state, final Level level, final BlockPos pos, final RandomSource random) {
		if (random.nextInt(4) == 0) {
			level.addParticle(ParticleTypes.SOUL, pos.getX() + 0.2 + random.nextDouble() * 0.6, pos.getY() + 1.0, pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0.0, 0.02, 0.0);
		}
	}
}
