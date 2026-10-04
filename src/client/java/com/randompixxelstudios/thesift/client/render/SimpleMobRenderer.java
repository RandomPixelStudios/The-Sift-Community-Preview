package com.randompixxelstudios.thesift.client.render;

import java.util.function.Function;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import org.jspecify.annotations.Nullable;

/** Renderer for the Sift's simple mobs: one model, one texture and an optional glowing layer. */
public class SimpleMobRenderer<T extends Mob, M extends EntityModel<LivingEntityRenderState>> extends MobRenderer<T, LivingEntityRenderState, M> {
	private final Identifier texture;
	private final float scale;

	public SimpleMobRenderer(
		final EntityRendererProvider.Context context,
		final Function<ModelPart, M> model,
		final ModelLayerLocation layer,
		final Identifier texture,
		final @Nullable Identifier glowTexture,
		final float shadow,
		final float scale
	) {
		super(context, model.apply(context.bakeLayer(layer)), shadow);
		this.texture = texture;
		this.scale = scale;
		if (glowTexture != null) {
			RenderType glow = RenderTypes.eyes(glowTexture);
			this.addLayer(new EyesLayer<>(this) {
				@Override
				public RenderType renderType() {
					return glow;
				}
			});
		}
	}

	@Override
	public void extractRenderState(final T entity, final LivingEntityRenderState state, final float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.scale *= this.scale;
	}

	@Override
	public Identifier getTextureLocation(final LivingEntityRenderState state) {
		return this.texture;
	}

	@Override
	public LivingEntityRenderState createRenderState() {
		return new LivingEntityRenderState();
	}
}
