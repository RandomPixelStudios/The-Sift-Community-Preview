package com.randompixxelstudios.thesift.client;

import com.randompixxelstudios.thesift.TheSift;
import com.randompixxelstudios.thesift.client.model.BlubModel;
import com.randompixxelstudios.thesift.client.model.IchorRayModel;
import com.randompixxelstudios.thesift.client.model.LumothModel;
import com.randompixxelstudios.thesift.client.model.PearlStagModel;
import com.randompixxelstudios.thesift.client.model.ShellbackModel;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.entity.BoatRenderer;
import com.randompixxelstudios.thesift.client.model.NuzzleModel;
import com.randompixxelstudios.thesift.client.model.SingerModel;
import com.randompixxelstudios.thesift.client.model.SifterModel;
import com.randompixxelstudios.thesift.client.render.BlubRenderer;
import com.randompixxelstudios.thesift.client.render.SifterRenderer;
import com.randompixxelstudios.thesift.client.render.SimpleMobRenderer;
import com.randompixxelstudios.thesift.registry.ModEntities;
import com.randompixxelstudios.thesift.registry.ModFluids;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;

public class TheSiftClient implements ClientModInitializer {
	public static final ModelLayerLocation BLUB_LAYER = new ModelLayerLocation(TheSift.id("blub"), "main");
	public static final ModelLayerLocation SIFTER_LAYER = new ModelLayerLocation(TheSift.id("sifter"), "main");
	public static final ModelLayerLocation NUZZLE_LAYER = new ModelLayerLocation(TheSift.id("nuzzle"), "main");
	public static final ModelLayerLocation SINGER_LAYER = new ModelLayerLocation(TheSift.id("singer"), "main");
	public static final ModelLayerLocation SHELLBACK_LAYER = new ModelLayerLocation(TheSift.id("shellback"), "main");
	public static final ModelLayerLocation PEARL_STAG_LAYER = new ModelLayerLocation(TheSift.id("pearl_stag"), "main");
	public static final ModelLayerLocation LUMOTH_LAYER = new ModelLayerLocation(TheSift.id("lumoth"), "main");
	public static final ModelLayerLocation BUMBLEHOG_LAYER = new ModelLayerLocation(TheSift.id("bumblehog"), "main");
	public static final ModelLayerLocation SHADE_LAYER = new ModelLayerLocation(TheSift.id("shade"), "main");
	public static final ModelLayerLocation GLOOMSTALKER_LAYER = new ModelLayerLocation(TheSift.id("gloomstalker"), "main");
	public static final ModelLayerLocation ICHOR_RAY_LAYER = new ModelLayerLocation(TheSift.id("ichor_ray"), "main");

	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(BLUB_LAYER, BlubModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(SIFTER_LAYER, SifterModel::createBodyLayer);
		EntityRendererRegistry.register(ModEntities.BLUB, BlubRenderer::new);
		EntityRendererRegistry.register(ModEntities.SIFTER, SifterRenderer::new);
		ModelLayerRegistry.registerModelLayer(NUZZLE_LAYER, NuzzleModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(SINGER_LAYER, SingerModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(SHELLBACK_LAYER, ShellbackModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(PEARL_STAG_LAYER, PearlStagModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(LUMOTH_LAYER, LumothModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(ICHOR_RAY_LAYER, IchorRayModel::createBodyLayer);
		EntityRendererRegistry.register(
			ModEntities.NUZZLE, c -> new SimpleMobRenderer<>(c, NuzzleModel::new, NUZZLE_LAYER, TheSift.id("textures/entity/nuzzle/nuzzle.png"), null, 0.4F, 1.0F)
		);
		EntityRendererRegistry.register(
			ModEntities.SINGER, c -> new SimpleMobRenderer<>(c, SingerModel::new, SINGER_LAYER, TheSift.id("textures/entity/singer/singer.png"), null, 0.5F, 1.0F)
		);
		EntityRendererRegistry.register(
			ModEntities.SHELLBACK,
			c -> new SimpleMobRenderer<>(c, ShellbackModel::new, SHELLBACK_LAYER, TheSift.id("textures/entity/shellback/shellback.png"), null, 0.8F, 1.0F)
		);
		EntityRendererRegistry.register(
			ModEntities.PEARL_STAG,
			c -> new SimpleMobRenderer<>(
				c,
				PearlStagModel::new,
				PEARL_STAG_LAYER,
				TheSift.id("textures/entity/pearl_stag/pearl_stag.png"),
				TheSift.id("textures/entity/pearl_stag/pearl_stag_glow.png"),
				0.6F,
				1.0F
			)
		);
		EntityRendererRegistry.register(
			ModEntities.LUMOTH,
			c -> new SimpleMobRenderer<>(
				c, LumothModel::new, LUMOTH_LAYER, TheSift.id("textures/entity/lumoth/lumoth.png"), TheSift.id("textures/entity/lumoth/lumoth_glow.png"), 0.2F, 1.0F
			)
		);
		EntityRendererRegistry.register(
			ModEntities.ICHOR_RAY,
			c -> new SimpleMobRenderer<>(c, IchorRayModel::new, ICHOR_RAY_LAYER, TheSift.id("textures/entity/ichor_ray/ichor_ray.png"), null, 0.5F, 1.0F)
		);
		ModelLayerRegistry.registerModelLayer(BUMBLEHOG_LAYER, com.randompixxelstudios.thesift.client.model.BumblehogModel::createBodyLayer);
		EntityRendererRegistry.register(
			ModEntities.BUMBLEHOG,
			c -> new SimpleMobRenderer<>(
				c, com.randompixxelstudios.thesift.client.model.BumblehogModel::new, BUMBLEHOG_LAYER, TheSift.id("textures/entity/bumblehog/bumblehog.png"), null, 0.6F, 1.0F
			)
		);
		ModelLayerRegistry.registerModelLayer(SHADE_LAYER, com.randompixxelstudios.thesift.client.model.ShadeModel::createBodyLayer);
		ModelLayerRegistry.registerModelLayer(GLOOMSTALKER_LAYER, com.randompixxelstudios.thesift.client.model.GloomstalkerModel::createBodyLayer);
		EntityRendererRegistry.register(
			ModEntities.SHADE,
			c -> new SimpleMobRenderer<>(
				c,
				com.randompixxelstudios.thesift.client.model.ShadeModel::new,
				SHADE_LAYER,
				TheSift.id("textures/entity/shade/shade.png"),
				TheSift.id("textures/entity/shade/shade_glow.png"),
				0.5F,
				1.0F
			)
		);
		EntityRendererRegistry.register(
			ModEntities.GLOOMSTALKER,
			c -> new SimpleMobRenderer<>(
				c,
				com.randompixxelstudios.thesift.client.model.GloomstalkerModel::new,
				GLOOMSTALKER_LAYER,
				TheSift.id("textures/entity/gloomstalker/gloomstalker.png"),
				TheSift.id("textures/entity/gloomstalker/gloomstalker_glow.png"),
				0.8F,
				1.0F
			)
		);

		boat(ModEntities.SIFT_WILLOW_BOAT, ModEntities.SIFT_WILLOW_CHEST_BOAT, "sift_willow");
		boat(ModEntities.DUSKWOOD_BOAT, ModEntities.DUSKWOOD_CHEST_BOAT, "duskwood");
		boat(ModEntities.PEARLBARK_BOAT, ModEntities.PEARLBARK_CHEST_BOAT, "pearlbark");

		FluidRenderingRegistry.register(
			ModFluids.ICHOR,
			ModFluids.FLOWING_ICHOR,
			new FluidModel.Unbaked(
				new Material(TheSift.id("block/ichor_still"), true), new Material(TheSift.id("block/ichor_flow"), true), null, null
			)
		);
	}

	private static void boat(
		final net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.vehicle.boat.AbstractBoat> boat,
		final net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.vehicle.boat.AbstractBoat> chestBoat,
		final String wood
	) {
		ModelLayerLocation boatLayer = new ModelLayerLocation(TheSift.id("boat/" + wood), "main");
		ModelLayerLocation chestLayer = new ModelLayerLocation(TheSift.id("chest_boat/" + wood), "main");
		ModelLayerRegistry.registerModelLayer(boatLayer, BoatModel::createBoatModel);
		ModelLayerRegistry.registerModelLayer(chestLayer, BoatModel::createChestBoatModel);
		EntityRendererRegistry.register(boat, c -> new BoatRenderer(c, boatLayer));
		EntityRendererRegistry.register(chestBoat, c -> new BoatRenderer(c, chestLayer));
	}
}
