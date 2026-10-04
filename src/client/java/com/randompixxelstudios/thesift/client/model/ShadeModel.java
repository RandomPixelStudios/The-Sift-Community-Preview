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

/**
 * The Shade: a hovering hooded spectre. A deep hood with two burning eyes, a narrow torso, a wide tattered
 * robe instead of legs, and long ghostly arms that reach out for its prey.
 */
public class ShadeModel extends EntityModel<LivingEntityRenderState> {
	private final ModelPart all;
	private final ModelPart head;
	private final ModelPart rightArm;
	private final ModelPart leftArm;
	private final ModelPart robe;
	private final ModelPart[] strands = new ModelPart[3];

	public ShadeModel(final ModelPart root) {
		super(root);
		this.all = root.getChild("all");
		this.head = this.all.getChild("head");
		this.rightArm = this.all.getChild("right_arm");
		this.leftArm = this.all.getChild("left_arm");
		this.robe = this.all.getChild("robe");
		for (int i = 0; i < 3; i++) {
			this.strands[i] = this.robe.getChild("strand" + i);
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition all = root.addOrReplaceChild("all", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition head = all.addOrReplaceChild(
			"head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(0.4F)),
			PartPose.offset(0.0F, 3.0F, 0.0F)
		);
		head.addOrReplaceChild("hood_tip", CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, -3.0F, 0.0F, 4.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -7.0F, 2.0F, -0.5F, 0.0F, 0.0F));
		all.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 10.0F, 5.0F), PartPose.offset(0.0F, 3.0F, 0.0F));
		PartDefinition robe = all.addOrReplaceChild(
			"robe", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, 0.0F, -3.5F, 10.0F, 11.0F, 7.0F), PartPose.offset(0.0F, 12.0F, 0.0F)
		);
		for (int i = 0; i < 3; i++) {
			robe.addOrReplaceChild(
				"strand" + i, CubeListBuilder.create().texOffs(i * 8, 52).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F), PartPose.offset(-3.5F + i * 3.5F, 10.0F, 3.0F)
			);
		}

		all.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F), PartPose.offset(-5.5F, 4.5F, 0.0F));
		all.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 32).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F), PartPose.offset(5.5F, 4.5F, 0.0F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(final LivingEntityRenderState state) {
		super.setupAnim(state);
		float t = state.ageInTicks;
		// it floats instead of walking
		this.all.y = Mth.sin(t * 0.1F) * 1.2F - 1.5F;
		this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
		this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
		float reach = -Mth.HALF_PI + 0.15F;
		this.rightArm.xRot = reach + Mth.sin(t * 0.13F) * 0.12F;
		this.leftArm.xRot = reach + Mth.sin(t * 0.13F + 1.3F) * 0.12F;
		this.rightArm.zRot = 0.08F + Mth.cos(t * 0.09F) * 0.06F;
		this.leftArm.zRot = -0.08F - Mth.cos(t * 0.09F) * 0.06F;
		float speed = Math.min(state.walkAnimationSpeed, 1.0F);
		this.robe.xRot = 0.15F * speed + Mth.sin(t * 0.08F) * 0.05F;
		for (int i = 0; i < 3; i++) {
			this.strands[i].xRot = 0.3F + Mth.sin(t * 0.2F + i * 1.7F) * 0.3F + speed * 0.4F;
		}
	}
}
