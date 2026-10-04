package com.randompixxelstudios.thesift.client.render;

import com.randompixxelstudios.thesift.TheSift;
import com.randompixxelstudios.thesift.client.TheSiftClient;
import com.randompixxelstudios.thesift.client.model.BlubModel;
import com.randompixxelstudios.thesift.entity.Blub;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class BlubRenderer extends MobRenderer<Blub, LivingEntityRenderState, BlubModel> {
	private static final Identifier TEXTURE = TheSift.id("textures/entity/blub/blub.png");

	public BlubRenderer(final EntityRendererProvider.Context context) {
		super(context, new BlubModel(context.bakeLayer(TheSiftClient.BLUB_LAYER)), 0.35F);
	}

	@Override
	public Identifier getTextureLocation(final LivingEntityRenderState state) {
		return TEXTURE;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}
}
