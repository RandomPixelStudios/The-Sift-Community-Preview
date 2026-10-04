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

/** A slender deer: long legs, a tall neck and a crown of branching golden antlers. */
public class PearlStagModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart[] legs = new ModelPart[4];

	public PearlStagModel(final ModelPart root) {
		super(root);
		this.neck = root.getChild("neck");
		this.head = this.neck.getChild("head");
		this.tail = root.getChild("tail");
		for (int i = 0; i < 4; i++) {
			this.legs[i] = root.getChild("leg" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -7.0F, 8.0F, 8.0F, 14.0F), PartPose.offset(0.0F, 13.0F, 0.0F));
		PartDefinition neck = root.addOrReplaceChild(
			"neck", CubeListBuilder.create().texOffs(44, 0).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 7.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 7.0F, -6.0F, 0.35F, 0.0F, 0.0F)
		);
		PartDefinition head = neck.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, -4.0F, -6.0F, 5.0F, 5.0F, 8.0F), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -0.35F, 0.0F, 0.0F)
		);
		head.addOrReplaceChild(
			"left_antler",
			CubeListBuilder.create().texOffs(28, 22).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F).texOffs(36, 22).addBox(-3.5F, -5.0F, -0.5F, 3.0F, 1.0F, 1.0F),
			PartPose.offsetAndRotation(-1.5F, -4.0F, 0.0F, 0.0F, 0.0F, -0.35F)
		);
		head.addOrReplaceChild(
			"right_antler",
			CubeListBuilder.create().texOffs(32, 22).addBox(-0.5F, -7.0F, -0.5F, 1.0F, 7.0F, 1.0F).texOffs(36, 26).addBox(0.5F, -5.0F, -0.5F, 3.0F, 1.0F, 1.0F),
			PartPose.offsetAndRotation(1.5F, -4.0F, 0.0F, 0.0F, 0.0F, 0.35F)
		);
		root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(40, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 7.0F, 0.4F, 0.0F, 0.0F));
		float[][] pos = {{-2.5F, -5.0F}, {2.5F, -5.0F}, {-2.5F, 5.0F}, {2.5F, 5.0F}};
		for (int i = 0; i < 4; i++) {
			root.addOrReplaceChild(
				"leg" + i, CubeListBuilder.create().texOffs(i * 10, 36).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 11.0F, 2.0F), PartPose.offset(pos[i][0], 13.0F, pos[i][1])
			);
		}

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		this.neck.yRot = state.yRot * Mth.DEG_TO_RAD * 0.5F;
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD * 0.5F;
		this.head.xRot += state.xRot * Mth.DEG_TO_RAD * 0.5F;
		this.tail.xRot += Mth.sin(state.ageInTicks * 0.3F) * 0.15F;
		float speed = Math.min(state.walkAnimationSpeed, 1.0F);
		for (int i = 0; i < 4; i++) {
			this.legs[i].xRot = Mth.cos(state.walkAnimationPos * 0.66F + (i == 0 || i == 3 ? 0.0F : Mth.PI)) * 1.2F * speed;
		}
	}
}
