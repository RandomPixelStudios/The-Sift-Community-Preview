package com.randompixxelstudios.thesift.command;

import com.randompixxelstudios.thesift.world.SiftDimension;
import com.randompixxelstudios.thesift.world.SiftPortalForcer;
import java.util.Set;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

/** /test - teleports you straight into the Sift, onto solid ground near your current x/z. */
public final class SiftCommands {
	private SiftCommands() {
	}

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
			Commands.literal("test").executes(context -> {
				ServerPlayer player = context.getSource().getPlayerOrException();
				ServerLevel sift = context.getSource().getServer().getLevel(SiftDimension.LEVEL);
				if (sift == null) {
					return 0;
				}

				BlockPos approx = BlockPos.containing(player.getX(), 80, player.getZ());
				BlockPos ground = SiftPortalForcer.findGround(sift, approx);
				if (ground == null) {
					ground = new BlockPos(approx.getX(), sift.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, approx.getX(), approx.getZ()) - 1, approx.getZ());
				}

				player.teleportTo(sift, ground.getX() + 0.5, ground.getY() + 1, ground.getZ() + 0.5, Set.of(), player.getYRot(), player.getXRot(), true);
				context.getSource().sendSuccess(() -> Component.literal("Welcome to the Sift"), false);
				return 1;
			})
		));
	}
}
