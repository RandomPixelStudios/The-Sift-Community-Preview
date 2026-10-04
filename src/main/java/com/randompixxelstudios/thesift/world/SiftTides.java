package com.randompixxelstudios.thesift.world;

import com.randompixxelstudios.thesift.entity.Sifter;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

/**
 * The Sift Tides: the dimension cycles between three tides that change how it treats everyone in it.
 * <ul>
 *   <li><b>Flow</b> - calm, nothing special happens.</li>
 *   <li><b>Thrive</b> - the souls are generous: players slowly regenerate and work faster.</li>
 *   <li><b>Endure</b> - the dimension pushes back: Sifters grow stronger and tougher.</li>
 * </ul>
 * The tide is derived from the world's game time, so it needs no saved state.
 */
public final class SiftTides {
	public static final int TIDE_LENGTH = 6000;
	private static Tide lastTide;

	private SiftTides() {
	}

	public enum Tide {
		FLOW("flow", BossEvent.BossBarColor.BLUE, ChatFormatting.AQUA),
		THRIVE("thrive", BossEvent.BossBarColor.GREEN, ChatFormatting.GREEN),
		ENDURE("endure", BossEvent.BossBarColor.RED, ChatFormatting.RED);

		private final String name;
		private final BossEvent.BossBarColor color;
		private final ChatFormatting formatting;

		Tide(final String name, final BossEvent.BossBarColor color, final ChatFormatting formatting) {
			this.name = name;
			this.color = color;
			this.formatting = formatting;
		}

		public Component title() {
			return Component.translatable("tide.thesift." + this.name).withStyle(this.formatting);
		}
	}

	public static Tide current(final Level level) {
		long cycle = Math.floorMod(level.getGameTime(), (long) TIDE_LENGTH * 3);
		return Tide.values()[(int) (cycle / TIDE_LENGTH)];
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(SiftTides::tick);
	}

	private static void tick(final MinecraftServer server) {
		ServerLevel sift = server.getLevel(SiftDimension.LEVEL);
		if (sift == null) {
			return;
		}

		Tide tide = current(sift);
		if (tide != lastTide) {
			if (lastTide != null) {
				for (ServerPlayer player : sift.players()) {
					player.sendSystemMessage(Component.translatable("tide.thesift.changed", tide.title()), true);
				}
			}

			lastTide = tide;
		}

		if (sift.getGameTime() % 40 != 0) {
			return;
		}

		switch (tide) {
			case THRIVE -> {
				for (ServerPlayer player : sift.players()) {
					player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false, true));
					player.addEffect(new MobEffectInstance(MobEffects.HASTE, 60, 0, true, false, true));
				}
			}
			case ENDURE -> {
				for (ServerPlayer player : sift.players()) {
					for (Sifter sifter : sift.getEntitiesOfClass(Sifter.class, player.getBoundingBox().inflate(48.0))) {
						sifter.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 60, 0, true, true));
						sifter.addEffect(new MobEffectInstance(MobEffects.SPEED, 60, 0, true, true));
					}
				}
			}
			default -> {
			}
		}
	}
}
