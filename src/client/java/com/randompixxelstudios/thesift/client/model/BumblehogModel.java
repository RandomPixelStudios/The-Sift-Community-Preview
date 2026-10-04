package com.randompixxelstudios.thesift.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/** A round fuzzy hog: a barrel body, a big head with a snout and four short legs. */
public class BumblehogModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart head;
	private final ModelPart[] legs = new ModelPart[4];

	public BumblehogModel(final ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		for (int i = 0; i < 4; i++) {
			this.legs[i] = root.getChild("leg" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -9.0F, -6.0F, 10.0F, 9.0F, 12.0F), PartPose.offset(0.0F, 20.0F, 0.0F));
		root.addOrReplaceChild(
			"head",
			CubeListBuilder.create().texOffs(0, 24).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 7.0F, 6.0F).texOffs(28, 24).addBox(-2.0F, -1.0F, -8.0F, 4.0F, 3.0F, 2.0F),
			PartPose.offset(0.0F, 15.0F, -6.0F)
		);
		int[][] uv = {{0, 44}, {12, 44}, {24, 44}, {36, 44}};
		float[][] pos = {{-3.0F, -4.0F}, {3.0F, -4.0F}, {-3.0F, 4.0F}, {3.0F, 4.0F}};
		for (int i = 0; i < 4; i++) {
			root.addOrReplaceChild(
				"leg" + i, CubeListBuilder.create().texOffs(uv[i][0], uv[i][1]).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F), PartPose.offset(pos[i][0], 20.0F, pos[i][1])
			);
		}

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
		float speed = Math.min(state.walkAnimationSpeed, 1.0F);
		for (int i = 0; i < 4; i++) {
			this.legs[i].xRot = Mth.cos(state.walkAnimationPos * 0.6F + (i == 0 || i == 3 ? 0.0F : Mth.PI)) * 0.8F * speed;
		}
	}
}
