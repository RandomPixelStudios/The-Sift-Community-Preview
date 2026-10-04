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

/** A squat, furry frog: wide body, two bulging eyes and a tuft of fluff on top. */
public class NuzzleModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart body;
	private final ModelPart[] feet = new ModelPart[4];

	public NuzzleModel(final ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		for (int i = 0; i < 4; i++) {
			this.feet[i] = root.getChild("foot" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild(
			"body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -6.0F, -5.0F, 10.0F, 6.0F, 10.0F), PartPose.offset(0.0F, 21.0F, 0.0F)
		);
		body.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(40, 0).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(-3.0F, -6.0F, -3.0F));
		body.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(40, 8).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(3.0F, -6.0F, -3.0F));
		body.addOrReplaceChild("tuft", CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(0.0F, -6.0F, 1.0F));
		int[][] feetPos = {{-4, -3}, {4, -3}, {-4, 3}, {4, 3}};
		for (int i = 0; i < 4; i++) {
			root.addOrReplaceChild(
				"foot" + i, CubeListBuilder.create().texOffs(i * 12, 20).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(feetPos[i][0], 21.0F, feetPos[i][1])
			);
		}

		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float speed = Math.min(state.walkAnimationSpeed, 1.0F);
		this.body.yRot = state.yRot * Mth.DEG_TO_RAD * 0.3F;
		this.body.xRot = -speed * 0.25F;
		// puffing throat / breathing
		float breathe = 1.0F + Mth.sin(state.ageInTicks * 0.2F) * 0.03F;
		this.body.xScale = breathe;
		this.body.zScale = breathe;
		for (int i = 0; i < 4; i++) {
			this.feet[i].xRot = Mth.cos(state.walkAnimationPos * 0.8F + (i < 2 ? 0.0F : Mth.PI)) * 1.0F * speed;
		}
	}
}
