package com.randompixxelstudios.thesift.client.render;

import com.randompixxelstudios.thesift.TheSift;
import com.randompixxelstudios.thesift.client.TheSiftClient;
import com.randompixxelstudios.thesift.client.model.SifterModel;
import com.randompixxelstudios.thesift.entity.Sifter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class SifterRenderer extends MobRenderer<Sifter, LivingEntityRenderState, SifterModel> {
	private static final Identifier TEXTURE = TheSift.id("textures/entity/sifter/sifter.png");
	private static final RenderType EYES = RenderTypes.eyes(TheSift.id("textures/entity/sifter/sifter_eyes.png"));

	public SifterRenderer(final EntityRendererProvider.Context context) {
		super(context, new SifterModel(context.bakeLayer(TheSiftClient.SIFTER_LAYER)), 0.5F);
		this.addLayer(new GlowingEyes(this));
	}

	@Override
	public Identifier getTextureLocation(final LivingEntityRenderState state) {
		return TEXTURE;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}

	private static class GlowingEyes extends EyesLayer<LivingEntityRenderState, SifterModel> {
		GlowingEyes(final RenderLayerParent<LivingEntityRenderState, SifterModel> renderer) {
			super(renderer);
		}

		@Override
		public RenderType renderType() {
			return EYES;
		}
	}
}
