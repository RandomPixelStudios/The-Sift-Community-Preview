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

/** A chunky blue cube with two tall ears and four stubby feet. */
public class BlubModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart body;
	private final ModelPart leftEar;
	private final ModelPart rightEar;
	private final ModelPart[] feet = new ModelPart[4];

	public BlubModel(final ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.leftEar = this.body.getChild("left_ear");
		this.rightEar = this.body.getChild("right_ear");
		for (int i = 0; i < 4; i++) {
			this.feet[i] = root.getChild("foot" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -4.5F, 10.0F, 8.0F, 9.0F), PartPose.offset(0.0F, 22.0F, 0.0F)
		);
		body.addOrReplaceChild(
			"left_ear", CubeListBuilder.create().texOffs(40, 0).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 6.0F, 2.0F), PartPose.offset(-2.5F, -8.0F, 0.5F)
		);
		body.addOrReplaceChild(
			"right_ear", CubeListBuilder.create().texOffs(50, 0).addBox(-1.5F, -6.0F, -1.0F, 3.0F, 6.0F, 2.0F), PartPose.offset(2.5F, -8.0F, 0.5F)
		);
		int[][] feetPos = {{-3, -3}, {3, -3}, {-3, 3}, {3, 3}};
		for (int i = 0; i < 4; i++) {
			root.addOrReplaceChild(
				"foot" + i,
				CubeListBuilder.create().texOffs(i * 12, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F),
				PartPose.offset(feetPos[i][0], 22.0F, feetPos[i][1])
			);
		}

		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float walk = state.walkAnimationPos;
		float speed = Math.min(state.walkAnimationSpeed, 1.0F);

		// bouncy hop while walking, gentle breathing while idle
		float hop = Math.abs(Mth.sin(walk * 0.6F)) * 2.5F * speed;
		float breathe = Mth.sin(state.ageInTicks * 0.12F) * 0.25F;
		this.body.y -= hop + breathe;
		this.body.yRot = state.yRot * Mth.DEG_TO_RAD * 0.25F;

		float earWobble = Mth.sin(state.ageInTicks * 0.15F) * 0.08F + Mth.cos(walk * 0.6F) * 0.35F * speed;
		this.leftEar.zRot = -0.12F - earWobble;
		this.rightEar.zRot = 0.12F + earWobble;
		this.leftEar.xRot = this.rightEar.xRot = -hop * 0.08F;

		for (int i = 0; i < 4; i++) {
			this.feet[i].xRot = Mth.cos(walk * 0.6F + (i % 2 == 0 ? 0.0F : Mth.PI)) * 0.8F * speed;
		}
	}
}
